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
import com.pillartechnology.jsptf.exception.JsptfRuntimeException;

public class WebViewRenderer extends ExternalResource {

	private final HttpServlet servlet;
	private final ServletContext servletContext = new MockServletContext();
	private final MockHttpServletRequest httpRequest = new MockHttpServletRequest();
	private final MockWebConnection webConnection = new MockWebConnection();
	private final WebClient webClient;
	private URL url;
	
	public static WebViewRenderer forServlet(HttpServlet servlet, WebViewRendererStrategy...strategies) throws Exception {
		WebViewRenderer renderer = new WebViewRenderer(servlet);
		renderer.setStrategies(strategies);
		return renderer;
	}

	public static WebViewRenderer forServletClass(Class<HttpServlet> clazz, WebViewRendererStrategy...strategies) throws Exception {
		return forServlet(clazz.newInstance(), strategies);
	}

	@SuppressWarnings("unchecked")
	public static WebViewRenderer forJsp(final String jspName, final String basePackageName, WebViewRendererStrategy...strategies) throws Exception {
		final String className = basePackageName + jspName.replaceAll("_", "_005f").replaceAll("-", "_002d").replace('.', '_').replace('/', '.');
		Class<HttpServlet> clazz = (Class<HttpServlet>) Class.forName(className);
		return forServlet(clazz.newInstance(), strategies);
	}

	private WebViewRenderer(HttpServlet servlet) {
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
			throw new JsptfRuntimeException(e);
		}
	}

	private String generateContent() {
		try {
			MockHttpServletResponse httpResponse = new MockHttpServletResponse();
			servlet.service(httpRequest, httpResponse);
			return httpResponse.getContentAsString();
		} catch (Exception e) {
			throw new JsptfRuntimeException(e);
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

	public void setStrategy(WebViewRendererStrategy strategy) {
		strategy.apply(this);
	}

	public void setStrategies(WebViewRendererStrategy... strategies) {
		for (WebViewRendererStrategy strategy : strategies) {
			setStrategy(strategy);
		}
	}
}
