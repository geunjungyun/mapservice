import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.io.FilenameUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.data.file.FileLayer;
import com.util.io.FileUt;

public class LayertoShape {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String srcPath = "D:\\workspace\\MapPlan\\temp";
		String dstPath = "D:\\workspace\\MapPlan\\shp\\";
		
		ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
		
		File srcPathFile = new File(srcPath);
		
		File[] shps = srcPathFile.listFiles();
		
		Vector<Future> fts = new Vector();

		for (File file : shps) {
			
//			if(file.getAbsolutePath().indexOf("04.철도") == -1) {
//				continue;
//			}

			Object lock = new Object();

			final File select = file;

			Future ft = executorService.submit(() -> {

				try {
					
					LayertoShape.copyFreelayerToShape(file.getAbsolutePath(), dstPath+FileUt.SEPERATOR+file.getName()+".shp");
					
				} catch (Exception ex) {
					//LOG.debug("error layer=" + select.getAbsolutePath() + ", " + ex.toString() + ", idx=" + idx, ex);
					throw new Exception(ex);
				}
				return 1;
			});
			fts.add(ft);

		}

		for (Future ft : fts) {
			try {
				ft.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		executorService.shutdown();

		
		
	}
	
	
	public static void copyFreelayerToShape(String srcFile, String dstFile) throws Exception{
		
		
		FileLayer fl = new FileLayer(srcFile, null);
		
		
        File newFile = new File(dstFile);

        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("url", newFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        newDataStore.setCharset(Charset.forName("utf-8"));
        //newDataStore.setStringCharset(Charset.forName("UTF-8"));
        //SimpleFeatureType sft =  fl.getSimpleFeatureType();
        
        FileLayer.SimpleFeatureIterator sfi = fl.iterator();
        
        SimpleFeatureType objSft = null;
        SimpleFeature firstSf = null;
    	while(sfi.hasNext()) {
    		firstSf = sfi.next();
    		objSft = firstSf.getFeatureType();
    		break;
    	}
    	
    	SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();
    	sftb.setName(newFile.getName());
    	for(int i=0; i<objSft.getAttributeCount(); i++) {
    		AttributeDescriptor ad = objSft.getDescriptor(i);
    		AttributeType at = ad.getType();
    		System.out.println(ad.getName()+"."+at.getBinding().getName());
    		if(ad.getType() instanceof GeometryType) {
    			
    			Geometry geo = (Geometry)firstSf.getDefaultGeometry();
    			
    			Class cl = null;
    			if(geo instanceof Point || geo instanceof MultiPoint) {
    				cl = MultiPoint.class;
    			}
    			else if(geo instanceof LineString || geo instanceof MultiLineString) {
    				cl = MultiLineString.class;
    			}
    			else if(geo instanceof MultiPolygon || geo instanceof Polygon){
    				cl = MultiPolygon.class;
    			}
    			
    			
    			GeometryType geomType = (GeometryType) ad.getType();
                GeometryTypeImpl gt = new GeometryTypeImpl(
                        new NameImpl("the_geom"), cl,
                        geomType.getCoordinateReferenceSystem(),
                        geomType.isIdentified(), geomType.isAbstract(),
                        geomType.getRestrictions(), geomType.getSuper(),
                        geomType.getDescription());

                GeometryDescriptor geomDesc = new GeometryDescriptorImpl(
                        gt, new NameImpl("the_geom"),
                        ad.getMinOccurs(), ad.getMaxOccurs(),
                        ad.isNillable(), ad.getDefaultValue());
                
                sftb.add(geomDesc);
    		}
    		else {
    			sftb.add(ad);
    		}
    	}
    	
    	objSft = sftb.buildFeatureType();
    	
        newDataStore.createSchema(objSft);
        
        SimpleFeatureType savesft = newDataStore.getSchema();
        
        SimpleFeatureBuilder savesfb = new SimpleFeatureBuilder(savesft);
        
        Transaction transaction = new DefaultTransaction("create");

        String typeName = newDataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);

        if (featureSource instanceof SimpleFeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;

            featureStore.setTransaction(transaction);
            try {
            	savesfb.reset();
            	ListFeatureCollection fc = new ListFeatureCollection(objSft);
            	
            	//shape file  저장 시 geometry 컬럼의 이름은 the_geom 으로 고정됨.
            	if(firstSf != null) {
            		
               		for(int j=0; j<firstSf.getAttributeCount(); j++) {
            			savesfb.add(firstSf.getAttribute(j));
            		}
            		
               		firstSf = savesfb.buildFeature(firstSf.getID());
 
            		
            		fc.add(firstSf);
            	}
            	
            	while(sfi.hasNext()) {
            		SimpleFeature sf = sfi.next();
            		
            		//sf = DataUtilities.reType(savesft, sf);
            		
            		for(int j=0; j<sf.getAttributeCount(); j++) {
            			savesfb.add(sf.getAttribute(j));
            		}
            		
            		sf = savesfb.buildFeature(sf.getID());
            		
            		fc.add(sf);
            		
            		if(fc.size() > 1000) {
                      featureStore.addFeatures(fc);
                      transaction.commit();
                      fc.clear();
            		}
            	}
            	
                featureStore.addFeatures(fc);
                transaction.commit();
                fc.clear();

            } catch (Exception problem) {
                problem.printStackTrace();
                transaction.rollback();

            } finally {
                transaction.close();
                newDataStore.dispose();
            }
            //System.exit(0); // success!
        } else {
            System.out.println(typeName + " does not support read/write access");
            //System.exit(1);
        }
	}


}
