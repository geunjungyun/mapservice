package com.gis.map;

import java.awt.Graphics2D;

import org.geotools.coverage.grid.GridCoverage2D;
import org.locationtech.jts.geom.Envelope;
import org.opengis.geometry.DirectPosition;

import com.gis2.storage.TileServiceMng;

public class RasterLayer extends Layer{
	
	private GridCoverage2D gc;
	
	private Envelope mbr;
	
	/*
	public void draw(Envelope env, Graphics2D img, int imgWidth, int imgHeight) {
		synchronized (this.lock) {
			if(mng  == null) {
				mng = TileServiceMng.emaps.get(this.getName());
			}
		}
		if(mng != null) {
			mng.draw(env, img, imgWidth, imgHeight);
		}
	}
	*/
	
	public void setGridCoverage2D(GridCoverage2D gc_) {
		this.gc = gc_;
		
		DirectPosition dp1 = this.gc.getEnvelope().getLowerCorner();
		double[] low = dp1.getCoordinate();
		
		DirectPosition dp2 = this.gc.getEnvelope().getUpperCorner();
		double[] upper = dp2.getCoordinate();
		
		this.mbr = new Envelope(low[0], upper[0], low[1], upper[1]);
		
	}
	
	
	public GridCoverage2D getGridCoverage2D() {
		return this.gc;
	}
	
	public boolean isIntersect(Envelope env) {
		
		return this.mbr.intersects(env);
		
	}
	
}
