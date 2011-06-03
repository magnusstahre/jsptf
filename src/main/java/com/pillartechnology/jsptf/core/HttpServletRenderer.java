package com.pillartechnology.jsptf.core;

import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

import org.junit.rules.ExternalResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class HttpServletRenderer extends ExternalResource{

	private final HttpServlet servlet;
	private final ServletContext servletContext = new MockServletContext();
	private final MockHttpServletRequest httpRequest = new MockHttpServletRequest();
	private final MockWebConnection webConnection = new MockWebConnection();
	private final WebClient webClient;
	private URL url;
	

	public HttpServletRenderer(HttpServlet servlet) {
		this.servlet = servlet;
		
		webClient = new WebClient(BrowserVersion.FIREFOX_3_6);
		webClient.setJavaScriptEnabled(false);
		webClient.setWebConnection(webConnection);
	}
	
	@Override
	protected void before() throws Throwable {
		servlet.init(new MockServletConfig(servletContext));
		url = new URL("http://localhost/anything.jsp");
	}
	
	@Override
	protected void after() {
		servlet.destroy();
	}
	
	public HtmlPage render() {
        webConnection.setResponse(url, generateContent());
        try {
			return webClient.getPage(url);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}

	private String generateContent() {
		try {
			MockHttpServletResponse httpResponse = new MockHttpServletResponse();
			servlet.service(httpRequest, httpResponse);
			return httpResponse.getContentAsString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void addContextAttribute(String name, Object value) {
		servletContext.setAttribute(name, value);
	}

	public void addRequestAttribute(String name, Object value) {
		httpRequest.setAttribute(name, value);
	}

	public void addRequestParameter(String name, String value) {
		httpRequest.setParameter(name, value);
	}
}
