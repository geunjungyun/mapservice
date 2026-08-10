import java.util.Iterator;

import org.geotools.data.collection.ListFeatureCollection;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;

import com.data.file.FileLayer;
import com.data.file.FileLayer.SimpleFeatureIterator;

public class PnuIntersact {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			FileLayer fl = new FileLayer("f:\\coops\\2024\\tileset\\output\\reg10\\layers\\fa", null);
			
//			FileLayer savefl = new FileLayer("H:\\coops\\2024\\tileset\\output\\reg10\\layers\\fatest", fl.getLayerInfo());
//			
//			SimpleFeatureIterator sfi = fl.iterator();
//			while(sfi.hasNext()) {
//				SimpleFeature sf = sfi.next();
//				savefl.writeFeature(sf);
//			}
//			
//			savefl.close();
//			fl.close();
			//String pnu = "1159010400100490034";
			//String pnu = "1159010400100490031";
			String pnu = "1117013000100560021";
			
			SimpleFeature selectf = fl.getFeature(pnu);
			
			Geometry selectGeo  = (Geometry)selectf.getDefaultGeometry();
			
			Envelope env = selectGeo.getEnvelopeInternal();
			
			ListFeatureCollection lfc = (ListFeatureCollection) fl.getFeatures(env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY());
			
			Iterator it = lfc.iterator();
			while(it.hasNext()) {
				SimpleFeature sf = (SimpleFeature)it.next();
				if(!sf.getAttribute("pnu").equals(pnu)) {
					
					Geometry geo = (Geometry)sf.getDefaultGeometry();
					
					Geometry interGeo = geo.intersection(selectGeo);
					
					System.out.println(sf.getAttribute("jibun")+"="+interGeo.toString());
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
