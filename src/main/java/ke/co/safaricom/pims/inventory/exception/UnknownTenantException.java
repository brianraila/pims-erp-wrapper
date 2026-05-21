package ke.co.safaricom.pims.inventory.exception;

public class UnknownTenantException extends RuntimeException {
    public UnknownTenantException(String tenantId) {
        super("No ERPNext site configured for tenant: " + tenantId);
    }
}
