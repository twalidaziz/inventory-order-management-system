package dev.twalidaziz.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
public class CmsApplication {

    public static void main(final String[] args) {
        SpringApplication.run(CmsApplication.class, args);
    }

}
