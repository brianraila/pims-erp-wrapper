package ke.co.safaricom.pims.inventory.config;

import ke.co.safaricom.pims.inventory.exception.UnknownTenantException;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * ERPNext site registry. Each key is the tenant/site ID resolved from the
 * incoming X-Tenant-Id header (or JWT tenant_id claim). The matching SiteConfig
 * supplies the explicit base URL and API credentials used by the proxy.
 */
@ConfigurationProperties(prefix = "erpnext")
public record ErpNextProperties(Map<String, SiteConfig> sites) {

    public ErpNextProperties {
        if (sites == null) sites = Map.of();
    }

    public SiteConfig siteFor(String tenantId) {
        SiteConfig site = sites.get(tenantId);
        if (site == null) throw new UnknownTenantException(tenantId);
        return site;
    }

    public record SiteConfig(String url, String apiKey, String apiSecret) {}
}
