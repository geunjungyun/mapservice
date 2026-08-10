package com.gis2.map.style;

import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;

import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class QueryStyle {

	public String name;
	public String value;
	
	public String deleteOverlapMode = "";
	public int extenstionArea = 0;
	
	public int priority = -1;

	public BasicStyleExtend style;
	
	public Filter filter =  null;
	
	public String cql = "";
	
	public boolean filterValidate = false; 

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setStyle(BasicStyleExtend style) {
		this.style = style;
	}
	
	public boolean filterValidate(SimpleFeatureType sft) {
		if(this.filterValidate == false) {
			boolean conOk = false;			
			for(int i=0; i<sft.getAttributeCount(); i++){
				AttributeDescriptor ad = sft.getDescriptor(i);
				String name = ad.getName().toString();
				
				if(cql.indexOf(name.toUpperCase()) > -1) {
					cql = cql.replaceAll(name.toUpperCase(), name);
					conOk = true;
				}
			}
			
			if(conOk) {
				try {
					this.filter = ECQL.toFilter(this.cql);
				} catch (CQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			this.filterValidate = true;
		}
		return this.filterValidate;
	}
}
