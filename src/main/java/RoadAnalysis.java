import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Vector;

import org.geotools.data.collection.ListFeatureCollection;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;

import com.data.file.FileLayer;
import com.data.file.FileLayer.SimpleFeatureIterator;
import com.gis2.servlet.MapService;
import com.gis2.storage.TileServiceMng;
import com.gis2.storage.Version;

public class RoadAnalysis {
	 
	String savePath = null;
	String tileSet = null;
	String roadInfos = null;
	double snap = 0;
	String pnu = null;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String tileSet = args[0];
		String snap = args[1];
		String roadInfos = args[2];
		String saveText = args[3];
		String pnu = null;
		
		if(args.length == 5) {
			pnu = args[4];
		}
		
		RoadAnalysis ra = new RoadAnalysis();
		ra.savePath = saveText;
		
		if(!roadInfos.equals("null")) {
			ra.roadInfos = roadInfos;
		}
		
		ra.tileSet = tileSet;
		ra.snap = Double.parseDouble(snap);
		ra.pnu = pnu;
		ra.save();
		
	}
		
	public void save() {
		
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(this.savePath), "UTF-8")) {
		
			File tileSet = new File(this.tileSet);
			
			TileServiceMng.initVersion(tileSet);
			
			Version ver = TileServiceMng.versions.get(tileSet.getName());
			
			if(pnu == null) {
				FileLayer fl = ver.layers.get("fa");
				
				System.out.println(fl.getName()+", size=" + fl.getObjectSize());
				
				long totalCnt = fl.getObjectSize();
				
				//double totalCnt = 1000;
				
				long interval = 10000;
		
				long startTime = System.currentTimeMillis();
				
				SimpleFeatureIterator sfi = fl.iterator();
				double nowCnt = 0;
				while(sfi.hasNext()) {
					SimpleFeature sf = sfi.next();
					nowCnt++;
					
					if(sf == null) {
						continue;
					}
					
					String jibun = (String) sf.getAttribute("jibun");
					if(jibun != null && jibun.endsWith("도")) {
						continue;
					}
					
					String pnu = (String) sf.getAttribute("pnu");
					
					Vector<ListFeatureCollection> list = MapService.getRoadsIntersaction(tileSet.getName(), pnu, roadInfos, snap);
					
					if(list.size() > 1) {
						String geoJson = MapService.getGeoJson(list);
						writer.write(geoJson);
						writer.write("\n");
					}
					
					if(nowCnt%interval == 0) {
						
						float percent = -1;
						
						long et = System.currentTimeMillis();
						
						double percentTmp = (nowCnt/totalCnt)*100.0;
						
						float compareValue = (new Float(String.format("%.2f", percentTmp))).floatValue();
						
						percent = compareValue;
						
						long jobTime = et - startTime;
						
						long restTime = (long) ((jobTime * 100.0)/(percentTmp) - jobTime);
						
						int jobHour = (int) (jobTime/1000.0/3600.0);
						int jobMin  = (int) ( ((jobTime/1000.0/3600.0) - jobHour)*60);
						
						int restHour = (int) (restTime/1000.0/3600.0);
						int restMin = (int) ( ((restTime/1000.0/3600.0) - restHour)*60);
						
						long totalTime = jobTime + restTime;
						
						int totalHour = (int) (totalTime/1000.0/3600.0);
						int totalMin = (int) ( ((totalTime/1000.0/3600.0) - totalHour)*60);
						
						Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						
						String endTime = formatter.format(startTime+restTime+jobTime);

						System.out.println("nowCnt="+nowCnt+", 종료 예상 시간 : "+ endTime+", 남은 진행율 : "+String.format("%.1f", 100.0-percent)+
								"%, 남은 예상 시간 = " + restHour +" h " +restMin+" m"+
								", 전체 소요 예상 시간 = " + (totalHour)+" h "+totalMin+" m \n");
						//System.out.println("###################################################################################### \n");
					}
					
				}

			}
			else {
				String[] pnus = pnu.split(",");
				for(String pnu : pnus) {
					Vector<ListFeatureCollection> list = MapService.getRoadsIntersaction(tileSet.getName(), pnu, roadInfos, snap);
					if(list.size() > 1) {
						String geoJson = MapService.getGeoJson(list);
						writer.write(geoJson);
						writer.write("\n");
					}
				}
				
			}
            
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		
	}
	
	
	
}
