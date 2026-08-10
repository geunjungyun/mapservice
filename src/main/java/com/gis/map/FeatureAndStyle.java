package com.gis.map;

import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;

import com.gis.map.style.PointStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class FeatureAndStyle implements Comparable {

	int priority;
	SimpleFeature ft;
	Style st;

	public FeatureAndStyle(int priority, SimpleFeature ft, Style st) {
		this.priority = priority;
		this.ft = ft;
		this.st = st;
	}

	public int compareTo(Object o) {
		FeatureAndStyle fas = (FeatureAndStyle) o;
		
		Geometry geo = (Geometry)fas.ft.getDefaultGeometry();
		
		Geometry thisgeo = (Geometry)this.ft.getDefaultGeometry();
		
		//if (fas.priority > this.priority) {
		if (this.priority < fas.priority) {
			return 1;
		} else if(fas.priority == this.priority){
			if(thisgeo.getArea() > geo.getArea()) {
				return 1;
			}
			else if(thisgeo.getArea() == geo.getArea()){
				return 0;
			}
			else {
				return -1;
			}
		}
		else{
			return -1;
		}


	}

}
