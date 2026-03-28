package ru.practicum.transfer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "ru.practicum")
public class TransferApplication {

    static void main(String[] args) {
        SpringApplication.run(TransferApplication.class, args);
    }
}
