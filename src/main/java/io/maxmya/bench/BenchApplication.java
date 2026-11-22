package io.maxmya.bench;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BenchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BenchApplication.class, args);
    }
}
