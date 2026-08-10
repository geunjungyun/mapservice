package com.gis.map;

import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.referencing.CRS;
//import com.gis.engine.datamanager.umdfile.UMDReader;
//import com.gis.engine.datamanager.umdfile.UMDWriter;
import org.locationtech.jts.geom.Envelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;

import com.data.file.FileLayer;
//import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class VectorLayer extends Layer {

	public Style style;
	
	String styleName;

	private FileLayer layerReader;
	
	public long date = -1;
	
	ListFeatureCollection features = null;
	
	private int epsgCode = -1;

	public boolean isVisible = true;
	
	public void clone(VectorLayer clone) {
		
		clone.setName(this.getName());
		
		clone.style = this.style;
		clone.styleName = this.styleName;
		clone.layerReader = this.layerReader;
		clone.date = this.date;

		this.setDrawPriority(this.getDrawPriority());
		this.isVisible = this.isVisible;
		

	}

	
	public void setStyleName(String styleName){
		this.styleName = styleName;
	}
	
	public String getStyleName(){
		return this.styleName;
	}

	public Envelope getBounds() {
		// TODO Auto-generated method stub
		//return this.umdR.getUADBlock().getBound();
		return this.layerReader.getEnvelope();
	}

	public Style getStyle() {
		// TODO Auto-generated method stub
		return this.style;
	}

	public FileLayer getFileReader() {
		// TODO Auto-generated method stub
		return this.layerReader;
	}
	
	public void setFileReader(FileLayer reader) {
		// TODO Auto-generated method stub
		this.layerReader = reader;
		
		if(this.layerReader != null) {
			try {
				epsgCode = CRS.lookupEpsgCode(this.layerReader.getSimpleFeatureType().getCoordinateReferenceSystem(), false);
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				epsgCode = -1;
			}
		}
	}
	
	public int getEPSGCode() {
		return this.epsgCode;
	}
	


	public void setStyle(Style style) {
		// TODO Auto-generated method stub
		this.style = style;
	}


//	public int compareTo(Object o) {
//		VectorLayer layerInfo = (VectorLayer) o;
//		if (layerInfo.drawPriority > this.drawPriority) {
//			return 1;
//		} else {
//			return -1;
//		}
//	}
	
	public void setFeatures(ListFeatureCollection fts) {
		this.features = fts;
	}
	
//	public int addFeature(SimpleFeature feature){
//		
//		if(this.re) {
//			
//		}
//		this.features.add(feature);
//		
//		return this.features.size();
//	}
//	
//	public void removeAll(){
//		//this.features.removeAllElements();
//		this.features.clear();
//	}

}
