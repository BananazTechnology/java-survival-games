package com.aaronrenner.discordnftbot.utils;

import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class UrlUtils {

	private RestTemplate restTemplate	= new RestTemplate();
	private StringUtils sUtils			= new StringUtils();
	private static final Logger LOGGER  = LoggerFactory.getLogger(UrlUtils.class);
	
	public String postRequest(String postURL, String body) throws Exception {
		// Variables for runtime
		URI createURI = sUtils.getURIFromString(postURL);
		ResponseEntity<String> result = null;
		String newResponse;
		// Variables for timing
		long startTime = System.currentTimeMillis();
		long endTime = System.currentTimeMillis();
		try {
			result = restTemplate.postForEntity(createURI, body, String.class);
			newResponse = result.getBody();
		} catch (HttpClientErrorException e) {
			LOGGER.error(String.format("Failed HTTP GET: [%s] %s - %s", e.getRawStatusCode(), e.getStatusText(), e.getResponseHeaders().toSingleValueMap()));
			throw new Exception(String.format("Failed HTTP GET: [%s] %s - %s", e.getRawStatusCode(), e.getStatusText(), e.getResponseHeaders().toSingleValueMap()));
		}
		LOGGER.debug(String.format("GET request took %sms", Long.valueOf(endTime-startTime).toString()));
		return newResponse;
	}
}
