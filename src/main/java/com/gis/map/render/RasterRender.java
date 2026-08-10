package com.gis.map.render;

import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.map.GridCoverageLayer;
import org.geotools.map.MapContent;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;

//import com.gis.map.RasterLayer;

public class RasterRender {
	
	public MapContent mapContent = new MapContent();
	public GTRenderer renderer = new StreamingRenderer();
	
	public RasterRender() {

//		Map<Object, Object> hints = new HashMap<>();
//		hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		this.renderer.setRendererHints(hints);
		
		renderer.setJava2DHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED));
		Map<Object, Object> rendererHints = new HashMap<Object, Object>();
		rendererHints.put("optimizedDataLoadingEnabled", Boolean.valueOf(true));
		renderer.setRendererHints(rendererHints);
		this.renderer.setMapContent(mapContent);
	}
	
	public void addRasterLayer(com.gis.map.RasterLayer rl) {
		StyleFactory sf = CommonFactoryFinder.getStyleFactory();
		RasterSymbolizer rs = sf.getDefaultRasterSymbolizer();

		Style style = SLD.wrapSymbolizers(rs);

		GridCoverageLayer gcl = new GridCoverageLayer(rl.getGridCoverage2D(), style);

		mapContent.addLayer(gcl);
		//this.renderer.setMapContent(mapContent);
	}
	
	public void addRasterLayer(com.gis2.map.RasterLayer rl) {
		StyleFactory sf = CommonFactoryFinder.getStyleFactory();
		RasterSymbolizer rs = sf.getDefaultRasterSymbolizer();

		Style style = SLD.wrapSymbolizers(rs);

		GridCoverageLayer gcl = new GridCoverageLayer(rl.getGridCoverage2D(), style);

		mapContent.addLayer(gcl);
		//this.renderer.setMapContent(mapContent);
	}
	
	
	public void clearLayer() {
		this.mapContent.layers().clear();
	}
}
