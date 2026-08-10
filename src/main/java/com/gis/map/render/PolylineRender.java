package com.gis.map.render;

import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;

//import com.gis.map.style.BasicStyleExtend;
import org.locationtech.jts.geom.Geometry;

import com.gis.map.style.BasicStyleExtend;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class PolylineRender implements IRender {

	public void draw(Graphics2D g, Shape shape, Style style) {
		
		
		BasicStyleExtend bse = (BasicStyleExtend) style;
		
		Composite oldComposite = g.getComposite();

		
		if (bse.isRenderingComposite() == true) {
			g.setComposite(bse.getComposite());
		}
		
		if (bse.isRenderingFill() == true) {
			g.setColor(bse.getFillColor());
			if (bse.isRenderingFillPattern() == true) {
				g.setPaint(bse.getFillPattern());
			}
			if(bse.getFillStroke() != null){
				g.setStroke(bse.getFillStroke());
				g.draw(shape);
			}
			if (bse.isRenderingLine() == true) {
				g.setStroke(bse.getLineStroke());
				g.setColor(bse.getLineColor());
				g.draw(shape);
			}
		} else {
			if (bse.isRenderingLine() == true) {
				g.setStroke(bse.getLineStroke());
				g.setColor(bse.getLineColor());
				//System.out.println(shape.toString());
				g.draw(shape);
			}
		}
		if (bse.isRenderingComposite() == true) {
			g.setComposite(oldComposite);
		}

	}

}
