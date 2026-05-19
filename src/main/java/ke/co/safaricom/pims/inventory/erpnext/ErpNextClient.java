package ke.co.safaricom.pims.inventory.erpnext;

import ke.co.safaricom.pims.inventory.exception.ConflictException;
import ke.co.safaricom.pims.inventory.exception.ResourceNotFoundException;
import ke.co.safaricom.pims.inventory.exception.ServiceValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * Low-level reactive HTTP client for ERPNext REST API calls.
 * Each call receives an auth header to keep the client stateless.
 */
@Component
public class ErpNextClient {

    private static final Logger logger = LoggerFactory.getLogger(ErpNextClient.class);

    private final WebClient webClient;

    public ErpNextClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public <T> Mono<T> get(
            String baseUrl,
            String authHeader,
            String path,
            MultiValueMap<String, String> params,
            Class<T> responseType) {

        return webClient.mutate().baseUrl(baseUrl).build()
                .get()
                .uri(uriBuilder -> uriBuilder.path(path).queryParams(params).build())
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(responseType)
                .onErrorMap(WebClientResponseException.class, this::mapHttpError);
    }

    public <T> Mono<T> post(
            String baseUrl,
            String authHeader,
            String path,
            Object body,
            Class<T> responseType) {

        return webClient.mutate().baseUrl(baseUrl).build()
                .post()
                .uri(path)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType)
                .onErrorMap(WebClientResponseException.class, this::mapHttpError);
    }

    public <T> Mono<T> put(
            String baseUrl,
            String authHeader,
            String path,
            Object body,
            Class<T> responseType) {

        return webClient.mutate().baseUrl(baseUrl).build()
                .put()
                .uri(path)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType)
                .onErrorMap(WebClientResponseException.class, this::mapHttpError);
    }

    public Mono<Void> delete(String baseUrl, String authHeader, String path) {
        return webClient.mutate().baseUrl(baseUrl).build()
                .delete()
                .uri(path)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorMap(WebClientResponseException.class, this::mapHttpError);
    }

    private Throwable mapHttpError(WebClientResponseException ex) {
        logger.warn("ERPNext API error: {} {}", ex.getStatusCode(), ex.getMessage());
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            return new ResourceNotFoundException(ex.getMessage());
        }
        if (ex.getStatusCode() == HttpStatus.CONFLICT) {
            return new ConflictException(ex.getResponseBodyAsString());
        }
        if (ex.getStatusCode() == HttpStatus.BAD_REQUEST
                || ex.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
            return new ServiceValidationException(ex.getResponseBodyAsString());
        }
        return ex;
    }
}
