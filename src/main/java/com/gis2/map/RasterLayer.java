package com.gis2.map;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.CoverageProcessor;
import org.geotools.coverage.processing.Operations;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.GridCoverageLayer;
import org.geotools.referencing.CRS;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.locationtech.jts.geom.Envelope;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.Multiply;
import org.opengis.filter.expression.NilExpression;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.expression.Subtract;
import org.opengis.geometry.DirectPosition;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.gis.map.Layer;
import com.gis2.map.style.BasicStyleExtend;

public class RasterLayer extends Layer{
	
	private GridCoverage2D gc;
	
	private Envelope mbr;
	
	/*
	public void draw(Envelope env, Graphics2D img, int imgWidth, int imgHeight) {
		synchronized (this.lock) {
			if(mng  == null) {
				mng = TileServiceMng.emaps.get(this.getName());
			}
		}
		if(mng != null) {
			mng.draw(env, img, imgWidth, imgHeight);
		}
	}
	*/
	
	public void setGridCoverage2D(GridCoverage2D gc_) {
		this.gc = gc_;
		
		DirectPosition dp1 = this.gc.getEnvelope().getLowerCorner();
		double[] low = dp1.getCoordinate();
		
		DirectPosition dp2 = this.gc.getEnvelope().getUpperCorner();
		double[] upper = dp2.getCoordinate();
		
		this.mbr = new Envelope(low[0], upper[0], low[1], upper[1]);
		
	}
	
	
	public GridCoverage2D getGridCoverage2D() {
		return this.gc;
	}
	
	public boolean isIntersect(Envelope env) {
		
		return this.mbr.intersects(env);
		
	}
	
}