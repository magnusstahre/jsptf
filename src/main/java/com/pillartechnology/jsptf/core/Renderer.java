package com.pillartechnology.jsptf.core;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

public class Renderer {

	private final HttpServlet servlet;
	private final ServletContext servletContext = new MockServletContext();
	

	public Renderer(HttpServlet servlet) {
		this.servlet = servlet;
	}
	
	public void render(){
		try{
			servlet.init(new MockServletConfig(servletContext));
		}catch(ServletException e){
			throw new RuntimeException(e);
		}
	}

	public void addContextAttribute(String name, Object value) {
		servletContext.setAttribute(name, value);
	}
}
