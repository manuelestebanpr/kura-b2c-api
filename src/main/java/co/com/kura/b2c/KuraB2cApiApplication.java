package co.com.kura.b2c;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class KuraB2cApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(KuraB2cApiApplication.class, args);
    }
}
