package com.gis2.storage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class WMTSMNg {
	
	Vector<String> wmtsLine = new Vector();
	
	Vector<String> layerLine = new Vector();
	
	int layerStartIdx = 0;
	int layerEndIdx = 0;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WMTSMNg mng = new WMTSMNg();
		
		mng.readXml(null);
		
		Vector<String> tileNames = new Vector();
		tileNames.add("emap/emp01");
		tileNames.add("emap/emp02");
		tileNames.add("reg10/reg10");
		tileNames.add("reg20/reg20");
		
		String value= mng.getWMTSCapabilities(tileNames,"0101");
		
		System.out.println(value);
		
//		for(String line : mng.wmtsLine) {
//			System.out.println("new Line = " + line);
//		}
	}
	
	public String getWMTSCapabilities(Vector<String> tileNames, String key) {
		String xml = "";
		
		//String value = "emap/emp01";
		String value1 = "path=emap/emp01";
		String value2 = ">emap/emp01<";
		Vector<String> wmtsLineNew = new Vector(this.wmtsLine);
		
		
		for(String tileName : tileNames) {
	        for (int i = layerLine.size() - 1; i >= 0; i--) {
	        	
	        	String line = layerLine.get(i);
	        	
	        	String newLine = line.replaceAll(value1, "key="+key+"&amp;path="+tileName);
	        	newLine = newLine.replaceAll(value2, ">"+tileName+"<");
	        	
	        	wmtsLineNew.add(this.layerStartIdx, newLine);
	        	
	            //System.out.println("Item at index " + i + ": " + layerLine.get(i));
	        }
		}
		
		for(String line : wmtsLineNew) {
			xml+=(line+"\n");
		}
				
		return xml;
	}
	
	public void readXml(String file) {
        //String filePath = "D:\\workspace\\MapService\\map_service\\WMTSCapabilities.xml"; // 읽을 파일의 경로
		String filePath = file; // 읽을 파일의 경로
        boolean ok = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            
            int idx = 0;
            
            while ((line = reader.readLine()) != null) {
                //System.out.println(line);
                
                if(line.indexOf("<Layer>") > -1) {
                	ok = true;
                	layerStartIdx = idx;
                }
                
                if(ok) {
                	layerLine.add(line);
                }
                
                if(line.indexOf("</Layer>") > -1) {
                	ok = false;
                	layerEndIdx = idx;
                }
                
                
                wmtsLine.add(line);
                
                idx++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
//        for(String line : layerLine) {
//        	System.out.println("layer line = " + line);
//        }
//        
//        System.out.println("layerStartIdx = " + this.layerStartIdx+", layerEndIdx=" + this.layerEndIdx);
        
        for(int i=0 ; i<=(this.layerEndIdx-this.layerStartIdx); i++) {
        	this.wmtsLine.remove(this.layerStartIdx);
        }
        
//        
//        for(String line:this.wmtsLine) {
//        	System.out.println("write = " + line);
//        }
        
	}

}
