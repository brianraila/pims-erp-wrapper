package ke.co.safaricom.pims.wrapper.exception;

public class TenantProvisioningException extends RuntimeException {
    public TenantProvisioningException(String tenantId, String status) {
        super("ERPNext site provisioning in progress for tenant " + tenantId
                + " (status=" + status + ")");
    }
}
