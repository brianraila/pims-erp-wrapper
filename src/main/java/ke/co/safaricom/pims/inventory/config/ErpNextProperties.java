package ke.co.safaricom.pims.inventory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * ERPNext connection properties.
 * Tenant base URL: {scheme}://{tenantId}.{domain}[:{port}]
 * Per-tenant API key/secret can be set under erpnext.tenants.{tenantId}.api-key/api-secret.
 * Falls back to erpnext.api-key / erpnext.api-secret if no tenant-specific credentials exist.
 */
@ConfigurationProperties(prefix = "erpnext")
public record ErpNextProperties(
        String domain,
        String apiKey,
        String apiSecret,
        String scheme,
        Integer port,
        Map<String, TenantCredentials> tenants
) {
    public ErpNextProperties {
        if (scheme == null || scheme.isBlank()) scheme = "https";
        if (tenants == null) tenants = Map.of();
    }

    public String baseUrlForTenant(String tenantId) {
        String base = scheme + "://" + tenantId + "." + domain;
        if (port != null) base += ":" + port;
        return base;
    }

    public String apiKeyFor(String tenantId) {
        TenantCredentials creds = tenants.get(tenantId);
        return (creds != null && creds.apiKey() != null) ? creds.apiKey() : apiKey;
    }

    public String apiSecretFor(String tenantId) {
        TenantCredentials creds = tenants.get(tenantId);
        return (creds != null && creds.apiSecret() != null) ? creds.apiSecret() : apiSecret;
    }

    public record TenantCredentials(String apiKey, String apiSecret) {}
}
