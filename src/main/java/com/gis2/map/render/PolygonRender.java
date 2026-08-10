package com.gis2.map.render;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

//import com.gis.map.style.BasicStyleExtend;
import org.locationtech.jts.geom.Geometry;

import com.gis2.map.render.java2D.GeometryCollectionShape;
import com.gis2.map.render.java2D.PolygonShape;
import com.gis2.map.style.BasicStyleExtend;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class PolygonRender extends PolylineRender implements IRender {
	
	GridObjectRender gor = new GridObjectRender();

	public void draw1(Graphics2D g, Shape shape, Style style, int gridSize) {
		// TODO Auto-generated method stub
		
		//PolygonShape ps = null;
		
		boolean isPoint = false;
		
		if(shape instanceof PolygonShape) {
			isPoint = ((PolygonShape) shape).isPoint();
		}
		else if(shape instanceof GeometryCollectionShape) {
			isPoint = ((GeometryCollectionShape) shape).isPoint();
		}
		
		BasicStyleExtend bse = (BasicStyleExtend) style;
		
		Composite oldComposite = null;
		
		if (bse.isRenderingComposite() == true) {
			oldComposite = g.getComposite();
			g.setComposite(bse.getComposite());
		}
		
		Composite oldfillComposite = null; 
		if(bse.isFillTransparency()) {
			oldfillComposite = g.getComposite();
			g.setComposite(bse.getFillComposite());
		}
		
		if (bse.isRenderingFill() == true) {
			g.setColor(bse.getFillColor());
			g.fill(shape);
		} 
		
		if (bse.isRenderingFillPattern() == true) {
			g.setPaint(bse.getFillPattern());
			g.fill(shape);
		}
		
		if(bse.isFillTransparency()) {
			g.setComposite(oldfillComposite);
		}
		
		Composite oldlineComposite = null; 
		
		if(bse.isLineTransparency()) {
			oldlineComposite = g.getComposite();
			g.setComposite(bse.getLineComposite());
		}
		
		if (bse.isRenderingLine() == true) {
			g.setStroke(bse.getLineStroke());
			
			
			//g.setStroke(new BasicStroke(2));
			g.setColor(bse.getLineColor());
			//super.draw(g, shape, style);
			//gor.draw(g, shape, gridSize, gridSize, style);
			
			
			
			Rectangle2D rect = shape.getBounds2D();
			if(isPoint) {
				
				g.drawRect((int)rect.getX(), (int)rect.getY(), 1, 1);
			}
			else {
				gor.draw(g, shape, gridSize, gridSize, style);
				//g.drawRect((int)rect.getX(), (int)rect.getY(), 1, 1);
			}
			
			//g.draw(shape);
		}
		
		if(bse.isLineTransparency()) {
			g.setComposite(oldlineComposite);
		}
		
		
		if (bse.isRenderingComposite() == true) {
			g.setComposite(oldComposite);
		}

	}
	
	public void drawLine(Graphics2D g, Shape shape, Style style) {
		// TODO Auto-generated method stub
		
		//PolygonShape ps = null;
		
		boolean isPoint = false;
		
		if(shape instanceof PolygonShape) {
			isPoint = ((PolygonShape) shape).isPoint();
		}
		else if(shape instanceof GeometryCollectionShape) {
			isPoint = ((GeometryCollectionShape) shape).isPoint();
		}
		
		BasicStyleExtend bse = (BasicStyleExtend) style;
		
		Composite oldComposite = null;
		
		if (bse.isRenderingComposite() == true) {
			oldComposite = g.getComposite();
			g.setComposite(bse.getComposite());
		}
		
		
		Composite oldlineComposite = null; 
		
		if(bse.isLineTransparency()) {
			oldlineComposite = g.getComposite();
			g.setComposite(bse.getLineComposite());
		}
		
		if (bse.isRenderingLine() == true) {
			g.setStroke(bse.getLineStroke());
			g.setColor(bse.getLineColor());
			
			Rectangle2D rect = shape.getBounds2D();
			if(isPoint) {
				
				g.drawRect((int)rect.getX(), (int)rect.getY(), 1, 1);
			}
			else {
				g.draw(shape);
				//gor.draw(g, shape, gridSize, gridSize, style);
				//g.drawRect((int)rect.getX(), (int)rect.getY(), 1, 1);
			}
			
			//g.draw(shape);
		}
		
		if(bse.isLineTransparency()) {
			g.setComposite(oldlineComposite);
		}
		
		
		if (bse.isRenderingComposite() == true) {
			g.setComposite(oldComposite);
		}

	}
	
	public void drawFill(Graphics2D g, Shape shape, Style style) {
		// TODO Auto-generated method stub
		
		//PolygonShape ps = null;
		
		boolean isPoint = false;
		
		if(shape instanceof PolygonShape) {
			isPoint = ((PolygonShape) shape).isPoint();
		}
		else if(shape instanceof GeometryCollectionShape) {
			isPoint = ((GeometryCollectionShape) shape).isPoint();
		}
		
		BasicStyleExtend bse = (BasicStyleExtend) style;
		
		Composite oldComposite = null;
		
		if (bse.isRenderingComposite() == true) {
			oldComposite = g.getComposite();
			g.setComposite(bse.getComposite());
		}
		
		Composite oldfillComposite = null; 
		if(bse.isFillTransparency()) {
			oldfillComposite = g.getComposite();
			g.setComposite(bse.getFillComposite());
		}
		
		if (bse.isRenderingFill() == true) {
			g.setColor(bse.getFillColor());
			g.fill(shape);
		} 
		
		if (bse.isRenderingFillPattern() == true) {
			g.setPaint(bse.getFillPattern());
			g.fill(shape);
		}
		
		if(bse.isFillTransparency()) {
			g.setComposite(oldfillComposite);
		}
		
		
		if (bse.isRenderingComposite() == true) {
			g.setComposite(oldComposite);
		}

	}

	
	public void draw(Graphics2D g, Shape shape, Style style) {
		
		
		boolean isPoint = false;
		
		if(shape instanceof PolygonShape) {
			isPoint = ((PolygonShape) shape).isPoint();
		}
		else if(shape instanceof GeometryCollectionShape) {
			isPoint = ((GeometryCollectionShape) shape).isPoint();
		}
		
		BasicStyleExtend bse = (BasicStyleExtend) style;
		
		
		Composite oldComposite = null;
		
		if (bse.isRenderingComposite() == true) {
			oldComposite = g.getComposite();
			g.setComposite(bse.getComposite());
		}
		
		Composite oldfillComposite = null; 
		if(bse.isFillTransparency()) {
			oldfillComposite = g.getComposite();
			g.setComposite(bse.getFillComposite());
		}
		
		
		if (bse.isRenderingFill() == true) {
			g.setColor(bse.getFillColor());

			Rectangle2D rec = shape.getBounds2D();
			
			Paint oldPaint = g.getPaint();
			
			if(bse.isGradient() && rec.getWidth()/2 > 0) {
				
				Point2D center = new Point2D.Float((float)rec.getCenterX(), (float)rec.getCenterY());
				RadialGradientPaint rgp = new RadialGradientPaint(center, (float)rec.getWidth()/2, 
						bse.getGradientDist(), bse.getGradientColors());			
				g.setPaint(rgp);
			}
			
			g.fill(shape);
			
			if(bse.isGradient() && rec.getWidth()/2 > 0) {
				g.setPaint(oldPaint);
			}
		}

		
		if (bse.isRenderingFillPattern() == true) {
			g.setPaint(bse.getFillPattern());
			g.fill(shape);
		}
		
		if(bse.isFillTransparency()) {
			g.setComposite(oldfillComposite);
		}
		
		
		Composite oldlineComposite = null; 
		
		if(bse.isLineTransparency()) {
			oldlineComposite = g.getComposite();
			g.setComposite(bse.getLineComposite());
		}
		
		
		if (bse.isRenderingLine() == true) {
			g.setStroke(bse.getLineStroke());
			g.setColor(bse.getLineColor());
			//System.out.println(shape.toString());
			Rectangle2D rect = shape.getBounds2D();
			if(isPoint) {
				
				g.drawRect((int)rect.getX(), (int)rect.getY(), 1, 1);
			}
			else {
				g.draw(shape);
			}
		}
		
		if(bse.isLineTransparency()) {
			g.setComposite(oldlineComposite);
		}
		
		
		if (bse.isRenderingComposite() == true) {
			g.setComposite(oldComposite);
		}

	}


}
