package ru.practicum.transfer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TransferApplication {

    static void main(String[] args) {
        SpringApplication.run(TransferApplication.class, args);
    }
}
