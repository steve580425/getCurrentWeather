package com.test.currentWeather.controller;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.test.currentWeather.pojo.WeatherInfo;
import com.test.currentWeather.service.WeatherService;

@RestController
@RequestMapping(value = "/weather")
public class WeatherRestController {
	
	@Value("${weatherapi.view}")
	private String weatherView;
	
	@Resource(name = "openWeatherService")
	private WeatherService weatherService;
	
	@GetMapping("/getCurWeather")
	public WeatherInfo getCityWeatherByCommonCityCode(@RequestParam("code") Long code) {
		return weatherService.getWeatherByCityCode(code);
	}
	
}
