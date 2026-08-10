/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */
package com.vividsolutions.jump.workbench.ui.renderer.style2;

//import com.vividsolutions.jts.util.Assert;

//import com.vividsolutions.jump.feature.Feature;
//import com.vividsolutions.jump.util.StringUtil;
//import com.vividsolutions.jump.workbench.model.Layer;
//import com.vividsolutions.jump.workbench.ui.GUIUtil;
//import com.vividsolutions.jump.workbench.ui.Viewport;

import java.awt.*;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;

import com.gis2.map.render.ArrowStroke;
import com.gis2.map.render.ShapeStroke;
import com.vividsolutions.jump.workbench.ui.renderer.style.Assert;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class BasicStyle implements Style {

	private boolean renderingFill = true;
	private boolean renderingLine = true;
	
	private boolean renderingLinePattern = false;
	private boolean renderingFillPattern = false;

	// The important thing here is the initial alpha. [Jon Aquino]
	private Color fillColor = new Color(0, 0, 0, 255);
	private Color lineColor = new Color(0, 0, 0, 255);
	
	
	private BasicStroke lineStroke;
	
	/*
	 * Polyline에서 내부 및 외곽을 처리한 위한
	 */
	private BasicStroke lineFillStroke;
	
	private Stroke shapeStroke;
	
	public Stroke arrowStroke;
	
	//private Stroke fillStroke = new BasicStroke(1);
	private boolean enabled = true;
	private String linePattern = "3";

	// Set fill pattern to something, so that the BasicStylePanel combobox won't
	// start empty. [Jon Aquino]
	//private Paint fillPattern = WKTFillPattern.createDiagonalStripePattern(4, 2, false, true);
	private Paint fillPattern = null;
	
	public float lineWidth = -1;
	
	public float lineFillWidth = -1;
	
	public int cap = BasicStroke.CAP_BUTT;
	public int join = BasicStroke.JOIN_ROUND;
	
	
	public Filter filter =  null;
	
	public String cql = null;
	
	public boolean filterValidate = false;
	
	
	public void setCql(String _cql) {
		this.cql = _cql;
	}
	
	public String getCql() {
		return this.cql;
	}
	
	public Filter getFilter() {
		return this.filter;
	}
	
	public synchronized boolean filterValidate(SimpleFeatureType sft) {
		if(this.filterValidate == false) {
			boolean conOk = false;			
			for(int i=0; i<sft.getAttributeCount(); i++){
				AttributeDescriptor ad = sft.getDescriptor(i);
				String name = ad.getName().toString();
				
				if(cql.indexOf(name.toUpperCase()) > -1) {
					cql = cql.replaceAll(name.toUpperCase(), name);
					conOk = true;
				}
				if(cql.indexOf(name.toLowerCase()) > -1) {
					cql = cql.replaceAll(name.toLowerCase(), name);
					conOk = true;
				}
			}
			
			if(conOk) {
				try {
					this.filter = ECQL.toFilter(this.cql);
				} catch (CQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			this.filterValidate = true;
		}
		return this.filterValidate;
	}

	
	public BasicStyle(Color fillColor) {
		setFillColor(fillColor);
		setLineColor(fillColor.darker());
		setLineWidth(1);
	}

	public BasicStyle() {
		this(Color.black);
	}
	
	public void setShapeStroke(int type, int width, int height, int repeat) {
		
		if(type != 3) {
			Polygon pg = new Polygon();
			pg.addPoint(0, height/2);
			pg.addPoint(width, 0);
			pg.addPoint(width, height);
			pg.addPoint(0, height/2);
			
			Rectangle2D.Float rec = new Rectangle2D.Float(0, 0, width, height);
					
			shapeStroke = new ShapeStroke(new Shape[] { type == 1 ? rec :  pg},repeat);
		}
		else {
			this.arrowStroke = new ArrowStroke(this.lineFillWidth, repeat);
		}
		
	}
	
	public Stroke getShapeStroke() {
		return this.shapeStroke;
	}
	
	public boolean isRenderingFillPattern() {
		return renderingFillPattern;
	}

	public BasicStyle setRenderingFillPattern(boolean renderingFillPattern) {
		this.renderingFillPattern = renderingFillPattern;
		return this;
	}

	public Paint getFillPattern() {
		return fillPattern;
	}

	/**
	 * Remember to call #setRenderingFillPattern(true).
	 */
	public BasicStyle setFillPattern(Paint fillPattern) {
		this.fillPattern = fillPattern;
		if (fillPattern instanceof BasicFillPattern) {
			((BasicFillPattern) fillPattern).setColor(this.fillColor);
		}
		return this;
	}

	public String getLinePattern() {
		return linePattern;
	}

	/**
	 * The actual dash pattern used internally will be the given dash pattern
	 * multiplied by the line length. Remember to call
	 * #setRenderingLinePattern(true).
	 * 
	 * @param linePattern
	 *            e.g. "5,2,3,2"
	 */
	public BasicStyle setLinePattern(String linePattern) {
		this.linePattern = linePattern;
		lineStroke = createLineStroke(lineStroke.getLineWidth());
		return this;
	}
	
	public void setLineFillStroke(float lineWidth) {
		//lineFillStroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND); 
		lineFillStroke = new BasicStroke(lineWidth, this.cap, this.join);
	}


	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isRenderingFill() {
		return renderingFill;
	}

	public boolean isRenderingLine() {
		return renderingLine;
	}

	public boolean isRenderingLinePattern() {
		return renderingLinePattern;
	}

	public void setRenderingFill(boolean renderingFill) {
		this.renderingFill = renderingFill;
	}

	public void setRenderingLine(boolean renderingLine) {
		this.renderingLine = renderingLine;
	}
	
	
	public void setRenderingLinePattern(boolean renderingLinePattern) {
		this.renderingLinePattern = renderingLinePattern;
//		lineStroke = createLineStroke(lineStroke.getLineWidth());
//		return this;
	}
	
	

	public void setFillColor(Color fillColor) {
		setFillColor(fillColor, getAlpha());
	}

	private BasicStyle setFillColor(Color fillColor, int alpha) {
		this.fillColor = alphaColor(fillColor, alpha);
		if (fillPattern instanceof BasicFillPattern) {
			((BasicFillPattern) fillPattern).setColor(this.fillColor);
		}
		return this;
	}

	public void setLineColor(Color lineColor) {
		this.lineColor = alphaColor(lineColor, getAlpha());
	}

	public void setLineWidth(float lineWidth) {
		this.lineWidth = lineWidth;
		lineStroke = createLineStroke(lineWidth);
	}
//	
//	public void setLineFillWidth(float lineWidth) {
//		this.lineFillWidth = lineWidth;
//		lineFillStroke = createLineStroke(lineWidth);
//	}
	

	private BasicStroke createLineStroke(float lineWidth) {
		//renderingLinePattern = true;
		//System.out.println(renderingLinePattern + ", " + linePattern.trim().length());
		//return (renderingLinePattern && (linePattern.trim().length() != 0) && (lineWidth > 0)) ? new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, dashingPattern1, 2.0f)
		//		: new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
		
		return (renderingLinePattern && (linePattern.trim().length() != 0) && (lineWidth > 0)) ? new BasicStroke(lineWidth, this.cap,
				this.join, 1.0f, toArray(linePattern, lineWidth), 0) : new BasicStroke(lineWidth, this.cap, this.join);
		
//		return (renderingLinePattern && (linePattern.trim().length() != 0) && (lineWidth > 0)) ? new BasicStroke(lineWidth, BasicStroke.CAP_BUTT,
//				BasicStroke.JOIN_ROUND, 1.0f, toArray(linePattern, lineWidth), 0) : new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
		
	}

	public static float[] toArray(String linePattern, float lineWidth) {
		List strings = fromCommaDelimitedString(linePattern);
		float[] array = new float[strings.size()];
		
		for (int i = 0; i < strings.size(); i++) {
			String string = (String) strings.get(i);
			array[i] = Float.parseFloat(string) * lineWidth;

			if (array[i] < 0) {
				throw new IllegalArgumentException("Negative dash length =" + linePattern);
			}
		}

		return array;
	}

	/**
	 * @return 0-255 (255 is opaque)
	 */
	public int getAlpha() {
		return fillColor.getAlpha();
	}

	public Color getFillColor() {
		return alphaColor(fillColor, 255);
	}

	public Color getLineColor() {
		return alphaColor(lineColor, 255);
	}

	public float getLineWidth() {
		return lineStroke.getLineWidth();
	}

	/**
	 * @param alpha
	 *            0-255 (255 is opaque)
	 */
	public void setAlpha(int alpha) {
		setFillColor(fillColor, alpha);
		lineColor = alphaColor(lineColor, alpha);
	}

	public BasicStroke getLineStroke() {
		return lineStroke;
	}
	
	public BasicStroke getLineFillStroke() {
		return lineFillStroke;
	}
	
	
    public Color alphaColor(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(),
                alpha);
    }
    
    public static List fromCommaDelimitedString(String s) {        
        if (s.trim().length() == 0) { return new ArrayList(); }
        ArrayList result = new ArrayList();        
        StringTokenizer tokenizer = new StringTokenizer(s, ",");

        while (tokenizer.hasMoreTokens()) {
            result.add(tokenizer.nextToken().toString().trim());
        }

        return result;
    }
    
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			Assert.shouldNeverReachHere();
			return null;
		}
	}
}
