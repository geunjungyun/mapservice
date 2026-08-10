import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class AnalysisTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		AnalysisTest at = new AnalysisTest();
		
			at.run(args[0]);

		
	}
	
	public void run(String _path) {
		String path = _path;
		String allPath = path+"/all";
		String sggPath = path+"/sgg";
		String umdPath = path+"/umd";
		
		File sggFile = new File(sggPath);
		File allFile = new File(allPath);
		File umdFile = new File(umdPath);
		
		File[] sggFiles = sggFile.listFiles();
		File[] allFiles = allFile.listFiles();
		File[] umdFiles = umdFile.listFiles();
		
		Vector<Value> values = new Vector();
		
		for(File sggF : sggFiles) {
			long sggCnt = 0;
			long allCnt = 0;
			long umdCnt = 0;

			try {
				sggCnt = this.readShape(sggF);
				
				if(sggCnt > 0) {
					File selAllFile = this.getSelectShape(sggF.getName(), allFiles);
					allCnt = this.readShape(selAllFile);
					File selUmdFile = this.getSelectShape(sggF.getName(), umdFiles);
					umdCnt = this.readShape(selUmdFile);
					Value value = new Value();
					value.name = sggF.getName();
					value.allCnt = allCnt;
					value.sggCnt = sggCnt;
					value.umdCnt = umdCnt;
					values.add(value);
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}

			
		}
		int idx = 1;
		for(Value value : values) {
			System.out.println(idx++ +","+value.name+", all="+value.allCnt+", sgg="+value.sggCnt+", umd="+value.umdCnt);
		}
	}
	
	public File getSelectShape(String name, File[] files) {
		File selFile = null;
		
		for(File file : files) {
			if(name.equals(file.getName())) {
				selFile = file;
				break;
			}
		}
		
		return selFile;
	}
	
	public long readShape(File file) throws Exception {
		long pnuCnt = 0;
		File shp = null;
		
		File[] files = file.listFiles();
		
		for(File temp : files) {
			String filePath = temp.getAbsolutePath();
			if(filePath.endsWith("shp")) {
				shp = temp;
			}
		}
		
		Map<String, Serializable> gridshpparams = new HashMap<String, Serializable>();
		gridshpparams.put("url", shp.toURI().toURL());
		gridshpparams.put("charset", "utf-8");
		DataStore gridDataStore = DataStoreFinder.getDataStore(gridshpparams);
		String gridName = gridDataStore.getTypeNames()[0];
		SimpleFeatureType gridFeatureType = gridDataStore.getSchema(gridName);
		
		SimpleFeatureSource gridFeatureSource = gridDataStore.getFeatureSource(gridName);
		SimpleFeatureCollection gridCollection = gridFeatureSource.getFeatures();
		
		int gridSize = gridCollection.size();
		
		int interval = gridSize/10;
		
		SimpleFeatureIterator gridReader = gridCollection.features();
		
		int idx = 0;
		while(gridReader.hasNext()) {
			SimpleFeature gridFeature = gridReader.next();
			int cnt = (int) gridFeature.getAttribute("pnusCnt");
			pnuCnt+=cnt;
		}
		gridDataStore.dispose();
		
		return pnuCnt;
	}
	
	public class Value{
		String name = "";
		long allCnt = 0;
		long sggCnt = 0;
		long umdCnt = 0;
	}
}
