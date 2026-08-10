package com.gis.map;

import java.awt.Graphics2D;

import org.locationtech.jts.geom.Envelope;

import com.gis2.storage.TileServiceMng;

public class TileLayer extends Layer{
	
	TileMapFactory2 mng = null;
	Object lock = new Object();
	

	public void draw(Envelope env, Graphics2D g, int imgWidth, int imgHeight) {
		synchronized (this.lock) {
			if(mng  == null) {
				mng = TileServiceMng.emaps.get(this.getName());
			}
		}
		if(mng != null) {
			mng.draw(env, g, imgWidth, imgHeight);
		}
	}
	
	public void draw(Envelope env, int level,  Graphics2D img, int imgWidth, int imgHeight) {
		synchronized (this.lock) {
			if(mng  == null) {
				mng = TileServiceMng.emaps.get(this.getName());
			}
		}
		if(mng != null) {
		
			mng.draw(env, level, img, imgWidth, imgHeight);
		}
	}
	

}
