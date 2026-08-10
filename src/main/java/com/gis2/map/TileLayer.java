package com.gis2.map;

import java.awt.Graphics2D;

import org.locationtech.jts.geom.Envelope;

import com.gis.map.Layer;
import com.gis.map.TileMapFactory2;
import com.gis2.storage.TileServiceMng;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class TileLayer extends Layer {
	
	private TileMapFactory2 tmf = null;
	
	Object lock = new Object();
	
	@Override
	public Envelope getBounds() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setName(String name) {
		super.setName(name);
		this.tmf = TileServiceMng.getTileMapFactorys().get(name);
	}

	public void draw(Envelope env, Graphics2D img, int imgWidth, int imgHeight) {
		synchronized (this.lock) {
			if(tmf  == null) {
				tmf = TileServiceMng.getTileMapFactorys().get(this.getName());
				if(tmf != null) {
					tmf.realTileMode = false;
				}
			}
		}
		if(tmf != null) {
			tmf.draw(env, img, imgWidth, imgHeight);
		}
	}
}
