package com.gis.map.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.IllegalPathStateException;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;

//import com.gis.map.style.BasicStyleExtend;
import org.locationtech.jts.geom.Geometry;

import com.gis.map.style.BasicStyleExtend;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class GridObjectRender implements IRender {

	public void draw(Graphics g, Geometry geo, Style style) {

	}

	public void draw(Graphics2D g2, Shape shape, int width, int height,	Style style) {
		
		double oriX = 254440.0;
		double oriY = 1232737.0;
		
		BasicStyleExtend bse = (BasicStyleExtend) style;
		// BasicStyleExtend bse = (BasicStyleExtend)style;
//		g2.setColor(bse.getFillColor());
//		if (bse.isRenderingFillPattern() == true) {
//			g2.setPaint(bse.getFillPattern());
//		}
//		g2.fill(shape);

		if (bse.isRenderingLine() == false) {
			return;
		}
		g2.setStroke(bse.getLineStroke());
		//g2.setStroke(new BasicStroke(2));
		g2.setColor(bse.getLineColor());

		int count = 0;
		double length = 0, lastX = Double.MAX_VALUE, lastY = Double.MAX_VALUE;
		//final double flatness = 0.1;
		//PathIterator iterator = shape.getPathIterator(null, flatness);
		PathIterator iterator = shape.getPathIterator(null);
		final double[] buffer = new double[6];

		while (!iterator.isDone()) {
			count++;
			switch (iterator.currentSegment(buffer)) {
			case PathIterator.SEG_LINETO: {
				count++;
				// length += ellipsoid.orthodromicDistance(lastX, lastY,
				// buffer[0], buffer[1]);

				double nowX = buffer[0];
				double nowY = buffer[1];

				double preX = lastX;
				double preY = lastY;

				double xtemp = lastX % width;
				double ytemp = lastY % height;
				
				int interval = 0;
				
				
				if (nowX == preX && (xtemp <= interval && xtemp >= -interval)) {
//					Color oldColor = g2.getColor();
//					System.out.println("x line nowX = " + nowX +", preX = " + preX +  "y line nowY = " + nowY +", preY = " + preY );
//					g2.setColor(Color.red);
//					g2.drawLine((int) lastX, (int) lastY, (int) buffer[0], (int) buffer[1]);
//					g2.setColor(oldColor);
				} else if (nowY == preY && (ytemp <= interval && ytemp >= -interval)) {
//					Color oldColor = g2.getColor();
//					g2.setColor(Color.red);
//					System.out.println("x line nowX = " + nowX +", preX = " + preX +  "y line nowY = " + nowY +", preY = " + preY );
//					g2.drawLine((int) lastX, (int) lastY, (int) buffer[0], (int) buffer[1]);
//					g2.setColor(oldColor);
				} else {

					g2.draw(new Line2D.Double(lastX,lastY,buffer[0],buffer[1]));
				}
				//g2.draw(new Line2D.Double(lastX,lastY,buffer[0],buffer[1]));
				
				// System.out.println("SEG_LINETO, x="+buffer[0]+", y="+buffer[1]);
				//g2.draw(new Line2D.Double(lastX,lastY,buffer[0],buffer[1]));
				lastX = buffer[0];
				lastY = buffer[1];
				break;
			}
			case PathIterator.SEG_MOVETO: {
				lastX = buffer[0];
				lastY = buffer[1];
				break;
			}
			default: {
			}
			}
			iterator.next();
		}
	}

	public void draw(Graphics2D g, Shape geo, Style style) {
		// TODO Auto-generated method stub

	}
}

// for(int i=0; i < polygon->PointCount;i++){
// if(i > 0 && this->polygonBrushStyle != 3 ){
// int nowX = polygon->Points[i].x;
// int nowY = polygon->Points[i].y;
//
// int preX = polygon->Points[i-1].x;
// int preY = polygon->Points[i-1].y;
//
// int xtemp = polygon->Points[i-1].x%800;
// int ytemp = polygon->Points[i-1].y%800;
//
// if (nowX == preX && (xtemp <= 1 && xtemp >= -1))
// {
// // grids.push_back(i-1);
// }
// else if (nowY == preY && (ytemp <= 1 && ytemp >= -1))
// {
// // grids.push_back(i-1);
// }
// else{
// MoveToEx(hdc, this->tempPoint[i-1].x, this->tempPoint[i-1].y ,NULL);
// LineTo(hdc, this->tempPoint[i].x, this->tempPoint[i].y);
// }
// }
// }
