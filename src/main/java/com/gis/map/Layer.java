package com.gis.map;

import org.locationtech.jts.geom.Envelope;

import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public abstract class Layer  implements ILayer, Comparable{
	
	private float drawPriority = -1;
	private String layerName  = "";
	

	@Override
	public int compareTo(Object o) {
		Layer layerInfo = (Layer) o;
		if (layerInfo.drawPriority > this.drawPriority) {
			return 1;
		} else {
			return -1;
		}
	}


	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.layerName;
	}

	@Override
	public void setName(String title) {
		// TODO Auto-generated method stub
		this.layerName = title;
	}

	@Override
	public float getDrawPriority() {
		// TODO Auto-generated method stub
		return this.drawPriority;
	}

	@Override
	public void setDrawPriority(float value) {
		// TODO Auto-generated method stub
		this.drawPriority  = value;
	}

	@Override
	public Envelope getBounds() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
