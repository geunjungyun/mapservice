package com.util.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.Vector;

import com.data.util.MapLog;

public class FileUt {

	/*
	 * @FILE ID
	 */
	public static final int ID_NoDIR_NoFILE = 0;
	public static final int ID_ExistDIR_ExistFile = 1;
	public static final int ID_ExistDIR_noFile = 2;

	/*
	 * @FILE SPEC
	 */
	public static final int FILE_PATH = 0;
	public static final int FILE_NAME = 1;
	public static final int FILE_EXT = 2;
	
	public static final String FILE_DOT = ".";
	public static final String FILE_EMPTY = "";
	public static final String SEPARATOR_WIN = "\\";
	public static final String SEPARATOR_LINUX = "/";
	public static char SEPERATOR = 0;

	static{
		SEPERATOR = File.separatorChar;		
		/*
		String system = System.getProperty("os.name").toLowerCase();

		if (system.startsWith("linux"))
			SEPERATOR = '/';
		else
			SEPERATOR = '\\';
		*/
	}

	/*
	 * @FILE FILTER TYPE
	 */
	public static final int FILE_NAME_FILTER = 0;
	public static final int FILE_EXT_FILTER = 1;

	/*
	 * @FILE EXTENSION DEFINE
	 */
	public static final String DEF_EXT_SHP = ".shp";
	public static final String DEF_EXT_DBF = ".dbf";
	public static final String DEF_EXT_SHX = ".shx";
	public static final String DEF_EXT_PRJ = ".prj";
	
	public static final String DEF_EXT_UMD = ".umd";
	public static final String DEF_EXT_UAD = ".uad";
	public static final String DEF_EXT_URX = ".urx";
	
//	public static final String DEF_EXT_UBX = ".ubx"; // btree - key & data.
//	public static final String DEF_EXT_UBX2 = ".ubx2"; // btree dulplication data.
//	public static final String DEF_EXT_UBT = ".ubt"; // btree - transaction
	
	public static final String DEF_EXT_UBX = ".t"; // btree - key & data.
	public static final String DEF_EXT_UBX2 = ".i.0"; // btree dulplication data.
	public static final String DEF_EXT_UBT = ".d.0"; // btree - transaction

	
	public static final String DEF_EXT_UJD = ".ujd";
	public static final String DEF_EXT_GRP = ".grp";
	public static final String DEF_EXT_LNK = ".uln";	// umd-Alias
	public static final String DEF_EXT_XML = ".xml";

	public static final String DEF_EXT_CSV = ".csv";
	public static final String DEF_EXT_TXT = ".txt";

	public static final String DEF_EXT_JPG = ".jpg";
	public static final String DEF_EXT_GIF = ".gif";
	public static final String DEF_EXT_PNG = ".png";
	


	/**
	 * 파일을 복사한다.
	 *
	 * @param src 원본파일
	 * @param dst 생성파일
	 * @return 성공여부
	 */
	public static boolean copy(String src, String dst) {
		File srcFile = new File(src);
		if(!srcFile.exists())
			return false;

		File dstFile = new File(dst);
		/*
		if(dstFile.exists())
			return false;
		*/

		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(srcFile.getPath()));
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dstFile.getPath()));

			try {
				byte[] buff = new byte[bis.available()];
				bis.read(buff);
				bos.write(buff);
				bos.flush();
			}
			finally {
				bos.close();
				bos = null;
				bis.close();
				bis = null;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * isFile 시리즈
	 *
	 * 파일이름을 보고 어떤파일인지 알아낸다.
	 */
	public static boolean isGrpFile(String fileName) {
		return isFile(fileName, DEF_EXT_GRP);
	}
	/**
	 *  파일확장자로 종류를 알아낸다.
	 * @param fileName 파일 풀패스
	 * @param DEF_EXT 확장자
	 * @return
	 */
	public static boolean isFile(String fileName, String DEF_EXT) {
		return fileName.toLowerCase().endsWith(DEF_EXT);
	}


	/**
	 * 입력한 파일의 대한 유무검사.
	 *
	 * @param fileName String 타입의 파일
	 * @return true 파일이 존재할때; false 파일이 존재하지 않을때.
	 */
	public static boolean checkFile(String fileName){
		//System.out.println("checkFile () " +fileName);
		File file = new File(fileName);
		if(file.exists() == false || file.length() == 0){
			return false;
		}
		else{
			return true;
		}
		//return file.exists() ? true : false;
	}

	/**
	 * .shp/.shx/.dbf 파일의 유무를 검사.
	 *
	 * @param fileName String 타입의 파일
	 * @return true 모두 있을때; false 하나라도 없을때
	 *
	 */
	public static boolean checkShpFile(String fileName){
		if(fileName.toLowerCase().endsWith(DEF_EXT_SHP)){
			if(!checkFile(fileName))
				return false;
			if(!checkFile(changeFileExt(fileName, DEF_EXT_DBF)))
				return false;
			if(!checkFile(changeFileExt(fileName, DEF_EXT_SHX)))
				return false;
		}else{
			String file = fileName + DEF_EXT_SHP;
			if(!checkFile(file))
				return false;
			if(!checkFile(changeFileExt(file, DEF_EXT_DBF)))
				return false;
			if(!checkFile(changeFileExt(file, DEF_EXT_SHX)))
				return false;
		}
		return true;
	}
	
    public static Vector<String> getShapeFileList(File _file){
    	Vector<String> shpFiles = new Vector();
    	
    	File[] files = _file.listFiles();
    	for(File file : files){
    		String name = file.getName();
    		int idx = name.indexOf(".");
    		if(idx <=0){
    			continue;
    		}
    		name = name.substring(0, idx);
    		boolean isExist = false;
    		for(String str : shpFiles){
    			if(str.equals(name)){
    				isExist = true;
    			}
    		}
    		if(isExist == true){
    			continue;
    		}
    		String fullName = _file.getPath()+"/"+ name;
    		int cnt = 0;
    		if((new File(fullName+".shp")).exists() == true){
    			cnt++;
    		}
    		if((new File(fullName+".dbf")).exists() == true){
    			cnt++;
    		}
    		if((new File(fullName+".shx")).exists() == true){
    			cnt++;
    		}
    		if(cnt > 2){
    			shpFiles.add(name);
    		}
    	}
    	return shpFiles;
    }
	

	/**
	 * .umd/.uad 파일의 유무를 검사.
	 *
	 * @param fileName String 타입의 파일
	 * @return true 모두 있을때; false 하나라도 없을때
	 *
	 */
	public static boolean checkUmdFile(String fileName){
		if(fileName.toLowerCase().endsWith(DEF_EXT_UMD)){
			if(!checkFile(fileName))
				return false;
			if(!checkFile(changeFileExt(fileName, DEF_EXT_UAD)))
				return false;
		}else{
			String file = fileName + DEF_EXT_UMD;
			if(!checkFile(file))
				return false;
			if(!checkFile(changeFileExt(fileName, DEF_EXT_UAD)))
				return false;
		}
		return true;
	}
	
	/**
	 * .umd/.uad 파일의 유무를 검사.
	 *
	 * @param fileName String 타입의 파일
	 * @return true 모두 있을때; false 하나라도 없을때
	 *
	 */
	public static boolean checkUmdBreeFile(String name, String colName){
		
//		public static final String DEF_EXT_UBX = ".ubx"; // btree - key & data.
//		public static final String DEF_EXT_UBX2 = ".ubx2"; // btree dulplication data.
//		public static final String DEF_EXT_UBT = ".ubt"; // btree - transaction

		
		String ubxName = getRemoveExt(name)+"_"+colName.toLowerCase()+"_idx"+DEF_EXT_UBX;
		String ubx2Name = getRemoveExt(name)+"_"+colName.toLowerCase()+"_idx"+DEF_EXT_UBX2;
		String ubtName = getRemoveExt(name)+"_"+colName.toLowerCase()+"_idx"+DEF_EXT_UBT;

		if(!checkFile(ubxName))
			return false;

		if(!checkFile(ubtName))
			return false;

//		if(!checkFile(ubtName))
//			return false;

		
		return true;
	}


	/**
	 * 파일의 종류를 구별해 낸다.
	 *
	 * @param s 파일의 대한 속성을
	 * @return ID_ExistDIR_noFile 디렉토리; ID_ExistDIR_ExistFile 파일; ID_NoDIR_NoFILE 존재하지 않음.
	 */
	public static int idFile(String s){
		int rs = -1;
		File file = new File(s);
		if(file.exists()){
			if(file.isDirectory()){
				rs = ID_ExistDIR_noFile;
			}else{
				rs = ID_ExistDIR_ExistFile;
			}
		}else{
			rs = ID_NoDIR_NoFILE;
		}
		return rs;
	}

	@Deprecated
	public static FilenameFilter simpleFilter(String file_ext){
		final String s = file_ext;
		FilenameFilter rs = new FilenameFilter(){
			public boolean accept(File dir, String name) {
				return name.endsWith(s);
			}
		};
		return rs;
	}

	@Deprecated
	public static String[] getFileList(String dir, String filter){
		File d = new File(dir);
		FilenameFilter f = simpleFilter(filter);
		return d.list(f);
	}

	/**
	 * 원래 파일의 확장자(ext) 를 변경
	 *
	 * @param file 원래 파일
	 * @param fileExt 변경할 확장자 명
	 * @return 변경된 파일
	 */
	public static String changeFileExt(String file, String fileExt){
		StringBuffer rs = null;
		if(file != null && fileExt != null){
			rs = new StringBuffer();
			String[] k = getFileNameSplit(file);
			rs.append(k[FILE_PATH]);
			rs.append(k[FILE_NAME]);
			if(fileExt.contains(FILE_DOT)){
				rs.append(fileExt);
			}else{
				rs.append(FILE_DOT);
				rs.append(fileExt);
			}
			k = null;
		}
		//System.out.println("changeFileExt " + rs.toString());
		return rs.toString();
		//2012.9.17 변경
		//return rs.toString().toLowerCase();
	}

	/**
	 * 원래 파일의 파일명(name)을 바꿈
	 *
	 * @param file 원래 파일
	 * @param fileName 변경할 파일명
	 * @return 변경된 파일
	 */
	public static String changeFileName(String file, String fileName){
		StringBuffer rs = null;
		if(file != null && fileName != null){
			rs = new StringBuffer();
			String[] k = getFileNameSplit(file);
			rs.append(k[FILE_PATH]);
			rs.append(fileName);
			rs.append(k[FILE_EXT]);
			k = null;
		}
		return rs.toString();
	}

	/**
	 * 원래 파일의 파일명(파일명 앞)에 더함
	 *
	 * @param file 원래 파일
	 * @param insert 변경할 파일명
	 * @return 변경된 파일
	 */
	public static String insertFileName(String file, String insert){
		StringBuffer rs = null;
		if(file != null && insert != null){
			rs = new StringBuffer();
			String[] k = getFileNameSplit(file);
			rs.append(k[FILE_PATH]);
			rs.append(insert);
			rs.append(k[FILE_NAME]);
			rs.append(k[FILE_EXT]);
			k = null;
		}
		return rs.toString();
	}
	
	public static boolean createPath(String path){
		File pathF = new File(path);
		if(!pathF.exists()){
			return pathF.mkdirs();
		}
		return false;
	}

	/**
	 * 원래 파일의 파일명(파일명 뒤)에 더함
	 *
	 * @param file 원래 파일
	 * @param append 변경할 파일명
	 * @return 변경된 파일
	 */
	public static String appendFileName(String file, String append){
		StringBuffer rs = null;
		if(file != null && append != null){
			rs = new StringBuffer();
			String[] k = getFileNameSplit(file);
			rs.append(k[FILE_PATH]);
			rs.append(k[FILE_NAME]);
			rs.append(append);
			rs.append(k[FILE_EXT]);
			k = null;
		}
		return rs.toString();
	}


	/**
	 * 원래 파일의 경로를 변경
	 *
	 * @param file 원래 파일
	 * @param filePath 변경할 경로명
	 * @return 변경된 파일
	 */
	public static String changeFilePath(String file, String filePath){
		StringBuffer rs = null;
		if(file != null && filePath != null){
			rs = new StringBuffer();
			String[] k = getFileNameSplit(file);
			rs.append(filePath.replace(SEPARATOR_WIN, SEPARATOR_LINUX));
			rs.append(k[FILE_NAME]);
			rs.append(k[FILE_EXT]);
			k = null;
		}
		return rs.toString();
	}
	
	/**
	 * 윈도우 경로 구분자를 유닉스 경로 구분자로 변경
	 * @param path
	 * @return
	 */
	public static String changeSeparator(String path){
		path = path.replace(SEPARATOR_WIN, SEPARATOR_LINUX);
		return path; 
	}


	/**
	 * 경로에 있는 파일들을 얻음
	 *
	 * @param dir 경로명
	 * @return 파일들
	 */
	public static String[] getFiles(String dir){
		return getFiles(dir, null, 0);
	}
	
	/**
	 * 현재 디렉토리에 name이름으로 존재하는 파일 리스트 리턴
	 * @return
	 */
	public static Vector<File> getShapeEXTList(String path, String name){
		Vector value = new Vector();
		File pathF = new File(path);
		File[] files = pathF.listFiles();
    	for(File file : files){
    		String fileName = file.getAbsolutePath();
    		String[] splits = FileUt.getFileNameSplit(fileName);
    		String nameOnly = splits[FileUt.FILE_NAME];
    		if(nameOnly.toLowerCase().equals(name.toLowerCase())){
    			value.add(file);
    		}
    	}
    	return value;
	}


	/**
	 * 경로에 있는 선택된 확장자들로 구성된 파일들을 얻음
	 *
	 * @param dir 경로명
	 * @param filter_FileExt 확장자명
	 * @return 파일들
	 */
	public static String[] getFiles(String dir, String filter_FileExt){
		return getFiles(dir, filter_FileExt, FILE_EXT_FILTER);
	}

	/**
	 * 경로에 있는 파일들을 필터를 사용하여 얻음.
	 *
	 * @param dir 경로명
	 * @param filter 선택한 필터
	 * @param filterType 사용될 필터의 종류
	 * @return 파일들
	 */
	public static String[] getFiles(String dir, String filter, int filterType){
		String[] rs = null;
		File f = new File(dir);
//		String separator = null;

		if(f.exists()){
			if(f.isDirectory()){
				// dir
				if(filter != null && !filter.equals("")){
					final String ss = filter;
					FilenameFilter ff = null;
					switch (filterType) {
						case FILE_EXT_FILTER:
							ff = new FilenameFilter(){
								public boolean accept(File file, String s) {
									return s.toLowerCase().endsWith(ss);
								}
							};
						break;
						case FILE_NAME_FILTER:
							ff = new FilenameFilter(){
								public boolean accept(File file, String s) {
									return s.toLowerCase().contains(ss);
								}
							};
						break;
						default:
							break;
					}
					rs = f.list(ff);
					if(!(dir.endsWith(SEPARATOR_LINUX) || dir.endsWith(SEPARATOR_WIN)))
						dir = dir + System.getProperty("file.separator");
					int c = 0;
					for(String k : rs){
						rs[c] = dir + k;
						c++;
					}
				}
				else{
					rs = f.list();
					int c = 0;
					if(!(dir.endsWith(SEPARATOR_LINUX) || dir.endsWith(SEPARATOR_WIN)))
						dir = dir + System.getProperty("file.separator");
					for(String k : rs){
						rs[c] = dir + k;
						c++;
					}
				}
			}else{
				// file
				rs = new String[1];
				rs[0] = dir;
			}
		}
//		f = null;
		if(rs.length == 0){
			rs = null;
		}
		f = null;
		return rs;
	}

	
	/**
	 * 파일 경로를 경로, 이름, 확장명으로 구분하여 리턴함. 0:path, 1:name, 2:ext
	 * @param file
	 * @return
	 */
	public static String[] getFileNameSplit(String file){
		String[] rs = null;
		File f = new File(file);
//		if(f.exists() && f.isFile()){
			rs = new String[3];
			String str = f.getAbsolutePath();
			int idx_end_dir = str.lastIndexOf(System.getProperty("file.separator")) + 1;
			int idx_start_ext = str.lastIndexOf(FILE_DOT);
			rs[FILE_PATH] = str.substring(0, idx_end_dir).replace(SEPARATOR_WIN, SEPARATOR_LINUX);
//			rs[FILE_NAME] = str.substring(idx_end_dir, idx_start_ext);
//			rs[FILE_EXT] = str.substring(idx_start_ext);

			if(idx_start_ext < idx_end_dir){
				rs[FILE_NAME] = str.substring(idx_end_dir, str.length());
				rs[FILE_EXT] = FILE_EMPTY;
			}else{
				rs[FILE_NAME] = str.substring(idx_end_dir, idx_start_ext);
				rs[FILE_EXT] = str.substring(idx_start_ext);
			}
//		}
		return rs;
	}


	/**
	 * 선택한 파일의 파일명과 확장자를 얻음 (경로 제외)
	 *
	 * @param absolutefile 절대경로가 포함된 파일
	 * @return 파일명 확장자
	 */
	public static String getFileName(String absolutefile){
		StringBuffer rs = new StringBuffer();
		String[] str = getFileNameSplit(absolutefile);
		rs.append(str[FILE_NAME]);
		rs.append(str[FILE_EXT]);
		str = null;
		return rs.toString();
	}

	/**
	 * 선택한 파일의 파일명만 얻음
	 *
	 * @param file 파일
	 * @return 파일이름
	 */
	public static String getOnlyName(String file){
		String[] str = getFileNameSplit(file);
		str[FILE_PATH] = null;
		str[FILE_EXT] = null;
		return str[FILE_NAME];
	}


	/**
	 * 선택한 파일의 확장자만 얻음
	 *
	 * @param file 파일
	 * @return 확장자
	 */
	public static String getOnlyExt(String file){
		String[] str = getFileNameSplit(file);
		str[FILE_PATH] = null;
		str[FILE_NAME] = null;
		return str[FILE_EXT];
	}

	/**
	 * 선택한 파일의 경로만 얻음
	 *
	 * @param file 파일
	 * @return 경로명
	 */
	public static String getOnlyPath(String file){
		String[] str = getFileNameSplit(file);
		str[FILE_EXT] = null;
		str[FILE_NAME] = null;
		return str[FILE_PATH];
	}

	/**
	 * 확장자를 제거한 파일 경로를 얻음
	 * @param file 파일
	 * @return 경로명 + 파일명
	 */
	public static String getRemoveExt(String file) {
		return getOnlyPath(file) + getOnlyName(file);
	}
	

	public static boolean deleteFile(String file){

		boolean ret = false;

		if (file == null) return false;

		File f = new File(file);
		if (f == null) return false;

		try{
			ret = f.delete();
		}catch(Exception e){e.printStackTrace();}

		return ret;

	}
	
	
	
	public static boolean deleteUMD(String umdFile, String[] fieldNames){
		File file = new File(umdFile);
		String fullFile = file.getAbsolutePath();
		String path = file.getParent();
		String name = file.getName();
		
		String layerName = name.substring(0, name.indexOf("."));
		
		File[] files = (new File(path)).listFiles();
		
		for(int i=0; i<files.length; i++){
			File temp = files[i];
			String tempS = temp.getAbsolutePath();
			String savedName = FileUt.getOnlyName(tempS);
			String createName = FileUt.getOnlyName(umdFile);
			if(savedName.equals(createName)){
				String ext = FileUt.getOnlyExt(tempS);
				if(FileUt.DEF_EXT_UAD.indexOf(ext) > -1 ||  FileUt.DEF_EXT_UMD.indexOf(ext) > -1
						|| FileUt.DEF_EXT_URX.indexOf(ext) > -1){
					if(temp.delete() == true){
						MapLog.getSCLog().debug("delete file name = " + tempS);
					}
					else{
						MapLog.getSCLog().error("error delete file name = " + tempS);
					}
				}
			}
			else{
				if(fieldNames != null){
					for(int j=0; j<fieldNames.length; j++){
						String idxName = createName.toLowerCase()+"_"+fieldNames[j].toLowerCase() +"_idx";
						
						if(savedName.indexOf(idxName) == 0){
							if(temp.delete() == true){
								
								MapLog.getSCLog().debug("delete file name = " + tempS);
							}
							else{
								MapLog.getSCLog().error("error delete file name = " + tempS);
							}
						}
						
						String fullidxName = createName.toLowerCase()+"_"+fieldNames[j].toLowerCase() +"_fulltext_idx";
						
						if(savedName.indexOf(fullidxName) == 0){
							if(temp.delete() == true){
								MapLog.getSCLog().debug("delete file name = " + tempS);
							}
							else{
								MapLog.getSCLog().error("error delete file name = " + tempS);
							}
						}
					}
				}
			}
		}
		
		return true;
	}
	
	public static boolean deleteUMD(String umdFile){
		File file = new File(umdFile);
		String fullFile = file.getAbsolutePath();
		String path = file.getParent();
		String name = file.getName();
		
		String layerName = name.substring(0, name.indexOf("."));
		
		File[] files = (new File(path)).listFiles();
		
		for(int i=0; i<files.length; i++){
			File temp = files[i];
			String tempS = temp.getAbsolutePath();
			String savedName = FileUt.getOnlyName(tempS);
			String createName = FileUt.getOnlyName(umdFile);
			if(savedName.indexOf(createName) >= 0){
				if(temp.delete() == true){
					System.out.println("delete file name = " + tempS);
				}
				else{
					System.out.println("error delete file name = " + tempS);
				}
			}
		}
		
		return true;
	}
	
	public static byte[] readFile(File file) throws Exception{
		byte[] data = new byte[(int)file.length()];
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		bis.read(data);
		bis.close();
		fis.close();
		return data;
	}
	
	public static boolean nioDirCopy(File src, File dst){
		
		boolean result = true;
		
		
		if(!src.exists()) {
			return false;
		}

		if(!dst.exists()) {
			dst.mkdirs();
		}
		
		File[] files = src.listFiles();
		
		for(File file : files) {
			
			if(file.isDirectory() ) {
				String dirN = file.getName();
				File dstN = new File(dst.getAbsoluteFile()+File.separator+dirN);
				FileUt.nioDirCopy(file, dstN);
			}
			else {
				String name = file.getName();
				FileUt.nioCopy(file.getAbsolutePath(), dst.getAbsolutePath()+File.separator+name);
			}
		}
		
		return true;
	}
	
	public static boolean nioDirMove(File src, File dst){
		
		boolean result = true;
		
		
		if(!src.exists()) {
			return false;
		}

		if(!dst.exists()) {
			dst.mkdirs();
		}
		
		File[] files = src.listFiles();
		
		for(File file : files) {
			
			if(file.isDirectory() ) {
				String dirN = file.getName();
				File dstN = new File(dst.getAbsoluteFile()+File.separator+dirN);
				FileUt.nioDirMove(file, dstN);
				file.delete();
			}
			else {
				String name = file.getName();
				FileUt.nioCopy(file.getAbsolutePath(), dst.getAbsolutePath()+File.separator+name);
				
				file.delete();
			}
		}
		
		src.delete();
		
		return true;
	}
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	public static boolean checkRoot(String path) {
		
		boolean result = false;
		
		String sub1 = path.substring(0, 1);
		String sub2 = path.substring(1, 2);
		
		if(sub1.equals("/") || sub1.equals("\\")) {
			result = true;
		}
		if(sub2.equals(":")) {
			result = true;
		}
		
		return result;
	}


	public static void nioCopy(String srcFile, String dstFile){
		String srcF = srcFile;
		String dstF = dstFile;

		FileInputStream fis = null;
		FileOutputStream fos = null;
		FileChannel inChannel = null;
	    FileChannel outChannel = null;

	    try {
	    	fis = new FileInputStream(srcF);
	    	fos = new FileOutputStream(dstF);
	    	inChannel = fis.getChannel();
	    	outChannel = fos.getChannel();

	        // magic number for Windows, 64Mb - 32Kb
	        //int maxCount = (64 * 1024 * 1024) - (32 * 1024);
	    	int maxCount = (64 * 1024 * 1024) - (32 * 1024);
	        long size = inChannel.size();
	        long position = 0;
	        while (position < size) {
	            position += inChannel.transferTo(position, maxCount, outChannel);
	        }
	    } catch (IOException e) {
	    	//MapLog.getSCLog().debug(e);
	    	e.printStackTrace();
	    } finally {
	    	try {
	    		if (inChannel != null){
	    			inChannel.close();
	    			inChannel = null;
	    		}
	    		if (outChannel != null){
	    			outChannel.close();
	    			outChannel = null;
	    		}
	    		if(fos != null){
	    			fos.close();
	    			fos = null;
	    		}
	    		if(fis != null){
	    			fis.close();
	    			fis = null;
	    		}
			} catch (Exception ee) {
				ee.printStackTrace();
				//MapLog.getSCLog().debug(ee);
			}
	    }

	}
	
	public static ByteBuffer bb = ByteBuffer.allocateDirect(300*1024);
	
	public static byte[] nioCopy(String srcFile){
		String srcF = srcFile;
		//String dstF = dstFile;

		FileInputStream fis = null;
		FileOutputStream fos = null;
		FileChannel inChannel = null;
	    //FileChannel outChannel = null;

	    try {
	    	fis = new FileInputStream(srcF);
	    	//fos = new FileOutputStream(dstF);
	    	inChannel = fis.getChannel();
	    	//outChannel = fos.getChannel();

	        // magic number for Windows, 64Mb - 32Kb
	        //int maxCount = (64 * 1024 * 1024) - (32 * 1024);
	    	
	    	//inChannel.tr
	    	
	    	
	    	//ByteChannel bc = null;
	    	
	    	//bb.flip();
	    	
	    	
	    	
	    	int maxCount = (200 * 1024);
	        long size = inChannel.size();
	        long position = 0;
	        while (position < size) {
	        	
	        	position += inChannel.read(bb);
	            //position += inChannel.transferTo(position, maxCount, bc);
	        }
	        
	        byte[] data = new byte[(int)size];
	        bb.rewind();
	        
	        bb.get(data);
	        
	        bb.rewind();
	        
	        return data;
	        
	    } catch (IOException e) {
	    	//MapLog.getSCLog().debug(e);
	    	e.printStackTrace();
	    } finally {
	    	try {
	    		if (inChannel != null){
	    			inChannel.close();
	    			inChannel = null;
	    		}
//	    		if (outChannel != null){
//	    			outChannel.close();
//	    			outChannel = null;
//	    		}
	    		if(fos != null){
	    			fos.close();
	    			fos = null;
	    		}
	    		if(fis != null){
	    			fis.close();
	    			fis = null;
	    		}
			} catch (Exception ee) {
				ee.printStackTrace();
				//MapLog.getSCLog().debug(ee);
			}
	    }
	    
	    return null;

	}



}
