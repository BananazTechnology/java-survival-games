package com.aaronrenner.discordnftbot.utils;

import org.apache.commons.text.StringEscapeUtils;

public class MockbinUtils {
	
	private String MOCKBIN_CREATE_URL = "https://mockbin.org/bin/create";
	private String MOCKBIN_BASE = "https://mockbin.org/bin/%s";
	private UrlUtils uUtils = new UrlUtils();
	private String POST_BODY_LAYOUT = "{\"status\":200,\"statusText\":\"OK\",\"httpVersion\":\"HTTP/1.1\",\"headers\":[{\"name\":\"Content-Type\",\"value\":\"text/csv\"},{\"name\":\"Content-Disposition\",\"value\":\"attachment ; filename = %s.csv\"}],\"cookies\":[],\"content\":{\"mimeType\":\"text/csv\",\"text\": \"%s\"}}";
	
	public String createBinCSV(String filename, String data) throws Exception {
		String bodyIn = String.format(POST_BODY_LAYOUT, filename, StringEscapeUtils.escapeJava(data));
		String out = this.uUtils.postRequest(MOCKBIN_CREATE_URL, bodyIn);
		return String.format(MOCKBIN_BASE, out);
	}
	

}
