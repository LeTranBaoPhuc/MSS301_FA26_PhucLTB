package fu.de190423.de190423_postservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class De190423PostServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(De190423PostServiceApplication.class, args);
    }

}
