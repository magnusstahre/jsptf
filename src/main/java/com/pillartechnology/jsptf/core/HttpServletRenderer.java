package com.pillartechnology.jsptf.core;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.junit.rules.ExternalResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

public class HttpServletRenderer extends ExternalResource{

	private final HttpServlet servlet;
	private final ServletContext servletContext = new MockServletContext();
	private final MockHttpServletRequest httpRequest = new MockHttpServletRequest();
	

	public HttpServletRenderer(HttpServlet servlet) {
		this.servlet = servlet;
	}
	
	@Override
	protected void before() throws Throwable {
		servlet.init(new MockServletConfig(servletContext));
	}
	
	@Override
	protected void after() {
		servlet.destroy();
	}
	
	public void render() {
		try {
			servlet.service(httpRequest, new MockHttpServletResponse());
		} catch (ServletException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
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
