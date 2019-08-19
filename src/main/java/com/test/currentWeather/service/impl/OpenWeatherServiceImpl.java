package com.test.currentWeather.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.test.currentWeather.pojo.WeatherCityInfo;
import com.test.currentWeather.pojo.WeatherInfo;
import com.test.currentWeather.service.WeatherService;

@Service("openWeatherService")
public class OpenWeatherServiceImpl implements WeatherService {

	private static final Logger sLogger = LoggerFactory.getLogger(OpenWeatherServiceImpl.class);

	private static final WeatherInfo EMPTY_INFO = new WeatherInfo(null, -1, null, null, null);

	@Value("${weatherapi.cityCode.common.json}")
	private String cityCodeMapperJsonAddr;

	@Value("${weatherapi.openweather.url}")
	private String openWeatherUrl;
	
	@Value("${weatherapi.openweather.APPID}")
	private String appId;
	

	private volatile LinkedHashMap<Long, WeatherCityInfo> cityCodeMap;

	public OpenWeatherServiceImpl() {
		new FileRefresh().start();
	}

	@Override
	public LinkedHashMap<Long, WeatherCityInfo> listCityCodeNamePair() {
		if (cityCodeMap == null) {
			synchronized (this) {
				if (cityCodeMap == null) {
					cityCodeMap = getCityCodeNamePairFromFile();
				}
			}
		}
		return cityCodeMap;
	}

	@Override
	public WeatherInfo getWeatherByCityCode(long pCode) {
		WeatherInfo result = EMPTY_INFO;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String url = openWeatherUrl + "?APPID=" + appId + "&id=" + pCode;
		try {
			HttpGet httpget = new HttpGet(url);
			CloseableHttpResponse response = httpclient.execute(httpget);
			try {
				HttpEntity entity = response.getEntity();
				int resCode = response.getStatusLine().getStatusCode();
				if (resCode == 200) {
					String unParsed = EntityUtils.toString(entity);
					try {
						JSONObject fullInfo = JSONObject.parseObject(unParsed);
						return convertJSON2WeatherInfo(fullInfo);
					} catch (Throwable err) {
						sLogger.error("Error parse result {} from remote server {} due to {}.", unParsed, url,
								err.getMessage());
					}
				} else {
					sLogger.error("Remote server {} error with status code {}.", url, resCode);
				}
			} finally {
				response.close();
			}
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			sLogger.error("Communication with remote server {} failed due to {}.", url, e.getMessage());
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	private WeatherInfo convertJSON2WeatherInfo(JSONObject pInfo) {
		WeatherInfo info = new WeatherInfo(pInfo.getString("name"), System.currentTimeMillis(),
				pInfo.getJSONArray("weather").getJSONObject(0).getString("description"),
				pInfo.getJSONObject("main").getDouble("temp") / 10 + "°C", pInfo.getJSONObject("wind").getString("speed") + "km/h");
		return info;
	}

	public String getCityCodeMapperJsonAddr() {
		return cityCodeMapperJsonAddr;
	}

	private LinkedHashMap<Long, WeatherCityInfo> getCityCodeNamePairFromFile() {
		InputStream stream = null;
		InputStreamReader reader = null;
		StringBuilder builder = null;
		try {
			stream = OpenWeatherServiceImpl.class.getResourceAsStream(cityCodeMapperJsonAddr);
			reader = new InputStreamReader(stream);
			builder = new StringBuilder();
			char[] cs = new char[1024];
			int i = 0;

			while ((i = reader.read(cs)) != -1) {
				builder.append(new String(cs, 0, i));
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (stream != null)
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			
		}
		
		LinkedHashMap<Long, WeatherCityInfo> codeMapper = new LinkedHashMap<>();
		try {
			JSONArray list = JSONArray.parseArray(builder.toString());
			for (Iterator<?> iter = list.iterator(); iter.hasNext();) {
				Object tar = iter.next();
				if (tar != null && tar instanceof JSONObject) {
					JSONObject parsed = (JSONObject) tar;
					WeatherCityInfo info = parseJSON(parsed);
					if (codeMapper.containsKey(info.getId())) {
						WeatherCityInfo org = codeMapper.get(info.getId());
						sLogger.warn("Duplicate WeatherCityInfo with same id {{}} {{}}", info.toString(),
								org.toString());
					}
					codeMapper.put(info.getId(), info);
				}
			}
		} catch (Throwable err) {
			err.printStackTrace();
			sLogger.error("Error {} occur while attempt to read json from {}", err.getMessage(),
					cityCodeMapperJsonAddr);
		}

		return codeMapper;
	}

	private WeatherCityInfo parseJSON(JSONObject pTar) {
		long id = pTar.getLongValue("id");
		String name = pTar.getString("name");
		String country = pTar.getString("country");
		Object coord = pTar.get("coord");
		double lon = -1;
		double lat = -1;
		if (coord != null && coord instanceof JSONObject) {
			JSONObject parsed = (JSONObject) coord;
			lon = parsed.getDoubleValue("lon");
			lat = parsed.getDoubleValue("lat");
		}

		return new WeatherCityInfo(id, name, country, lon, lat);
	}

	private class FileRefresh extends Thread {

		final String name = "weather-file-reloader";
		final int interval = 1000 * 60 * 5;
		int reloadCnt;

		FileRefresh() {
			setDaemon(Boolean.TRUE);
		}

		@Override
		public void run() {
			Thread.currentThread().setName(name);
			sLogger.info("Thread {} launched, daemon={}, reloadInterval={} ms", name, true, interval);
			for (;;) {
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					sLogger.error("Thread {} exit due to interruped exception, reloader {} times.", name, reloadCnt);
				}
				reloadCnt++;
				
				//A race condition may happen when business thread call 'listCityCodeNamePair()' before cityCodeMap structure initialized.
				if(OpenWeatherServiceImpl.this.cityCodeMap == null) {
					synchronized (OpenWeatherServiceImpl.this) {
						OpenWeatherServiceImpl.this.cityCodeMap = getCityCodeNamePairFromFile();
					}
				}else
					OpenWeatherServiceImpl.this.cityCodeMap = getCityCodeNamePairFromFile();
				
			}
		}

	}
}
