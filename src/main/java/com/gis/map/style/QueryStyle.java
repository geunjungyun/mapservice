package com.gis.map.style;

import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class QueryStyle {

	public String name;
	public String value;
	
	public String deleteOverlapMode = "";
	public int extenstionArea = 0;
	
	public int priority = -1;

	public BasicStyleExtend style;

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setStyle(BasicStyleExtend style) {
		this.style = style;
	}
}
