

import java.util.Vector;

import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;

public class JobUtil {
	
	static GeometryFactory gft = new GeometryFactory();
	
	static public Geometry getSimplePolygonm(Geometry _geo, int distance) {

		if (_geo.getGeometryType().equals("Polygon")) {
			return JobUtil.getSimplePolygon((Polygon) _geo, distance);
		} else if (_geo.getGeometryType().equals("MultiPolygon")) {
			Vector<Polygon> newps = new Vector();
			for (int i = 0; i < _geo.getNumGeometries(); i++) {
				Geometry subgeo = _geo.getGeometryN(i);
				Geometry conv = JobUtil.getSimplePolygon((Polygon) subgeo, distance);
				if (conv != null) {
					newps.add((Polygon) conv);
				}
			}

			Polygon[] pls = new Polygon[newps.size()];
			for (int i = 0; i < newps.size(); i++) {
				pls[i] = newps.get(i);
			}
			return gft.createMultiPolygon(pls);
		}

		return null;
	}
	
	public static Geometry getSimplePolygon(Polygon _geo, int distance) {

		Vector<LineString> interV = new Vector();
		Polygon geo = _geo;
		LineString rl = geo.getExteriorRing();
		double rlArea = Math.abs(CGAlgorithms.signedArea(rl.getCoordinates()));
		if ((distance * distance) > rlArea) {
			return null;
		}
		int interCnt = geo.getNumInteriorRing();
		for (int i = 0; i < interCnt; i++) {
			LineString inter = geo.getInteriorRingN(i);
			double itArea = Math.abs(CGAlgorithms.signedArea(inter.getCoordinates()));
			if ((distance * distance) < itArea) {
				interV.add(inter);
			}
		}
		LinearRing[] lrs = new LinearRing[interV.size()];
		for (int i = 0; i < lrs.length; i++) {
			lrs[i] = (LinearRing) interV.get(i);
		}
		return gft.createPolygon((LinearRing) rl, lrs);
	}
	
	/**
	 * 입력된 여러 feature의 geometry를 GeometryCollection 객체로 합쳐서 리턴 
	 * @param fts
	 * @param bufferSize
	 * @return
	 */
	static public Geometry getGeometry(Vector<SimpleFeature> fts, double bufferSize) {

		// Geometry[] array = GeometryFactory.toGeometryArray(fts);
		Geometry[] array = new Geometry[fts.size()];
		for (int i = 0; i < fts.size(); i++) {
			array[i] = (Geometry) fts.get(i).getDefaultGeometry();
		}

		GeometryCollection gc = new GeometryCollection(array, JobUtil.gft);
		Geometry geo  = gc.buffer(bufferSize);
		Geometry geov  = geo.buffer(-bufferSize);
		return geov;
		
	}
}
