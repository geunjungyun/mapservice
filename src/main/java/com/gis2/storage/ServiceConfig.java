package com.gis2.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.gis.protocol.MapInfo;
import com.gis.protocol.Styles;

public class ServiceConfig {
	
	
	public static String tiles = "tiles";
	public static String layers = "layers";
	
	public static String grid = "tiles/grid";
	
	public static MapInfo readMapInfo(String path) throws Exception{
		MapInfo obj = null;
		FileInputStream fis = new FileInputStream(path);
		JAXBContext jc = JAXBContext.newInstance("com.gis.protocol");
		Unmarshaller u = jc.createUnmarshaller();
		JAXBElement<MapInfo> element = (JAXBElement<MapInfo>) u.unmarshal(fis);
		obj = element.getValue();
		fis.close();
		return obj;
	}
	
	public static void writeMapInfo(String path, MapInfo mapInfo) throws Exception{
		
		File file = new File(path);
		
		File parent = file.getParentFile();
		if(!parent.exists()) {
			parent.mkdirs();
		}

		JAXBContext jc = JAXBContext.newInstance(mapInfo.getClass());
		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		FileOutputStream fos = new FileOutputStream(path);
		OutputStreamWriter isr = new OutputStreamWriter(fos, "utf-8");
		
		JAXBElement<MapInfo> jaxbElement = new com.gis.protocol.ObjectFactory().createMapInfo(mapInfo);

		m.marshal(jaxbElement, isr);
		
		isr.close();
		fos.close();
	}
	
	public static Styles readStyles(String path) throws Exception{
		Styles obj = null;
		FileInputStream fis = new FileInputStream(path);
		JAXBContext jc = JAXBContext.newInstance("com.gis.protocol");
		Unmarshaller u = jc.createUnmarshaller();
		JAXBElement<Styles> element = (JAXBElement<Styles>) u.unmarshal(fis);
		obj = element.getValue();
		fis.close();
		return obj;
	}
	
	public static void writeStyles(String path, Styles styles) throws Exception{
		
		File file = new File(path);
		
		File parent = file.getParentFile();
		if(!parent.exists()) {
			parent.mkdirs();
		}
		
		JAXBContext jc = JAXBContext.newInstance(styles.getClass());
		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		FileOutputStream fos = new FileOutputStream(path);
		OutputStreamWriter isr = new OutputStreamWriter(fos, "utf-8");
		
		JAXBElement<Styles> jaxbElement = new com.gis.protocol.ObjectFactory().createStyles(styles);

		m.marshal(jaxbElement, isr);
		
		isr.close();
		fos.close();
	}
	
	
	public static com.gis.protocol.freegis3.Styles readNewStyles(String path) throws Exception{
		com.gis.protocol.freegis3.Styles obj = null;
		FileInputStream fis = new FileInputStream(path);
		
		JAXBContext jc = getJAXBContext("com.gis.protocol.freegis3");

		Unmarshaller u = jc.createUnmarshaller();
		JAXBElement<com.gis.protocol.freegis3.Styles> element = (JAXBElement<com.gis.protocol.freegis3.Styles>) u.unmarshal(fis);
		obj = element.getValue();
		fis.close();
		
		/*
		String baseName = FilenameUtils.getBaseName(path);
		
		File styleFile = new File(path);
		
		String parrentS = styleFile.getParent();
		
		File saveFile = new File(parrentS+File.separator+baseName+".json");
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		String value = gson.toJson(obj);
		
		FileUtils.write(saveFile, value, Charset.forName("utf-8"));
		*/
		return obj;
	}
	
	public static HashMap<String, JAXBContext> jaxbs = new HashMap();
	
	public static synchronized JAXBContext getJAXBContext(String packageName) {
		
		if(!jaxbs.containsKey(packageName)) {
			try {
				JAXBContext jc = JAXBContext.newInstance(packageName);
				jaxbs.put(packageName, jc);
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return jaxbs.get(packageName);
	}

}
