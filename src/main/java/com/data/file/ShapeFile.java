package com.data.file;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class ShapeFile {

	ShapefileDataStore newDataStore;

	SimpleFeatureType objSft;

	Transaction createTransaction;
	
	SimpleFeatureSource featureSource;
	
	ListFeatureCollection fc;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		ShapeFile shp = new ShapeFile(new File("F:\\down\\F0010000.shp"), null, "utf-8");
		
		try {
			SimpleFeatureIterator sfi = shp.getReader();
			while(sfi.hasNext()) {
				SimpleFeature sf = sfi.next();
				System.out.println("sf="+sf.toString());
			}
			sfi.close();
			
			shp.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ShapeFile(Map<String, Serializable> params, SimpleFeatureType objSft) {
		//ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
		try {
			this.init(params, objSft);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ShapeFile(File newFile, SimpleFeatureType objSft, String charset) {
		//ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
		try {
			
			File path = newFile.getParentFile();
			
			if(!path.exists()) {
				path.mkdirs();
			}
			
			Map<String, Serializable> params = new HashMap<String, Serializable>();
			params.put("url", newFile.toURI().toURL());
			//params.put("create spatial index", Boolean.TRUE);
			params.put("charset", charset);

			this.init(params, objSft);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void init(Map<String, Serializable> params, SimpleFeatureType objSft) throws Exception {

		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

		newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
		
		if(objSft != null) {
			//newDataStore.setCharset(Charset.forName("utf-8"));
			newDataStore.createSchema(objSft);
		}
		
		//this.objSft = objSft;
		
		
		
		String typeName = newDataStore.getTypeNames()[0];
		
		featureSource  = newDataStore.getFeatureSource(typeName);
		
		
		
		this.objSft = featureSource.getSchema();
		
	}
	
	public SimpleFeatureSource getSource() {
		return this.featureSource;
	}

	public synchronized void addFeature(SimpleFeature sf) throws Exception {
		
		SimpleFeatureStore featureStore = null;
		if (featureSource instanceof SimpleFeatureStore) {
			featureStore = (SimpleFeatureStore) featureSource;
		}
		
		if (this.createTransaction == null) {
			this.createTransaction = new DefaultTransaction("create");
			featureStore.setTransaction(createTransaction);
			
			fc = new ListFeatureCollection(objSft);
		}
		
		fc.add(sf);

		if (fc.size() > 1000) {
			featureStore.addFeatures(fc);
			createTransaction.commit();
			fc.clear();
		}

	}
	
	public SimpleFeatureIterator getReader() throws Exception {
		
		SimpleFeatureCollection collection = this.featureSource.getFeatures();

		return collection.features();
	}

	public void close() throws Exception {
		
		SimpleFeatureStore featureStore = null;
		if (featureSource instanceof SimpleFeatureStore) {
			featureStore = (SimpleFeatureStore) featureSource;
		}
		
		if(fc != null && fc.size() > 0) {
			featureStore.addFeatures(fc);
			fc.clear();
		}
		
		if(createTransaction != null) {
			createTransaction.commit();
			createTransaction.close();
		}
		
		newDataStore.dispose();
	}
}
