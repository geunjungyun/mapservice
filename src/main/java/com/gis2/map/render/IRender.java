package com.gis2.map.render;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;


import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public interface IRender {

	public void draw(Graphics2D g, Shape geo, Style style);

	// public boolean write();
}
