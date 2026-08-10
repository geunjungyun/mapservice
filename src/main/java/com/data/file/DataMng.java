package com.data.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOError;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;

import com.data.exception.FLReadException;
import com.data.org.apache.jdbm.DB;
import com.data.org.apache.jdbm.DBMaker;
import com.data.org.apache.jdbm.DBStore;
import com.data.org.apache.jdbm.HTree;


public class DataMng extends Transaction{
	
	WKBReader wkbReader = new WKBReader();
	WKBWriter wkbWriter = new WKBWriter();
	SimpleFeatureBuilder sfb = null;
	
	//SimpleBigConversion sbc = new SimpleBigConversion();
	
	File path = null;
	
	String dataPath = "data";
	
	DBStore db = null;
	
	String recids = "recids";
	
	String btreeName = "btree";
	
	Object synObj = new Object();
	
	HTree btree = null;
	
	
	public int commit() {
		//int size = changeSize;
		
		this.db.commit();
		//this.changeSize = 0;
		return super.commit();
	}
	
	/**
	 * 
	 * @param path 저장 패스
	 * @param createMode 생성 : true, 읽기 : false 
	 */
	public void createData(File path, boolean createMode) throws Exception{
		this.path = path;
		File data = new File(path+FileName.DS+dataPath);
		
		if(!data.exists()){
			data.mkdirs();
		}
		else{
			if(createMode == true){
				File[] files = data.listFiles();
				for(File file : files){
					if(file.isFile()){
						file.delete();
					}
					else{
						
						
					}
				}
			}
		}
		
		
		File dataP = new File(path+FileName.DS+dataPath+FileName.DS+"data");
		if(createMode == false){
			File subDataPath = new File(path+FileName.DS+dataPath+FileName.DS+"data.d.0");
			if(!subDataPath.exists()) {
				throw new FLReadException(path+FileName.DS+dataPath+FileName.DS+"data.d.0" +" not exist");
			}
			if(subDataPath.length() == 0) {
				throw new FLReadException(path+FileName.DS+dataPath+FileName.DS+"data.d.0 size is 0");
			}
		}
		else{
				
			if(createMode == true){
				File[] files = data.listFiles();
				if(files != null) {
					for(File file : files){
						if(file.isFile()){
							file.delete();
						}
						else{
							
							
						}
					}
				}
			}
		}
		
		//this.db = (DBStore) DBMaker.openFile(dataP.getAbsolutePath()).enableMRUCache().disableLocking().make();
		this.db = (DBStore) DBMaker.openFile(dataP.getAbsolutePath()).disableTransactions().disableLocking().make();
		//db = (DB) DBMaker.openFile(fileName).disableTransactions().closeOnExit().make();
		HTree fid = null;
		
		synchronized(this.synObj) {
			if( (fid = (HTree) this.db.getHashMap(this.recids)) == null){
				fid = (HTree) this.db.createHashMap(this.recids);
			}
		}
		//this.db = (DBStore) DBMaker.openFile(dataP.getAbsolutePath()).closeOnExit().enableMRUCache().disableTransactions().make();
	}
	
	
	public void write(String code, long page) {
		
		
		synchronized(this.synObj) {
			if( btree == null){
				btree = (HTree) this.db.createHashMap(this.btreeName);
			}
		}
		
		btree.put(code, page);
	}
	
	public long read(String code) throws Exception{
		
		if(this.btree == null) {
			btree = (HTree) this.db.getHashMap(this.btreeName);
		}
		
		Object temp = btree.get(code);
		long page = -1;
		if(temp != null) {
			page = (long)temp;
		}
		
		return page;
	}
	
	
	public long write(SimpleFeature sf, SimpleFeatureType sft) throws Exception{
		
		//System.out.println("SimpleFeature = " + sf.toString());
		if(sfb == null){
			sfb = new SimpleFeatureBuilder(sft);
		}
		
		HTree pages = null;
		
		synchronized(this.synObj) {
			if( (pages = (HTree) this.db.getHashMap(this.recids)) == null){
				pages = (HTree) this.db.createHashMap(this.recids);
			}
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    DataOutputStream dos = new DataOutputStream(baos);
	    //System.out.println("");
	    
	    byte[] isNull = new byte[sft.getAttributeCount()];
		for(int i=0; i<sft.getAttributeCount(); i++){
			Object obj = sf.getAttribute(i);
			if(obj == null){
				isNull[i] = 1;
			}
			else{
				isNull[i] = -1;
			}
		}
		dos.writeByte(sft.getAttributeCount());
	    dos.write(isNull);
	    
	    //isNull = null;
	    
	    //String tmp = "";
		for(int i=0; i<sft.getAttributeCount(); i++){
			
			
			AttributeDescriptor ad = sft.getDescriptor(i);
			
			String name = ad.getName().toString();
			AttributeType at = (AttributeType) ad.getType();
			//System.out.println("addFeature, name="+name+", type="+ad.getType().getBinding()+", data="+sf.getAttribute(i));
			
			Map userData = ad.getUserData();
			Boolean mode = (Boolean)userData.get("index");
			
			Object obj = sf.getAttribute(i);
			
			if(obj == null && isNull[i] == 1){
				continue;
			}
			
			if(name.equals("geometry") || name.equals("the_geom") || name.toLowerCase().equals("shape")){
				Geometry geo = (Geometry)obj;
				byte[] data = this.wkbWriter.write(geo);
				//System.out.println("geo size=" + data.length);
				dos.writeInt(data.length);
				dos.write(data);
				data = null;
			}
			else{
				
				//tmp+=(","+obj);
				//Object obj = sf.getAttribute(i);
				if(at.getBinding().equals(Integer.class)){
						dos.writeInt((Integer)obj);
				}
				else if(at.getBinding().equals(Byte.class)){
						dos.writeByte((Integer)obj);
				}
				else if(at.getBinding().equals(Short.class)){
						dos.writeShort((Short)obj);	
				}
				else if(at.getBinding().equals(Double.class)){
						dos.writeDouble((Double)obj);
				}
				else if(at.getBinding().equals(Float.class)){
						dos.writeFloat((Float)obj);	
				}
				else if(at.getBinding().equals(Long.class)){
						dos.writeLong((Long)obj);
				}
				else if(at.getBinding().equals(Boolean.class)){
					dos.writeBoolean((Boolean)obj);
				}
				else if(at.getBinding().equals(Date.class)) {
					dos.writeLong(((Date)obj).getTime());
				}
				else if(at.getBinding().equals(String.class)){
					//String value = (String)obj;
					String value = ((String)obj).trim();
					
					if(value.length() > 0){
						byte[] data = ((String)obj).getBytes("utf-8");
						dos.writeShort(data.length);
						dos.write(data);
					}
					else{
						dos.writeShort(0);
					}
				}
				else if(at.getBinding().equals(BigDecimal.class)) {
					dos.writeDouble(((BigDecimal)obj).doubleValue());
				}
			}
		}
		//System.out.println("tmp="+tmp);
		dos.flush();
		byte[] datas = baos.toByteArray();
		
		long page = -1;
		
		synchronized(this.synObj) {
			//대용량 데이타 문제 해결 필요, 8메가 이상의 파일은 객체 하나에 파일 하나로 매핑 되면서 파일로 저장하고 있음.
			if(datas.length < 1024*1024*8) {
				page = this.db.insert(datas);
				pages.put(page, page);
			}
			else {
				//int size = 0;
				//byte[] data = new byte[4];
				//this.sbc.int2byte(size, data, 0);
				byte[] attrCnt = new byte[1];
				attrCnt[0] = 0;
				page = this.db.insert(attrCnt);
				this.saveFile(page, datas);
				pages.put(page, page);
				//System.out.println("page num = " +",error size="+datas.length);
			}
		}
		this.changeSize ++;
		
		datas = null;
		
//		if(this.commitSize == this.changeSize) {
//			this.db.commit();
//			this.changeSize = 0;
//		}
		
		//this.db.commit();
		dos.close();
		baos.close();

		return page;
	}
	
	private void saveFile(long pageId, byte[] data) throws Exception{
		FileOutputStream fis = new FileOutputStream(this.path+FileName.DS+this.dataPath+FileName.DS+pageId);
		BufferedOutputStream bis = new BufferedOutputStream(fis);
		bis.write(data);
		bis.flush();
		fis.flush();
		bis.close();
		fis.close();
	}
	
	private byte[] readFile(long pageId) throws Exception{
		File file = new File(this.path +FileName.DS +this.dataPath+FileName.DS+pageId);
		int fileSize = (int) file.length();
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		byte[] data = new byte[fileSize];
		bis.read(data);
		bis.close();
		fis.close();
		
		return data;
	}

	Object obj = new Object();
	
	HashMap<Long , SimpleFeature> cache = new HashMap();
	
	SimpleFeature read(long pageId, SimpleFeatureType sft) throws Exception{
		boolean bigData = false;
		
		
		if(cache.containsKey(pageId)) {
			return cache.get(pageId);
		}
		
		
		if(sfb == null){
			sfb = new SimpleFeatureBuilder(sft);
		}
		
		byte[] data = null;
		
		data = this.db.fetch(pageId);
		
		if(data == null) {
			return null;
		}
		
		if(data.length == 1 && data[0] == 0) {
			data = this.readFile(pageId);
			bigData = true;
		}
		
		//System.out.println("DataMng read size = " + data.length);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
	    DataInputStream dis = new DataInputStream(bais);
	    
	    //SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(this.sft);
	    
	    int saveAttrCnt = dis.readByte();
	    
	    if(saveAttrCnt != sft.getAttributeCount()){
	    	System.out.println("error : not match attr cnt");
	    	return null;
	    }
	    
	    Object[] objs = new Object[sft.getAttributeCount()];
	    
	    byte[] isNulls = new byte[saveAttrCnt];
	    
	    for(int i=0; i<saveAttrCnt; i++){
	    	isNulls[i] = dis.readByte();
	    }
		//System.out.println("");
		for(int i=0; i<sft.getAttributeCount(); i++){
			
			if(isNulls[i] == 1){
				continue;
			}
			
			AttributeDescriptor ad = sft.getDescriptor(i);
			
			String name = ad.getName().toString();
			AttributeType at = (AttributeType) ad.getType();
			Map userData = ad.getUserData();
			Boolean mode = (Boolean)userData.get("index");
			
			if(name.equals("geometry") || name.equals("the_geom") || name.toLowerCase().equals("shape")){
				int geoSize = dis.readInt();
				byte[] geoByte = new byte[geoSize];
				dis.readFully(geoByte);
				Geometry geo = null;
				//WKBReader read, write unsafe thread
				synchronized(obj){
					geo = wkbReader.read(geoByte);
				}
				geoByte = null;
				objs[i] = geo;
			}
			else{
				if(at.getBinding().equals(Integer.class)){
					int value = dis.readInt();
					objs[i] = value;
				}
				else if(at.getBinding().equals(Byte.class)){
					objs[i] = dis.readByte();
				}
				else if(at.getBinding().equals(Short.class)){
					objs[i] = dis.readShort();
				}
				else if(at.getBinding().equals(Double.class)){
					objs[i] = dis.readDouble();
				}
				else if(at.getBinding().equals(Float.class)){
					objs[i] = dis.readFloat();
				}
				else if(at.getBinding().equals(Long.class)){
					objs[i] = dis.readLong();
				}
				else if(at.getBinding().equals(Boolean.class)){
					objs[i] = dis.readBoolean();
				}
				else if(at.getBinding().equals(Date.class)) {
					objs[i] = new Date(dis.readLong());
				}
				else if(at.getBinding().equals(String.class)){
					short len = dis.readShort();
					if(len > 0){
						byte[] value = new byte[len];
						dis.readFully(value);
						objs[i] = new String(value, "utf-8");
					}
				}
				else if(at.getBinding().equals(BigDecimal.class)) {
					objs[i] = new BigDecimal(dis.readDouble());
				}
			}
			//System.out.print(","+objs[i]);
		}
		SimpleFeature sf = null;
		synchronized(obj){
			
			try {
				sf = sfb.buildFeature(pageId+"",objs);
			}
			catch(Exception e) {
								
				System.out.println("sft cnt=" + sft.getAttributeCount()+", obj len="+objs.length);
				System.out.println("sft value =" + sft.toString());
				for(int i=0; i<objs.length; i++) {
					System.out.println("obj["+i+"]=" + objs[i].toString());
				}
				
				throw e;
			}
		}
		
		if(bigData) {
			this.cache.put(pageId, sf);
		}
		
		dis.close();
		bais.close();
		
		data = null;
		
		return sf;

	}
	
	
	/**
	 * 
	 * @param pageId
	 * @param sft
	 * @return
	 * @throws Exception
	 */
	SimpleFeature remove(long pageId,SimpleFeatureType sft) throws Exception{
		
		
		HTree fid = null;
		if( (fid = (HTree) this.db.getHashMap(this.recids)) == null){
			fid = (HTree) this.db.createHashMap(this.recids);
		}
		
		if(fid.containsKey(pageId)){
			fid.remove(pageId);
		}
		else{
			FileLayer.LOG.debug("페이지가 존재 하지 않습니다. pageId="+pageId);
			return null;
		}
		
		SimpleFeature sf = this.read(pageId, sft);
		if(sf == null){
			FileLayer.LOG.debug("데이타가 존재 하지 않습니다. pageId="+pageId);
			return null;
		}
		
		this.db.delete(pageId);
		
		changeSize++;
	
		return sf;
	}
	
	int getSize() {
		HTree recid = (HTree)db.getHashMap(recids);
		if(recid != null) {
			return recid.size();
		}
		else {
			return 0;
		}
	}
	
	
	Iterator getIterator(){
		
		HTree pages = (HTree)db.getHashMap(recids);
		
		try {
			Set set = pages.keySet();
			Iterator it = null;
			return it = set.iterator();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	void close(){
		
		if(this.db != null && !this.db.isClosed()) {
			this.db.commit();
		}
		if(this.db != null && !this.db.isClosed()) {
			this.db.close();
		}
	}
	
	boolean isClose() {
		return this.db.isClosed();
	}
	
}
