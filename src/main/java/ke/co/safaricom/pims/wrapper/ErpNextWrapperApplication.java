package ke.co.safaricom.pims.wrapper;

import ke.co.safaricom.pims.wrapper.config.ErpNextProperties;
import ke.co.safaricom.pims.wrapper.config.MsAesProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
@EnableConfigurationProperties({ErpNextProperties.class, MsAesProperties.class})
@EnableR2dbcRepositories
public class ErpNextWrapperApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpNextWrapperApplication.class, args);
    }
}
