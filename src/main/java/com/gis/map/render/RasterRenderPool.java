package com.gis.map.render;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;

public class RasterRenderPool extends BasePooledObjectFactory<RasterRender>{
	
	@Override
	public RasterRender create() throws Exception {
		// TODO Auto-generated method stub
		RasterRender rr = new RasterRender();
		return rr;
	}

	@Override
	public PooledObject<RasterRender> wrap(RasterRender renderer) {
		// TODO Auto-generated method stub

//		renderer.clearLayer();
		
		return new DefaultPooledObject<RasterRender>(renderer);
	}
	
	/**
	 * 객체 재사용을 위한 객체 초기화
	 */
	@Override
    public void passivateObject(PooledObject p)
            throws Exception {
		//System.out.println("passivateObject");
		RasterRender img = (RasterRender)p.getObject();
		img.mapContent.layers().clear();
    }

}
