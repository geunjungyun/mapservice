package com.data.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.geotools.referencing.CRS;

import com.data.exception.ExceptionDesc;
import com.data.exception.FLException;
import com.data.exception.FLReadException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mapplan.LayerInfo;

/**
 * 레이어의 메타 정보를 읽고 쓰는 클래스
 *
 */
public class MetaInfo {

	String fileName = "layerInfo.json";

	/**
	 * Geotools 20.1 버전에서 epsg 코드를 저장하는 hsql에 데이타가 2015년 버전으로 최전 버전으로 업데이트가 필요함.
	 * 
	 * @param _path ex)
	 *              "F:\\dawul\\service\\dawul_service\\umd\\sido\\layerinfo.json"
	 * @param code  epsg 코드
	 * @return
	 */
	public boolean fixCRS(String _path, String code) {

		boolean result = false;
		String path = _path;

		String saveWKT = "";
		try {

			// URL url =
			// Thread.currentThread().getContextClassLoader().getResource("com/dawul/epsg.txt");
			URL url = this.getClass().getResource("./epsg.txt");
			// FileInputStream fis1 = new FileInputStream("./resources/epsg.txt");
			FileInputStream fis1 = new FileInputStream(url.getFile());
			InputStreamReader isr1 = new InputStreamReader(fis1, "utf-8");
			BufferedReader br1 = new BufferedReader(isr1);

			String seper = "\\t";
			String line = "";
			int i = 0;
			while ((line = br1.readLine()) != null) {

				String[] values = line.split("=");
				String epsgCode = values[0];
				String epsgwkt = values[1];
				if (epsgCode.equals(code)) {
					saveWKT = epsgwkt;
				}
			}

			if (saveWKT.length() > 0) {
				LayerInfo li = MetaInfo.readLayerInfo(path);
				li.setProjection(saveWKT);
				MetaInfo.writeLayerInfo(path, li);
				result = true;
			}

			br1.close();
			isr1.close();
			fis1.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	static public void writeLayerInfo(String path, LayerInfo lyif) {
		try {
			
			JAXBContext jc = JAXBContext.newInstance(lyif.getClass());
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			FileOutputStream fos = new FileOutputStream(path);
			OutputStreamWriter isr = new OutputStreamWriter(fos, "utf-8");
			
			LayerInfo ss = new LayerInfo();
			JAXBElement<LayerInfo> jaxbElement = new com.mapplan.ObjectFactory().createLayerInfo(lyif);

			m.marshal(jaxbElement, isr);
			
			isr.close();
			fos.close();
			
			/*
			FileOutputStream fis1 = new FileOutputStream(path);
			OutputStreamWriter isr1 = new OutputStreamWriter(fis1, "utf-8");
			BufferedWriter br1 = new BufferedWriter(isr1);

			Gson reqGson = new GsonBuilder().serializeSpecialFloatingPointValues().setPrettyPrinting()
					.registerTypeAdapter(XMLGregorianCalendar.class, new MetaInfo.Deserializer())
					.registerTypeAdapter(XMLGregorianCalendar.class, new MetaInfo.Serializer()).create();

			reqGson.toJson(lyif, br1);

			br1.flush();
			br1.close();
			isr1.close();
			fis1.close();
			*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static public LayerInfo readLayerInfo(String _path) throws FLException {

		LayerInfo obj = null;

		String path = _path;
		
		if(path.lastIndexOf("json") > -1) {
			
			int idx = path.lastIndexOf("json");
			path = path.substring(0, idx) + "xml";
		}
		
		File file = new File(path);

		String jsonFileName = path.replaceAll("xml", "json");

		File jsonFile = new File(jsonFileName);

		if (jsonFile.exists()) {
			try {
				FileInputStream fis1 = new FileInputStream(jsonFile);
				InputStreamReader isr1 = new InputStreamReader(fis1, "utf-8");
				BufferedReader br1 = new BufferedReader(isr1);

				Gson reqGson = new GsonBuilder().serializeSpecialFloatingPointValues().setPrettyPrinting()
						.registerTypeAdapter(XMLGregorianCalendar.class, new MetaInfo.Deserializer())
						.registerTypeAdapter(XMLGregorianCalendar.class, new MetaInfo.Serializer()).create();

				obj = (LayerInfo) reqGson.fromJson(br1, LayerInfo.class);

				br1.close();
				isr1.close();
				fis1.close();

				JAXBContext jc = JAXBContext.newInstance(com.mapplan.LayerInfo.class);
				Marshaller m = jc.createMarshaller();
				m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

				FileOutputStream fos = new FileOutputStream(path);
				OutputStreamWriter isr = new OutputStreamWriter(fos, "utf-8");
				
				JAXBElement<LayerInfo> jaxbElement = new com.mapplan.ObjectFactory().createLayerInfo(obj);

				m.marshal(jaxbElement, isr);
				
				isr.close();
				fos.close();
				jsonFile.renameTo(new File(jsonFileName+"_bak"));
				
			} catch (Exception e) {
				e.printStackTrace();
				//throw new FLReadException(ExceptionDesc.read_layerInfo);
				throw new FLReadException(e.toString());
			}

		}

		if (!file.exists()) {
			throw new FLReadException(ExceptionDesc.read_layerInfo+", "+file.getAbsolutePath());
		}

		try {

			FileInputStream fis = new FileInputStream(file);
			JAXBContext jc = JAXBContext.newInstance("com.mapplan");

			Unmarshaller u = jc.createUnmarshaller();

			JAXBElement<LayerInfo> element = (JAXBElement<LayerInfo>) u.unmarshal(fis);
			obj = element.getValue();
			fis.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new FLReadException(ExceptionDesc.read_layerInfo);
		}
		return obj;

	}

	// public class XMLGregorianCalendarConverter {

	public static class Serializer implements JsonSerializer<XMLGregorianCalendar> {
		@Override
		public JsonElement serialize(XMLGregorianCalendar xmlGregorianCalendar, Type type,
				JsonSerializationContext jsonSerializationContext) {
			return new JsonPrimitive(xmlGregorianCalendar.toXMLFormat());
		}
	}

	public static class Deserializer implements JsonDeserializer<XMLGregorianCalendar> {
		@Override
		public XMLGregorianCalendar deserialize(JsonElement jsonElement, Type type,
				JsonDeserializationContext jsonDeserializationContext) {
			try {
				return DatatypeFactory.newInstance().newXMLGregorianCalendar(jsonElement.getAsString());
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public static void main(String[] args) {
		MetaInfo mi = new MetaInfo();


	}

	// }
}
