package com.pillartechnology.jsptf.tiles;

import static org.apache.tiles.servlet.context.ServletUtil.CURRENT_CONTAINER_ATTRIBUTE_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.tiles.AttributeContext;
import org.apache.tiles.TilesContainer;
import org.apache.tiles.impl.BasicTilesContainer;
import org.mockito.Mockito;

import com.pillartechnology.jsptf.core.WebViewRenderer;
import com.pillartechnology.jsptf.core.WebViewRendererStrategy;

public class TilesWebViewRendererStrategy implements WebViewRendererStrategy {

	public void apply(WebViewRenderer renderer) {
		TilesContainer container = mock(BasicTilesContainer.class);
		AttributeContext context = mock(AttributeContext.class);
		when(container.getAttributeContext(Mockito.any(Object[].class))).thenReturn(context);
		renderer.addRequestAttribute(CURRENT_CONTAINER_ATTRIBUTE_NAME, container);
	}
}
