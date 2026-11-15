package ru.kotletkin.positiongen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PositiongenApplication {

    static void main(String[] args) {
        SpringApplication.run(PositiongenApplication.class, args);
    }

}
