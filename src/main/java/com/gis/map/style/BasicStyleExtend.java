package com.gis.map.style;

import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Stroke;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class BasicStyleExtend extends BasicStyle {

	
	
	BasicStroke fillStroke = null;
	
	String name;
	String desc;

	// 0 : query로 정의 된 것과 나머지 표출
	// 1 : query로 정의 된 것만 표출
	static public final int QUERY_ALL = 0;
	static public final int QUERY = 1;

	int queryMode = 0;

	Vector<QueryStyle> querys = null;

	Composite composite;
	boolean isComposite;
	
	float lineTransparency = 0;
	float fillTransparency = 0;
	
	String drawOrder = "";
	
	public void setDrawOrder(String value) {
		this.drawOrder = value;
	}
	
	public String getDrawOrder() {
		return this.drawOrder;
	}
	
	public void setLineTransparency(float lineTransparency_) {
		this.lineTransparency = lineTransparency;
	}

	public float getLineTransparency() {
		return this.lineTransparency;
	}

	public void setFillTransparency(float fillTransparency_) {
		this.fillTransparency = fillTransparency_;
	}

	public float getfFllTransparency_() {
		return this.fillTransparency;
	}


	public void setName(String name) {
		this.name = name;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getName() {
		return this.name;
	}

	public String getDesc() {
		return this.desc;
	}

	public void setComposite(Composite com) {
		this.composite = com;
	}

	public Composite getComposite() {
		return this.composite;
	}

	public void setRenderingComposite(boolean mode) {
		this.isComposite = mode;
	}

	public boolean isRenderingComposite() {
		return this.isComposite;
	}
	
	public void setFillStroke(float lineWidth) {
		this.fillStroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND); 
	}
	
	public Stroke getFillStroke(){
		return this.fillStroke;
	}

	public boolean isQuery() {
		if (this.querys != null && this.querys.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	public void setQuerys(Vector<QueryStyle> querys) {
		this.querys = querys;
		Collections.sort(this.querys, new Comparator<QueryStyle>() {

			@Override
			public int compare(QueryStyle o1, QueryStyle o2) {
				// TODO Auto-generated method stub
				if(o1.priority < o2.priority) {
					return -1;
				}
				else if(o1.priority  > o1.priority) {
					return 1;
				}
				
				return 0;
			}
			
		}
		);
		
	}

	public Vector<Style> getAddStyle() {
		
		Vector<Style> addStyles = new Vector();
		
		for (QueryStyle qs : this.querys) {
			if(qs.name == null || qs.name.trim().length() == 0) {
				addStyles.add(qs.style);
			}
		}
		
		
		
		return addStyles;
	}

//	public Style getQueryStyle(SimpleFeature feature) {
//		
//		if(this.querys == null || this.querys.size() == 0){
//			return null;
//		}
//		
//		for (QueryStyle qs : this.querys) {
//			String fieldName = qs.name.toLowerCase();
//			
//			if(fieldName == null || fieldName.trim().length() == 0) {
//				continue;
//			}
//			
//			SimpleFeatureType fs = feature.getFeatureType();
//			int fieldNameIdx = fs.indexOf(fieldName);
//			
//			
//			Object featureValue = feature.getAttribute(fieldNameIdx);
//			if (featureValue instanceof String) {
//				String value = (String) featureValue;
//				int idx = value.indexOf(".");
//				if(idx > -1){
//					value = value.substring(0, idx);
//				}
//				if (qs.value != null && value.trim().equals(qs.value.trim())) {
//					return qs.style;
//				}
//			}else if (featureValue instanceof Integer) {
//				Integer value = (Integer) featureValue;
//				if (value == Integer.parseInt(qs.value)) {
//					return qs.style;
//				}
//			}
//			
//			if(qs.value == null){
//				return qs.style;
//			}
//			if(featureValue.equals(qs.value.trim())){
//				return qs.style;
//			}
//		}
//		return null;
//	}
	
//	public QueryStyle getQueryStyle(SimpleFeature feature) {
//		
//		if(this.querys == null || this.querys.size() == 0){
//			return null;
//		}
//		
//		for (QueryStyle qs : this.querys) {
//			String fieldName = qs.name.toLowerCase();
//			
//			if(fieldName == null || fieldName.trim().length() == 0) {
//				continue;
//			}
//			
//			SimpleFeatureType fs = feature.getFeatureType();
//			int fieldNameIdx = fs.indexOf(fieldName);
//			
//			
//			Object featureValue = feature.getAttribute(fieldNameIdx);
//			if (featureValue instanceof String) {
//				String value = (String) featureValue;
//				int idx = value.indexOf(".");
//				if(idx > -1){
//					value = value.substring(0, idx);
//				}
//				if (qs.value != null && value.trim().equals(qs.value.trim())) {
//					return qs;
//				}
//			}else if (featureValue instanceof Integer) {
//				Integer value = (Integer) featureValue;
//				if (value == Integer.parseInt(qs.value)) {
//					return qs;
//				}
//			}
//			
//			if(qs.value == null){
//				//return qs;
//				continue;
//			}
//			
//			if(featureValue == null) {
//				return null;
//			}
//			
//			
//			if(featureValue.equals(qs.value.trim())){
//				return qs;
//			}
//		}
//		return null;
//	}

	public Vector<QueryStyle> getQueryStyle(SimpleFeature feature) {
		
		Vector<QueryStyle> qss = new Vector();
		
		if(this.querys == null || this.querys.size() == 0){
			return null;
		}
		
		for (QueryStyle qs : this.querys) {
			
			if(qs.name == null && qs.value == null) {
				qss.add(qs);
				continue;
			}
			
			
			String fieldName = qs.name.toLowerCase();
			
			if(fieldName == null || fieldName.trim().length() == 0) {
				continue;
			}
			
			if(qs.value == null){
				System.out.println("qs = " + qs.name+" is value null");
				continue;
			}
			
			
			
			SimpleFeatureType fs = feature.getFeatureType();
			
			int fieldNameIdx = fs.indexOf(qs.name);
			
			if(fieldNameIdx == -1) {
				fieldNameIdx = fs.indexOf(qs.name.toLowerCase());
			}
			
			
			if(fieldNameIdx == -1) {
				System.out.println("getQueryStyle::layerName="+fs.getName()+" not exist column : " + fieldName+", query name = " + qs.name);
				return qss;
			}
			
			
			Object featureValue = feature.getAttribute(fieldNameIdx);
			
			if(featureValue == null) {
				continue;
			}
			
			
			String value = featureValue+"";
			
			if(qs.value.equals(value)) {
				qss.add(qs);
			}
			

		}
		return qss;
	}


	public boolean isQueryStyle(SimpleFeature feature) {
		for (QueryStyle qs : this.querys) {
			String fieldName = qs.name;
			SimpleFeatureType fs = feature.getFeatureType();
			int fieldNameIdx = fs.indexOf(fieldName);
			Object featureValue = feature.getAttribute(fieldNameIdx);
			if (featureValue instanceof String) {
				String value = (String) featureValue;
				if (value.trim().equals(qs.value)) {
					return true;
				}
			}
		}
		return false;
	}

	public void setCompare(Style st) {

	}
	
//	@Override
//	public void setLineWidth(float lineWidth) {
//		// Don't use BasicStroke.JOIN_ROUND or JOIN_BEVEL -- when the line
//		// width is 1, one of the corners will not be drawn. [Jon Aquino]
//		BasicStroke lineStroke = createLineStroke(lineWidth);
//	}
	


}
