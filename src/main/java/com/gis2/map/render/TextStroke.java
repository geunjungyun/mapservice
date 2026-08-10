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

package com.gis2.map.render;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.Vector;

import com.gis2.map.render.OverlapMng.Overlap;
import org.locationtech.jts.geom.Coordinate;

public class TextStroke {
	
//	private String text;
//	private Font font;
//	private boolean stretchToFit = false;
//	private boolean repeat = false;
//	private AffineTransform t = new AffineTransform();
	
	private Rectangle viewR = null;

	private static final float FLATNESS = 10;
	//Vector<Rectangle> roadNameOverlaps = new Vector();
	boolean overlapMode = true;
	
	int repeatTextInteval = 700;
	//int repeatTextInteval = 100;
	
	public TextStroke( ) {
//		this( text, font, true, false );
		
	}
	
	/*
	 * 현재 화면 사이즈
	 */
	public void setViewSize(Rectangle viewR){
		this.viewR = viewR;
	}
	
	public void removeAllOverlapData(){
		//this.roadNameOverlaps.removeAllElements();
	}

//	public TextStroke( String text, Font font, boolean stretchToFit, boolean repeat ) {
//		this.text = text;
//		this.font = font;
////		this.stretchToFit = stretchToFit;
////		this.repeat = repeat;
//	}
	
//	public Shape createStrokedShape(Shape shape, String txt, Font font){
//		return this.createStrokedShape(shape);
//	}
	
	public boolean chechRoadNameOverlap(Rectangle srcRT, Vector<Rectangle> overRects ) {

		for (Rectangle dstRT : overRects) {
			if (dstRT.intersects(srcRT)) {
				return true;
			}
		}

		return false;
	}

	AffineTransform t = new AffineTransform();
//	FontRenderContext frc = new FontRenderContext(null, true, true);

	public Shape createStrokedShape( Shape shape, String text, Font font, Graphics2D g, Overlap overlay, int extensionArea, float tracking) {
		

		FontRenderContext frc = g.getFontRenderContext();
		
		GlyphVector glyphVector = font.createGlyphVector(frc, text);
		
		char[]  cas = new char[text.length()];
		
		
		for(int i=0; i<text.length(); i++){
			cas[text.length()-1 -i] = text.charAt(i); 
		}
		
		String temp = new String(cas);
		
		GlyphVector glyphVector1 = font.createGlyphVector(frc, temp);
		
		//GlyphVector glyphVector_ = font.createGlyphVector(frc, text);
		
		GeneralPath result = new GeneralPath();
		//PathIterator it = new FlatteningPathIterator( shape.getPathIterator( t ), FLATNESS );
		
		PathIterator it = new FlatteningPathIterator( shape.getPathIterator( null ), FLATNESS);
		
		//shape.getPathIterator(t);
		//PathIterator it =shape.getPathIterator( t );
		float points[] = new float[6];
		float moveX = 0, moveY = 0;
		float lastX = 0, lastY = 0;
		float thisX = 0, thisY = 0;
		int type = 0;
		boolean first = false;
		float next = 0;
		int currentChar = 0;
		//글자 개수
		int length = glyphVector.getNumGlyphs();

		if ( length == 0 )
            return result;

        //float factor = stretchToFit ? measurePathLength( shape )/(float)glyphVector.getLogicalBounds().getWidth() : 1.0f;
		//자간 벡터
		float factor = 1.0f+tracking;
        float nextAdvance = 0;
        
        int resultCnt = 0;
                
        int shapeLenght = (int) this.measurePathLength(shape);
        
        if(this.repeatTextInteval > shapeLenght){
        	next += (shapeLenght/2 - glyphVector.getLogicalBounds().getWidth()/2);
        }
        else{
        	next += glyphVector.getLogicalBounds().getWidth()/2+shapeLenght/4;
        }

        //double fontHeight = glyphVector.getLogicalBounds().getHeight()/2;
        
//        for(int i=0; i<length ; i++){
//        	Shape glyph = glyphVector.getGlyphOutline( currentChar );
//        	t.setToTranslation( i*10, i*10 );
//        	result.append( t.createTransformedShape( glyph ), false );
//        }
//        
//        currentChar = length;
        
//      if(resultCnt == 0){
//    	return null;
//      }
        
        
        
//		if(angle > 90 || angle < -90){
//			Coordinate[] temps = new Coordinate[coords.length];
//			for(int m=0; m<temps.length; m++){
//				temps[m] = new Coordinate();
//				temps[m].x = coords[coords.length-1 -m].x;
//				temps[m].y = coords[coords.length-1 -m].y;
//			}
//			
//			
//			for(int i=0; i<coords.length; i++){
//				coords[i].x = temps[i].x;
//				coords[i].y = temps[i].y;
//			}
//			subGeo.geometryChanged();
//		}
        
        int intervalCnt = 0;
        boolean upsideDown = false;
        Vector<Shape> chars = new Vector();

        boolean overlap = false;
        
        
		//while ( currentChar < length && !it.isDone() ) {
        while (!it.isDone() ) {
			type = it.currentSegment( points );
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

			switch( type ){
			case PathIterator.SEG_MOVETO:
				moveX = lastX = points[0];
				moveY = lastY = points[1];
				result.moveTo( moveX, moveY );
				first = true;
                nextAdvance = glyphVector.getGlyphMetrics( currentChar ).getAdvance() * 0.5f;
                //next = nextAdvance;
                break;
			case PathIterator.SEG_CLOSE:
				points[0] = moveX;
				points[1] = moveY;
				// Fall into....
			case PathIterator.SEG_LINETO:
				thisX = points[0];
				thisY = points[1];
				float dx = thisX-lastX;
				float dy = thisY-lastY;
				//이전 포인트와 현재 포인트 거리
				float distance = (float)Math.sqrt( dx*dx + dy*dy );
				
				float angle = (float)Math.atan2( dy, dx );
				
				double degree = Math.toDegrees(angle);
				
				if(currentChar == 0){
				if((degree > 90 || degree < -90) ){
					upsideDown = true;
					//System.out.println("rotate angle = "+ Math.toDegrees(angle));
				}
				else{
					upsideDown = false;
				}
				}
				
				//이전 포인트와의 거리가 next(현재 글자를 그리는데 필요한 거리)
				//만약 크지 않을 경우
				if ( distance >= next ) {
					float r = 1.0f/distance;

					
					//upsideDown = true;
					if(upsideDown){
						//Math.toRadians(Math.toDegrees(angle)+180);
						angle = (float) Math.toRadians(Math.toDegrees(angle)+180);
					}
					else{
						angle = angle;
					}
					
					while ( currentChar < length && distance >= next ) {
					//while ( currentChar < length) {
						Shape glyph = null;
						Point2D p = null;
						if(upsideDown){
							glyph = glyphVector1.getGlyphOutline( currentChar );
							p = glyphVector1.getGlyphPosition(currentChar);
						}
						else{
							glyph = glyphVector.getGlyphOutline( currentChar );
							p = glyphVector.getGlyphPosition(currentChar);
						}
						//Point2D p = glyphVector.getGlyphPosition(currentChar);
						
						float px = (float)p.getX();
						float py = (float)p.getY();
						float x = lastX + next*dx*r;
						float y = lastY + next*dy*r;
						
                        float advance = nextAdvance;
                        if(upsideDown){
                        	nextAdvance = currentChar < length-1 ? glyphVector1.getGlyphMetrics(currentChar+1).getAdvance() * 0.5f : 0;
                        }
                        else{
                        	nextAdvance = currentChar < length-1 ? glyphVector.getGlyphMetrics(currentChar+1).getAdvance() * 0.5f : 0;
                        }
						t.setToTranslation( x, y );
						g.setColor(Color.BLACK);
						//g.fillRect((int)x-3, (int)y-3, 6, 6);
						
						
						

						
						t.rotate( angle );
						
						
						
						
						//-5 값은 폰트 높이의 절반 값
						double charHeight = glyph.getBounds().getHeight()/2;
						
						t.translate( -px-advance, -(py-charHeight) );
						
						
						// 라인 위로 그리는 로직
						//t.translate( -px-advance, -py );
						Shape oneCharShape = t.createTransformedShape( glyph );
						Rectangle charRect = oneCharShape.getBounds();
						
						//if(this.viewR.intersects(charRect)){
							chars.add(oneCharShape);
						//}
						
						if(overlay != null && overlay.chechOverlap(charRect, extensionArea)){
							overlap = true;
						}
						
//						if(this.chechRoadNameOverlap(charRect, overRects)){
//							overlap = true;
//						}
						
						//result.append( oneCharShape, false );
						
						next += (advance+nextAdvance) * factor;
						
						//System.out.println("next="+next);
						currentChar++;
						
						if(currentChar == length){
							currentChar = 0;
							next += repeatTextInteval;
							
							if(upsideDown){
								nextAdvance = glyphVector1.getGlyphMetrics( currentChar ).getAdvance() * 0.5f;
							}
							else{
								nextAdvance = glyphVector.getGlyphMetrics( currentChar ).getAdvance() * 0.5f;
							}
							
							//띄어 쓰기의 경우 문제
							if(overlap == false){
								for(Shape shpe : chars){
									result.append( shpe, false );
									//this.roadNameOverlaps.add(shpe.getBounds());
									if(overlay != null){
										overlay.add(shpe.getBounds(), extensionArea);
									}
									
									resultCnt++;

								}
								
							}

							//upsideDown = false;
							overlap = false;
							chars.removeAllElements();
						}
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
        
        
        
        
        //오버랩 확인 로직
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
       //System.out.println("result cnt="+resultCnt);
        shape = null;
        return result;
	}
	

	public float measurePathLength( Shape shape ) {
		PathIterator it = new FlatteningPathIterator( shape.getPathIterator( null ), FLATNESS );
		float points[] = new float[6];
		float moveX = 0, moveY = 0;
		float lastX = 0, lastY = 0;
		float thisX = 0, thisY = 0;
		int type = 0;
        float total = 0;

		while ( !it.isDone() ) {
			type = it.currentSegment( points );
			switch( type ){
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
				float dx = thisX-lastX;
				float dy = thisY-lastY;
				total += (float)Math.sqrt( dx*dx + dy*dy );
				lastX = thisX;
				lastY = thisY;
				break;
			}
			it.next();
		}

		return total;
	}

}
