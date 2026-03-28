package ru.practicum.cash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "ru.practicum")
public class CashApplication {

    static void main(String[] args) {
        SpringApplication.run(CashApplication.class, args);
    }
}
