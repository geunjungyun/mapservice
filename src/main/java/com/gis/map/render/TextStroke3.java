/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.gis.map.render;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.Vector;

import com.gis.map.MapContext;
import com.gis.map.render.OverlapMng.Overlap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

public class TextStroke3 {

//	private String text;
//	private Font font;
//	private boolean stretchToFit = false;
//	private boolean repeat = false;
//	private AffineTransform t = new AffineTransform();

	private Rectangle viewR = null;
	private static final float FLATNESS = 10;
	// Vector<Rectangle> roadNameOverlaps = new Vector();
	boolean overlapMode = true;

	// int repeatTextInteval = 100;
	// int repeatTextInteval = 100;

	public TextStroke3() {
//		this( text, font, true, false );

	}

	/*
	 * 현재 화면 사이즈
	 */
	public void setViewSize(Rectangle viewR) {
		this.viewR = viewR;
	}

	public void removeAllOverlapData() {
		// this.roadNameOverlaps.removeAllElements();
	}

	public boolean chechRoadNameOverlap(Rectangle srcRT, Vector<Rectangle> overRects) {

		for (Rectangle dstRT : overRects) {
			if (dstRT.intersects(srcRT)) {
				return true;
			}
		}

		return false;
	}

//	FontRenderContext frc = new FontRenderContext(null, true, true);

	public Shape createStrokedShape(Shape shape, String text, Font font, Graphics2D g, Overlap overlay,
			int extensionArea, float tracking, float lineExtensionWidthRatio, float lineIntervalPixel) {

		AffineTransform at1 = new AffineTransform();
		//AffineTransform at2 = new AffineTransform();

		float extensionLineRatio = 0.0f;
		float repeatTextInteval = 300;

		float shapeLenght = this.measurePathLength(shape);

		// System.out.println("createStrokedShape = " + text + ", len=" + shapeLenght);

//		if ((text.indexOf("←23분 1.3km 16분→") > -1)) {
//			// g.setColor(Color.blue);
//			// g.draw(shape);
//			return null;
//		} else if ((text.indexOf("←28분 1.6km 20분→") > -1)) {
//
//			// g.setColor(Color.orange);
//			// g.draw(shape);
//			return null;
//		} else {
//			// g.setColor(Color.gray);
//			// g.draw(shape);
//			// return null;
//		}
		
		FontRenderContext frc = g.getFontRenderContext();

		GlyphVector glyphVector = font.createGlyphVector(frc, text);

		GlyChar[] gcs = new GlyChar[text.length()];

		for (int i = 0; i < text.length(); i++) {
			gcs[i] = new GlyChar(glyphVector, i);
		}

		GeneralPath result = new GeneralPath();
		PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), 10);

		// shape.getPathIterator(t);
		// PathIterator it =shape.getPathIterator( t );
		float points[] = new float[6];
		float moveX = 0, moveY = 0;
		float lastX = 0, lastY = 0;
		float thisX = 0, thisY = 0;
		int type = 0;
		boolean first = false;
		float next = 0;
		int currentChar = 0;
		// 글자 개수
		int length = glyphVector.getNumGlyphs();

		if (length == 0)
			return result;

		// float factor = stretchToFit ? measurePathLength( shape
		// )/(float)glyphVector.getLogicalBounds().getWidth() : 1.0f;
		// 자간 벡터
		float factor = 1.0f + tracking;
		float nextAdvance = 0;

		int resultCnt = 0;

		int textWidth = (int) glyphVector.getLogicalBounds().getWidth();
		float addTextWidth = 0;
		if (lineExtensionWidthRatio > 0) {
			addTextWidth = textWidth * lineExtensionWidthRatio;
			textWidth += addTextWidth;
		}
		if (lineIntervalPixel > 0) {
			repeatTextInteval = (lineIntervalPixel + addTextWidth);
		}

		if (shapeLenght < textWidth) {
			return null;
		}

		if ((repeatTextInteval * 3 + textWidth * 2) > shapeLenght) {

			next += (shapeLenght / 2.0 - textWidth / 2.0f);

		} else {

			float x = 1;
			float y = 0;
			while (true) {
				// 텍스트 라인 거리 * 반복 회수 + 간격거리 (반복횟수 + 1) + 2 양끝 간격 = 라인 전체 거리
				y = (shapeLenght - (textWidth) * x - repeatTextInteval * (x + 1)) / 2.0f;

				if ((textWidth + repeatTextInteval) / 2.0 > y) {
					break;
				}

				x++;
			}
			float distance = (textWidth) * x + repeatTextInteval * (x + 1) + y * 2;

			// System.out.println("distance = " + distance + ", shape len = " +
			// shapeLenght);

			// float startInteval = (shapeLenght - textWidth * x - repeatTextInteval * (x +
			// 1)) / 2.0f;
			next = y + repeatTextInteval / 2;

		}

		int intervalCnt = 0;
		// boolean upsideDown = false;

		Vector<Shape> chars = new Vector();

		Vector<LineChar> lineChars = new Vector();

		int cnt = 0;

		boolean overlap = false;

		//글자가 화면 뷰 영역에 교차하는지 체크
		//boolean viewIntersect = true;

		// while ( currentChar < length && !it.isDone() ) {
		while (!it.isDone()) {
			type = it.currentSegment(points);
//        	if(currentChar == length-1){
//        		currentChar = 0;
//				moveX = lastX = points[0];
//				moveY = lastY = points[1];
//				result.moveTo( moveX, moveY );
//				first = true;
//                nextAdvance = glyphVector.getGlyphMetrics( currentChar ).getAdvance() * 0.5f;
//                next = nextAdvance = 0;
//                continue;
//        	}

			switch (type) {
			case PathIterator.SEG_MOVETO:
				// System.out.println("SEG_MOVETO");
				moveX = lastX = points[0];
				moveY = lastY = points[1];
				result.moveTo(moveX, moveY);
				first = true;
				// nextAdvance = glyphVector.getGlyphMetrics(currentChar).getAdvance() * 0.5f;
				nextAdvance = gcs[currentChar].advance * 0.5f;
				// next = nextAdvance;
				break;
			case PathIterator.SEG_CLOSE:
				// System.out.println("SEG_CLOSE");
				points[0] = moveX;
				points[1] = moveY;
				// Fall into....
			case PathIterator.SEG_LINETO:
				// System.out.println("SEG_LINETO");
				thisX = points[0];
				thisY = points[1];
				// g.drawString(cnt+"", thisX, thisY);
				float dx = thisX - lastX;
				float dy = thisY - lastY;

				// 이전 포인트와 현재 포인트 거리
				float distance = (float) Math.sqrt(dx * dx + dy * dy);
				// x축을 기준으로한 각도
				float angle = (float) Math.atan2(dy, dx);

				if (distance >= next) {

					float r = 1.0f / distance;
					while (currentChar < length && distance >= next) {

						LineChar lc = new LineChar();

						Shape glyph = null;
						Point2D p = null;

						// glyph = glyphVector.getGlyphOutline(currentChar);
						// p = glyphVector.getGlyphPosition(currentChar);
						glyph = gcs[currentChar].glyph;
						p = gcs[currentChar].p;

						float px = (float) p.getX();
						float py = (float) p.getY();

						// 라인에서 글자가 그려지는 위치
						float x = lastX + next * dx * r;
						float y = lastY + next * dy * r;

						float advance = nextAdvance;

						nextAdvance = currentChar < length - 1 ? gcs[currentChar + 1].advance * 0.5f : 0;

						at1.setToTranslation(x, y);

						lc.x = x;
						lc.y = y;

						at1.rotate(angle);

						lc.angle = angle;

						// -5 값은 폰트 높이의 절반 값
						double charHeight = glyph.getBounds().getHeight() / 2;

						at1.translate(-px - advance, -(py - charHeight));

						lc.transX = -px - advance;
						lc.transY = -(py - charHeight);

						

						Rectangle2D charMbrTemp = gcs[currentChar].charRect;

						Rectangle charMbr = new Rectangle((int) (x - charMbrTemp.getWidth() / 2) -2,
								(int) (y - charMbrTemp.getHeight() / 2)-2, (int) charMbrTemp.getWidth() + 4,
								(int) charMbrTemp.getHeight()+4);

						if (!this.viewR.intersects(charMbr)) {
							lc.viewIntersect = false;
						}
						
						lineChars.add(lc);

						next += (advance + nextAdvance) * factor;

						currentChar++;

						if (currentChar == length) {

							//boolean reverse = false;

							//시작점과 종료점의 방향이 왼쪽 방향의 경우 
							if (lineChars.get(0).x > lineChars.get(lineChars.size() - 1).x) {
								//reverse = true;
								lineChars = this.getReverse(lineChars, gcs, factor);
								//reverse = false;
							}

							currentChar = 0;
							next += repeatTextInteval;
							
							nextAdvance = gcs[currentChar].advance * 0.5f;
							
							boolean viewIntersect = false;
							
							for(int k=0; k<lineChars.size(); k++) {
								LineChar lineChar = lineChars.get(k);
								if(lineChar.viewIntersect) {
									viewIntersect = true;
									break;
								}
							}

							if (viewIntersect) {
								
								Vector<Shape> addResult = new Vector();

								int nextCharLen = 0;
								int idx = 0;
								for (int i = 0; i < lineChars.size(); i++) {

									// int idx = i;
									int lcIdx = i;

//									if (reverse) {
//										lcIdx = lineChars.size() - 1 - i;
//									}

									LineChar lineChar = lineChars.get(lcIdx);

									// Point2D pt = glyphVector.getGlyphPosition(idx);
									Point2D pt = gcs[idx].p;
									nextCharLen = (int) pt.getX();

									// Shape glp = glyphVector.getGlyphOutline(idx, -nextCharLen, 0);
									Shape glp = gcs[idx].originGlyph;

									Rectangle rect = glp.getBounds();

									AffineTransform at2 = new AffineTransform();

									at2.translate(lineChar.x - rect.getWidth() / 2, lineChar.y + rect.getHeight() / 2);
									
//									tt.rotate(reverse == false ? lineChar.angle : lineChar.angle + Math.toRadians(180),
//											rect.getWidth() / 2, -rect.getHeight() / 2);
									at2.rotate(lineChar.angle , rect.getWidth() / 2, -rect.getHeight() / 2);

									// Point2D pt = glyphVector.getGlyphPosition(idx);
									// nextCharLen = (int) pt.getX();

									Shape addShape = at2.createTransformedShape(glp);

									Rectangle2D charRect = addShape.getBounds2D();

									if (overlay != null && overlay.chechOverlap(charRect, extensionArea)) {
										overlap = true;
										
										if (MapContext.debugOverlap) {
											addResult.add(addShape);
										}
										else {
											addResult.clear();
											break;
										}
										/*
										if (MapContext.debugOverlap) {
											
											Color old = g.getColor();
											
											if(overlay.id.equals("main")) {
												g.setColor(Color.RED);
											}
											else {
												g.setColor(Color.BLUE);
											}
											g.setStroke(new BasicStroke(1f));
											
											g.drawRect((int)charRect.getX() - extensionArea, (int)charRect.getY() - extensionArea, 
													(int)charRect.getWidth() + extensionArea*2, (int)charRect.getHeight() + extensionArea*2);
											g.fill(addShape);
											g.setColor(old);
										}
										else {
											break;
										}
										*/
										
									} else {
										addResult.add(addShape);
									}
									idx++;
								}
								
								if (MapContext.debugOverlap && overlap) {
									//addResult.add(addShape);
									
									for (Shape shp : addResult) {

										Rectangle2D rec = shp.getBounds();
										if (this.viewR.intersects(rec)) {
											Color old = g.getColor();
											
											if(overlay.id.equals("main")) {
												g.setColor(Color.RED);
											}
											else {
												g.setColor(Color.BLUE);
											}
											g.setStroke(new BasicStroke(1f));
											Rectangle2D charRect = shp.getBounds2D();
											
											g.drawRect((int)charRect.getX() - extensionArea, (int)charRect.getY() - extensionArea, 
													(int)charRect.getWidth() + extensionArea*2, (int)charRect.getHeight() + extensionArea*2);
											g.fill(shp);
											g.setColor(old);
											
											//result.append(shp, false);
										}
									}
									
								}
								
								for (Shape shp : addResult) {

									if (overlay != null) {
										overlay.add(shp.getBounds(), extensionArea);
									}

									Rectangle2D rec = shp.getBounds();
									if (this.viewR.intersects(rec)) {
										result.append(shp, false);
									}
								}


							}
							overlap = false;
							chars.removeAllElements();
							lineChars.clear();
							cnt++;
							//viewIntersect = true;
						}
						//viewIntersect = true;
					}
				}
				next -= distance;
				first = false;
				lastX = thisX;
				lastY = thisY;
				break;
			}
			it.next();
		}

		// 오버랩 확인 로직
//		g.setColor(Color.blue);
//		for(Rectangle rec : this.roadNameOverlaps){
//			g.drawRect(rec.x, rec.y, rec.width, rec.height);
//		}

//        if(resultCnt > 0){
//        	return result;
//        }
//        else{
//        	return null;
//        }
		// System.out.println("result cnt="+resultCnt);
		shape = null;
		return result;
	}
	
	
	/**
	 * 주기를 표현하는 좌표가 왼쪽 방향의 경우 오른쪽으로 재 정렬하고 글자의 좌표를 다시 산정함.
	 * @param _lcs
	 * @param _gcs
	 * @param factor
	 * @return
	 */
	public Vector<LineChar> getReverse(Vector<LineChar> _lcs, GlyChar[] _gcs, float factor) {
		
		AffineTransform t = new AffineTransform();
		
		Vector<LineChar> lineChars = new Vector();
		
		LineChar[] lcs = new LineChar[_lcs.size()];
		for(int i=0; i<_lcs.size(); i++) {
			lcs[i] = _lcs.get(_lcs.size() - 1 - i);
		}
		
		
		//라인의 끝에 한글자의 폭으로 연장함.
		LineSegment sls = new LineSegment(lcs[_lcs.size()-2].x, lcs[_lcs.size()-2].y, lcs[_lcs.size()-1].x, lcs[_lcs.size()-1].y);
		double slsLen = sls.getLength();
		double wd = _gcs[0].charRect.getWidth();
		double fac = slsLen/wd;
		Coordinate s = sls.pointAlong(1.0+fac);
		lcs[_lcs.size()-1].x = (float) s.getX();
		lcs[_lcs.size()-1].y = (float) s.getY();
		
		
		int length = _lcs.size();
		
		GlyChar[] gcs = _gcs;
		
		int currentChar = 0;
		float moveX = 0, moveY = 0;
		float lastX = 0, lastY = 0;
		float thisX = 0, thisY = 0;
		float next = 0;
		float nextAdvance = 0;
		
		for(int i=0; i<lcs.length; i++) {
			
			if(i == 0) {
				moveX = lastX = lcs[i].x;
				moveY = lastY = lcs[i].y;
				nextAdvance = gcs[currentChar].advance * 0.5f;
				continue;
			}
			
			thisX = lcs[i].x;
			thisY = lcs[i].y;
			
			// g.drawString(cnt+"", thisX, thisY);
			float dx = thisX - lastX;
			float dy = thisY - lastY;

			// 이전 포인트와 현재 포인트 거리
			float distance = (float) Math.sqrt(dx * dx + dy * dy);
			// x축을 기준으로한 각도
			
			if (distance >= next) {

				float r = 1.0f / distance;
				float angle = (float) Math.atan2(dy, dx);
				while (currentChar < length && distance >= next) {

					LineChar lc = new LineChar();

					Shape glyph = null;
					Point2D p = null;

					glyph = gcs[currentChar].glyph;
					p = gcs[currentChar].p;

					float px = (float) p.getX();
					float py = (float) p.getY();

					// 라인에서 글자가 그려지는 위치
					float x = lastX + next * dx * r;
					float y = lastY + next * dy * r;

					float advance = nextAdvance;

					nextAdvance = currentChar < length - 1 ? gcs[currentChar + 1].advance * 0.5f : 0;

					t.setToTranslation(x, y);

					lc.x = x;
					lc.y = y;

					t.rotate(angle);

					lc.angle = angle;

					// -5 값은 폰트 높이의 절반 값
					double charHeight = glyph.getBounds().getHeight() / 2;

					t.translate(-px - advance, -(py - charHeight));

					lc.transX = -px - advance;
					lc.transY = -(py - charHeight);

					Rectangle2D charMbrTemp = gcs[currentChar].charRect;

					Rectangle charMbr = new Rectangle((int) (x - charMbrTemp.getWidth() / 2) -2,
							(int) (y - charMbrTemp.getHeight() / 2)-2, (int) charMbrTemp.getWidth() + 4,
							(int) charMbrTemp.getHeight()+4);

					if (!this.viewR.intersects(charMbr)) {
						lc.viewIntersect = false;
					}
					
					lineChars.add(lc);

					next += (advance + nextAdvance) * factor;
					
					currentChar++;
				}
				
			}
			
			next -= distance;

			lastX = thisX;
			lastY = thisY;
			//break;
			
		}
		
		
		
		return lineChars;
	}
	

	class LineChar {

		int idx = 0;

		float x = 0f;
		float y = 0f;

		float stransX = 0f;
		float stransY = 0f;

		float angle = 0f;

		double transX = 0.0;
		double transY = 0.0;
		
		boolean viewIntersect = true;
	}

	public float measurePathLength(Shape shape) {
		PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), FLATNESS);
		float points[] = new float[6];
		float moveX = 0, moveY = 0;
		float lastX = 0, lastY = 0;
		float thisX = 0, thisY = 0;
		int type = 0;
		float total = 0;

		while (!it.isDone()) {
			type = it.currentSegment(points);
			switch (type) {
			case PathIterator.SEG_MOVETO:
				moveX = lastX = points[0];
				moveY = lastY = points[1];
				break;

			case PathIterator.SEG_CLOSE:
				points[0] = moveX;
				points[1] = moveY;
				// Fall into....

			case PathIterator.SEG_LINETO:
				thisX = points[0];
				thisY = points[1];
				float dx = thisX - lastX;
				float dy = thisY - lastY;
				total += (float) Math.sqrt(dx * dx + dy * dy);
				lastX = thisX;
				lastY = thisY;
				break;
			}
			it.next();
		}

		return total;
	}

}
