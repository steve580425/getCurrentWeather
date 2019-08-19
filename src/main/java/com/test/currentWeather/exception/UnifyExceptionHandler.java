package com.test.currentWeather.exception;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;

@ControllerAdvice
public class UnifyExceptionHandler {
	
	public static final int ERROR_NUMBER_FORMAT = 100;
	
	public static final int ERROR_COMMON = 1000;
	
	@Value("${controller.error.code.name}")
	private String errorCodeName;
	
	@Value("${controller.error.msg.name}")
	private String errorMsgName;
	
	private Map<String, String> constrCommErr(int pCode, String pCase) {
		ImmutableMap.Builder<String, String> resultBuilder = ImmutableMap.builder();
		resultBuilder.put(errorCodeName, String.valueOf(pCode));
		resultBuilder.put(errorMsgName, pCase);
		return resultBuilder.build();
	}
	
	@ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Object errorHandler(Exception ex, HttpServletResponse pResponse) {
		pResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
		if(ex instanceof NumberFormatException) {
			return constrCommErr(ERROR_NUMBER_FORMAT, ex.getMessage());
		}
		return constrCommErr(ERROR_COMMON, ex.getMessage());
    }
}
