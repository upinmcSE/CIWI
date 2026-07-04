package io.github.upinmcse.invalidator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class InvalidatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvalidatorApplication.class, args);
    }

}
