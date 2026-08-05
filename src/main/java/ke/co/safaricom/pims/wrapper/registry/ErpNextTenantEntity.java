package ke.co.safaricom.pims.wrapper.registry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("erpnext_tenant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErpNextTenantEntity {

    @Id
    @Column("tenant_id")
    private UUID tenantId;

    @Column("pharmacy_id")
    private Long pharmacyId;

    private String slug;
    private String fqdn;

    @Column("site_url")
    private String siteUrl;

    @Column("api_key")
    private String apiKey;

    @Column("api_secret_encrypted")
    private String apiSecretEncrypted;

    @Column("operator_job_id")
    private String operatorJobId;

    @Column("provisioning_status")
    private String provisioningStatus;

    @Column("error_message")
    private String errorMessage;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("provisioned_at")
    private LocalDateTime provisionedAt;
}
