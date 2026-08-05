package ke.co.safaricom.pims.wrapper.registry;

import ke.co.safaricom.pims.wrapper.config.ErpNextProperties;
import ke.co.safaricom.pims.wrapper.exception.TenantProvisioningException;
import ke.co.safaricom.pims.wrapper.exception.UnknownTenantException;
import ke.co.safaricom.pims.wrapper.util.AesDecryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ErpNextTenantResolver {

    private static final Logger log = LoggerFactory.getLogger(ErpNextTenantResolver.class);
    private static final String SUCCEEDED = "SUCCEEDED";

    private final ErpNextTenantRepository repository;
    private final ErpNextProperties properties;
    private final AesDecryptor aesDecryptor;

    public ErpNextTenantResolver(
            ErpNextTenantRepository repository,
            ErpNextProperties properties,
            AesDecryptor aesDecryptor) {
        this.repository = repository;
        this.properties = properties;
        this.aesDecryptor = aesDecryptor;
    }

    public Mono<ErpNextProperties.SiteConfig> resolve(String tenantId) {
        UUID uuid;
        try {
            uuid = UUID.fromString(tenantId);
        } catch (IllegalArgumentException ex) {
            // Non-UUID tenant IDs (e.g. pharmacy-b) only exist in static YAML fallback.
            return fallbackOrUnknown(tenantId);
        }

        return repository.findByTenantId(uuid)
                .flatMap(entity -> {
                    String status = entity.getProvisioningStatus();
                    if (SUCCEEDED.equalsIgnoreCase(status)) {
                        if (entity.getSiteUrl() == null || entity.getSiteUrl().isBlank()
                                || entity.getApiKey() == null || entity.getApiSecretEncrypted() == null) {
                            return Mono.error(new IllegalStateException(
                                    "ERPNext tenant " + tenantId + " is marked succeeded but credentials are incomplete"));
                        }
                        String secret = aesDecryptor.decrypt(entity.getApiSecretEncrypted());
                        return Mono.just(new ErpNextProperties.SiteConfig(
                                entity.getSiteUrl(),
                                entity.getApiKey(),
                                secret));
                    }
                    return Mono.error(new TenantProvisioningException(tenantId, status));
                })
                .switchIfEmpty(fallbackOrUnknown(tenantId));
    }

    private Mono<ErpNextProperties.SiteConfig> fallbackOrUnknown(String tenantId) {
        if (properties.fallbackEnabled()) {
            try {
                ErpNextProperties.SiteConfig site = properties.siteFor(tenantId);
                log.debug("Using static YAML fallback for tenant {}", tenantId);
                return Mono.just(site);
            } catch (UnknownTenantException ex) {
                return Mono.error(ex);
            }
        }
        return Mono.error(new UnknownTenantException(tenantId));
    }
}
