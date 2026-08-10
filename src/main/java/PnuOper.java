import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.data.file.FileLayer;
import com.data.file.ShapeFile;

public class PnuOper {
	
	
	String layerPath = "D:\\coops\\2024\\layers";
	String dstPath = "";
	String listFile = "";
	
	Vector<FileLayer> lys = new Vector();
	
	FileLayer fa = null;
	
	String pnu = null;
	
	String GS_ORG_ID = "";
	String GS_YEAR = "";
	String GS_NO = "";
	String UCODE = "";
	String JIJUNG_YN = "";
	String CONFIRM_YN = "";
	String FINAL_YN = "";
	int ROUND_M = 0;
	String TYPE = "";
	
	String charset = "UTF-8";
	
	CoordinateReferenceSystem crs = null;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long st = System.currentTimeMillis();
		PnuOper oper = new PnuOper(); 
		String input = null;
		if(args.length == 3) {
			oper.layerPath = args[0];
			oper.dstPath = args[1];
			input = args[2];
		}
		else {
			//D:\coops\2024\layers D:\coops\2024\output C,3611011900106540015_3611011900106440623_3611012000200430000_3611012000102010045,UBO100,3560000,2024,121,Y,Y,Y,50
			
			oper.layerPath = "D:\\coops\\2024\\tileset\\output\\20240715\\layers";
			oper.dstPath = "D:\\coops\\2024\\tileset\\pnu_output";
			//input = "A,3611011900106540015,*,3560000,2024,121,Y,Y,Y,0";
			
			//input = "A,4711312100115560005,*,3560000,2024,121,Y,Y,Y,0";
			//input = "A,3611011900106540015,UBO100_UFM200,3560000,2024,121,Y,Y,Y,0";
			
			input = "B,3611011900106540015,UBO100_UBO200_UBO300,3560000,2024,121,Y,Y,Y,50";
			//input = "C,3611011900106540015_3611011900106440623_3611012000200430000_3611012000102010045,UBO100,3560000,2024,121,Y,Y,Y,50";
			//input = "C,1168010800100860004_1168010800100860004_1168010800100860005_1168010800100860018_1168010800100860014_1168010800100860015,UBO100,3560000,2024,121,Y,Y,Y,50";
		}
		
		
		try {
			oper.readLayers();
			
			oper.run(input);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long ed = System.currentTimeMillis();
		System.out.println("time =  " + (ed -st)/1000.0+" 초");
	}
	
	public void run(String line) throws Exception{
		
		String[] args = line.split(",");
		
        if(line.startsWith("A") && args.length == 10) {
        	pnu = args[1];
        	UCODE = args[2];
        	GS_ORG_ID = args[3];
        	GS_YEAR = args[4];
        	GS_NO = args[5];
        	JIJUNG_YN = args[6];
        	CONFIRM_YN = args[7];
        	FINAL_YN = args[8];
        	ROUND_M = Integer.parseInt(args[9]);
        	TYPE = "A";
        	this.oper1();
        }
        else if(line.startsWith("B") && args.length == 10) {
        	pnu = args[1];
        	UCODE = args[2];
        	GS_ORG_ID = args[3];
        	GS_YEAR = args[4];
        	GS_NO = args[5];
        	JIJUNG_YN = args[6];
        	CONFIRM_YN = args[7];
        	FINAL_YN = args[8];
        	ROUND_M = Integer.parseInt(args[9]);
        	TYPE = "B";
        	this.oper2();
        }
        else if(line.startsWith("C") && args.length == 10) {
        	pnu = args[1];
        	UCODE = args[2];
        	GS_ORG_ID = args[3];
        	GS_YEAR = args[4];
        	GS_NO = args[5];
        	JIJUNG_YN = args[6];
        	CONFIRM_YN = args[7];
        	FINAL_YN = args[8];
        	ROUND_M = Integer.parseInt(args[9]);
        	TYPE = "C";
        	this.oper3();
        }
	}
	
	public void oper1() throws Exception{
		
		Vector<String> ucodes = new Vector();
		
		if(!this.UCODE.equals("*")) {
			//String[] codes = this.UCODE.split("\\|");
			String[] codes = this.UCODE.split("_");
			for(String code : codes) {
				ucodes.add(code);
			}
		}
		
		HashMap<String, Vector<SimpleFeature>> results = new HashMap();
		
		SimpleFeature pnuSf = this.fa.getFeature(pnu);
		
		if(pnuSf == null) {
			System.out.println("존재하지 않은 pnu = " + pnu);
			return;
		}
		
		Geometry geo = (Geometry)pnuSf.getDefaultGeometry();
		
		Envelope env = geo.getEnvelopeInternal();
		
		Vector<SimpleFeature> lists = this.getFeatures(geo, ucodes);
		
		for(SimpleFeature sf : lists) {
			System.out.println(sf.getType().getName().getLocalPart());
			String ucode = this.getUcode(sf);
			if(!results.containsKey(ucode)) {
				Vector<SimpleFeature> newL = new Vector();
				results.put(ucode, newL);
			}
			
			Vector<SimpleFeature> sfList = results.get(ucode);
			
			SimpleFeature saveSf = getPolygonFeature();
			
			saveSf.setDefaultGeometry(sf.getDefaultGeometry());
			
			saveSf.setAttribute("UCODE", ucode);
			
			sfList.add(saveSf);
		}
		
		//SimpleFeature pnuSaveSf = getPolygonFeature();
		SimpleFeature pnuSaveSf = this.getPnuPolygonFeature();
		
		pnuSaveSf.setDefaultGeometry(pnuSf.getDefaultGeometry());
		
		pnuSaveSf.setAttribute("UCODE", "");
		pnuSaveSf.setAttribute("PNU", pnu);
		
		Vector<SimpleFeature> pnus = new Vector();
		pnus.add(pnuSaveSf);
		
		this.savePnu(pnu, pnus ,pnu+".shp");
		
		this.saveUcode(null, results, null);
	}
	
	
	public void savePnu(String code, Vector<SimpleFeature> features, String fileName) throws Exception {
		
		if(features.size() == 0) {
			return;
		}
		String savePath = this.dstPath+File.separator+code;
		
		File path = new File(savePath);
		
		if(path.exists()) {
			FileUtils.deleteDirectory(path);
			//System.out.println("delete path " + path.getAbsolutePath());
		}
		
		path.mkdirs();
		
		ShapeFile sf = new ShapeFile(new File(savePath+File.separator+fileName), features.get(0).getFeatureType(), this.charset);
		for(SimpleFeature feature : features) {
			sf.addFeature(feature);
		}
		
		sf.close();
		
	}
	

	public void saveUcode(String code, HashMap<String, Vector<SimpleFeature>> features, String saveName) throws Exception {
		
		if(features.size() == 0) {
			return;
		}
		
		String savePath = "";
		
		if(code == null) {
			savePath = this.dstPath;
		}
		else {
			savePath = this.dstPath+File.separator+code;
		}
		
		
		File path = new File(savePath);
		
		path.mkdirs();
		
		Set set = features.keySet();
		
		Iterator it = set.iterator();
		
		while(it.hasNext()) {
			
			String ucode = (String) it.next();
			
			Vector<SimpleFeature> fts = features.get(ucode);
			
			this.savePnu(ucode, fts, saveName != null ? saveName : ucode+".shp");
		}
		
	}
	
	
	public SimpleFeature getPolygonFeature() {
		SimpleFeature saveSf = SimpleFeatureBuilder.template(this.getTypePolygon(), "1");
		
		saveSf.setAttribute("GS_ORG_ID", GS_ORG_ID);
		saveSf.setAttribute("GS_YEAR", GS_YEAR);
		saveSf.setAttribute("GS_NO", GS_NO);
		saveSf.setAttribute("UCODE", UCODE);
		saveSf.setAttribute("JIJUNG_YN", JIJUNG_YN);
		saveSf.setAttribute("CONFIRM_YN", CONFIRM_YN);
		saveSf.setAttribute("FINAL_YN", FINAL_YN);
		saveSf.setAttribute("ROUND_M", ROUND_M);
		saveSf.setAttribute("TYPE", TYPE);
		return saveSf;
	}
	
	public SimpleFeature getPnuPolygonFeature() {
		SimpleFeature saveSf = SimpleFeatureBuilder.template(this.getTypePnuPolygon(), "1");
		
		saveSf.setAttribute("GS_ORG_ID", GS_ORG_ID);
		saveSf.setAttribute("GS_YEAR", GS_YEAR);
		saveSf.setAttribute("GS_NO", GS_NO);
		saveSf.setAttribute("UCODE", UCODE);
		saveSf.setAttribute("JIJUNG_YN", JIJUNG_YN);
		saveSf.setAttribute("CONFIRM_YN", CONFIRM_YN);
		saveSf.setAttribute("FINAL_YN", FINAL_YN);
		saveSf.setAttribute("ROUND_M", ROUND_M);
		saveSf.setAttribute("TYPE", TYPE);
		return saveSf;
	}	
	
	public SimpleFeature getPointFeature() {
		SimpleFeature saveSf = SimpleFeatureBuilder.template(this.getTypePoint(), "1");
		
		saveSf.setAttribute("GS_ORG_ID", GS_ORG_ID);
		saveSf.setAttribute("GS_YEAR", GS_YEAR);
		saveSf.setAttribute("GS_NO", GS_NO);
		saveSf.setAttribute("UCODE", UCODE);
		saveSf.setAttribute("JIJUNG_YN", JIJUNG_YN);
		saveSf.setAttribute("CONFIRM_YN", CONFIRM_YN);
		saveSf.setAttribute("FINAL_YN", FINAL_YN);
		saveSf.setAttribute("ROUND_M", ROUND_M);
		saveSf.setAttribute("TYPE", TYPE);
		return saveSf;
	}

	
	public Vector<SimpleFeature> getFeatures(Geometry geo, Vector<String> ucodes) throws Exception{
		Vector<SimpleFeature> fts = new Vector();
		
		Envelope env = geo.getEnvelopeInternal();
		
		for(FileLayer fl : this.lys) {

			
			ListFeatureCollection sfc = (ListFeatureCollection) fl.getFeatures(env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY());
			SimpleFeatureIterator sfi = sfc.features();
			while(sfi.hasNext()) {
				SimpleFeature sf = sfi.next();
				
				boolean isUcode = true;
				
				if(ucodes.size() > 0) {
					
					isUcode = false;
					
					//String mnum = (String) sf.getAttribute("mnum");
					String mnum = this.getUcode(sf);
					
					for(String ucode : ucodes) {
						if(ucode.equals(mnum)) {
							isUcode = true;
							break;
						}
					}
				}
				
//				String mnum = (String) sf.getAttribute("mnum");
//				if(mnum == null) {
//					System.out.println(fl.getName()+", mnum is null");
//				}
				

				
				Geometry geom = (Geometry) sf.getDefaultGeometry();
				if(geom.intersects(geo) && isUcode) {
					//System.out.println(fl.getName()+", "+sf.toString());
					fts.add(sf);
				}
			}
		}
		
		return fts;
	}
	
	
	public String getUcode(SimpleFeature sf) {
		String ucode = null;
		if(sf.getType().indexOf("mnum") > -1) {
			ucode = (String) sf.getAttribute("mnum");
		}
		else if(sf.getType().indexOf("atrb_se") > -1) {
			ucode = (String) sf.getAttribute("atrb_se");
		}
		return ucode;
	}
	
	
	public void oper2() throws Exception{
		
		SimpleFeature pnuSf = this.fa.getFeature(pnu);
		
		if(pnuSf == null) {
			System.out.println("존재하지 않은 pnu = " + pnu);
			return;
		}
		
		Geometry geo = (Geometry)pnuSf.getDefaultGeometry();
		
		
		//SimpleFeature pnuSaveSf = getPolygonFeature();
		SimpleFeature pnuSaveSf = this.getPnuPolygonFeature();
		
		pnuSaveSf.setDefaultGeometry(pnuSf.getDefaultGeometry());
		
		pnuSaveSf.setAttribute("UCODE", "");
		pnuSaveSf.setAttribute("PNU", pnu);
		
		Vector<SimpleFeature> pnus = new Vector();
		
		pnus.add(pnuSaveSf);
		
		this.savePnu(pnu, pnus ,pnu+".shp");

		SimpleFeature ucodesf = this.getPointFeature();
		
		Point centor = geo.getCentroid();
		ucodesf.setDefaultGeometry(centor);
		
		
		String[] ucodeArray = this.UCODE.split("_");
		
		for(String ucd : ucodeArray) {
			
			ucodesf.setAttribute("UCODE", ucd);
			Vector<SimpleFeature> sfs = new Vector();
			sfs.add(ucodesf);
			
			this.savePnu(ucd, sfs ,ucd+".shp");
		}
	}
	
	public void oper3() throws Exception{
		Vector<String> pnus = new Vector();
		
		//String[] codes = this.pnu.split("\\|");
		String[] codes = this.pnu.split("_");
		for(String code : codes) {
			pnus.add(code);
			//System.out.println("add code = " + code);
		}
		
		Vector<SimpleFeature> sfs = new Vector();
		
		for(String pnu : pnus) {
			SimpleFeature pnuSf = this.fa.getFeature(pnu);
			
			//SimpleFeature pnuSaveSf = getPolygonFeature();
			SimpleFeature pnuSaveSf = this.getPnuPolygonFeature();
			
			if(pnuSf == null) {
				System.out.println("pnu is null ="+pnu);
				continue;
			}
			
			pnuSaveSf.setDefaultGeometry(pnuSf.getDefaultGeometry());
			pnuSaveSf.setAttribute("PNU", pnu);
			
			sfs.add(pnuSaveSf);
		}
		
		Geometry geo = JobUtil.getGeometry(sfs, 0.5);
		
		Geometry geov = JobUtil.getSimplePolygonm(geo, 10);
		
		//this.savePnu(pnu, sfs ,"pnu.shp");
		this.savePnu(codes[0], sfs ,codes[0]+".shp");
		
		SimpleFeature ucodeSaveSf = getPolygonFeature();
		ucodeSaveSf.setDefaultGeometry(geov);
		
		Vector<SimpleFeature> ucodesf = new Vector();
		ucodesf.add(ucodeSaveSf);
		
		
		this.savePnu(UCODE, ucodesf ,UCODE+".shp");
	}
	
	public void readLayers() throws Exception{
		long st = System.currentTimeMillis();
		File path = new File(this.layerPath);
		File[] layerList = path.listFiles();
		for(File file : layerList) {
			if(file.isDirectory() && (file.getName().startsWith("a") || 
					file.getName().startsWith("b") || file.getName().startsWith("c") ||
					file.getName().startsWith("d") || file.getName().startsWith("e"))) {
				FileLayer ly = new FileLayer(file.getAbsolutePath(), null);
				this.lys.add(ly);
			}
			
			if(file.getName().equals("fa")) {
				this.fa = new FileLayer(file.getAbsolutePath(), null);
				
				this.crs = fa.getSimpleFeatureType().getCoordinateReferenceSystem();
			}
		}
		long ed = System.currentTimeMillis();
		
		System.out.println("layers size ="+this.lys.size()+", fa size = " + this.fa.getObjectSize() );
	}
	


//	sftb.add("GS_ORG_ID",String.class, 7);
//	sftb.add("GS_YEAR",String.class, 4);
//	sftb.add("GS_NO",String.class, 8);
//	sftb.add("UCODE",String.class, 6);
//	sftb.add("JIJUNG_YN",String.class, 1);
//	sftb.add("CONFIRM_YN",String.class, 1);
//	sftb.add("FINAL_YN",String.class, 1);
	
	public SimpleFeatureType getTypePolygon() {
		SimpleFeatureType sft = null;
		
		SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();
		
		sftb.setName("pnu");
		sftb.setCRS(this.crs);
		sftb.add("the_geom", MultiPolygon.class);
		sftb.length(7).add("GS_ORG_ID",String.class);
		sftb.length(4).add("GS_YEAR",String.class);
		sftb.length(8).add("GS_NO",String.class);
		sftb.length(6).add("UCODE",String.class);
		sftb.length(1).add("JIJUNG_YN",String.class);
		sftb.length(1).add("CONFIRM_YN",String.class);
		sftb.length(1).add("FINAL_YN",String.class);
		sftb.add("ROUND_M",Integer.class);
		sftb.length(1).add("TYPE",String.class);
		
		sft = sftb.buildFeatureType();
		
		return sft;
	}
	
	
	public SimpleFeatureType getTypePnuPolygon() {
		SimpleFeatureType sft = null;
		
		SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();
		
		sftb.setName("pnu");
		sftb.setCRS(this.crs);
		sftb.add("the_geom", MultiPolygon.class);
		sftb.length(7).add("GS_ORG_ID",String.class);
		sftb.length(4).add("GS_YEAR",String.class);
		sftb.length(8).add("GS_NO",String.class);
		sftb.length(6).add("UCODE",String.class);
		sftb.length(1).add("JIJUNG_YN",String.class);
		sftb.length(1).add("CONFIRM_YN",String.class);
		sftb.length(1).add("FINAL_YN",String.class);
		sftb.add("ROUND_M",Integer.class);
		sftb.length(1).add("TYPE",String.class);
		sftb.length(19).add("PNU",String.class);
		
		sft = sftb.buildFeatureType();
		
		return sft;
	}	
	public SimpleFeatureType getTypePoint() {
		SimpleFeatureType sft = null;
		
		SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();
		
		sftb.setName("pnu");
		
		sftb.setCRS(this.crs);
		sftb.add("the_geom", Point.class);
		sftb.length(7).add("GS_ORG_ID",String.class);
		sftb.length(4).add("GS_YEAR",String.class);
		sftb.length(8).add("GS_NO",String.class);
		sftb.length(6).add("UCODE",String.class);
		sftb.length(1).add("JIJUNG_YN",String.class);
		sftb.length(1).add("CONFIRM_YN",String.class);
		sftb.length(1).add("FINAL_YN",String.class);
		sftb.add("ROUND_M",Integer.class);
		sftb.length(1).add("TYPE",String.class);
		
		sft = sftb.buildFeatureType();
		
		return sft;
	}
	
}
