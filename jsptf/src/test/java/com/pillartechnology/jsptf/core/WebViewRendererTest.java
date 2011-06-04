package com.pillartechnology.jsptf.core;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.mock.web.MockHttpServletResponse;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

@RunWith(MockitoJUnitRunner.class)
public class WebViewRendererTest {

	@Mock
	private HttpServlet servlet;

	private WebViewRenderer renderer;

	@Before
	public void setUp() throws Exception {
		renderer = WebViewRenderer.forServlet(servlet);
		configureServletToReturnString(StringUtils.EMPTY);
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
		renderer.before();
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
		renderer.render();
		
		assertThat(captor.getValue(), hasContextAttribute(attributeName, attributeValue));
	}
	
	@Test
	public void renderCallsServiceWithRequestContainingAddedAttributes() throws Throwable {
		final String attributeName = "name";
		final String attributeValue = "value";
		renderer.addRequestAttribute(attributeName, attributeValue);
		final ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
		doNothing().when(servlet).service(captor.capture(), Mockito.any(HttpServletResponse.class));
		
		renderer.before();
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
		
		renderer.before();
		renderer.render();
		
		assertThat(captor.getValue(), hasRequestParameter(parameterName, parameterValue));
	}
	
	@Test
	public void generatesHtmlPageFromContentInResponse() throws Throwable {
		final String expectedOutput = "<html><head/><body>blah</body></html>";
		configureServletToReturnString(expectedOutput);
		
		renderer.before();
		HtmlPage page = renderer.render();
		
		assertThat(StringUtils.deleteWhitespace(page.asXml()), containsString(expectedOutput));
	}
	
	@Test
	public void afterCallsDestroyOnServlet() throws Exception {
		renderer.after();
		
		verify(servlet).destroy();
	}
	
	@Test
	public void renderPassesItselfToStrategyWhenSet() throws Exception {
		WebViewRendererStrategy strategy = mock(WebViewRendererStrategy.class);
		
		renderer.setStrategy(strategy);
		
		verify(strategy).apply(renderer);
	}
	
	@Test
	public void setStrategiesAppliesAllStrategiesPassedIn() throws Exception {
		WebViewRendererStrategy strategy = mock(WebViewRendererStrategy.class);
		WebViewRendererStrategy strategy2 = mock(WebViewRendererStrategy.class);
		
		renderer.setStrategies(strategy, strategy2);
		
		verify(strategy).apply(renderer);
		verify(strategy2).apply(renderer);
	}
	
	@Test
	public void callingFactoryMethodWithStrategiesAppliesAllStrategies() throws Exception {
		WebViewRendererStrategy strategy = mock(WebViewRendererStrategy.class);
		WebViewRendererStrategy strategy2 = mock(WebViewRendererStrategy.class);

		WebViewRenderer strategyRenderer = WebViewRenderer.forServlet(servlet, strategy, strategy2);
		
		verify(strategy).apply(strategyRenderer);
		verify(strategy2).apply(strategyRenderer);
	}

	private void configureServletToReturnString(final String expectedOutput) throws Exception {
		doAnswer(new Answer<Object>() {
			
			public Object answer(InvocationOnMock invocation) throws Throwable {
				MockHttpServletResponse response = (MockHttpServletResponse) invocation.getArguments()[1];
				response.getWriter().append(expectedOutput);
				return null;
			}
		}).when(servlet).service(Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class));
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
