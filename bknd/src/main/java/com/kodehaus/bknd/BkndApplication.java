package com.kodehaus.bknd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.kodehaus.bknd",
    "com.kodehaus.plaza.controller",    // ✅ Controllers
    "com.kodehaus.plaza.service",       // ✅ Services
    "com.kodehaus.plaza.security",      // ✅ Security configs
    "com.kodehaus.plaza.config"         // ✅ Configs
})
@EntityScan(basePackages = {"com.kodehaus.plaza.entity"})
@EnableJpaRepositories(basePackages = {"com.kodehaus.plaza.repository"})
public class BkndApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BkndApplication.class, args);
    }
}
