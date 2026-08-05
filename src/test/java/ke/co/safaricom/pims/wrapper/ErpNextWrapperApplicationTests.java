package ke.co.safaricom.pims.wrapper;

import ke.co.safaricom.pims.wrapper.registry.ErpNextTenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ErpNextWrapperApplicationTests {

    @MockBean
    private ErpNextTenantRepository erpNextTenantRepository;

    @Test
    void contextLoads() {
    }
}
