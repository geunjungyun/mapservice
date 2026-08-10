package com.gis2.map.render;

import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;

public class GTRendererPool extends BasePooledObjectFactory<GTRenderer>{

	@Override
	public GTRenderer create() throws Exception {
		// TODO Auto-generated method stub
		GTRenderer renderer = new StreamingRenderer();
		renderer.setJava2DHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED));
		Map<Object, Object> rendererHints = new HashMap<Object, Object>();
		rendererHints.put("optimizedDataLoadingEnabled", Boolean.valueOf(true));
		renderer.setRendererHints(rendererHints);
		return renderer;
	}

	@Override
	public PooledObject<GTRenderer> wrap(GTRenderer renderer) {
		// TODO Auto-generated method stub
		renderer.setMapContent(null);
		return new DefaultPooledObject<GTRenderer>(renderer);
	}

//	@Override
//	public Object makeObject() throws Exception {
//		// TODO Auto-generated method stub
//		GTRenderer gtRenderer = new StreamingRenderer();
//		gtRenderer.setJava2DHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED));
//		Map<Object, Object> RendererHints = new HashMap<Object, Object>();
//		RendererHints.put("optimaizedDataLoadingEnabled", Boolean.valueOf(true));
//		gtRenderer.setRendererHints(RendererHints);
//		return gtRenderer;
//	}
}
