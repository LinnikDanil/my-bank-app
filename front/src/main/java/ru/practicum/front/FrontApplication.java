package ru.practicum.front;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.practicum.front.integration.ApiClientProperties;

@SpringBootApplication
@EnableConfigurationProperties(ApiClientProperties.class)
public class FrontApplication {

    static void main(String[] args) {
        SpringApplication.run(FrontApplication.class, args);
    }

}
