package ke.co.safaricom.pims.wrapper.registry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ErpNextTenantRepository extends ReactiveCrudRepository<ErpNextTenantEntity, UUID> {

    Mono<ErpNextTenantEntity> findByTenantId(UUID tenantId);
}
