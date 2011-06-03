package com.pillartechnology.jsptf.core;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HttpServletRendererTest {

	@Mock
	private HttpServlet servlet;

	private HttpServletRenderer renderer;

	@Before
	public void setUp() {
		renderer = new HttpServletRenderer(servlet);
	}

	@Test
	public void beforeInitializesServlet() throws Throwable {
		renderer.before();

		verify(servlet).init(Mockito.any(ServletConfig.class));
	}

	@Test
	public void beforeInitsServletWithContextContainingAddedAttributes() throws Throwable {
		final String attributeName = "name";
		final String attributeValue = "value";
		renderer.addContextAttribute(attributeName, attributeValue);
		final ArgumentCaptor<ServletConfig> captor = ArgumentCaptor.forClass(ServletConfig.class);
		doNothing().when(servlet).init(captor.capture());

		renderer.before();

		assertThat(captor.getValue(), hasContextAttribute(attributeName, attributeValue));
	}
	
	@Test
	public void whenRenderingCallsServiceOnTheServlet() throws Throwable {
		renderer.render();
		
		verify(servlet).service(Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class));
	}
	
	@Test
	public void renderCallsServiceWithRequestContextContainingAddedAttributes() throws Throwable {
		final String attributeName = "name";
		final String attributeValue = "value";
		renderer.addContextAttribute(attributeName, attributeValue);
		final ArgumentCaptor<ServletConfig> captor = ArgumentCaptor.forClass(ServletConfig.class);
		doNothing().when(servlet).init(captor.capture());
		
		renderer.before();
		
		assertThat(captor.getValue(), hasContextAttribute(attributeName, attributeValue));
	}
	
	@Test
	public void renderCallsServiceWithRequestContainingAddedAttributes() throws Throwable {
		final String attributeName = "name";
		final String attributeValue = "value";
		renderer.addRequestAttribute(attributeName, attributeValue);
		final ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
		doNothing().when(servlet).service(captor.capture(), Mockito.any(HttpServletResponse.class));
		
		renderer.render();
		
		assertThat(captor.getValue(), hasRequestAttribute(attributeName, attributeValue));
	}
	
	@Test
	public void renderCallsServiceWithRequestContainingAddedParameters() throws Throwable {
		final String parameterName = "name";
		final String parameterValue = "value";
		renderer.addRequestParameter(parameterName, parameterValue);
		final ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
		doNothing().when(servlet).service(captor.capture(), Mockito.any(HttpServletResponse.class));
		
		renderer.render();
		
		assertThat(captor.getValue(), hasRequestParameter(parameterName, parameterValue));
	}
	
	@Test
	public void afterCallsDestroyOnServlet() throws Exception {
		renderer.after();
		
		verify(servlet).destroy();
	}

	private static final Matcher<ServletConfig> hasContextAttribute(final String name, final Object value) {
		return new TypeSafeMatcher<ServletConfig>() {

			@Override
			protected boolean matchesSafely(final ServletConfig config) {
				return value.equals(config.getServletContext().getAttribute(name));
			}

			public void describeTo(Description description) {
				description.appendText(format("a ServletConfig containing a ServletContext with attribute [%s,%s]", name, value));
			}

			@Override
			protected void describeMismatchSafely(final ServletConfig config, final Description description) {
				description.appendText("got: a ServletConfig containing ");
				ServletContext servletContext = config.getServletContext();
				description.appendText(format("a ServletConfig with attributes {%s}", attributes(servletContext)));
			}

			@SuppressWarnings("unchecked")
			private String attributes(final ServletContext context) {
				StringBuilder builder = new StringBuilder();
				Enumeration<String> attributeNames = context.getAttributeNames();
				while (attributeNames.hasMoreElements()) {
					String name = attributeNames.nextElement();
					builder.append(format("[%s, %s]", name, context.getAttribute(name)));
				}
				return builder.toString();
			}
		};
	}
	
	private static final Matcher<HttpServletRequest> hasRequestAttribute(final String name, final Object value) {
		return new TypeSafeMatcher<HttpServletRequest>() {
			
			@Override
			protected boolean matchesSafely(final HttpServletRequest request) {
				return value.equals(request.getAttribute(name));
			}
			
			public void describeTo(Description description) {
				description.appendText(format("a HttpServletRequest containing an attribute [%s,%s]", name, value));
			}
			
			@Override
			protected void describeMismatchSafely(final HttpServletRequest request, final Description description) {
				description.appendText("got: a HttpServletRequest containing ");
				description.appendText(format(" attributes {%s}", attributes(request)));
			}
			
			@SuppressWarnings("unchecked")
			private String attributes(final HttpServletRequest request) {
				StringBuilder builder = new StringBuilder();
				Enumeration<String> attributeNames = request.getAttributeNames();
				while (attributeNames.hasMoreElements()) {
					String name = attributeNames.nextElement();
					builder.append(format("[%s, %s]", name, request.getAttribute(name)));
				}
				return builder.toString();
			}
		};
	}
	
	private static final Matcher<HttpServletRequest> hasRequestParameter(final String name, final Object value) {
		return new TypeSafeMatcher<HttpServletRequest>() {
			
			@Override
			protected boolean matchesSafely(final HttpServletRequest request) {
				return value.equals(request.getParameter(name));
			}
			
			public void describeTo(Description description) {
				description.appendText(format("a HttpServletRequest containing a parmeter [%s,%s]", name, value));
			}
			
			@Override
			protected void describeMismatchSafely(final HttpServletRequest request, final Description description) {
				description.appendText("got: a HttpServletRequest containing ");
				description.appendText(format("attributes {%s}", attributes(request)));
			}
			
			@SuppressWarnings("unchecked")
			private String attributes(final HttpServletRequest request) {
				StringBuilder builder = new StringBuilder();
				Enumeration<String> parameterNames = request.getParameterNames();
				while (parameterNames.hasMoreElements()) {
					String name = parameterNames.nextElement();
					builder.append(format("[%s, %s]", name, request.getParameter(name)));
				}
				return builder.toString();
			}
		};
	}
}
