package com.test.currentWeather.pojo;

import java.io.Serializable;
import java.util.Date;

public class WeatherInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String cityName;
	private final String updatedTime;
	private final String weatherDesc;
	private final String temperatureDesc;
	private final String wind;

	public WeatherInfo(String pCityName, long pUpdatedTime, String pWeatherDesc, String pTemperatureDesc,
			String pWind) {
		this.cityName = pCityName;
		this.updatedTime = new Date(pUpdatedTime).toString();
		this.weatherDesc = pWeatherDesc;
		this.temperatureDesc = pTemperatureDesc;
		this.wind = pWind;
	}

	public String getCityName() {
		return cityName;
	}

	public String getUpdatedTime() {
		return updatedTime;
	}

	public String getWeatherDesc() {
		return weatherDesc;
	}

	public String getTemperatureDesc() {
		return temperatureDesc;
	}

	public String getWind() {
		return wind;
	}
	
}
