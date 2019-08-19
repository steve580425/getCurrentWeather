package com.test.currentWeather.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class WeatherControllerTest {
	
	static void getCityWeatherByCommonCityCodeTestMultithread() {
		final int threads = 4;
		final int perCount = 4;
		final CountDownLatch start = new CountDownLatch(1);
		final CountDownLatch stop = new CountDownLatch(threads);
		final AtomicLong summary = new AtomicLong(0);
		final ConcurrentHashMap<Throwable, AtomicLong> errors = new ConcurrentHashMap<>();
		final List<Long> ltimeCost = Collections.synchronizedList(new ArrayList<>());
		for(int i = 0; i < threads; i++) {
			new Thread(new Runnable() {
				public void run() {
					try {
						start.await();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					try {
						long longgest = 0;
						for(int i = 0; i <perCount; i++) {
							long start = System.currentTimeMillis();
							try {
								getCityWeatherByCommonCityCodeTest();
							}catch(Throwable e) {
								e.printStackTrace();
								AtomicLong error = errors.get(e);
								if(error == null) {
									AtomicLong newErr = errors.putIfAbsent(e, error);
									if(newErr != null)
										error = newErr;
								}
								error.incrementAndGet();
							}finally {
								long timeCost = System.currentTimeMillis() - start;
								if(timeCost > longgest)
									longgest = timeCost;
								summary.addAndGet(timeCost);
							}
						}
						
						ltimeCost.add(longgest);
					}finally {
						stop.countDown();
					}
				}
			}).start();
		}
		
		long startTime = System.currentTimeMillis();
		start.countDown();
		try {
			stop.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("Total time cost:" + (System.currentTimeMillis() - startTime));
		System.out.println("Total request:" + (threads * perCount));
		long longgest = 0;
		for(Iterator<Long> iter = ltimeCost.iterator(); iter.hasNext(); ) {
			long next = iter.next();
			if(longgest < next)
				longgest = next;
		}
		System.out.println("Longgest time cost:" + longgest);
		
		System.out.println("Exception map:");
		for(Iterator<Entry<Throwable, AtomicLong>> errIter = errors.entrySet().iterator(); errIter.hasNext(); ) {
			Entry<Throwable, AtomicLong> entry = errIter.next();
			System.out.println("\t" + entry.getKey().getMessage() + ":" + entry.getValue().get());
		}
		
		System.out.println("Error count:" + errors.size());
		System.out.println("Average response time:" + (summary.get() / ((threads * perCount))));
	}
	
	static void getCityWeatherByCommonCityCodeTest() {
		String url = "http://127.0.0.1:9999/weather/getCurWeather?code=12312";
		System.out.println(send(url));
	}
	
	static void weatherTest() {
		String url = "http://127.0.0.1:9999/weather/main";
		System.out.println(send(url));
	}
	
	private static Object send(String pUrl) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			HttpGet httpget = new HttpGet(pUrl);
			CloseableHttpResponse response = httpclient.execute(httpget);
			try {
				HttpEntity entity = response.getEntity();
				int resCode = response.getStatusLine().getStatusCode();
				if (resCode == 200) {
					String unParsed = EntityUtils.toString(entity);
					try {
						return unParsed;
					} catch (Throwable err) {
						err.printStackTrace();
						return null;
					}
				} else {
					System.out.println(resCode);
					return null;
				}
			} finally {
				response.close();
			}
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static void main(String[] args) {
		getCityWeatherByCommonCityCodeTestMultithread();
	}
}
