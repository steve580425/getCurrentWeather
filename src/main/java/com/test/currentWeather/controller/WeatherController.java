package com.test.currentWeather.controller;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.ImmutableList;
import com.test.currentWeather.pojo.WeatherCityInfo;
import com.test.currentWeather.service.WeatherService;

@Controller
@RequestMapping(value = "/weather")
public class WeatherController {
	
	@Value("${weatherapi.view}")
	private String weatherView;
	
	@Resource(name = "openWeatherService")
	private WeatherService weatherService;
	
	@Value("${weatherapi.retrieve.interval}")
	private long retrieveInterval;
	
	@GetMapping("/main")
	public Object weather() {
		ModelAndView mv = new ModelAndView(weatherView);
		Map<Long, WeatherCityInfo> avaliableCities = weatherService.listCityCodeNamePair();
		ImmutableList.Builder<WeatherCityInfo> builder = ImmutableList.builder();
		builder.addAll(avaliableCities.values());
		mv.addObject("weather", builder.build());
		mv.addObject("weatherUrl", "/weather/getCurWeather?code=");
		mv.addObject("retrieveInterval", retrieveInterval);
		return mv;
	}

}
