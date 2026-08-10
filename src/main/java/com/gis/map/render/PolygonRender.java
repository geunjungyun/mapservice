package com.gis.map.render;//

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;

//import com.gis.map.style.BasicStyleExtend;
import org.locationtech.jts.geom.Geometry;

import com.gis.map.style.BasicStyleExtend;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class PolygonRender extends PolylineRender implements IRender {
	GridObjectRender gor = new GridObjectRender();

	public void draw(Graphics2D g, Shape shape, Style style, int gridSize) {
		// TODO Auto-generated method stub
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
			g.fill(shape);
//			if (bse.isRenderingLine()) {
//				g.setStroke(bse.getLineStroke());
//				g.setColor(bse.getLineColor());
//				
//				super.draw(g, shape, style);
//				//gor.draw(g, shape, 256, 256, style);
//				
//				//gor.draw(g, shape, gridSize, gridSize, style);
//			}
		} 
		if (bse.isRenderingLine() == true) {
			//g.setStroke(bse.getLineStroke());
			
			//g.setStroke(new BasicStroke(2));
			g.setStroke(bse.getLineStroke());
			g.setColor(bse.getLineColor());
			//super.draw(g, shape, style);
			gor.draw(g, shape, gridSize, gridSize, style);
			//g.draw(shape);
		}
		
//		if (bse.isRenderingFill() == true) {
//			g.setColor(bse.getFillColor());
//			if (bse.isRenderingFillPattern() == true) {
//				g.setPaint(bse.getFillPattern());
//			}
//			g.fill(shape);
//			
//		} 
		
		if (bse.isRenderingComposite() == true) {
			g.setComposite(oldComposite);
		}

	}

}
