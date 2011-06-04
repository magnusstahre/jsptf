package com.pillartechnology.jsptf.tiles;

import static org.apache.tiles.servlet.context.ServletUtil.CURRENT_CONTAINER_ATTRIBUTE_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import org.apache.tiles.AttributeContext;
import org.apache.tiles.TilesContainer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.pillartechnology.jsptf.core.WebViewRenderer;

@RunWith(MockitoJUnitRunner.class)
public class TilesWebViewRendererStrategyTest {
	
	@Mock
	private WebViewRenderer renderer;
	
	private final TilesWebViewRendererStrategy strategy = new TilesWebViewRendererStrategy();
	
	@Test
	public void addsContainerOntoTheRequest() throws Exception {
		strategy.apply(renderer);
		
		verify(renderer).addRequestAttribute(Mockito.eq(CURRENT_CONTAINER_ATTRIBUTE_NAME), Mockito.any(TilesContainer.class));
	}

	@Test
	public void attributeContainerReturnsContext() throws Exception {
		ArgumentCaptor<TilesContainer> captor = ArgumentCaptor.forClass(TilesContainer.class);
		doNothing().when(renderer).addRequestAttribute(Mockito.eq(CURRENT_CONTAINER_ATTRIBUTE_NAME), captor.capture());
		
		strategy.apply(renderer);
		
		TilesContainer tilesContainer = captor.getValue();
		assertThat(tilesContainer.getAttributeContext(new Object()), instanceOf(AttributeContext.class));
	}
}
