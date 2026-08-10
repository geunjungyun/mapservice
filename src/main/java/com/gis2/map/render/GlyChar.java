package com.gis2.map.render;

import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class GlyChar {
	
	/**
	 * 전체 글자가 표현되는 상태에서 형상 좌표
	 */
	Shape glyph = null;
	
	/**
	 * 글자 하나가 표현되는 상태에서 형상 좌표
	 */
	Shape originGlyph = null;
	
	/**
	 * 전체 글자에서 현재 글자가 시작되는 좌표 
	 */
	Point2D p = null;
	
	
	float advance = 0.0f;
	
	Rectangle2D charRect = null;
	
	public GlyChar(GlyphVector gv, int idx) {
		glyph = gv.getGlyphOutline(idx);
		charRect = glyph.getBounds2D();
		p = gv.getGlyphPosition(idx);
		originGlyph = gv.getGlyphOutline(idx, (int)-p.getX(), 0);
		advance = gv.getGlyphMetrics(idx).getAdvance();
		
	}
}
