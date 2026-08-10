package com.gis.storage;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.crypto.Cipher;
import javax.imageio.ImageIO;



//import com.dawul.util.LEDataInputStream;
//import com.dawul.util.LEDataOutputStream;
//
//import com.util.MapLog;


import com.util.io.FileUt;



import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.awt.Image;
import java.awt.Toolkit;

/**
 * RocksDB implementation for Tile Storage
 * 
 * @author gjyoun
 *
 */

public class TileDB {

	static {
		RocksDB.loadLibrary();
	}

	RocksDB db = null;
	Options options = null;
	public String path;
	boolean readOnly = true;
	boolean createMode = false;
	String dbFileName = null;

	boolean init = false;

	//public PathMapping pm = null;

	Date lastModified = null;

	/**
	 * 
	 * @param path     디렉토리 이름
	 * @param readOnly
	 */
	public TileDB(String path, boolean readOnly, boolean createMode) {
		this.path = path;

		File pathF = new File(this.path);
		if (!pathF.exists() && !readOnly) {
			pathF.mkdirs();
		}

		if (createMode == true) {
			File[] files = pathF.listFiles();
			if (files != null) {
				for (File file : files) {
					String fileName = file.getName();
					int pathNameLen = pathF.getName().length();
					if (fileName.length() < pathNameLen) {
						continue;
					}
					if (fileName.substring(0, pathNameLen).toLowerCase().equals(pathF.getName().toLowerCase())) {
						file.delete();
					}
				}
			}
		}

		this.readOnly = readOnly;
		this.createMode = createMode;
		this.init();
	}

	public Date getLastModified() {
		return this.lastModified;
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

	public boolean init() {

		if (this.init) {
			return this.init;
		}

		File pathF = new File(this.path);

		if (this.readOnly) {

			if (!pathF.exists()) {
				return false;
			}

			File[] files = pathF.listFiles();
			if (files == null || files.length == 0) {
				return false;
			}
		}

		try {

			String fileName = pathF.getName();
			File[] files = pathF.listFiles();

			// RocksDB is directory based.
			// Check for potential existing DB directory or decide where to create.

			HashMap<String, Integer> names = new HashMap();
			if (files != null) {
				for (File file : files) {
					// In RocksDB migration, we treat directories as candidates too.

					if (file.isDirectory()) {
						String name = file.getName();
						// Heuristic: check if this directory looks like a DB we want
						if (!names.containsKey(name)) {
							names.put(name, 1);
						} else {
							Integer value = names.get(name);
							names.put(name, value + 1);
						}
					}
				}
			}

			if (!names.isEmpty()) {
				int tmpCnt = Integer.MIN_VALUE;
				Set set = names.keySet();
				Iterator it = set.iterator();
				while (it.hasNext()) {
					String key = (String) it.next();
					Integer value = names.get(key);
					if (value >= tmpCnt) {
						tmpCnt = value;
						fileName = key;
					}
				}
			}

			// Construct full path. logic maintained from original as much as possible,
			// but ensuring it points to a valid place for RocksDB.
			// If pathF is ".../tiledb", then we might be opening ".../tiledb/tiledb" if
			// fileName is "tiledb".
			this.init(readOnly, pathF.getAbsolutePath() + File.separator + fileName);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void init(boolean readOnly, String fileName) throws Exception {
		// System.out.println("TileDB init path = " + fileName);

		options = new Options().setCreateIfMissing(true);

		try {
			File dbFile = new File(fileName);
			if (!dbFile.exists() && this.createMode) {
				dbFile.mkdirs();
			}

			if (!readOnly) {
				db = RocksDB.open(options, fileName);
			} else {
				db = RocksDB.openReadOnly(options, fileName);

				if (dbFile.exists()) {
					this.lastModified = new Date(dbFile.lastModified());
				}
			}
		} catch (RocksDBException e) {
			throw new Exception("RocksDB Init Failed: " + e.getMessage(), e);
		}

		this.init = true;

	}

	public ConcurrentMap getMap() {
		// RocksDB does not support ConcurrentMap view directly.
		throw new UnsupportedOperationException("getMap() is not supported in RocksDB implementation.");
	}

	public byte[] getByte(File file) {
		String fileName = file.getAbsolutePath();
		String tilePath = fileName.substring(this.path.length(), fileName.length());
		return getByte(FileUt.changeSeparator(tilePath));
	}

	public byte[] getByte(String tilePath) {
		if (db == null)
			return null;
		try {
			return db.get(FileUt.changeSeparator(tilePath).getBytes());
		} catch (RocksDBException e) {
			e.printStackTrace();
			return null;
		}
	}

	public BufferedImage getImage(File file) {
		// long st = System.currentTimeMillis();
		String fileName = file.getAbsolutePath();
		String tilePath = fileName.substring(this.path.length(), fileName.length());
		byte[] data = getByte(FileUt.changeSeparator(tilePath));
		// long et = System.currentTimeMillis();

		// System.out.println("tile db getImage time = " + (et - st)/1000.0);

		if (data == null || data.length == 0) {
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

	public void close() {
		if (this.db != null) {
			this.db.close();
		}
		if (this.options != null) {
			this.options.close();
		}
		this.db = null;
		this.options = null;
	}

	public void commit() {
		// RocksDB auto-commits
		// Explicit flush if needed:
		// try {
		// if (this.db != null) this.db.flush(new FlushOptions());
		// } catch (RocksDBException e) {
		// e.printStackTrace();
		// }
	}

	public synchronized void putTile(String file, byte[] data) {
		try {
			if (db != null) {
				db.put(FileUt.changeSeparator(file).getBytes(), data);
			}
		} catch (RocksDBException e) {
			e.printStackTrace();
			System.out.println("RocksDB Put Error");
		}
	}

	public static void saveImage(TileDB db, File rootPath, File file_) {
		File[] files = file_.listFiles();
		if (files == null)
			return;
		for (File file : files) {
			if (file.isDirectory()) {
				saveImage(db, rootPath, file);
			} else {
				String name = file.getName();
				if (name.indexOf("png") > -1) {
					try {

						byte[] data = new byte[(int) file.length()];
						FileInputStream fis = new FileInputStream(file);
						BufferedInputStream bis = new BufferedInputStream(fis);

						bis.read(data);
						bis.close();

						String fullName = file.getAbsolutePath();

						int nameLen = file.getName().length();
						int rootLen = rootPath.getAbsolutePath().length();

						String savePath = fullName.substring(rootLen, fullName.length() - nameLen);

						db.putTile(savePath + file.getName(), data);

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	private static boolean dummyTile(File file) throws IOException {
		int conA = 255;
		int conR = 114;
		int conG = 167;
		int conB = 198;

		BufferedImage buffImg = ImageIO.read(file);
		int width = buffImg.getWidth();
		int height = buffImg.getHeight();

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int pixel = buffImg.getRGB(i, j);
				int alpha = (pixel >> 24) & 0xff;
				int red = (pixel >> 16) & 0xff;
				int green = (pixel >> 8) & 0xff;
				int blue = (pixel) & 0xff;

				if ((conR != red) || (conG != green) || (conB != blue)) {
					return true;
				}
			}
		}

		System.out.println("DummyTile: " + file.getAbsolutePath());
		return false;
	}

	/**
	 * 
	 * @param rootPath
	 * @param file_
	 * @param os
	 * @param cnt
	 */
	public static void saveImage(File rootPath, File file_, LEDataOutputStream os, int cnt) {
		File[] files = file_.listFiles();
		if (files == null)
			return;
		for (File file : files) {
			if (file.isDirectory()) {
				saveImage(rootPath, file, os, cnt);
			} else {
				String name = file.getName();
				if (name.indexOf("png") > -1) {
					try {

						byte[] data = new byte[(int) file.length()];
						FileInputStream fis = new FileInputStream(file);
						fis.read(data);
						fis.close();

						String fullName = file.getAbsolutePath();

						int nameLen = file.getName().length();
						int rootLen = rootPath.getAbsolutePath().length();

						String savePath = fullName.substring(rootLen, fullName.length() - nameLen) + file.getName();

						savePath = FileUt.changeSeparator(savePath);

						byte[] savePathB = savePath.getBytes();
						int strLen = savePathB.length;

						os.writeInt(strLen);
						os.write(savePathB);
						os.writeInt(data.length);
						os.write(data);
						os.flush();

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * 개개의 파일로 존재하는 이미지를 하나의 바이너리 파일로 저장
	 * 
	 * @param imgPath  타일 이미지가 존재하는 최상위 경로
	 * @param savePath 하나의 바이너가 저장될 경로
	 * @throws IOException
	 */
	public static void saveBinary(String imgPath, String savePath) throws IOException {
		File path = new File(savePath);
		System.out.println("savePath=" + path.getAbsolutePath());
		if (!path.exists()) {
			path.mkdirs();
		}
		File[] files = path.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile()) {
					String name = file.getName();
					if (name.toLowerCase().indexOf(path.getName().toLowerCase()) > -1) {
						file.delete();
					}
				}
			}
		}

		File file = new File(savePath + File.separator + path.getName());
		if (file.exists()) {
			file.delete();
		}

		FileOutputStream fos = new FileOutputStream(file);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		LEDataOutputStream los = new LEDataOutputStream(bos);

		File imgFile = new File(imgPath);
		if (imgFile.exists()) {
			saveImage(imgFile, imgFile, los, 0);
		}

		los.flush();
		bos.flush();
		fos.flush();

		los.close();
		bos.close();
		fos.close();
	}

	/**
	 * 파일로 형태로 존재하는 파일 이미지를 jdbm 파일 구조로 저장
	 * 
	 * @param imgPath    타일 이미지가 존재하는 경로
	 * @param savePath   jdbm 파일이 저장될 경로
	 * @param readOnly
	 * @param createMode
	 * @throws IOException
	 */
	public static void saveDBbinary(String imgPath, String savePath, boolean readOnly, boolean createMode)
			throws IOException {
		File path = new File(imgPath);
		//MapLog.getSCLog().debug("saveDBbinary start");
		long st = System.currentTimeMillis();
		TileDB tdbm = new TileDB(savePath, readOnly, createMode);

		saveImage(tdbm, path, path);

		tdbm.close();
		long et = System.currentTimeMillis();

//		MapLog.getSCLog().debug("src=" + imgPath + ", save=" + savePath + ", readOnly=" + readOnly + ",createMode="
//				+ createMode + ", min=" + (et - st) / 1000 / 60);

	}

	/**
	 * 하나의 바이너리 파일에 저장된 타일을 파일 시스템으로 저장
	 * 
	 * @param dbPath  하나의 바이너리 파일 경로
	 * @param outpath 타일 파일 시스템 저장 경로
	 * @throws Exception
	 */
	public static void saveToTileFromBin(String dbPath, String outpath) throws Exception {

		File file = new File(dbPath);

		File outPathFile = new File(outpath);

		if (outPathFile.exists() == false) {
			outPathFile.mkdirs();
		}

		File inputFile = new File(file.getAbsolutePath() + File.separator + file.getName());

		FileInputStream fis = new FileInputStream(inputFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		LEDataInputStream lis = new LEDataInputStream(bis);

		long st = System.currentTimeMillis();
		int cnt = 0;
		try {
			while (true) {

				int strLen = lis.readInt();
				byte[] strB = new byte[strLen];
				lis.readFully(strB);

				String key = new String(strB);
				int dataLen = lis.readInt();

				byte[] data = new byte[dataLen];
				lis.readFully(data);

				String[] pass = FileUt.getFileNameSplit(outpath + key);

				File savePath = new File(pass[0]);
				if (savePath.exists() == false) {
					savePath.mkdirs();
				}

				FileOutputStream fos = new FileOutputStream(outpath + key);
				fos.write(data);
				fos.flush();
				fos.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			lis.close();
			bis.close();
			fis.close();
		}

	}

	/**
	 * jdbm 형태로 저장된 타일을 파일 시스템으로 저장
	 * 
	 * @param dbPath  jdbm 파일 경로
	 * @param outpath 파일 시스템 경로
	 *                /v10_20140621/12/3/667/2/12_3667_2610.png 국민은행
	 */
	public static void saveToTileFromDB(String dbPath, String outpath) {

		File file = new File(dbPath);

		File outPathFile = new File(outpath);

		if (outPathFile.exists() == false) {
			outPathFile.mkdirs();
		}

		Options options = new Options();

		try (RocksDB db = RocksDB.openReadOnly(options, file.getAbsolutePath() + File.separator + file.getName())) {

			long st = System.currentTimeMillis();
			int cnt = 0;

			RocksIterator iter = db.newIterator();
			for (iter.seekToFirst(); iter.isValid(); iter.next()) {
				String key = new String(iter.key());
				byte[] data = iter.value();

				// System.out.println(key);

				if (data == null) {
					System.out.println("tile is null =" + key);
					continue;
				}

				try {

					String fileName = FileUt.changeSeparator(outpath + key);
					String[] pass = FileUt.getFileNameSplit(fileName);

					File savePath = new File(pass[0]);
					if (savePath.exists() == false) {
						savePath.mkdirs();
					}

					FileOutputStream fos = new FileOutputStream(fileName);
					fos.write(data);
					fos.flush();
					fos.close();
					cnt++;
					if (cnt % 1000 == 0) {
						System.out.println("save cnt=" + cnt + ", time=" + (System.currentTimeMillis() - st));
						st = System.currentTimeMillis();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} catch (RocksDBException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 타일 디렉토리를 바이너리로 저장
	 * 
	 */
	public static void main(String[] args) {
		// TileDBMng jt = new TileDBMng();

		try {
			System.out.println(args[0] + " , " + args[1] + " , " + args[2]);
			long st = System.currentTimeMillis();

			if (args[0].equals("binary")) {

				String imgPath = args[1];
				String savePath = args[2];
				String binType = args[3];
				String readOnly = args[4];
				String createMode = args[5];

				if (binType.equals("binary")) {
					TileDB.saveBinary(imgPath, savePath);
				} else {
					TileDB.saveDBbinary(imgPath, savePath, readOnly.equals("true") ? true : false,
							createMode.equals("true") ? true : false);
				}
			} else if (args[0].equals("tile")) {
				String binaryName = args[1];
				String savePath = args[2];

				TileDB.saveToTileFromBin(binaryName, savePath);

			} else if (args[0].equals("fromDBtoFile")) {
				String binaryName = args[1];
				String savePath = args[2];
				TileDB.saveToTileFromDB(binaryName, savePath);
			}

			long et = System.currentTimeMillis();
			System.out.println(args[0] + " , " + args[1] + " , " + args[2] + ", min=" + (et - st) / 1000 / 60);
			// jt.testLarge();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
