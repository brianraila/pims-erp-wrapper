package ke.co.safaricom.pims.wrapper;

import ke.co.safaricom.pims.wrapper.config.ErpNextProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ErpNextProperties.class)
public class ErpNextWrapperApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpNextWrapperApplication.class, args);
    }
}
