package ke.co.safaricom.pims.wrapper.proxy;

import ke.co.safaricom.pims.wrapper.config.ErpNextProperties;
import ke.co.safaricom.pims.wrapper.security.TenantContextResolver;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Set;

@RestController
public class ErpNextProxyController {

    private static final Set<String> HOP_BY_HOP = Set.of(
            "connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
            "te", "trailers", "transfer-encoding", "upgrade", "host");

    private final ErpNextProperties properties;
    private final TenantContextResolver tenantResolver;
    private final WebClient webClient;

    public ErpNextProxyController(
            ErpNextProperties properties,
            TenantContextResolver tenantResolver,
            WebClient webClient) {
        this.properties = properties;
        this.tenantResolver = tenantResolver;
        this.webClient = webClient;
    }

    @RequestMapping("/**")
    public Mono<Void> proxy(ServerWebExchange exchange) {
        return resolveTenantId(exchange)
                .flatMap(tenantId -> forward(tenantId, exchange));
    }

    private Mono<String> resolveTenantId(ServerWebExchange exchange) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .flatMap(auth -> tenantResolver.resolveTenantId(auth, exchange))
                .switchIfEmpty(
                        Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(TenantContextResolver.TENANT_HEADER))
                                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                        "Tenant context missing. Provide tenant_id claim in JWT or X-Tenant-Id header."))));
    }

    private Mono<Void> forward(String tenantId, ServerWebExchange exchange) {
        ErpNextProperties.SiteConfig site = properties.siteFor(tenantId);
        ServerHttpRequest req = exchange.getRequest();

        String query = req.getURI().getRawQuery();
        String targetUri = site.url() + req.getURI().getRawPath() + (query != null ? "?" + query : "");

        String authHeader = "token " + site.apiKey() + ":" + site.apiSecret();

        return webClient.method(req.getMethod())
                .uri(URI.create(targetUri))
                .headers(headers -> {
                    req.getHeaders().forEach((name, values) -> {
                        if (!HOP_BY_HOP.contains(name.toLowerCase())) {
                            headers.addAll(name, values);
                        }
                    });
                    headers.set(HttpHeaders.AUTHORIZATION, authHeader);
                })
                .body(req.getBody(), DataBuffer.class)
                .exchangeToMono(upstream -> {
                    ServerHttpResponse resp = exchange.getResponse();
                    resp.setStatusCode(upstream.statusCode());
                    upstream.headers().asHttpHeaders().forEach((name, values) -> {
                        if (!HOP_BY_HOP.contains(name.toLowerCase())) {
                            resp.getHeaders().addAll(name, values);
                        }
                    });
                    return resp.writeWith(upstream.bodyToFlux(DataBuffer.class));
                });
    }
}
