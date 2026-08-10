package com.gis2.map;

import org.opengis.feature.simple.SimpleFeature;

//import com.gis.map.style.PointStyle;

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

		if (fas.priority > this.priority) {
			return 1;
		} else if(fas.priority == this.priority){
			return 0;
		}
		else{
			return -1;
		}

	}

}
