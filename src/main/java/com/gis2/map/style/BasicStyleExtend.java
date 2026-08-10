package com.gis2.map.style;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Stroke;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

//import com.vividsolutions.jump.util.StringUtil;
//import com.gis.xml.raster.QueryS;
//import com.vividsolutions.jump.feature.Feature;
//import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.ui.renderer.style2.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class BasicStyleExtend extends BasicStyle {
	
	
	public String symbolPath = null;
	
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
	
	Composite lineComposite = null;
	Composite fillComposite = null;
	
	boolean isLineComposite = false;
	boolean isFillComposite = false;
	
	boolean isGradient = false;
	
	// 항공영상용 안티알리아싱 여부
	boolean isAntiAliasing = false;
	
	public boolean isAntiAliasing() {
		return this.isAntiAliasing;
	}
	
	public boolean isGradient() {
		return this.isGradient;
	}
	
	float[] gradientDist = null;
	Color[] gradientcolors = null;
	
	
	public void setGradient(boolean value) {
		this.isGradient = value;
		
        this.gradientDist = new float[2];
        this.gradientDist[0] = 0.0f;
        this.gradientDist[1] = 1.0f;
        
        this.gradientcolors = new Color[2];
        this.gradientcolors[0] = getFillColor().brighter();
        this.gradientcolors[1] = getFillColor().darker().darker();
		
	}
	
	public float[] getGradientDist() {
		return this.gradientDist;
	}
	
	public Color[] getGradientColors() {
		return this.gradientcolors;
	}
	
	public void setAntiAliasing(boolean isAntiAliasing) {
		this.isAntiAliasing = isAntiAliasing;
	}
	
	public void setLineTransparency(float lineTransparency_) {
		this.isLineComposite = true;
		this.lineComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (100-lineTransparency_) * 0.01f);
	}

	public Composite getLineComposite() {
		return this.lineComposite;
	}
	
	public boolean isLineTransparency(){
		return this.isLineComposite;
	}
	
	public boolean isFillTransparency(){
		return this.isFillComposite;
	}


	public void setFillTransparency(float fillTransparency_) {
		this.isFillComposite = true;
		this.fillComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (100-fillTransparency_) * 0.01f);
	}

	public Composite getFillComposite() {
		return this.fillComposite;
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
		this.lineFillWidth = lineWidth;
		this.fillStroke = new BasicStroke(lineWidth, this.cap, this.join); 
		//this.fillStroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
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
//		Collections.sort(this.querys, new Comparator<QueryStyle>() {
//
//			@Override
//			public int compare(QueryStyle o1, QueryStyle o2) {
//				// TODO Auto-generated method stub
//				if(o1.priority < o2.priority) {
//					return -1;
//				}
//				else if(o1.priority  > o1.priority) {
//					return 1;
//				}
//				
//				return 0;
//			}
//			
//		}
//		);
		
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


	public Vector<QueryStyle> getQueryStyle(SimpleFeature feature) {
		
		Vector<QueryStyle> qss = new Vector();
		
		if(this.querys == null || this.querys.size() == 0){
			return null;
		}
		//System.out.println("k_reg_tp=" + feature.getAttribute("k_reg_tp"));
		for (QueryStyle qs : this.querys) {
			
			if(qs.filter != null) {
				
				synchronized(qs.filter) {
					if(qs.filterValidate == false) {
						qs.filterValidate(feature.getFeatureType());
					}
				}
				
				if(qs.filter.evaluate(feature)) {
					qss.add(qs);
				}
				
				continue;
			}
			
			if(qs.name == null && qs.value == null && (qs.cql == null || qs.cql.length() == 0)) {
				qss.add(qs);
				continue;
			}
			
			if(qs.name == null ) {
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
			
			if(featureValue == null && !qs.value.equals("null")) {
				continue;
			}
			
			AttributeDescriptor ad = fs.getDescriptor(fieldNameIdx);
			
			ad.getType().getBinding();
			
			if(this.isLike(ad.getType().getBinding(), featureValue, qs.value)) {
				qss.add(qs);
				continue;
			}
			
			
			String value = featureValue+"";
//			
//			if (qs.value.equals(value)  ) {
//				qss.add(qs);
//				continue;
//			}
			
			int intValue = 0;
			try {
				//intValue = Integer.parseInt(value);
				intValue = (int) Double.parseDouble(value);
			} catch (Exception e) {
				// TODO: handle exception
				continue;
			}
			
			String[] queryValues = qs.value.split("\\|");
			
			if (queryValues.length > 1) {
				if (Integer.parseInt(queryValues[0]) <= intValue &&
						intValue <= Integer.parseInt(queryValues[1])) {
					qss.add(qs);
				}
			}
			
		}
		return qss;
	}
	
	
	public boolean isLike(Class cls, Object src, String value) {
		if(cls.equals(Integer.class)){
			Double conv = Double.parseDouble(value);
			if(((Integer)src).intValue() == conv.intValue()) {
				return true;
			}
		}
		else if(cls.equals(Byte.class)){
			Double conv = Double.parseDouble(value);
			if( ((Byte)src).byteValue() == conv.byteValue()) {
				return true;
			}
		}
		else if(cls.equals(Short.class)){
			Double conv = Double.parseDouble(value);
			if(((Short)src).shortValue() == conv.shortValue() ) {
				return true;
			}
		}
		else if(cls.equals(Double.class)){
			Double conv = Double.parseDouble(value);
			if(((Double)src).doubleValue() == conv.doubleValue() ) {
				return true;
			}
		}
		else if(cls.equals(Float.class)){
			Float conv = Float.parseFloat(value);
			if(((Float)src).floatValue() == conv.floatValue() ) {
				return true;
			}
		}
		else if(cls.equals(Long.class)){
			Long conv = Long.parseLong(value);
			if(((Long)src).longValue() == conv.longValue() ) {
				return true;
			}

		}
		else if(cls.equals(Boolean.class)){
			Boolean conv = Boolean.parseBoolean(value);
			if(((Boolean)src).booleanValue() == conv.booleanValue()) {
				return true;
			}
		}
		else if(cls.equals(Date.class)) {
			Date conv = new Date(value);
			if(src.equals(conv)) {
				return true;
			}
		}
		else if(cls.equals(String.class)){
			if(((String)(src)).trim().toLowerCase().equals(value.trim().toLowerCase())) {
				return true;
			}
		}
		else if(cls.equals(BigDecimal.class)) {
			double srcb = ((BigDecimal)src).doubleValue();
			double conv = Double.parseDouble(value);
			if(srcb == conv) {
				return true;
			}
			//dos.writeDouble(((BigDecimal)obj).doubleValue());
		}
		return false;

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
	
	public Vector<QueryStyle> getQuerys() {
		return this.querys;
	}
	

}
