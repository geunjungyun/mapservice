package com.gis2.map.render;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

//import com.gis.map.style.BasicStyleExtend;
import org.locationtech.jts.geom.Geometry;

import com.gis2.map.style.BasicStyleExtend;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class ArrowRender implements IRender {
	
	//private Shape shapes[];

	
	
	public void draw(Graphics2D g, Shape shape, Style style) {
		
		
		BasicStyleExtend bse = (BasicStyleExtend) style;
		
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
	
	
	
	private float advance;
	
	private static final float FLATNESS = 1;
	
	private AffineTransform at = new AffineTransform();
	
	private boolean repeat = true;
	
	public Shape createStrokedShape( Shape shape ) {
		
		Shape shapes[] = new Shape[1];
		
		Rectangle2D.Float rec = new Rectangle2D.Float(0, 0, 5, 5);
		
		shapes[0] = rec;
		
		for(int i=0; i<shapes.length; i++) {
			Rectangle2D bounds = shapes[i].getBounds2D();
			at.setToTranslation( -bounds.getCenterX(), -bounds.getCenterY() );
			shapes[i] = at.createTransformedShape( shapes[i] );
		}
		
		advance = 100;
		
		GeneralPath result = new GeneralPath();
		PathIterator it = new FlatteningPathIterator( shape.getPathIterator( null ), FLATNESS );
		float points[] = new float[6];
		float moveX = 0, moveY = 0;
		float lastX = 0, lastY = 0;
		float thisX = 0, thisY = 0;
		int type = 0;
		boolean first = false;
		float next = 0;
		int currentShape = 0;
		int length = shapes.length;

		float factor = 1;

		while ( currentShape < length && !it.isDone() ) {
			type = it.currentSegment( points );
			switch( type ){
			case PathIterator.SEG_MOVETO:
				moveX = lastX = points[0];
				moveY = lastY = points[1];
				//result.moveTo( moveX, moveY );
				
				first = true;
				next = 0;
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
				float distance = (float)Math.sqrt( dx*dx + dy*dy );
				if ( distance >= next ) {
					float r = 1.0f/distance;
					float angle = (float)Math.atan2( dy, dx );
					while ( currentShape < length && distance >= next ) {
						float x = lastX + next*dx*r;
						float y = lastY + next*dy*r;
						at.setToTranslation( x, y );
						at.rotate( angle );
						result.append( at.createTransformedShape( shapes[currentShape] ), false );
						next += advance;
						currentShape++;
						if ( repeat )
							currentShape %= length;
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

		return result;
	}

	/*
	public Shape createTrimmedStrokedShape(Shape shape, float trimStart, float trimEnd) {
		
		
		
		
		
		
		int height = 10;
		int width = 10;
		
		Polygon pg = new Polygon();
		
		pg.addPoint(0, 0);
		pg.addPoint(width, 0);
		pg.addPoint(width/2, height);
		pg.addPoint(0, 0);

		
		Shape arrowShape = null;
		
		Rectangle2D bounds = pg.getBounds2D();
		
		at.setToTranslation( -bounds.getCenterX(), -bounds.getCenterY() );
		arrowShape = at.createTransformedShape( pg );
		
		
	    GeneralPath result = new GeneralPath();
	    PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), FLATNESS);
	    float[] points = new float[6];

	    float moveX = 0, moveY = 0;
	    float lastX = 0, lastY = 0;
	    float thisX = 0, thisY = 0;
	    float accumulatedDistance = 0;

	    boolean trimmingStart = trimStart > 0;
	    boolean trimmingEnd = trimEnd > 0;

	    float totalPathLength = calculatePathLength(shape); // 전체 경로 길이
	    float trimmedLength = totalPathLength - trimEnd;

	    boolean isFirstMove = true;

	    float startAngle = 0;
	    float endAngle = 0;

	    while (!it.isDone()) {
	        int type = it.currentSegment(points);
	        switch (type) {
	            case PathIterator.SEG_MOVETO:
	                moveX = lastX = points[0];
	                moveY = lastY = points[1];
	                if (!trimmingStart) {
	                    result.moveTo(moveX, moveY);
	                }
	                isFirstMove = false;
	                break;

	            case PathIterator.SEG_LINETO:
	                thisX = points[0];
	                thisY = points[1];
	                float dx = thisX - lastX;
	                float dy = thisY - lastY;
	                float segmentDistance = (float) Math.sqrt(dx * dx + dy * dy);

	                if (trimmingStart) {
	                    if (accumulatedDistance + segmentDistance > trimStart) {
	                        // Adjust start point
	                        float ratio = (trimStart - accumulatedDistance) / segmentDistance;
	                        float newX = lastX + ratio * dx;
	                        float newY = lastY + ratio * dy;
	                        result.moveTo(newX, newY);
	                        trimmingStart = false; // Start trimming complete
	                        startAngle = (float) Math.atan2(dy, dx); // Calculate start angle
	                        result.lineTo(thisX, thisY);
	                    }
	                } else if (accumulatedDistance + segmentDistance >= trimmedLength && trimmingEnd) {
	                    // Adjust end point
	                    float ratio = (trimmedLength - accumulatedDistance) / segmentDistance;
	                    float newX = lastX + ratio * dx;
	                    float newY = lastY + ratio * dy;
	                    result.lineTo(newX, newY);
	                    endAngle = (float) Math.atan2(dy, dx); // Calculate end angle
	                    System.out.println("Start angle (radians): " + startAngle);
	                    System.out.println("End angle (radians): " + endAngle);
	                    System.out.println("Start angle (degrees): " + Math.toDegrees(startAngle));
	                    System.out.println("End angle (degrees): " + Math.toDegrees(endAngle));
	                    
						at.setToTranslation(newX, newY);
						at.rotate(endAngle- (float) Math.PI / 2);
	                    
						Shape addShape = at.createTransformedShape(arrowShape);
	                    
	                    result.append(addShape, false);
	                    
	                    return result; // Stop processing further
	                } else {
	                    // Add the full segment
	                    if (isFirstMove) {
	                        result.moveTo(lastX, lastY);
	                        isFirstMove = false;
	                    }
	                    result.lineTo(thisX, thisY);
	                }

	                accumulatedDistance += segmentDistance;
	                lastX = thisX;
	                lastY = thisY;
	                break;

	            case PathIterator.SEG_CLOSE:
	                if (!trimmingStart && !trimmingEnd) {
	                    result.closePath();
	                }
	                break;
	        }
	        it.next();
	    }

	    // Print angles if not already calculated
	    if (startAngle == 0 || endAngle == 0) {
	        System.out.println("Start angle (radians): " + startAngle);
	        System.out.println("End angle (radians): " + endAngle);
	        System.out.println("Start angle (degrees): " + Math.toDegrees(startAngle));
	        System.out.println("End angle (degrees): " + Math.toDegrees(endAngle));
	    }

	    return result;
	}
	*/
	/*
	private float calculatePathLength(Shape shape) {
	    PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), FLATNESS);
	    float[] points = new float[6];
	    float lastX = 0, lastY = 0;
	    float totalLength = 0;

	    while (!it.isDone()) {
	        int type = it.currentSegment(points);
	        if (type == PathIterator.SEG_MOVETO) {
	            lastX = points[0];
	            lastY = points[1];
	        } else if (type == PathIterator.SEG_LINETO) {
	            float dx = points[0] - lastX;
	            float dy = points[1] - lastY;
	            totalLength += (float) Math.sqrt(dx * dx + dy * dy);
	            lastX = points[0];
	            lastY = points[1];
	        }
	        it.next();
	    }
	    return totalLength;
	}
	*/
	
	public GeneralPath[] createSegmentedPath(Shape shape, float segmentLength) {
	    // List to store individual segments
	    List<GeneralPath> segmentedPaths = new ArrayList<>();

	    GeneralPath currentSegment = new GeneralPath();
	    PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), 0.1);
	    float[] points = new float[6];

	    float moveX = 0, moveY = 0;
	    float lastX = 0, lastY = 0;
	    float accumulatedDistance = 0;

	    boolean isFirstMove = true;

	    while (!it.isDone()) {
	        int type = it.currentSegment(points);
	        switch (type) {
	            case PathIterator.SEG_MOVETO:
	                moveX = lastX = points[0];
	                moveY = lastY = points[1];
	                if (isFirstMove) {
	                    currentSegment.moveTo(moveX, moveY);
	                    isFirstMove = false;
	                }
	                break;

	            case PathIterator.SEG_LINETO:
	                float thisX = points[0];
	                float thisY = points[1];
	                float dx = thisX - lastX;
	                float dy = thisY - lastY;
	                float segmentDistance = (float) Math.sqrt(dx * dx + dy * dy);

	                while (accumulatedDistance + segmentDistance >= segmentLength) {
	                    // Calculate interpolation ratio
	                    float ratio = (segmentLength - accumulatedDistance) / segmentDistance;
	                    float newX = lastX + ratio * dx;
	                    float newY = lastY + ratio * dy;

	                    // Add the interpolated point to the current segment
	                    currentSegment.lineTo(newX, newY);

	                    // Save the current segment
	                    segmentedPaths.add(currentSegment);

	                    // Start a new segment from the last interpolated point
	                    currentSegment = new GeneralPath();
	                    currentSegment.moveTo(newX, newY);

	                    // Reset for the next segment
	                    accumulatedDistance = 0;
	                    lastX = newX;
	                    lastY = newY;

	                    // Update remaining distance
	                    dx = thisX - lastX;
	                    dy = thisY - lastY;
	                    segmentDistance = (float) Math.sqrt(dx * dx + dy * dy);
	                }

	                // Add the remaining part of the current segment
	                currentSegment.lineTo(thisX, thisY);
	                accumulatedDistance += segmentDistance;
	                lastX = thisX;
	                lastY = thisY;
	                break;

	            case PathIterator.SEG_CLOSE:
	                currentSegment.closePath();
	                segmentedPaths.add(currentSegment);
	                currentSegment = new GeneralPath();
	                break;
	        }
	        it.next();
	    }

	    // Add the last segment if it's not empty
	    if (!currentSegment.getBounds().isEmpty()) {
	        segmentedPaths.add(currentSegment);
	    }

	    // Convert the list to an array and return
	    return segmentedPaths.toArray(new GeneralPath[0]);
	}
	
	/*
	public Shape createTrimmedStrokedShape(Shape shape, float trimStart, float trimEnd) {
	    GeneralPath result = new GeneralPath();
	    PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), FLATNESS);
	    float[] points = new float[6];

	    float moveX = 0, moveY = 0;
	    float lastX = 0, lastY = 0;
	    float thisX = 0, thisY = 0;
	    float accumulatedDistance = 0;

	    float totalPathLength = calculatePathLength(shape); // 전체 경로 길이
	    float trimmedLength = totalPathLength - trimEnd;

	    boolean trimmingStart = trimStart > 0;
	    boolean trimmingEnd = trimEnd > 0;

	    boolean isFirstSegmentProcessed = false;
	    boolean isPathStarted = false;

	    float startAngle = 0;
	    float endAngle = 0;

	    while (!it.isDone()) {
	        int type = it.currentSegment(points);
	        switch (type) {
	            case PathIterator.SEG_MOVETO:
	                moveX = lastX = points[0];
	                moveY = lastY = points[1];
	                if (!trimmingStart) {
	                    result.moveTo(moveX, moveY);
	                    isPathStarted = true;
	                }
	                break;

	            case PathIterator.SEG_LINETO:
	                thisX = points[0];
	                thisY = points[1];
	                float dx = thisX - lastX;
	                float dy = thisY - lastY;
	                float segmentDistance = (float) Math.sqrt(dx * dx + dy * dy);

	                if (trimmingStart) {
	                    if (accumulatedDistance + segmentDistance >= trimStart) {
	                        // Adjust start point
	                        float ratio = (trimStart - accumulatedDistance) / segmentDistance;
	                        float newX = lastX + ratio * dx;
	                        float newY = lastY + ratio * dy;

	                        result.moveTo(newX, newY);
	                        startAngle = (float) Math.atan2(dy, dx); // Calculate start angle
	                        isPathStarted = true;

	                        // Process the remaining segment
	                        result.lineTo(thisX, thisY);
	                        trimmingStart = false; // Start trimming complete
	                    }
	                } else if (accumulatedDistance + segmentDistance >= trimmedLength && trimmingEnd) {
	                    // Adjust end point
	                    float ratio = (trimmedLength - accumulatedDistance) / segmentDistance;
	                    float newX = lastX + ratio * dx;
	                    float newY = lastY + ratio * dy;

	                    result.lineTo(newX, newY);
	                    endAngle = (float) Math.atan2(dy, dx); // Calculate end angle

	                    // Print angles
	                    System.out.println("Start angle (radians): " + startAngle);
	                    System.out.println("End angle (radians): " + endAngle);
	                    System.out.println("Start angle (degrees): " + Math.toDegrees(startAngle));
	                    System.out.println("End angle (degrees): " + Math.toDegrees(endAngle));
	                    return result; // Stop processing further
	                } else {
	                    // Add the full segment
	                    if (!isPathStarted) {
	                        result.moveTo(lastX, lastY);
	                        isPathStarted = true;
	                    }
	                    result.lineTo(thisX, thisY);
	                }

	                accumulatedDistance += segmentDistance;
	                lastX = thisX;
	                lastY = thisY;
	                break;

	            case PathIterator.SEG_CLOSE:
	                if (!trimmingStart && !trimmingEnd) {
	                    result.closePath();
	                }
	                break;
	        }
	        it.next();
	    }

	    // Print angles if not already calculated
	    if (startAngle != 0 || endAngle != 0) {
	        System.out.println("Start angle (radians): " + startAngle);
	        System.out.println("End angle (radians): " + endAngle);
	        System.out.println("Start angle (degrees): " + Math.toDegrees(startAngle));
	        System.out.println("End angle (degrees): " + Math.toDegrees(endAngle));
	    }

	    return result;
	}	
	*/
	
	public GeneralPath createTrimmedStrokedShape(Shape shape, float trimStart, float trimEnd) {
		
		
		int height = 5;
		int width = 5;
		
		Polygon pg = new Polygon();
		
		pg.addPoint(0, 0);
		pg.addPoint(width, 0);
		pg.addPoint(width/2, height);
		pg.addPoint(0, 0);

		
		Shape arrowShape = null;
		
		Rectangle2D bounds = pg.getBounds2D();
		
		at.setToTranslation( -bounds.getCenterX(), -bounds.getCenterY() );
		arrowShape = at.createTransformedShape( pg );
		
		float pgOffsetX = -1;
		float pgOffsetY = -1;
		
		float pgAngle  = -1;
		
		
	    GeneralPath result = new GeneralPath();
	    PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), FLATNESS);
	    float[] points = new float[6];

	    float moveX = 0, moveY = 0;
	    float lastX = 0, lastY = 0;
	    float thisX = 0, thisY = 0;
	    float accumulatedDistance = 0;

	    boolean trimmingStart = trimStart > 0;
	    boolean trimmingEnd = trimEnd > 0;

	    float totalPathLength = calculatePathLength(shape); // 전체 경로 길이
	    float trimmedLength = totalPathLength - trimEnd;

	    boolean isFirstMove = true;

	    float startAngle = 0;
	    float endAngle = 0;

	    while (!it.isDone()) {
	        int type = it.currentSegment(points);
	        switch (type) {
	            case PathIterator.SEG_MOVETO:
	                moveX = lastX = points[0];
	                moveY = lastY = points[1];
	                if (!trimmingStart) {
	                    result.moveTo(moveX, moveY);
	                }
	                isFirstMove = false;
	                break;

	            case PathIterator.SEG_LINETO:
	                thisX = points[0];
	                thisY = points[1];
	                float dx = thisX - lastX;
	                float dy = thisY - lastY;
	                float segmentDistance = (float) Math.sqrt(dx * dx + dy * dy);

	                if (trimmingStart) {
	                    if (accumulatedDistance + segmentDistance > trimStart) {
	                        // Adjust start point
	                        float ratio = (trimStart - accumulatedDistance) / segmentDistance;
	                        float newX = lastX + ratio * dx;
	                        float newY = lastY + ratio * dy;
	                        result.moveTo(newX, newY);
	                        trimmingStart = false; // Start trimming complete
	                        startAngle = (float) Math.atan2(dy, dx); // Calculate start angle
	                        result.lineTo(thisX, thisY);
	                        
		                    pgOffsetX = thisX;
		                    pgOffsetY = thisY;
		                    pgAngle = startAngle- (float) Math.PI / 2;
	                        
	                    }
	                } else if (accumulatedDistance + segmentDistance >= trimmedLength && trimmingEnd) {
	                    // Adjust end point
	                    float ratio = (trimmedLength - accumulatedDistance) / segmentDistance;
	                    float newX = lastX + ratio * dx;
	                    float newY = lastY + ratio * dy;
	                    result.lineTo(newX, newY);
	                    endAngle = (float) Math.atan2(dy, dx); // Calculate end angle
	                    printAngles(startAngle, endAngle);

	                    
	                    pgOffsetX = newX;
	                    pgOffsetY = newY;
	                    pgAngle = endAngle- (float) Math.PI / 2;
	                    
	                    
	                    at.setToTranslation(newX, newY);
						at.rotate(endAngle- (float) Math.PI / 2);
						Shape addShape = at.createTransformedShape(arrowShape);
	                    result.append(addShape, false);
	                    
	                    return result; // Stop processing further
	                } else {
	                    // Add the full segment
	                    if (isFirstMove) {
	                        result.moveTo(lastX, lastY);
	                        isFirstMove = false;
	                        startAngle = (float) Math.atan2(dy, dx); // Calculate start angle for single segment
	                    }
	                    result.lineTo(thisX, thisY);
	                }

	                accumulatedDistance += segmentDistance;
	                lastX = thisX;
	                lastY = thisY;
	                break;

	            case PathIterator.SEG_CLOSE:
	                if (!trimmingStart && !trimmingEnd) {
	                    result.closePath();
	                }
	                break;
	        }
	        it.next();
	    }

	    // Calculate end angle if it's not set during the loop
	    if (endAngle == 0 && totalPathLength > trimStart) {
	        endAngle = startAngle; // For single-segment paths, the angle remains the same
	    }

	    printAngles(startAngle, endAngle);
	    
	    
	    if(pgOffsetX != -1 && pgOffsetY != -1 && pgAngle != -1) {
            at.setToTranslation(pgOffsetX, pgOffsetY);
			at.rotate(pgAngle);
			Shape addShape = at.createTransformedShape(arrowShape);
            result.append(addShape, false);
	    }
	    
	    
	    return result;
	}
	

	private void printAngles(float startAngle, float endAngle) {
	    System.out.println("Start angle (radians): " + startAngle);
	    System.out.println("End angle (radians): " + endAngle);
	    System.out.println("Start angle (degrees): " + Math.toDegrees(startAngle));
	    System.out.println("End angle (degrees): " + Math.toDegrees(endAngle));
	}

	private float calculatePathLength(Shape shape) {
	    PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), FLATNESS);
	    float[] points = new float[6];
	    float lastX = 0, lastY = 0;
	    float totalLength = 0;

	    while (!it.isDone()) {
	        int type = it.currentSegment(points);
	        if (type == PathIterator.SEG_MOVETO) {
	            lastX = points[0];
	            lastY = points[1];
	        } else if (type == PathIterator.SEG_LINETO) {
	            float dx = points[0] - lastX;
	            float dy = points[1] - lastY;
	            totalLength += (float) Math.sqrt(dx * dx + dy * dy);
	            lastX = points[0];
	            lastY = points[1];
	        }
	        it.next();
	    }
	    return totalLength;
	}

}
