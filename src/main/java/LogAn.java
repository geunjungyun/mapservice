import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.datatype.XMLGregorianCalendar;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LogAn {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LogAn an = new LogAn();
		an.test();
	}
	
	Vector<Double> vscale = new Vector();
	
	double width = 1914.0;
	double height = 939.0;
	
	public void test() {
		try{
			
			
			HashMap<Integer, Double> scales = new HashMap();
			
			
			scales.put(1,	2048.0);
			scales.put(2,	1024.0);
			scales.put(3,	512.0);
			scales.put(4,	256.0);
			scales.put(5,	128.0);
			scales.put(6,	64.0);
			scales.put(7,	32.0);
			scales.put(8,	16.0);
			scales.put(9,	8.0);
			scales.put(10,	4.0);
			scales.put(11,	2.0);
			scales.put(12,	1.0);
			scales.put(13,	0.5);
			scales.put(14,	0.25);
			scales.put(15,	0.125);

			
			vscale.add(2048.0);
			vscale.add(1024.0);
			vscale.add(512.0);
			vscale.add(256.0);
			vscale.add(128.0);
			vscale.add(64.0);
			vscale.add(32.0);
			vscale.add(16.0);
			vscale.add(8.0);
			vscale.add(4.0);
			vscale.add(2.0);
			vscale.add(1.0);			
			vscale.add(0.5);
			vscale.add(0.25);			
			vscale.add(0.125);
			

			
			String path = "D:\\coops\\temp\\load";
			
			File pathF = new File(path);
			
			File[] files = pathF.listFiles();
			
			//for(int i=1; i<23;i++) {
			for(File file : files) {
				FileInputStream fis1 = new FileInputStream(file);
				InputStreamReader isr1 = new InputStreamReader(fis1, "utf-8");
				BufferedReader br1 = new BufferedReader(isr1);
				
				String line = null;
				
				double maxSize = Double.MIN_VALUE;
				
				double maxTime = Double.MIN_VALUE;
				
				String mode = "";
				
				//구분자 
				
				while((line = br1.readLine())!= null) {
					if(line.split(",").length > 1) {
						String[] aa = line.split(",");
						
						String mtime = aa[2];
						String[] mbb = mtime.split(" ");
						double time = Double.parseDouble(mbb[3]);
						
						String msize = aa[4];
						
						String[] bb = msize.split(" ");
						
						double size = Double.parseDouble(bb[3]);
						if(size > maxSize) {
							maxSize = size;
							mode = aa[0];
							maxTime = time;
						}
						
						
						
						
					}
				}
				System.out.println("day = " + file.getName() +", maxSize=" + maxSize+", maxTime="+maxTime+", mode="+mode);
				
		
				br1.close();
				isr1.close();
				fis1.close();
			}
			
			HashMap<Double, String> logs = new HashMap();
			
			path = "D:\\coops\\temp\\req";
			
			pathF = new File(path);
			
			files = pathF.listFiles();
			
			//for(int i=1; i<23;i++) {
			for(File file : files) {
				FileInputStream fis1 = new FileInputStream(file);
				InputStreamReader isr1 = new InputStreamReader(fis1, "utf-8");
				BufferedReader br1 = new BufferedReader(isr1);
				
				String line = null;
				
				double maxSize = 2000;
				
				double maxTime = 3;
				
				String mode = "";
				
				
				
				while((line = br1.readLine())!= null) {
					if(line.split("\\|").length > 1) {
						String[] aa = line.split("\\|");
						
						String mtime = aa[2];
						String[] mbb = mtime.split(" ");
						
						double time = Double.parseDouble(mbb[0]);
						
						String msize = aa[3];
						
						String[] bb = msize.split(" ");
						
						double size = Double.parseDouble(bb[0]);
						
						//if(time > maxTime || size > maxSize) {
						if(size > maxSize) {
							
							String url = aa[4];
							int sidx = url.indexOf("&mbr=");
							
							int eidx = url.indexOf("&", sidx+4);
							
							String mbr = url.substring(sidx+5, eidx);
							
							//System.out.println(mbr);
							
							String[] coords = mbr.split("%2C");
							
							double x = Double.parseDouble(coords[2])  - Double.parseDouble(coords[0]);
							double y = Double.parseDouble(coords[3])  - Double.parseDouble(coords[1]);
							
							NumberFormat format = NumberFormat.getInstance();
							format.setGroupingUsed(false);
							
							double area = x*y;
							
							int selectScale = -1;
							for(int i=0; i<vscale.size(); i++) {
								double scale = vscale.get(i);
								
								double ab = (width*scale)*(height*scale);
								
								if(ab < area) {
									selectScale = i;
									break;
								}
							}
							
							int sl = this.getLevel(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2]), Double.parseDouble(coords[3]));
							
							System.out.println("nlevel="+sl+", "+selectScale+","+format.format((x*y))+", " + line);
							
							logs.put(size, line);
						}
					}
				}
			}
			
//			Object[] mapkey = logs.keySet().toArray();
//			Arrays.sort(mapkey);
//
//			// 결과 출력
//			for (Double nKey : logs.keySet())
//			{
//				System.out.println(nKey+","+logs.get(nKey));
//			}
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public int getLevel(double minx, double miny, double maxx, double maxy) {
		int level = -1;
		
		double rwidth = maxx - minx;
		double rheight = maxy - miny;
		
		
		
		for(int i=0; i < this.vscale.size(); i++) {
			
			double lwidth = this.vscale.get(i)*this.width;
			double lheight = this.vscale.get(i)*this.height;
			
			if(rwidth < lwidth && rheight < lheight) {
				level = (i+1);
			}
		}
		
		return level;
	}
}
