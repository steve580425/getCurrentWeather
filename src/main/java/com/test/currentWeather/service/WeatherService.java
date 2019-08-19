package com.test.currentWeather.service;

import java.util.Map;

import com.test.currentWeather.pojo.WeatherCityInfo;
import com.test.currentWeather.pojo.WeatherInfo;

public interface WeatherService {

	public Map<Long, WeatherCityInfo> listCityCodeNamePair();
	
	public WeatherInfo getWeatherByCityCode(long pCode);
}
