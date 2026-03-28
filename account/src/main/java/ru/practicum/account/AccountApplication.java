package ru.practicum.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "ru.practicum")
public class AccountApplication {

    static void main(String[] args) {
        SpringApplication.run(AccountApplication.class, args);
    }
}
