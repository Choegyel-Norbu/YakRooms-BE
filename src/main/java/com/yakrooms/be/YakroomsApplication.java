package com.yakrooms.be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@SpringBootApplication
@EnableScheduling
public class YakroomsApplication {

	public static void main(String[] args) {
		SpringApplication.run(YakroomsApplication.class, args);
	}
	
	@Bean
	public jakarta.validation.Validator validator() {
		return new LocalValidatorFactoryBean();
	}
}
