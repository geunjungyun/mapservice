import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.text.cql2.CQL;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

public class AnalysisArea {
	
	GeometryFactory gft = new GeometryFactory();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		AnalysisArea aa = new AnalysisArea();
		
		try {
			aa.run(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run(String[] args) throws Exception{
		
		
		long st = System.currentTimeMillis();
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
		
		String saveLayerName = args[0];
		File saveLayerFile = new File(saveLayerName);
		System.out.println(format.format(new Date(System.currentTimeMillis()))+","+"저장 = " + saveLayerFile.getAbsolutePath());
		String gridLayerName = args[1];
		File gridLayerFile = new File(gridLayerName);
		System.out.println(format.format(new Date(System.currentTimeMillis()))+","+"그리드 = " + gridLayerFile.getAbsolutePath());
		String jiguLayerName = args[2];
		File jiguLayerFile = new File(jiguLayerName);
		System.out.println(format.format(new Date(System.currentTimeMillis()))+","+"분석 = " + jiguLayerFile.getAbsolutePath());
		String fieldName = args[3];
		System.out.println(format.format(new Date(System.currentTimeMillis()))+","+"분석 컬럼 = " + fieldName);
		Vector<Field> fds = new Vector();
		
		
		
		for(int i=4 ; i<args.length; i++) {
			
			String value = args[i];
			
			System.out.println(format.format(new Date(System.currentTimeMillis()))+","+"분석 값 = " + value);
			
			String[] value1 = value.split("=");
			
			String addFieldName = value1[0];
			String valuesS = value1[1];
			
			String[] selFieldNames = null;
			String[] selF = valuesS.split("#");
			
			if(selF.length > 1) {
				valuesS = selF[0];
				
				String sfn = selF[1];
				
				String[] sfns  = sfn.split(",");
				
				selFieldNames = sfns;
				
			}
			
			String[] values  = valuesS.split(",");
			Field fd = new Field();
			fd.addFieldName = addFieldName;
			fd.values = values;
			fd.selFieldName = selFieldNames;
			fds.add(fd);
		}
		
		//String layerName = FilenameUtils.getBaseName(gridLayerFile.getAbsolutePath());

		Map<String, Serializable> gridshpparams = new HashMap<String, Serializable>();
		gridshpparams.put("url", gridLayerFile.toURI().toURL());
		gridshpparams.put("charset", "utf-8");
		DataStore gridDataStore = DataStoreFinder.getDataStore(gridshpparams);
		String gridName = gridDataStore.getTypeNames()[0];
		SimpleFeatureType gridFeatureType = gridDataStore.getSchema(gridName);
		
		SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();
		sftb.setName(gridName);
		for(int i=0; i<gridFeatureType.getAttributeCount(); i++) {
			sftb.add(gridFeatureType.getDescriptor(i));
		}
		
		for(Field fd : fds) {
			AttributeTypeBuilder build = new AttributeTypeBuilder();
			build.setNillable(true);
			build.setBinding(Float.class);
			sftb.add(build.buildDescriptor(fd.addFieldName));
		}
		
		AttributeTypeBuilder gridAll = new AttributeTypeBuilder();
		gridAll.setNillable(true);
		gridAll.setBinding(Float.class);
		
		AttributeTypeBuilder buildAll = new AttributeTypeBuilder();
		buildAll.setNillable(true);
		buildAll.setBinding(Float.class);

		sftb.add(gridAll.buildDescriptor("grid_all"));
		
		sftb.add(buildAll.buildDescriptor("build_all"));
		
		//sftb.setCRS(gridFeatureType.getCoordinateReferenceSystem());
		sftb.setSRS("EPSG:5174");
		
		SimpleFeatureType saveFeatureType = sftb.buildFeatureType();
		
		SimpleFeatureBuilder savesfb = new SimpleFeatureBuilder(saveFeatureType);
		
		Map<String, Serializable> jiguShpparams = new HashMap<String, Serializable>();
		jiguShpparams.put("url", jiguLayerFile.toURI().toURL());
		jiguShpparams.put("charset", "utf-8");
		DataStore jiguDataStore = DataStoreFinder.getDataStore(jiguShpparams);
		String jiguName = jiguDataStore.getTypeNames()[0];
		SimpleFeatureType jiguFeatureType = jiguDataStore.getSchema(jiguName);
		
		//BBOX(the_geom, 110, -45, 155, -10)
		
		ShapefileDataStoreFactory savedataStoreFactory = new ShapefileDataStoreFactory();
		
		Transaction saveTransaction = null;
		SimpleFeatureStore saveFeatureStore = null;

		Map<String, Serializable> saveParams = new HashMap<String, Serializable>();
		saveParams.put("url", saveLayerFile.toURI().toURL());
		saveParams.put("create spatial index", Boolean.TRUE);

		ShapefileDataStore saveDataStore = (ShapefileDataStore) savedataStoreFactory
				.createNewDataStore(saveParams);
		saveDataStore.setCharset(Charset.forName("utf-8"));
		saveDataStore.createSchema(saveFeatureType);

		SimpleFeatureType savesft = saveDataStore.getSchema();

		saveTransaction = new DefaultTransaction("create");
		String typeName = saveDataStore.getTypeNames()[0];
		saveFeatureStore = (SimpleFeatureStore) saveDataStore.getFeatureSource(typeName);
		saveFeatureStore.setTransaction(saveTransaction);
		
		ListFeatureCollection tempSavefc = new ListFeatureCollection(savesft);
		
		//Query gridQuery = new Query(gridName);
		SimpleFeatureSource gridFeatureSource = gridDataStore.getFeatureSource(gridName);
		SimpleFeatureCollection gridCollection = gridFeatureSource.getFeatures();
		
		int gridSize = gridCollection.size();
		
		int interval = gridSize/10;
		
		SimpleFeatureIterator gridReader = gridCollection.features();
		
		int idx = 0;
		while(gridReader.hasNext()) {
			
			for(Field fd : fds) {
				fd.area = 0;
			}
			
			SimpleFeature gridFeature = gridReader.next();
			Geometry gridGeo = (Geometry) gridFeature.getDefaultGeometry();
			
			Envelope env = gridGeo.getEnvelopeInternal();
			
			Filter filter = CQL.toFilter("BBOX(the_geom,"+env.getMinX()+","+env.getMinY()+","+env.getMaxX()+","+env.getMaxY()+")");
			
			Query query = new Query(jiguName, filter);
			
			FeatureReader<SimpleFeatureType, SimpleFeature> jiguReader = jiguDataStore.getFeatureReader(query, Transaction.AUTO_COMMIT);
			
			double area = 0;
			
			while(jiguReader.hasNext()) {
				SimpleFeature jiguFeature = jiguReader.next();
				
				Geometry jiguGeo = (Geometry)jiguFeature.getDefaultGeometry();
				
				Geometry intersactions = null;
				
				try {
					intersactions = jiguGeo.intersection(gridGeo);
				}
				catch(Exception e) {
					jiguGeo = this.getPolygon2(jiguGeo.buffer(0.00001));
				}
				
				intersactions = this.getPolygon2(intersactions);
				
				if(intersactions != null && intersactions.isEmpty() == false) {
					
					double interArea = intersactions.getArea();
					area += interArea;
					
					String fieldValue = (String)jiguFeature.getAttribute(fieldName);
					for(Field fd : fds) {
						if(isCheck(fieldValue.trim(), fd.values)) {
							
							
							
							if(fd.selFieldName != null && fd.selFieldName.length > 0) {
								
								for(String sfName : fd.selFieldName) {
									Object obj = jiguFeature.getAttribute(sfName);
									if(obj instanceof String) {
										Double fdv = Double.parseDouble((String)obj);
										fd.area += fdv;
									}
									else if(obj instanceof Byte) {
										Byte fdv = (Byte)obj;
										fd.area += fdv;
									}
									else if(obj instanceof Integer) {
										Integer fdv = (Integer)obj;
										fd.area += fdv;
									}
									else if(obj instanceof Short) {
										Short fdv = (Short)obj;
										fd.area += fdv;
									}
									else if(obj instanceof Float) {
										Float fdv = (Float)obj;
										fd.area += fdv;
									}
									else if(obj instanceof Double) {
										Double fdv = (Double)obj;
										fd.area += fdv;
									}
									else if(obj instanceof Long) {
										Long fdv = (Long)obj;
										fd.area += fdv;
									}
									else if(obj instanceof Byte) {
										Byte fdv = (Byte)obj;
										fd.area += fdv;
									}
									else if(obj instanceof BigDecimal) {
										BigDecimal fdv = (BigDecimal)obj;
										fd.area += fdv.doubleValue();
									}
								}
								
								//fd.area += obj;
							}
							else {
								fd.area += interArea;
							}
						}
					}
				}
			}
			
			jiguReader.close();
			
			SimpleFeature saveFeature = SimpleFeatureBuilder.retype(gridFeature, savesft);
			
			for(Field fd : fds) {
				saveFeature.setAttribute(fd.addFieldName, fd.area);
			}
			
			saveFeature.setAttribute("build_all", area);
			saveFeature.setAttribute("grid_all", gridGeo.getArea());
			
			tempSavefc.add(saveFeature);
			
    		if(tempSavefc.size() > 1000) {
                saveFeatureStore.addFeatures(tempSavefc);
                saveTransaction.commit();
                tempSavefc.clear();
      		}
    		idx ++;
    		
    		int percent = idx/interval;
    		int rest = idx%interval;
    		
    		if(rest == 0) {
    			System.out.println(percent + "0 %");
    		}
		}
		
		saveFeatureStore.addFeatures(tempSavefc);
		saveTransaction.commit();
		tempSavefc.clear();
		
        saveTransaction.close();
        saveDataStore.dispose();
        
        gridDataStore.dispose();
        jiguDataStore.dispose();
        
        String grid = gridLayerFile.getAbsolutePath().replace(".shp", ".prj");
        
        String save = saveLayerFile.getAbsolutePath().replace(".shp", ".prj");
        
        FileUtils.copyFile(new File(grid), new File(save));
        
        
        System.out.println(format.format(new Date(System.currentTimeMillis()))+
        		","+"소요시간  = " + String.format("%.5f",(System.currentTimeMillis()-st)/1000.0/60.0) +" min") ;
	}
	
	public boolean isCheck(String ori, String[] dsts) {
		
		boolean ret = false;
		
		if(ori == null || ori.trim().length() == 0) {
			return false;
		}
		
		for(int k=0; k<dsts.length; k++) {
			
			String dst = dsts[k].trim();
			
			if(dst.length() == ori.length()) {
				if(dst.equals(ori)) {
					ret = true;
					break;
				}
			}
			else {
				
				boolean ret2 = false;
				for(int i=0; i<dst.length(); i++) {
					char ca = dst.charAt(i);
					if(ori.indexOf(ca) > -1){
						ret2 = true;
						break;
					}
				}
				if(ret2) {
					break;
				}
			}
			
		}
		
		return ret;
	}
	
	/*
	public boolean isCheck(String ori, String[] dsts) {
		
		boolean ret = true;
		
		if(ori == null || ori.trim().length() == 0) {
			return false;
		}
		
		for(int k=0; k<dsts.length; k++) {
			
			String dst = dsts[k].trim();
			
			boolean ok = true;
			for(int i=0; i<dst.length(); i++) {
				char ca = dst.charAt(i);
				if(ori.indexOf(ca) == -1){
					ok = false;
					break;
				}
			}
			
			if(ok == false) {
				ret = ok;
				break;
			}
			
		}
		
		return ret;
	}
	*/
	
	public Geometry getPolygon2(Geometry geo) {

		// double lenArea = 2.2;

		if (geo instanceof Polygon || geo instanceof MultiPolygon) {
			return geo;
		} else if (geo instanceof GeometryCollection) {
			Vector<Polygon> newps = new Vector();
			GeometryCollection geoc = (GeometryCollection) geo;
			int cnt = geoc.getNumGeometries();
			for (int i = 0; i < cnt; i++) {
				Geometry subGeo = geoc.getGeometryN(i);
				if (subGeo instanceof Polygon) {
					newps.add((Polygon) subGeo);
				} else if (subGeo instanceof MultiPolygon) {
					int mcnt = subGeo.getNumGeometries();
					for (int k = 0; k < mcnt; k++) {
						Geometry subPg = subGeo.getGeometryN(k);
						newps.add((Polygon) subPg);
					}
				}
			}

			Polygon[] pls = new Polygon[newps.size()];
			for (int m = 0; m < newps.size(); m++) {
				pls[m] = newps.get(m);
			}
			return gft.createMultiPolygon(pls);

		}
		return null;
	}
	
	public class Field{
		public String addFieldName = "";
		public String[] values = null;
		
		public double area = -1;
		
		public String[] selFieldName = null;
		public Field(){
			
		}
		

	}

}
