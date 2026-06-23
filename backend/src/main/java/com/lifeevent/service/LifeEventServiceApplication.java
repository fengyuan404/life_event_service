package com.lifeevent.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lifeevent.service.mapper")
public class LifeEventServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LifeEventServiceApplication.class, args);
    }
}

