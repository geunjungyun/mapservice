package com.gis2.map;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.data.file.FileLayer;
import com.gis.map.Layer;
//import com.dawul.data.file.wrapper.UMDReader;
//import com.gis.engine.datamanager.umdfile.UMDWriter;
//import com.gis.protocol.freegis3.Conditions;
//import com.gis.protocol.freegis3.Field;
//import com.gis.protocol.freegis3.SubLayer;
import com.gis2.map.style.PointStyle;

import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
//import com.gis.engine.datamanager.umdfile.UMDReader;
//import com.gis.engine.datamanager.umdfile.UMDWriter;
import org.locationtech.jts.geom.Envelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;

//import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class VectorLayer extends Layer {


//	private Style style;
//	
//	private String styleName;
	
	private Vector<Style> style;
	
	private Vector<String> styleName;
	

	private FileLayer layerReader;
	
	public long date = -1;
	
	private ListFeatureCollection features = null;
	
	private int epsgCode = -1;

	public boolean isVisible = true;
	

	public void clone(VectorLayer clone) {
		
		clone.setName(this.getName());
		
		clone.style = this.style;
		clone.styleName = this.styleName;
		clone.layerReader = this.layerReader;
		
		clone.date = this.date;
		//this.drawPriority = this.getDrawPriority();
		this.setDrawPriority(this.getDrawPriority());
		this.isVisible = this.isVisible;
		
	}
	
	
	
	
	public void setStyleName(String styleName){
		if(this.styleName == null) {
			this.styleName = new Vector();
		}
		this.styleName.clear();
		this.styleName.add(styleName);
	}
	
	public String getStyleName(){
		if(this.styleName == null) {
			return null;
		}
		
		if(this.styleName.size() > 0) {
			return this.styleName.get(0);
		}
		return null;
	}	
	
	public void setStyle(Style style) {
		// TODO Auto-generated method stub
		if(this.style == null ) {
			this.style = new Vector();
		}
		this.style.clear();
		this.style.add(style);
	}
	
	public Style getStyle(int idx) {
		// TODO Auto-generated method stub
		if(this.style.size() > 0) {
			return this.style.get(idx);
		}
		return null;
	}
	
	public int getStyleCnt() {
		if(this.style == null || this.style.size() == 0) {
			return 0;
		}
		return this.style.size();
	}
	
	public Style getStyle() {
		// TODO Auto-generated method stub
		if(this.style != null && this.style.size() > 0) {
			return this.style.get(0);
		}
		return null;
	}

	
	public void addStyle(Style style) {
		if(this.style == null ) {
			this.style = new Vector();
		}
		this.style.add(style);
	}

	public Envelope getBounds() {
		// TODO Auto-generated method stub
		//return this.umdR.getUADBlock().getBound();
		return this.layerReader.getEnvelope();
	}


	
	/*
	public UMDReader getUMDReader() {
		// TODO Auto-generated method stub
		return this.umdR;
	}
	
	public void setUMDReader(UMDReader reader) {
		// TODO Auto-generated method stub
		this.umdR = reader;
		
		if(this.umdR != null) {
			try {
				epsgCode = CRS.lookupEpsgCode(this.umdR.getFreeLayer().getSimpleFeatureType().getCoordinateReferenceSystem(), false);
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				epsgCode = -1;
			}
		}
		
		if(this.subLayer != null) {
			
				SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();
				//freeLayer.getSimpleFeatureType().getAttributeDescriptors();
				sftb.addAll(this.umdR.getFreeLayer().getSimpleFeatureType().getAttributeDescriptors());
				
				List<Field> fds = this.subLayer.getField();
				
				for(Field fd : fds) {
					Class cls = null;
					
					if(fd.getType().equals("byte")) {
						cls = Byte.class;
					}
					else if(fd.getType().equals("short")) {
						cls = Short.class;
					}
					else if(fd.getType().equals("int")) {
						cls = Integer.class;
					}
					else if(fd.getType().equals("long")) {
						cls = Long.class;
					}
					else if(fd.getType().equals("double")) {
						cls = Double.class;
					}
					else if(fd.getType().equals("date")) {
						cls = Date.class;
					}
					else if(fd.getType().equals("string")) {
						cls = String.class;
					}
					else if(fd.getType().equals("float")) {
						cls = Float.class;
					}
					sftb.add(fd.getName(), cls);
					
				}
				
				sftb.setName(this.getName());
				this.subSFT = sftb.buildFeatureType();
				this.subSfb = new SimpleFeatureBuilder(this.subSFT);
			}
	}
	
	*/
	
	public int getEPSGCode() {
		return this.epsgCode;
	}
	
	public boolean isPointStyle() {
		for(Style st : this.style) {
			if(st instanceof PointStyle) {
				return true;
			}
		}
		return false;
	}
	

//	public UMDWriter getUMDWriter() {
//		// TODO Auto-generated method stub
//		return null;
//	}




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
	
	public ListFeatureCollection getFeatures() {
		return this.features;
	}
	
	public int getGridSize() {
		//return this.layerReader
		//return 0;
		return this.layerReader.getGridSize();
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

	
	

}
