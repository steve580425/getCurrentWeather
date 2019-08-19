package com.test.currentWeather.pojo;

import java.io.Serializable;

public class WeatherCityInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private final long id;
	private final String name;
	private final String country;
	private final Coord coord;
	private final String infoCache;
	
	public WeatherCityInfo(long pId, String pName, String pCountry, Coord pCoord) {
		this.id = pId;
		this.name = pName;
		this.country = pCountry;
		this.coord = pCoord;
		StringBuilder builder = new StringBuilder();
		builder.append(id).append(":").append(name).append(":").append(country).append(":").append(coord.toString());
		this.infoCache = builder.toString();
	}
	
	public WeatherCityInfo(long pId, String pName, String pCountry, double pLon, double pLat) {
		this(pId, pName, pCountry, new Coord(pLon, pLat));
	}
	
	@Override
	public String toString() {
		return this.infoCache;
	}
	
	public static class Coord implements Serializable {

		private static final long serialVersionUID = 1L;

		private final double lon;
		private final double lat;

		public Coord(double pLon, double pLat) {
			this.lat = pLat;
			this.lon = pLon;
		}

		public double getLon() {
			return lon;
		}

		public double getLat() {
			return lat;
		}

		@Override
		public String toString() {
			return lon + ":" + lat;
		}
	}

	public Coord getCoord() {
		return coord;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getCountry() {
		return country;
	}

}
