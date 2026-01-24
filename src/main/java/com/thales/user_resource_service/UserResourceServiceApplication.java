package com.thales.user_resource_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = {"com.thales"})
@ComponentScan(basePackages = {"com.thales", "com.thales.user_resource_service.mapper"})
@EnableFeignClients
public class UserResourceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserResourceServiceApplication.class, args);
	}

}
