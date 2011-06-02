package com.pillartechnology.jsptf.core;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;

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
public class RendererTest {

	@Mock
	private HttpServlet servlet;

	private Renderer renderer;

	@Before
	public void setUp() {
		renderer = new Renderer(servlet);
	}

	@Test
	public void renderInitializesServlet() throws Exception {
		renderer.render();

		verify(servlet).init(Mockito.any(ServletConfig.class));
	}

	@Test
	public void initsServletWithContextContainingAddedAttributes() throws Exception {
		final String attributeName = "name";
		final String attributeValue = "value";
		renderer.addContextAttribute(attributeName, attributeValue);
		ArgumentCaptor<ServletConfig> captor = ArgumentCaptor.forClass(ServletConfig.class);
		doNothing().when(servlet).init(captor.capture());

		renderer.render();

		assertThat(captor.getValue(), hasContextAttribute(attributeName, attributeValue));
	}

	private Matcher<ServletConfig> hasContextAttribute(final String name, final Object value) {
		return new TypeSafeMatcher<ServletConfig>() {

			@Override
			protected boolean matchesSafely(ServletConfig config) {
				return value.equals(config.getServletContext().getAttribute(name));
			}

			public void describeTo(Description description) {
				description.appendText(format("a ServletConfig containing a ServletContext with attribute [%s,%s]", name, value));
			}

			@Override
			protected void describeMismatchSafely(ServletConfig servletConfig, Description description) {
			}
		};
	}
}
