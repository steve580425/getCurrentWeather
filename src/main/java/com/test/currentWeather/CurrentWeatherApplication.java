package com.test.currentWeather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@SpringBootApplication
public class CurrentWeatherApplication {

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(CurrentWeatherApplication.class);
		springApplication.run(args);
	}
	
	
}
