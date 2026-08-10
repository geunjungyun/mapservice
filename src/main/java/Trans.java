import java.io.File;
import java.util.List;
import java.util.Vector;

import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.operation.builder.MappedPosition;
import org.geotools.referencing.operation.builder.RubberSheetBuilder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Transformation;

import com.data.file.ShapeFile;


public class Trans {
	
	String mappingFile = "D:\\coops\\2024\\mapping_5186.shp";
	String input = "D:\\coops\\2024\\down\\인천_옹진군\\LSMD_CONT_LDREG_28720_202405.shp";
	String input_encoding = "x-windows-949";
	String output = "D:\\coops\\2024\\down\\인천_옹진군\\LSMD_CONT_LDREG_28720_202405_conv.shp";
	String output_encoding = "x-windows-949";
	
	GeometryFactory gf = new GeometryFactory();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Trans rt = new Trans();
		
		try {
			
			long st = System.currentTimeMillis();
			
			if(args.length == 6) {
				rt.test(args[0],
						args[1],
						args[2],args[3], 
						args[4],args[5]);
			}
			else {
//				rt.test("D:\\coops\\2024\\mapping_5186.shp",
//						"D:\\coops\\2024\\area_5186.shp",
//						"D:\\coops\\2024\\down\\인천_옹진군\\LSMD_CONT_LDREG_28720_202405.shp","x-windows-949", 
//						"D:\\coops\\2024\\down\\인천_옹진군\\LSMD_CONT_LDREG_28720_202405_conv.shp", "x-windows-949");
//				rt.test("D:\\coops\\2024\\20240611\\aaa\\mapping.shp",
//				"D:\\coops\\2024\\20240611\\aaa\\area.shp",
//				"D:\\coops\\2024\\20240611\\aaa\\LSMD_CONT_LDREG_11680_202405.shp","x-windows-949", 
//				"D:\\coops\\2024\\20240611\\aaa\\LSMD_CONT_LDREG_11680_202405_conv.shp", "x-windows-949");
				
//				rt.test("D:\\coops\\2024\\강남구샘플\\mapping.shp",
//						"D:\\coops\\2024\\강남구샘플\\area.shp",
//						"D:\\coops\\2024\\강남구샘플\\LSMD_CONT_LDREG_11680_202405.shp","x-windows-949", 
//						"D:\\coops\\2024\\강남구샘플\\LSMD_CONT_LDREG_11680_202405_conv.shp", "x-windows-949");		
				
				rt.test("E:\\down\\11110_5179\\11110_5179\\11110_mapping.shp",
				"E:\\down\\11110_5179\\11110_5179\\area.shp",
				"E:\\down\\11110_5179\\11110_5179\\ZA0010_11110.shp","x-windows-949", 
				"E:\\down\\11110_5179\\11110_5179\\ZA0010_11110_conv1.shp", "x-windows-949");					
				
			}
			long ed = System.currentTimeMillis();
			
			System.out.println("time = " + (ed-st)/1000+" sec");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void test(String mapping, String area, String input, String inputChar, String output, String outputChar) throws Exception{
		
		//ThinPlateSplineTransform 
		
		System.out.println("mapping       =" + mapping);
		System.out.println("area          =" + area);
		System.out.println("input         =" + input);
		System.out.println("inputEncoding =" + inputChar);
		
		System.out.println("output        =" + output);
		System.out.println("outputEncoding=" + outputChar);
		
		Vector<Oper> opers = new Vector();
		
		opers.add(new Oper());
		
		List<MappedPosition> vectors = new Vector();
		
		ShapeFile areaShp = new ShapeFile(new File(area), null, "utf-8");
		
		SimpleFeatureIterator sfi = areaShp.getReader();
		
		while(sfi.hasNext()) {
			SimpleFeature sf = sfi.next();
			Geometry geom = (Geometry)sf.getDefaultGeometry();
			Oper oper = new Oper();
			oper.geo = geom;
			opers.add(oper);
		}
		
		sfi.close();
		areaShp.close();
		
		
		ShapeFile mappingShp = new ShapeFile(new File(mapping), null, "utf-8");
		
		sfi = mappingShp.getReader();
		
		CoordinateReferenceSystem srs = mappingShp.getSource().getBounds().getCoordinateReferenceSystem();
		
		for(Oper oper : opers) {
			oper.srs = srs;
		}
		
		Envelope mbr = new Envelope();
		
		while(sfi.hasNext()) {
			SimpleFeature sf = sfi.next();
			Geometry geom = (Geometry)sf.getDefaultGeometry();
			if(geom == null) {
				continue;
			}

			Coordinate[] coords = geom.getCoordinates();
			
			DirectPosition2D srcDP = new DirectPosition2D(srs, coords[0].x, coords[0].y);
			
			DirectPosition2D tgDP = new DirectPosition2D(srs, coords[coords.length-1].x, coords[coords.length-1].y);
			
			MappedPosition mp = new MappedPosition(srcDP, tgDP);
			
			
			Point pt = gf.createPoint(coords[0]);
			
			Oper selectOper = null;
			
			for(Oper oper : opers) {
				if(oper.geo != null && oper.geo.contains(pt)) {
					selectOper = oper;
				}
			}
			
			if(selectOper != null) {
				selectOper.vectors.add(mp);
			}
			else {
				Oper oper = opers.get(0);
				oper.vectors.add(mp);
			}
			//mbr.expandToInclude(geom.getEnvelopeInternal());
		}
		
		sfi.close();
		mappingShp.close();
		
		ShapeFile srcShp = new ShapeFile(new File(input), null, inputChar);
		
		//mbr = srcShp.getSource().getBounds();
		
		mbr.expandToInclude(srcShp.getSource().getBounds());
		
		List<DirectPosition> vertices = new Vector();
		
		mbr.expandBy(1000);
		
		Oper alloper = opers.get(0);
		alloper.env = mbr;
		
		int idx = 0;
		for(Oper oper : opers) {
			oper.set();
			
			if(oper.env != null) {
				System.out.println("Area "+idx+", mapping size = " + oper.vectors.size());
			}
			else {
				System.out.println("Area "+idx+", mapping size = " + oper.vectors.size());
			}
			idx++;
		}
		
		
//		Transformation  tf = rsb.getTransformation();
		
//		MathTransform mtf = rsb.getMathTransform();
		
		
		ShapeFile dstShp = new ShapeFile(new File(output), srcShp.getSource().getSchema(), outputChar);		
		
		
		
		sfi = srcShp.getReader();
		
		int cnt = 0;
		while(sfi.hasNext()) {
			SimpleFeature sf = sfi.next();
			cnt++;
		}		
		
		sfi.close();
		
		System.out.println("total size = " + cnt);
		
		double intervalSize = cnt/10;
		
		double nowCnt = 0;
		
		sfi = srcShp.getReader();
		
		while(sfi.hasNext()) {
			SimpleFeature sf = sfi.next();
			
			Geometry srcGeo = (Geometry)sf.getDefaultGeometry();
			
//			Coordinate[] coords = srcGeo.getCoordinates();
//			
//			for(Coordinate coord : coords) {
//				
//				DirectPosition2D srcdp = new DirectPosition2D(srs, coord.getX() , coord.getY());
//				DirectPosition2D dstdp = new DirectPosition2D(srs, coord.getX() , coord.getY());
//				
//				mtf.transform(srcdp, dstdp);
//				
//				System.out.println("srcDP=" + srcdp.toString()+", dstDP="+dstdp.toString());
//			}
			
			MathTransform mtf = null;
			
			Oper selectOper = null;
			
			for(Oper oper : opers) {
				//if(oper.geo != null && oper.geo.contains(srcGeo)) {
				if(oper.geo != null && oper.geo.intersects(srcGeo)) {
					selectOper = oper;
				}
			}
			
			RubberSheetBuilder rsb = null;
			
			if(selectOper != null) {
				mtf = selectOper.rsb.getMathTransform();
				rsb = selectOper.rsb;
			}
			else {
				Oper oper = opers.get(0);
				if(oper.rsb != null) {
					mtf = oper.rsb.getMathTransform();
					rsb = oper.rsb;
				}
			}
			
			if(rsb != null) {
				
				Geometry convGeo = JTS.transform(srcGeo, mtf);
				
				sf.setDefaultGeometry(convGeo);
			}
			
			dstShp.addFeature(sf);
			nowCnt++;
			if(nowCnt%intervalSize == 0) {
				System.out.println("작업="+cnt+"/"+nowCnt+", "+ Math.ceil(((float)nowCnt/(float)cnt)*100.0)+" % ");	
			}
			
		}
		
		sfi.close();
		
		srcShp.close();
		dstShp.close();
		
	}
	
	class Oper {
		Geometry geo;
		Envelope env;
		List<MappedPosition> vectors = new Vector();
		CoordinateReferenceSystem srs;
		
		RubberSheetBuilder rsb;
		
		void set() {
			
			if(vectors.size() == 0) {
				return;
			}
			Envelope mbr = null;
			
			if(this.geo != null) {
				mbr = this.geo.getEnvelopeInternal();
			}
			else if(this.env != null) {
				mbr = this.env;
			}
			
			mbr.init(818764, 1241743, 1433029, 2070751);
			
			System.out.println("작업 영역="+mbr.toString());
			
			Vector<MappedPosition> temps = new Vector();
			
			for(MappedPosition mp : vectors) {
				DirectPosition sc = mp.getSource();
				double[] cds = sc.getCoordinate();
				
				if(!mbr.contains(cds[0], cds[1])) {
					continue;
				}
				
				DirectPosition tg = mp.getTarget();	
				cds = tg.getCoordinate();
				
				if(!mbr.contains(cds[0], cds[1])) {
					continue;
				}
				temps.add(mp);
			}
			
			vectors.clear();
			vectors.addAll(temps);
			
			
			List<DirectPosition> vertices = new Vector();
			
			mbr.expandBy(1000);
			
			DirectPosition2D lb = new DirectPosition2D(srs, mbr.getMinX() , mbr.getMinY());
			DirectPosition2D rb = new DirectPosition2D(srs, mbr.getMaxX(), mbr.getMinY());
			
			DirectPosition2D rt = new DirectPosition2D(srs, mbr.getMaxX(), mbr.getMaxY());
			DirectPosition2D lt = new DirectPosition2D(srs, mbr.getMinX(), mbr.getMaxY());
			
			vertices.add(lb);
			vertices.add(rb);
			vertices.add(rt);
			vertices.add(lt);
			
			rsb = new RubberSheetBuilder(vectors, vertices);

			
		}
	}

}
