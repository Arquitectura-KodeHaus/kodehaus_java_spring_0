package com.kodehaus.bknd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.kodehaus.bknd", "com.kodehaus.plaza"})
@EntityScan(basePackages = {"com.kodehaus.plaza.model"})
@EnableJpaRepositories(basePackages = {"com.kodehaus.plaza.repository"})
public class BkndApplication {
    public static void main(String[] args) {
        SpringApplication.run(BkndApplication.class, args);
    }
}
