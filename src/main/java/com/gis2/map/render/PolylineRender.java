package com.gis2.map.render;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;

//import com.gis.map.style.BasicStyleExtend;
import org.locationtech.jts.geom.Geometry;

import com.gis2.map.style.BasicStyleExtend;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class PolylineRender implements IRender {
	
	
	public void draw(Graphics2D g, Shape shape_, Style style) {
		
		Shape shape = shape_;
		
		BasicStyleExtend bse = (BasicStyleExtend) style;
		
		if(bse.arrowStroke != null) {
			shape = bse.arrowStroke.createStrokedShape(shape_);
		}
		
		
		Composite oldComposite = g.getComposite();
		
		if (bse.isRenderingComposite() == true) {
			oldComposite = g.getComposite();
			g.setComposite(bse.getComposite());
		}
	
		Composite oldfillComposite = null; 
		
		
		Stroke oldStroke = null;
		
		Color oldColor = g.getColor();
		
		
		if(bse.isFillTransparency()) {
			oldfillComposite = g.getComposite();
			g.setComposite(bse.getFillComposite());
		}
		
		oldStroke = g.getStroke();
		
		if (bse.isRenderingFill() == true) {
			
			g.setColor(bse.getFillColor());
			if(bse.getFillStroke() != null){
				g.setStroke(bse.getFillStroke());
			}
			g.draw(shape);
		}
		
		g.setStroke(oldStroke);
		
		if(bse.isFillTransparency()) {
			g.setComposite(oldfillComposite);
		}
		
		Composite oldlineComposite = null; 
		
		if(bse.isLineTransparency()) {
			oldlineComposite = g.getComposite();
			g.setComposite(bse.getLineComposite());
		}
		
		if (bse.isRenderingFillPattern() == true) {
			g.setPaint(bse.getFillPattern());
			g.fill(shape);
		}
		
		oldStroke = g.getStroke();
		
		if (bse.isRenderingLine() == true) {
			g.setStroke(bse.getLineStroke());
			g.setColor(bse.getLineColor());
			//System.out.println(shape.toString());
			
			g.draw(shape);
			
		}
		
		if(bse.getShapeStroke() != null) {
			
			g.setColor(bse.getLineColor());
			g.setStroke(bse.getShapeStroke());
			g.draw(shape);

		}
		
		if(bse.isLineTransparency()) {
			g.setComposite(oldlineComposite);
		}
		
		if (bse.isRenderingComposite() == true) {
			g.setComposite(oldComposite);
		}

		g.setStroke(oldStroke);
		g.setColor(oldColor);
		
	}
	
	/**
	 * 격자로 잘려진 라인을 지우기 위해서 채움색으로 라인을 그림
	 * @param g
	 * @param shape
	 * @param style
	 */
	public void draw3(Graphics2D g, Shape shape, Style style) {
		
		
		BasicStyleExtend bse = (BasicStyleExtend) style;
		
		Composite oldComposite = g.getComposite();
		
		if (bse.isRenderingComposite() == true) {
			oldComposite = g.getComposite();
			g.setComposite(bse.getComposite());
		}
	
		Composite oldfillComposite = null; 
		
		
		Stroke oldStroke = null;
		
		Color oldColor = g.getColor();
		
		
		/*
		if(bse.isFillTransparency()) {
			oldfillComposite = g.getComposite();
			g.setComposite(bse.getFillComposite());
		}
		
		oldStroke = g.getStroke();
		
		if (bse.isRenderingFill() == true) {
			
			g.setColor(bse.getFillColor());
			if(bse.getFillStroke() != null){
				g.setStroke(bse.getFillStroke());
			}
			g.draw(shape);
		}
		
		g.setStroke(oldStroke);
		
		if(bse.isFillTransparency()) {
			g.setComposite(oldfillComposite);
		}
		*/
		
		Composite oldlineComposite = null; 
		
		if(bse.isLineTransparency()) {
			oldlineComposite = g.getComposite();
			g.setComposite(bse.getLineComposite());
		}
		
		oldStroke = g.getStroke();
		
		if (bse.isRenderingLine() == true) {
			g.setStroke(bse.getLineStroke());
			//g.setColor(bse.getFillColor());
			g.setColor(Color.red);
			//System.out.println(shape.toString());
			
			g.draw(shape);
			
		}
		
		if(bse.getShapeStroke() != null) {
			
			g.setColor(bse.getFillColor());
			g.setStroke(bse.getShapeStroke());
			g.draw(shape);

		}
		
		if(bse.isLineTransparency()) {
			g.setComposite(oldlineComposite);
		}
		
		if (bse.isRenderingComposite() == true) {
			g.setComposite(oldComposite);
		}

		g.setStroke(oldStroke);
		g.setColor(oldColor);
		
	}
	

}
