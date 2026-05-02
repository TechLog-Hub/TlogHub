package com.techloghub.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class TechlogHubApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TechlogHubApiApplication.class, args);
	}

}
