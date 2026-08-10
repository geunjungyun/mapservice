package com.gis2.storage;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.ConcurrentMap;

import javax.crypto.Cipher;
import javax.imageio.ImageIO;

import com.data.org.apache.jdbm.DB;
import com.data.org.apache.jdbm.DBMaker;
import com.data.util.MapLog;
//import com.gis.engine.util.FileUt;
//import com.gis.engine.util.MapLog;
//import com.gis.map.ui.TileMapFactory;
//import com.gis.protocol.freegis3.PathMapping;
import com.util.io.FileUt;

import java.awt.Image;
import java.awt.Toolkit;


public class TileDB {
	
	DB db = null; 
	String path;
	boolean readOnly = true;
	boolean createMode = false;
	ConcurrentMap cm = null;
	//String dbFileName = null;
	
	boolean init = false;
	
	//public PathMapping pm = null;
	
	Date lastModified = null;
	
	public String dbpath = null;
	
	public int autoCommitSize = 10000;
	/**
	 * 
	 * @param path 디렉토리 이름
	 * @param readOnly
	 */
	public TileDB (String path, boolean readOnly, boolean createMode){
		this.path = path;
		
		File pathF = new File(this.path);
		if(!pathF.exists() && !readOnly){
			pathF.mkdirs();
		}
		
		if(createMode == true){
			File[] files = pathF.listFiles();
			for(File file : files){
				 String fileName = file.getName();
				 int pathNameLen = pathF.getName().length();
				 if(fileName.length() < pathNameLen){
					 continue;
				 }
				if(fileName.substring(0,pathNameLen).toLowerCase().equals(pathF.getName().toLowerCase())){
					file.delete();
				}
			}
		}
		
		this.readOnly = readOnly;
		this.createMode = createMode;
		this.init();
		//this.init(readOnly, pathF.getAbsolutePath() +File.separator+pathF.getName());
	}
	
	public void reload() {
		System.out.println("TileDB reload");
		this.close();
		this.init = false;
		this.init();
	}
	
	public Date getLastModified(){
		return this.lastModified;
	}
	
	public boolean init(){
		
		if(this.init){
			return this.init;
		}
		
		File pathF = new File(this.path);
		
		if(this.readOnly){
			
			if(!pathF.exists()){
				return false;
			}
			
			//String[] list = pathF.flist();
			File[] files = pathF.listFiles();
			if(files.length == 0){
				return false;
			}
//			for(File file : files){
//				if(file.isFile() && !file.getName().startsWith(pathF.getName())){
//					return false;
//				}
//			}
		}
		
		try{
			/*
			String fileName = pathF.getName();
			
			File[] subList = pathF.listFiles();
			
			File subTile = null;
			
			for(File sub : subList) {
				if(sub.isDirectory()) {
					subTile = sub;
				}
			}
			
			if(subTile != null) {
				String name = subTile.getName();
				try {
					int nameInt = Integer.parseInt(name);
				}
				catch(Exception e) {
					subTile = null;
				}
			}
			
			if(TileServiceMng.nowPath == null) {
				subTile = null;
			}
			
			if(subTile == null) {
				this.init(readOnly, pathF.getAbsolutePath() +File.separator+fileName);	
			}
			else {
				this.init(readOnly, subTile.getAbsolutePath() +File.separator+fileName+File.separator+fileName);
			}
			*/
			
			
			
			String fileName = pathF.getName();
			File[] files = pathF.listFiles();
			int cnt = Integer.MIN_VALUE;
			String tmpName = null;
			int tmpNameCnt = 0;
			
			Vector<String> tileNames = new Vector();
			
			//디렉토리 하위에 존재하는 파일 이름을 인식하기 위한 로직
			HashMap<String, Integer> names = new HashMap();
			for(File file : files) {
				
				if(file.isDirectory()) {
					continue;
				}
				
				String fn = file.getName().toLowerCase();
				
				if(fn.endsWith(".d.0") || fn.endsWith(".i.0")) {
					int idx = file.getName().indexOf(".");
					String name = file.getName().substring(0, idx);
					fileName = name;
				}
				
			}
			
			this.init(readOnly, pathF.getAbsolutePath() +File.separator+fileName);

		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param path 디렉토리 이름
	 * @param readOnly
	 */
//	public TileDBMng (String path, String dbName, boolean readOnly, boolean createMode){
//		this.path = path;
//		this.dbFileName = dbName;
//		File pathF = new File(this.path);
//		if(!pathF.exists()){
//			pathF.mkdirs();
//		}
//		
//		if(createMode == true){
//			File[] files = pathF.listFiles();
//			for(File file : files){
//				 String fileName = file.getName();
//				 int pathNameLen = dbName.length();
//				 if(fileName.length() < pathNameLen){
//					 continue;
//				 }
//				if(fileName.substring(0,pathNameLen).toLowerCase().equals(dbName.toLowerCase())){
//					file.delete();
//				}
//			}
//		}
//		
//		this.init(readOnly, pathF.getAbsolutePath() +File.separator+dbName);
//	}
	
	public boolean isCheck() {
		File pathF = new File(this.path);
		String fileName = pathF.getName();
		
		File[] subList = pathF.listFiles();
		
		File subTile = null;
		
		for(File sub : subList) {
			if(sub.isDirectory()) {
				subTile = sub;
			}
		}
		
		if(subTile != null) {
			String name = subTile.getName();
			try {
				int nameInt = Integer.parseInt(name);
			}
			catch(Exception e) {
				subTile = null;
			}
		}
		
		String dbP = "";
		
		if(subTile == null) {
			//dbP = pathF.getAbsolutePath() +File.separator+fileName;
			return false;
		}
		else {
			dbP = subTile.getAbsolutePath() +File.separator+fileName+File.separator+fileName;
			System.out.println("ori="+this.dbpath);
			System.out.println("dst="+dbP);
			if(dbP.equals(this.dbpath)) {
				return false;
			}
			else {
				return true;
			}
		}

	}
	
	
	public File getNewVersion() {
		File pathF = new File(this.path);
		
		String tileName = pathF.getName();
		
		File[] subList = pathF.listFiles();
		
		File subTile = null;
		
		for(File sub : subList) {
			if(sub.isDirectory() && sub.getName().equals("new")) {
				File[] subLists  = sub.listFiles();
				for(File subTemp : subLists) {
					if(subTemp.isDirectory() && subTemp.getName().equals(tileName)) {
						TileDB subDB = new TileDB(subTemp.getAbsolutePath(), true, false);
						if(subDB != null) {
							subDB.close();
							return subTemp;
						}
						
					}
				}
			}
		}
		return null;
	}

	
	public void init(boolean readOnly, String fileName) throws Exception{
		
		boolean isRead = true;
		
		//sSystem.out.println("TileDB init path = " + fileName);
		this.dbpath = fileName;
		//DBMaker.openFile(thisDirectory + getIdxname()).useRandomAccessFile().closeOnExit().enableMRUCache().make()
		if(!readOnly){
			//saveDB = (DB) DBMaker.openFile(saveDBPath+File.separator+saveDBName).useRandomAccessFile().disableLocking().closeOnExit().disableCache().make();
			//20200816
			//db = (DB) DBMaker.openFile(fileName).disableTransactions().closeOnExit().make();
			db = (DB) DBMaker.openFile(fileName).disableTransactions().closeOnExit().make();
			//db = (DB) DBMaker.openFile(fileName).disableTransactions().enableSoftCache().make();
			if(db.getHashMap("map") == null){
				this.cm = db.createHashMap("map");
			}
			else{
				this.cm = db.getHashMap("map");
			}
		}
		else{
			//db = (DB) DBMaker.openFile(fileName).useRandomAccessFile().readonly().enableMRUCache().make();
			//db = (DB) DBMaker.openFile(fileName).readonly().disableLocking().enableHardCache().closeOnExit().make();
			
			
			
			File file = new File(fileName+".d.0");
			
			if(file.exists() && file.length() == 0) {
				isRead = false;
			}
			
			if(isRead) {
				db = (DB) DBMaker.openFile(fileName).readonly().disableLocking().enableHardCache().make();
				this.cm = db.getHashMap("map");
			}
		}
		
		File file = new File(fileName+".i.0");
		
		if(file.exists()){
			this.lastModified = new Date(file.lastModified());
		}
		
		this.init = isRead;
		
	}
	
	public ConcurrentMap getMap(){
		return this.cm;
	}

	
	public byte[] getByte(File file){
		String fileName = file.getAbsolutePath();
		
		String tilePath = fileName.substring(this.path.length(), fileName.length());
		
		if(this.init) {
			return (byte[])this.cm.get(FileUt.changeSeparator(tilePath));	
		}
		else {
			return null;
		}
	}
	
	public byte[] getByte(String tilePath){
		if(this.init) {
			//return (byte[])this.cm.get(FileUt.changeSeparator(tilePath));
			
			if(this.cm == null) {
				return null;
			}
			
			Object obj = this.cm.get(FileUt.changeSeparator(tilePath));
			
			if(obj != null) {
				return (byte[])obj;
			}
			else {
				return null;
			}
			
		}
		else {
			return null;
		}
	}
	
	
	public BufferedImage getImage(File file){
		long st = System.currentTimeMillis();
		String fileName = file.getAbsolutePath();
		String tilePath = fileName.substring(this.path.length(), fileName.length());
		byte[] data = (byte[])this.cm.get(FileUt.changeSeparator(tilePath));
		long et = System.currentTimeMillis();
		
		//System.out.println("tile db getImage time = " + (et - st)/1000.0);
		
		if(data == null || data.length ==0){
			return null;
		}
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(new ByteArrayInputStream(data));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bi;
	}
	
	public void close(){
		if(this.init == true && this.db != null){
			if(this.db.isClosed() == false) {
				if(this.readOnly == false) {
					this.db.commit();
				}
				this.db.close();
			}
		}
		this.db = null;
	}
	
	public void commit(){
		if(this.init == true && this.db != null){
			this.db.commit();
		}
	}

	
//	public void putTile(File file, byte[] data){
//		String fileName = file.getAbsolutePath();
//		String tilePath = fileName.substring(this.path.length(), fileName.length());
//		this.cm.put(FileUt.changeSeparator(tilePath), data);
//		if(this.cm.size() % 200 == 0){
//			this.db.commit();
//		}
//	}
	
	long ast = 0;
	long st = 0;
	public synchronized void putTile(String file, byte[] data){
		try{
			
			String key = FileUt.changeSeparator(file);
//			if(this.cm.containsKey(key)){
//				this.cm.remove(key);
//			}
			this.cm.put(FileUt.changeSeparator(file), data);
			if(this.cm.size() % autoCommitSize == 0){
				long et = System.currentTimeMillis();
				//System.out.println("commit"+", size="+this.cm.size()+", min="+(et-st)/1000);
				this.db.commit();
				st = et;
			}
		}catch(java.io.IOError ev){
			System.out.println("putTile IOError="+file);
			ev.printStackTrace();
		}
	}
	
	public static void saveImage(TileDB db, File rootPath, File file_){
		File[] files = file_.listFiles();
		for(File file: files){
			if(file.isDirectory()){
				saveImage(db, rootPath, file);
			}
			else{
				String name = file.getName();
				if(name.indexOf("png") > -1){
					try {
						
						byte[] data = new byte[(int)file.length()];
						FileInputStream fis = new FileInputStream(file);
						BufferedInputStream bis = new BufferedInputStream(fis);
						
						bis.read(data);
						bis.close();
						
						String fullName = file.getAbsolutePath();
						
						int nameLen = file.getName().length();
						int rootLen = rootPath.getAbsolutePath().length();
						
						String savePath = fullName.substring(rootLen, fullName.length()-nameLen);
						
						db.putTile(savePath+file.getName(), data);
						
						
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	

	
	private static boolean dummyTile(File file) throws IOException{
		//int conA = 255;
		//int conR = 153;
		//int conG = 204;
		//int conB = 255;
		int conA = 255;
		int conR = 114;
		int conG = 167;
		int conB = 198;
		
		BufferedImage buffImg = ImageIO.read(file);
		int width  = buffImg.getWidth();
		int height = buffImg.getHeight();
		
		
		for(int i=0; i<width; i++){				
			for(int j=0; j<height; j++){
				int pixel = buffImg.getRGB(i, j);
				int alpha = (pixel >> 24) & 0xff;
				int red = (pixel >> 16) & 0xff;
				int green = (pixel >> 8) & 0xff;
				int blue = (pixel) & 0xff;
				
				if((conR != red) || (conG != green) || (conB != blue)){
					//System.out.println("FileSize:" + file.length() + " width:" + buffImg.getWidth() + ", height:" + buffImg.getHeight() +  ", Alpha:" + alpha  + ", R:" + red + " G:" + green + " B:" + blue);				
					return true;
				}
			}
		}
		
		System.out.println("DummyTile: " + file.getAbsolutePath());
		return false;
	}

	
	public static void main(String[] args) {
		//TileDBMng jt = new TileDBMng();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

		if(args.length >= 1 && args[0].equals("fromDBtoFile")) {

			if(args.length != 3) {
				System.out.println("fromDBtoFile 사용법: fromDBtoFile <dbPath> <outputDir>");
				return;
			}

			String dbPath = args[1];
			String outpath = args[2];

			long st = System.currentTimeMillis();
			System.out.println(format.format(new Date(System.currentTimeMillis()))+", fromDBtoFile dbPath = " + dbPath + ", outpath = " + outpath);
			saveToTileFromDB(dbPath, outpath);
			long et = System.currentTimeMillis();
			System.out.println(format.format(new Date(System.currentTimeMillis()))+", end fromDBtoFile, time = " + String.format("%.5f", (et-st)/1000.0/60.0 ) +" min" );
			return;
		}

		if(args.length == 1) {
			
			long st = System.currentTimeMillis();
			String srcS = args[0];
			File srcFile = new File(srcS);
			System.out.println(format.format(new Date(System.currentTimeMillis()))+", defragFile = " + srcFile.getAbsolutePath());
			TileDB saveDB = new TileDB(srcFile.getAbsolutePath(), false, false);
			saveDB.defrag();
			long et = System.currentTimeMillis();
			System.out.println(format.format(new Date(System.currentTimeMillis()))+", end defrag = " + srcFile.getName() 
			+", time = " + String.format("%.5f", (et-st)/1000.0/60.0 ) +" min" );
			return;
		}
		
		try {
			
			long st = System.currentTimeMillis();
			
			String srcS = args[0];
			File srcFile = new File(srcS);
			System.out.println(format.format(new Date(System.currentTimeMillis()))+", saveFile = " + srcFile.getAbsolutePath());
			File[] dstFiles = new File[args.length -1];
			
			for(int i=1; i<args.length; i++) {
				dstFiles[i-1] = new File(args[i]);
				System.out.println(format.format(new Date(System.currentTimeMillis()))+", addFile = " + dstFiles[i-1].getAbsolutePath());
			}
			
			TileDB saveDB = new TileDB(srcFile.getAbsolutePath(), false, false);
			saveDB.autoCommitSize = Integer.MAX_VALUE;
			System.out.println("autoCommitSize=" + saveDB.autoCommitSize);
	        Runtime rt = Runtime.getRuntime();
	        
	        rt.addShutdownHook(
	            new Thread() {
	                public void run() {
	                	saveDB.close();
	            }
	        } );
			
			for(File addFile : dstFiles) {
				long sst = System.currentTimeMillis();
				TileDB addDB = new TileDB(addFile.getAbsolutePath(), true, false);
				
				int size = 0;
				Set<String> set = addDB.cm.keySet();
				size = addDB.cm.size();
				
				//int interval = size/100;
				int interval = size/10;
				
				Iterator<String> it = set.iterator();
				int idx = 0;
				while(it.hasNext()){
					
					String key = it.next();
					
					byte[] data = (byte[])addDB.cm.get(key);
					
					saveDB.putTile(key, data);
					idx++;
					
					int percent = idx/interval;
					
					if(idx%interval == 0) {
						System.out.println(format.format(new Date(System.currentTimeMillis()))+", "+percent +"0 % ");
					}
					
				}
				addDB.close();
				long eet = System.currentTimeMillis();
				//String.format("%.5f",(pnet-pnst)/1000.0/60.0)
				
				
				
				System.out.println(format.format(new Date(System.currentTimeMillis()))+", add tile = " + addFile.getName() 
				+", time = " + String.format("%.5f", (eet-sst)/1000.0/60.0 ) +" min" );
				
			}
			
			saveDB.commit();
			
//			saveDB.db.defrag(false);
			saveDB.close();
			
			long et = System.currentTimeMillis();
			
			System.out.println(format.format(new Date(System.currentTimeMillis()))+", end tile = " + srcFile.getName() 
			+", time = " + String.format("%.5f", (et-st)/1000.0/60.0 ) +" min" );
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//TileDBMng tdbm = new TileDBMng();
	}
	
	public void defrag() {
		this.db.commit();
		this.db.defrag(false);
	}
	
	public static void saveToTileFromDB(String dbPath, String outpath){
		
		File file = new File(dbPath);
		
		File outPathFile = new File(outpath);
        
		if(outPathFile.exists() == false){
			outPathFile.mkdirs();
		}
		//long st = System.currentTimeMillis();
		
		DB db = (DB) DBMaker.openFile(file.getAbsolutePath()+File.separator+file.getName()).readonly().disableLocking().enableMRUCache().closeOnExit().make();
		//DB db = (DB) DBMaker.openFile(file.getAbsolutePath()+File.separator+file.getName()).disableTransactions().useRandomAccessFile().readonly().make();
		
		ConcurrentMap cm = db.getHashMap("map");

		//http://192.168.100.17:8080/Map/MapPlan?req=timg&timg=2020331/tile0/6/1/0/54_42.png
		
		//byte[] tt = (byte[]) cm.get("/6/1/0/54_42.png");
		
		long st = System.currentTimeMillis();
		int cnt = 0;
		Set<String> set = cm.keySet();
		Iterator<String> it = set.iterator();
		while(it.hasNext()){
			
			String key = it.next();
			
			byte[] data = (byte[])cm.get(key);
			
			System.out.println(key);
			
			if(data == null) {
				System.out.println("tile is null =" + key);
			}
			
			try {
				
				String fileName = FileUt.changeSeparator(outpath+key);
				String[] pass = FileUt.getFileNameSplit(fileName);

				String tempKey = FileUt.changeSeparator(key).substring(0, 4);
				
				File savePath = new File(pass[0]);
				if(savePath.exists() == false){
					savePath.mkdirs();
				}
				
				FileOutputStream fos = new FileOutputStream(fileName);
				fos.write(data);
				fos.flush();
				fos.close();
				cnt++;
				if(cnt%1000 == 0){
					System.out.println("save cnt="+cnt+", time="+(System.currentTimeMillis() - st));
					st = System.currentTimeMillis();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		db.close();
			
	}


}
