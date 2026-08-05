package ke.co.safaricom.pims.wrapper.config;

import ke.co.safaricom.pims.wrapper.exception.UnknownTenantException;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * ERPNext site registry. Each key is the tenant/site ID resolved from the
 * incoming X-Tenant-Id header (or JWT tenant_id claim). The matching SiteConfig
 * supplies the explicit base URL and API credentials used by the proxy.
 *
 * <p>When {@code registry.fallback-enabled} is true (dev), static {@code sites}
 * YAML is used if the DB registry has no row for the tenant.
 */
@ConfigurationProperties(prefix = "erpnext")
public record ErpNextProperties(Map<String, SiteConfig> sites, RegistryConfig registry) {

    public ErpNextProperties {
        if (sites == null) sites = Map.of();
        if (registry == null) registry = new RegistryConfig(false);
    }

    public SiteConfig siteFor(String tenantId) {
        SiteConfig site = sites.get(tenantId);
        if (site == null) throw new UnknownTenantException(tenantId);
        return site;
    }

    public boolean fallbackEnabled() {
        return registry != null && registry.fallbackEnabled();
    }

    public record SiteConfig(String url, String apiKey, String apiSecret) {}

    public record RegistryConfig(boolean fallbackEnabled) {}
}
