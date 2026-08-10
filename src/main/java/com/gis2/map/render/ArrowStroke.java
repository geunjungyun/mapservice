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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

//import com.gis.map.style.BasicStyleExtend;
import org.locationtech.jts.geom.Geometry;

import com.gis2.map.style.BasicStyleExtend;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class ArrowStroke implements Stroke  {
	
	float fillWidth = -1;
	int repeatDistance = -1;
	
	public ArrowStroke(float fillWidth, int repeatDistance) {
		this.fillWidth = fillWidth;
		this.repeatDistance = repeatDistance;
	}
	
	
	private float advance;
	
	private static final float FLATNESS = 1;
	
	private AffineTransform at = new AffineTransform();
	
	private boolean repeat = true;
	
	public Shape createStrokedShape( Shape shape ) {
		
		double dist = this.calculatePathLength(shape);
		
		GeneralPath[] segments = null;
		
		if(dist > repeatDistance) {
			segments = createSegmentedPathByPixels(shape, repeatDistance);
		}
		else {
			segments = new GeneralPath[1];
			segments[0] = (GeneralPath) shape;
		}
		
		
		//GeneralPath[] segments = splitGeneralPath(shape, (double)repeatDistance);
		
		//System.out.println("dist="+dist+", count=" + segments.length+", repeat="+repeatDistance);
		
		//System.out.println("dist=" + dist +", startTrim=" + fillWidth+", endTrim="+fillWidth*2);

		
		GeneralPath allShape = new GeneralPath();
		
		for(GeneralPath segment :  segments) {
			
			//allShape.append(createTrimmedStrokedShape(segment, fillWidth, fillWidth*2), false);
			
			
			allShape.append(trimPathWithAngles(segment, fillWidth, fillWidth*2), false);
			
		}

		return allShape;
	}
	
	public GeneralPath[] createSegmentedPathByPixels(Shape shape, float segmentLength) {
	    // List to store individual segments
	    List<GeneralPath> segmentedPaths = new ArrayList<>();

	    GeneralPath currentSegment = new GeneralPath();
	    PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), 0.1);
	    float[] points = new float[6];

	    float lastX = 0, lastY = 0;
	    float accumulatedDistance = 0;

	    while (!it.isDone()) {
	        int type = it.currentSegment(points);
	        switch (type) {
	            case PathIterator.SEG_MOVETO:
	                lastX = points[0];
	                lastY = points[1];
	                currentSegment = new GeneralPath();
	                currentSegment.moveTo(lastX, lastY);
	                accumulatedDistance = 0; // Reset accumulated distance for a new segment
	                break;

	            case PathIterator.SEG_LINETO:
	                float thisX = points[0];
	                float thisY = points[1];
	                float dx = thisX - lastX;
	                float dy = thisY - lastY;
	                float segmentDistance = (float) Math.sqrt(dx * dx + dy * dy);

	                // Split the line segment into multiple segments of the specified length
	                while (accumulatedDistance + segmentDistance >= segmentLength) {
	                    // Calculate the point where the current segment should end
	                    float remainingLength = segmentLength - accumulatedDistance;
	                    float ratio = remainingLength / segmentDistance;
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

	                // Add the remaining part of the segment to the current segment
	                currentSegment.lineTo(thisX, thisY);
	                accumulatedDistance += segmentDistance;
	                lastX = thisX;
	                lastY = thisY;
	                break;

	            case PathIterator.SEG_CLOSE:
	                currentSegment.closePath();
	                segmentedPaths.add(currentSegment);
	                break;
	        }
	        it.next();
	    }

	    // Add the last segment if it's not empty
	    if (accumulatedDistance > 0 && !currentSegment.getBounds().isEmpty()) {
	        segmentedPaths.add(currentSegment);
	    }

	    // Convert the list to an array and return
	    return segmentedPaths.toArray(new GeneralPath[0]);
	}

	
	public GeneralPath[] createSegmentedPath5(Shape shape, float segmentLength) {
	    // List to store individual segments
	    List<GeneralPath> segmentedPaths = new ArrayList<>();

	    GeneralPath currentSegment = new GeneralPath();
	    PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), 0.1);
	    float[] points = new float[6];

	    float moveX = 0, moveY = 0;
	    float lastX = 0, lastY = 0;
	    float accumulatedDistance = 0;

	    while (!it.isDone()) {
	        int type = it.currentSegment(points);
	        switch (type) {
	            case PathIterator.SEG_MOVETO:
	                moveX = lastX = points[0];
	                moveY = lastY = points[1];
	                currentSegment = new GeneralPath();
	                currentSegment.moveTo(moveX, moveY);
	                accumulatedDistance = 0; // Reset accumulated distance for a new segment
	                break;

	            case PathIterator.SEG_LINETO:
	                float thisX = points[0];
	                float thisY = points[1];
	                float dx = thisX - lastX;
	                float dy = thisY - lastY;
	                float segmentDistance = (float) Math.sqrt(dx * dx + dy * dy);

	                // Continue accumulating distance until we reach the segment length
	                while (accumulatedDistance + segmentDistance >= segmentLength) {
	                    // Calculate interpolation ratio
	                    float remainingLength = segmentLength - accumulatedDistance;
	                    float ratio = remainingLength / segmentDistance;
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

	                // Add the remaining part to the current segment
	                currentSegment.lineTo(thisX, thisY);
	                accumulatedDistance += segmentDistance;
	                lastX = thisX;
	                lastY = thisY;
	                break;

	            case PathIterator.SEG_CLOSE:
	                // Close the current segment path
	                currentSegment.closePath();
	                segmentedPaths.add(currentSegment);
	                break;
	        }
	        it.next();
	    }

	    // Add the last segment if it's not empty and valid
	    if (accumulatedDistance > 0 && !currentSegment.getBounds().isEmpty()) {
	        segmentedPaths.add(currentSegment);
	    }

	    // Convert the list to an array and return
	    return segmentedPaths.toArray(new GeneralPath[0]);
	}
	
	
	public GeneralPath[] createSegmentedPath4(Shape shape, float segmentLength) {
	    // List to store individual segments
	    List<GeneralPath> segmentedPaths = new ArrayList<>();

	    GeneralPath currentSegment = new GeneralPath();
	    PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), 0.1);
	    float[] points = new float[6];

	    float moveX = 0, moveY = 0;
	    float lastX = 0, lastY = 0;
	    float accumulatedDistance = 0;

	    while (!it.isDone()) {
	        int type = it.currentSegment(points);
	        switch (type) {
	            case PathIterator.SEG_MOVETO:
	                moveX = lastX = points[0];
	                moveY = lastY = points[1];
	                currentSegment = new GeneralPath();
	                currentSegment.moveTo(moveX, moveY);
	                accumulatedDistance = 0; // Reset accumulated distance for a new segment
	                break;

	            case PathIterator.SEG_LINETO:
	                float thisX = points[0];
	                float thisY = points[1];
	                float dx = thisX - lastX;
	                float dy = thisY - lastY;
	                float segmentDistance = (float) Math.sqrt(dx * dx + dy * dy);

	                while (accumulatedDistance + segmentDistance >= segmentLength) {
	                    // Calculate interpolation ratio
	                    float remainingLength = segmentLength - accumulatedDistance;
	                    float ratio = remainingLength / segmentDistance;
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

	                // Add the remaining part to the current segment
	                currentSegment.lineTo(thisX, thisY);
	                accumulatedDistance += segmentDistance;
	                lastX = thisX;
	                lastY = thisY;
	                break;

	            case PathIterator.SEG_CLOSE:
	                // Close the current segment path
	                currentSegment.closePath();
	                segmentedPaths.add(currentSegment);
	                break;
	        }
	        it.next();
	    }

	    // Add the last segment if it's not empty and valid
	    if (accumulatedDistance > 0 && !currentSegment.getBounds().isEmpty()) {
	        segmentedPaths.add(currentSegment);
	    }

	    // Convert the list to an array and return
	    return segmentedPaths.toArray(new GeneralPath[0]);
	}
	
	
	
    public GeneralPath[] splitGeneralPath(Shape path, double distance) {
        PathIterator iterator = path.getPathIterator(null, 0.01); // 곡선 세밀도 설정
        double[] coords = new double[6];
        double[] lastMove = new double[2];
        double[] lastPoint = new double[2];
        List<GeneralPath> paths = new ArrayList<>();
        GeneralPath currentPath = null;

        while (!iterator.isDone()) {
            int type = iterator.currentSegment(coords);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    // 새로운 경로 시작
                    lastMove[0] = coords[0];
                    lastMove[1] = coords[1];
                    lastPoint[0] = coords[0];
                    lastPoint[1] = coords[1];
                    currentPath = new GeneralPath();
                    currentPath.moveTo(coords[0], coords[1]);
                    break;

                case PathIterator.SEG_LINETO:
                    if (currentPath != null) {
                        List<double[]> segmentPoints = subdivideLine(lastPoint, coords, distance);
                        for (int i = 1; i < segmentPoints.size(); i++) {
                            currentPath.lineTo(segmentPoints.get(i)[0], segmentPoints.get(i)[1]);
                        }
                        paths.add(currentPath);
                        currentPath = new GeneralPath(); // 새 경로 시작
                        currentPath.moveTo(coords[0], coords[1]);
                    }
                    lastPoint[0] = coords[0];
                    lastPoint[1] = coords[1];
                    break;

                case PathIterator.SEG_CLOSE:
                    if (currentPath != null) {
                        List<double[]> segmentPoints = subdivideLine(lastPoint, lastMove, distance);
                        for (int i = 1; i < segmentPoints.size(); i++) {
                            currentPath.lineTo(segmentPoints.get(i)[0], segmentPoints.get(i)[1]);
                        }
                        currentPath.closePath();
                        paths.add(currentPath);
                    }
                    break;
            }
            iterator.next();
        }

        return paths.toArray(new GeneralPath[0]);
    }

    private List<double[]> subdivideLine(double[] start, double[] end, double distance) {
        List<double[]> points = new ArrayList<>();
        double dx = end[0] - start[0];
        double dy = end[1] - start[1];
        double length = Math.sqrt(dx * dx + dy * dy);
        int numPoints = (int) (length / distance);

        for (int i = 0; i <= numPoints; i++) {
            double t = i / (double) numPoints;
            points.add(new double[]{
                start[0] + t * dx,
                start[1] + t * dy
            });
        }
        return points;
    }	
    
    public GeneralPath[] createSegmentedPath3(Shape shape, float segmentLength) {
        // List to store individual segments
        List<GeneralPath> segmentedPaths = new ArrayList<>();

        GeneralPath currentSegment = new GeneralPath();
        PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), 0.1);
        float[] points = new float[6];

        float moveX = 0, moveY = 0;
        float lastX = 0, lastY = 0;
        float accumulatedDistance = 0;

        while (!it.isDone()) {
            int type = it.currentSegment(points);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    moveX = lastX = points[0];
                    moveY = lastY = points[1];
                    currentSegment = new GeneralPath();
                    currentSegment.moveTo(moveX, moveY);
                    accumulatedDistance = 0; // Reset accumulated distance for a new segment
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

                    // Add the remaining part to the current segment
                    currentSegment.lineTo(thisX, thisY);
                    accumulatedDistance += segmentDistance;
                    lastX = thisX;
                    lastY = thisY;
                    break;

                case PathIterator.SEG_CLOSE:
                    // Close the current segment path
                    currentSegment.closePath();
                    segmentedPaths.add(currentSegment);
                    break;
            }
            it.next();
        }

        // Add the last segment if it's not empty and valid
        if (accumulatedDistance > 0 && !currentSegment.getBounds().isEmpty()) {
            segmentedPaths.add(currentSegment);
        }

        // Convert the list to an array and return
        return segmentedPaths.toArray(new GeneralPath[0]);
    }
    
    
    public GeneralPath[] createSegmentedPath2(Shape shape, float segmentLength) {
        // List to store individual segments
        List<GeneralPath> segmentedPaths = new ArrayList<>();

        GeneralPath currentSegment = new GeneralPath();
        PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), 0.1);
        float[] points = new float[6];

        float moveX = 0, moveY = 0;
        float lastX = 0, lastY = 0;
        float accumulatedDistance = 0;

        while (!it.isDone()) {
            int type = it.currentSegment(points);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    moveX = lastX = points[0];
                    moveY = lastY = points[1];
                    currentSegment = new GeneralPath();
                    currentSegment.moveTo(moveX, moveY);
                    accumulatedDistance = 0; // Reset accumulated distance for a new segment
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

                    // Add the remaining part to the current segment
                    currentSegment.lineTo(thisX, thisY);
                    accumulatedDistance += segmentDistance;
                    lastX = thisX;
                    lastY = thisY;
                    break;

                case PathIterator.SEG_CLOSE:
                    // Close the current segment path
                    currentSegment.closePath();
                    segmentedPaths.add(currentSegment);
                    break;
            }
            it.next();
        }

        // Add the last segment if it's not empty
        if (!currentSegment.getBounds().isEmpty()) {
            segmentedPaths.add(currentSegment);
        }

        // Add remaining segment as a separate path if distance is smaller than segmentLength
        if (accumulatedDistance > 0) {
            segmentedPaths.add(currentSegment);
        }

        // Convert the list to an array and return
        return segmentedPaths.toArray(new GeneralPath[0]);
    }
    
    
    public GeneralPath[] createSegmentedPath1(Shape shape, float segmentLength) {
        // List to store individual segments
        List<GeneralPath> segmentedPaths = new ArrayList<>();

        GeneralPath currentSegment = new GeneralPath();
        PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), 0.1);
        float[] points = new float[6];

        float moveX = 0, moveY = 0;
        float lastX = 0, lastY = 0;
        float accumulatedDistance = 0;

        while (!it.isDone()) {
            int type = it.currentSegment(points);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    moveX = lastX = points[0];
                    moveY = lastY = points[1];
                    currentSegment = new GeneralPath();
                    currentSegment.moveTo(moveX, moveY);
                    accumulatedDistance = 0; // Reset accumulated distance for a new segment
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

                    // Add the remaining part to the current segment
                    currentSegment.lineTo(thisX, thisY);
                    accumulatedDistance += segmentDistance;
                    lastX = thisX;
                    lastY = thisY;
                    break;

                case PathIterator.SEG_CLOSE:
                    // Close the current segment path
                    currentSegment.closePath();
                    segmentedPaths.add(currentSegment);
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
	
	
	public GeneralPath createTrimmedStrokedShape(Shape shape, float trimStart, float trimEnd) {
		
		
		int height = 4;
		int width = 4;
		
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
	
    public GeneralPath trimPathWithAngles(GeneralPath path, double startTrim, double endTrim) {
    	AffineTransform at = new AffineTransform();
    	
		int height = 5;
		int width = 5;
		
		
		Polygon pg = new Polygon();
		
		pg.addPoint(0, 0);
		pg.addPoint(width, 0);
		pg.addPoint(width/2, height);
		pg.addPoint(0, 0);

		
		Shape arrowShape = null;
		
		Rectangle2D bounds = pg.getBounds2D();
		
		//at.setToTranslation( -bounds.getCenterX(), -bounds.getCenterY() );
		at.setToTranslation( -bounds.getCenterX(), bounds.getHeight()/3 );
		arrowShape = at.createTransformedShape( pg );
		
		/*
		GeneralPath pg = new GeneralPath();
		pg.moveTo(0, 0);
		pg.lineTo(width/2, height/2);
		pg.lineTo(width, 0);
		Shape arrowShape = null;
		Rectangle2D bounds = pg.getBounds2D();
		
		at.setToTranslation( -bounds.getCenterX(), -bounds.getCenterY() );
		arrowShape = at.createTransformedShape( pg );
		*/

		//Shape resultPath = new Shape();
    	
        GeneralPath trimmedPath = new GeneralPath();
        PathIterator iterator = path.getPathIterator(null);
        double[] coords = new double[6];

        Point2D.Double lastPoint = new Point2D.Double();
        Point2D.Double currentPoint = new Point2D.Double();
        Point2D.Double firstTrimmedPoint = null;
        Point2D.Double lastTrimmedPoint = null;

        boolean trimmingStart = startTrim > 0;
        double currentLength = 0;
        double totalLength = calculatePathLength(path);

        double trimEndThreshold = totalLength - endTrim;

        while (!iterator.isDone()) {
            int segmentType = iterator.currentSegment(coords);

            switch (segmentType) {
                case PathIterator.SEG_MOVETO:
                    currentPoint.setLocation(coords[0], coords[1]);
                    if (currentLength >= startTrim && currentLength <= trimEndThreshold) {
                        trimmedPath.moveTo(coords[0], coords[1]);
                        if (firstTrimmedPoint == null) {
                            firstTrimmedPoint = new Point2D.Double(coords[0], coords[1]);
                        }
                    }
                    break;

                case PathIterator.SEG_LINETO:
                    lastPoint.setLocation(currentPoint);
                    currentPoint.setLocation(coords[0], coords[1]);

                    double segmentLength = lastPoint.distance(currentPoint);

                    // Trim start
                    if (trimmingStart && currentLength + segmentLength > startTrim) {
                        double ratio = (startTrim - currentLength) / segmentLength;
                        double newX = lastPoint.x + (currentPoint.x - lastPoint.x) * ratio;
                        double newY = lastPoint.y + (currentPoint.y - lastPoint.y) * ratio;
                        trimmedPath.moveTo(newX, newY);
                        firstTrimmedPoint = new Point2D.Double(newX, newY);
                        trimmingStart = false;
                    }

                    // Trim end
                    if (!trimmingStart && currentLength < trimEndThreshold) {
                        if (currentLength + segmentLength > trimEndThreshold) {
                            double ratio = (trimEndThreshold - currentLength) / segmentLength;
                            double newX = lastPoint.x + (currentPoint.x - lastPoint.x) * ratio;
                            double newY = lastPoint.y + (currentPoint.y - lastPoint.y) * ratio;
                            trimmedPath.lineTo(newX, newY);
                            lastTrimmedPoint = new Point2D.Double(newX, newY);
                            
                            
                            double startAngle = calculateAngle(firstTrimmedPoint, lastTrimmedPoint);
                            
                            double endAngle = calculateAngle(lastPoint, currentPoint);
                            
//    	                    at.setToTranslation(lastTrimmedPoint.x, lastTrimmedPoint.y);
//    						at.rotate(startAngle);
//
//    						Shape startA = at.createTransformedShape(arrowShape);
//    						
//    						trimmedPath.append(startA, false);
    						
    						at.setToTranslation(lastTrimmedPoint.x, lastTrimmedPoint.y);
    						at.rotate(endAngle);

    						Shape endA = at.createTransformedShape(arrowShape);
    						
    						trimmedPath.append(endA, false);
    						
//    						trimmedPath.append(trimmedPath, false);
    						
    						
                            
                            //return new PathInfo(trimmedPath, calculateAngle(firstTrimmedPoint, lastTrimmedPoint), calculateAngle(lastPoint, currentPoint));
                            return trimmedPath;
                            
                        } else {
                            trimmedPath.lineTo(currentPoint.x, currentPoint.y);
                            lastTrimmedPoint = currentPoint;
                        }
                    }

                    currentLength += segmentLength;
                    break;

                case PathIterator.SEG_CLOSE:
                    if (currentLength <= trimEndThreshold) {
                        trimmedPath.closePath();
                    }
                    break;
            }

            iterator.next();
        }

        //return new PathInfo(trimmedPath, 0, 0); // 각도를 계산하지 못한 경우 기본값 반환
        return trimmedPath;
    }

    private double calculatePathLength(GeneralPath path) {
        PathIterator iterator = path.getPathIterator(null);
        double[] coords = new double[6];

        Point2D.Double lastPoint = new Point2D.Double();
        Point2D.Double currentPoint = new Point2D.Double();
        double totalLength = 0;

        while (!iterator.isDone()) {
            int segmentType = iterator.currentSegment(coords);

            switch (segmentType) {
                case PathIterator.SEG_MOVETO:
                    currentPoint.setLocation(coords[0], coords[1]);
                    break;

                case PathIterator.SEG_LINETO:
                    lastPoint.setLocation(currentPoint);
                    currentPoint.setLocation(coords[0], coords[1]);
                    totalLength += lastPoint.distance(currentPoint);
                    break;
            }

            iterator.next();
        }

        return totalLength;
    }

    private double calculateAngle(Point2D.Double p1, Point2D.Double p2) {
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        //return Math.toDegrees(Math.atan2(dy, dx));
        return Math.atan2(dy, dx) - (float) Math.PI / 2;
    }	

}

