import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.io.FileUtils;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.simple.SimpleSchema;
import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryType;

import com.data.file.FileLayer;
import com.data.file.FileLayer.SimpleFeatureIterator;
import com.gis2.servlet.MapService;
import com.mapplan.Field;
import com.mapplan.FieldType;
import com.mapplan.LayerInfo;
import com.util.io.FileUt;
import com.util2.thread.CarMultiPoolManager;
import com.util2.thread.CarMultiThread;
import com.util2.thread.NotCreateException;

//-p D:\coops\service\log\2021042220.log^1|UQA240|0|D:\coops\service\20200331\layers|20210231|D:\coops\service\analysis\UQA240|1|*|1|25^1|UQA121|0|D:\coops\service\20200331\layers|20210231|D:\coops\service\analysis\UQA121|2|*|2|25^1|UQA240,UQA121|1|D:\coops\service\20200331\layers|20210231|D:\coops\service\analysis\UQA240_UQA121|3|*|1|25
public class Analysis {

	HashMap<String, FileLayer> hm = new HashMap<String, FileLayer>();

	int maxStringLength = 65535;
	// int maxStringLength = 1000;

	public static void main(String[] args) {
		Analysis al = new Analysis();
		if (args.length == 1) {
			al.run(args[0], null);
		} else if (args.length == 2) {
			if (args[0].equals("-p")) {
				al.run(null, args[1]);
			}
		}	
		System.exit(0);
	}

	public void run(String path, String data) {
		System.out.println("start run");
		//ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
		CarMultiPoolManager cmpm = null;
		try {
			cmpm = CarMultiPoolManager.getInstance();
			cmpm.initMng(5, 10000);
		} catch (NotCreateException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		
		Vector<Job> jobs = this.read(path, data);

		try {
			for (Job job : jobs) {
				if (job.ing) {
					long st = System.currentTimeMillis();

					String ucode = null;

					for (int m = 0; m < job.codes.length; m++) {
						if (ucode == null) {
							ucode = job.codes[m];
						} else {
							ucode = ucode + "," + job.codes[m];
						}
					}
					// System.out.println("job id=" + job.id+", admin="+job.selectAdmin);

					String logData = "";
					String ingString = "완료";
					for (int i = 0; i < jobs.size(); i++) {
						Job tjob = jobs.get(i);
						String joblog = tjob.id + "\t" + tjob.dstPath + "\t";

						int nowJobId = Integer.parseInt(job.id);
						int jobId = Integer.parseInt(tjob.id);

						if (jobId == nowJobId) {
							joblog += "진행";
						} else if (jobId < nowJobId) {
							joblog += "완료";
						} else {
							joblog += "대기";
						}
						joblog += "\n";
						logData += joblog;
					}
					File logFile = new File(this.logPath);
					File parentLogFile = logFile.getParentFile();
					if (!parentLogFile.exists()) {
						parentLogFile.mkdirs();
					}
					FileUtils.write(logFile, logData, Charset.forName("utf-8"), false);

					int id = 1;

					FileLayer dongLayer = null;

					FileLayer jijukLayer = null;

					Vector<FileLayer> layers = new Vector();

					boolean geojsonSave = true;
					boolean shapeSave = true;

					shapeSave = job.isShape;
					geojsonSave = job.isGeoJson;

					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					format.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

					String createDate = format.format(new Date(System.currentTimeMillis()));
					// System.out.println("jobWasExecuted time=" + format.format(new
					// Date(System.currentTimeMillis())));

					File srcF = new File(job.srcPath);
					File[] srcFs = srcF.listFiles();

					for (File layer : srcFs) {
						if (layer.isDirectory()) {
							// if(layer.getName().toLowerCase().equals("fd")) {
							if (layer.getName().toLowerCase().equals(job.selectAdmin.toLowerCase())) {
								String fdPath = layer.getAbsolutePath();
								if (this.hm.containsKey(fdPath)) {
									dongLayer = this.hm.get(fdPath);
								} else {
									dongLayer = new FileLayer(fdPath, null);
									this.hm.put(fdPath, dongLayer);
								}
							} else if (layer.getName().toLowerCase().equals("fa")) {
								String fdPath = layer.getAbsolutePath();
								if (this.hm.containsKey(fdPath)) {
									jijukLayer = this.hm.get(fdPath);
								} else {
									jijukLayer = new FileLayer(fdPath, null);
									this.hm.put(fdPath, jijukLayer);
								}

							} else {
								String lyN = layer.getName();
								String lyPath = layer.getAbsolutePath();
								if (lyN.startsWith("a") || lyN.startsWith("b") || lyN.startsWith("c")
										|| lyN.startsWith("d") || lyN.startsWith("e")) {

									FileLayer addLayer = null;
									if (this.hm.containsKey(lyPath)) {
										addLayer = this.hm.get(lyPath);
									} else {
										addLayer = new FileLayer(lyPath, null);
										this.hm.put(lyPath, addLayer);
									}
									layers.add(addLayer);
								}
							}
						}
					}

					// File newFile = new File(dstFile);
					File dstFile = new File(job.dstPath + ".shp");

					File parentFile = dstFile.getParentFile();

					if (!parentFile.exists()) {
						parentFile.mkdirs();
					}

					SimpleFeatureType type =
							// DataUtilities.createType(dstFile.getName(),
							// "the_geom:MultiPolygon,code:String,cdn:String,area:Double,pnus:String");
							DataUtilities.createType(dstFile.getName(),
									"the_geom:MultiPolygon," + "ctime:String," + "jobNum:String," + "id:String,"
											+ "code:String," + "cdn:String," + "p_area:Double," + "distance:Double,"
											+ "pnus:String," + "ctDate:String," + "pnusCnt:Integer");

					// SimpleFeatureType type = this.getType();

					SimpleFeatureBuilder savesfb = new SimpleFeatureBuilder(type);

					Transaction transaction = null;
					SimpleFeatureStore featureStore = null;

					FileOutputStream fosa = null;
					OutputStreamWriter osra = null;
					BufferedWriter bwa = null;

					if (shapeSave) {

						ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

						Map<String, Serializable> params = new HashMap<String, Serializable>();
						params.put("url", dstFile.toURI().toURL());
						params.put("create spatial index", Boolean.TRUE);

						ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory
								.createNewDataStore(params);
						newDataStore.setCharset(Charset.forName("utf-8"));
						newDataStore.createSchema(type);

						SimpleFeatureType savesft = newDataStore.getSchema();

						transaction = new DefaultTransaction("create");
						String typeName = newDataStore.getTypeNames()[0];
						featureStore = (SimpleFeatureStore) newDataStore.getFeatureSource(typeName);
						featureStore.setTransaction(transaction);

						String textPath = dstFile.getAbsolutePath();
						// String name = dstFile.getName();
						int idx = textPath.lastIndexOf(".");

						if (job.pnu) {
							fosa = new FileOutputStream(textPath.substring(0, idx) + ".txt");
							osra = new OutputStreamWriter(fosa, "utf-8");
							bwa = new BufferedWriter(osra);
						}
						// \t
						// bwa.write("위도,경도,코드,시도,경로,id\n");

					}
					// System.out.println("job admin=" + job.selectAdmin);
					SimpleFeatureType sftTemp = dongLayer.getSimpleFeatureType();

					String codeFieldName = "";

					if (sftTemp.indexOf("emd_cd") > -1) {
						codeFieldName = "emd_cd";
					} else if (sftTemp.indexOf("adm_sect_c") > -1) {
						codeFieldName = "adm_sect_c";
					}

					String codeNameFieldName = "";

					if (sftTemp.indexOf("sgg_nm") > -1) {
						codeNameFieldName = "sgg_nm";
					} else if (sftTemp.indexOf("emd_nm") > -1) {
						codeNameFieldName = "emd_nm";
					}

					ListFeatureCollection fc = new ListFeatureCollection(type);
					
					SimpleFeatureIterator it = dongLayer.iterator();
					
					int dongIdx = 0;
					int dongCnt = (int)dongLayer.getObjectSize();
					
					while (it.hasNext()) {
						dongIdx++;
						SimpleFeature sf = it.next();
						
						Geometry geoDong = ((Geometry) sf.getDefaultGeometry()).buffer(0.0);

						Envelope env = geoDong.getEnvelopeInternal();

						String code = "";
						if (sf.getFeatureType().indexOf(codeFieldName) > -1) {
							code = (String) sf.getAttribute(codeFieldName);
						}

						if (job.hcode.length() > 0 && !code.toLowerCase().startsWith(job.hcode.toLowerCase())) {
							continue;
						}

						String name = "";
						if (sf.getFeatureType().indexOf(codeNameFieldName) > -1) {
							name = (String) sf.getAttribute(codeNameFieldName);
						}

						if (job.selectAdmin.equals("fd")) {
							long sst = System.currentTimeMillis();
							Geometry all = null;

							boolean isRun = true;

							for (int k = 0; k < job.codes.length; k++) {
								long cst = System.currentTimeMillis();
								// 하나의 코드에 교차하는 영역
								Geometry unionG = null;

								Vector<Geometry> codeAll = new Vector();

								for (FileLayer layer : layers) {

									SimpleFeatureCollection sfc = null;

									try {
										sfc = layer.getFeatures(env.getMinX(), env.getMinY(), env.getMaxX(),
												env.getMaxY(), job.codes[k]);
									} catch (Exception e) {
										System.out.println(
												"error layerName = " + layer.getName() + ", code = " + job.codes[0]);
										e.printStackTrace();
										continue;
									}

									org.geotools.data.simple.SimpleFeatureIterator sfi = sfc.features();

									while (sfi.hasNext()) {
										SimpleFeature feature = sfi.next();
										Geometry jiguG = (Geometry) feature.getDefaultGeometry();

										if (jiguG instanceof LineString || jiguG instanceof MultiLineString) {
											continue;
										}

										Geometry temp = null;

										try {
											temp = jiguG.intersection(geoDong);
										} catch (Exception e) {
											jiguG = jiguG.buffer(0.0);
											temp = jiguG.intersection(geoDong.buffer(0.0000001));
										}

										Geometry inters = this.getPolygon2(temp);

										if (inters == null || inters.isEmpty()) {
											continue;
										}

										inters = this.getPolygon2(inters.buffer(0.0));

										codeAll.add(inters);

									}

								}

								if (codeAll.size() > 0) {
									// System.out.println("codeAll size = " + codeAll.size()+", layerName=");
									Geometry[] array = new Geometry[codeAll.size()];

									for (int kk = 0; kk < codeAll.size(); kk++) {
										array[kk] = codeAll.get(kk);
									}

									GeometryCollection gc = gft.createGeometryCollection(array);

									Geometry tempp = gc.buffer(0.0);
									// unionG = this.getPolygon(tempp, -1, job.exceptionArea);
									unionG = this.getPolygon2(tempp);
								}

								if (unionG == null) {
									isRun = false;
									break;
								}

								if (all == null) {
									all = unionG;
								} else {
									try {
										Geometry allGeo = all.intersection(unionG);
										all = this.getPolygon2(allGeo);
									} catch (Exception e) {
										unionG = unionG.buffer(0.0000001);
										Geometry allGeo = all.intersection(unionG);
										all = this.getPolygon2(allGeo);
									}
								}
								long cet = System.currentTimeMillis();
								//System.out.println("code = " + (cet - cst) / 1000.0 + " sec");
							}

							if (isRun == false) {
								continue;
							}

							if (all == null || all.isEmpty() || all.getArea() == 0) {
								continue;
							}

							long eet = System.currentTimeMillis();

							//System.out.println(ucode + ", layer create time = " + (eet - sst) / 1000.0 / 60.0 + " min" + ", size=" + all.getNumGeometries()+", code="+code);

							if (all == null || all.isEmpty()) {
								continue;
							}
							
							int pcnt = all.getNumGeometries();
							for (int mm = 0; mm < pcnt; mm++) {
								long pnst = System.currentTimeMillis();
								
								
								Geometry subG = all.getGeometryN(mm);

								if (subG.isEmpty()) {
									continue;
								}

								String pnuTemp = "";
								Vector<String> pnuV = new Vector();

								if (job.pnu) {
									Envelope allEnv = subG.getEnvelopeInternal();

									Vector idxs = jijukLayer.getFeatureIdxs(allEnv.getMinX(), allEnv.getMinY(),
											allEnv.getMaxX(), allEnv.getMaxY());
									
									for (int i = 0; i < idxs.size(); i++) {
										long jst = System.currentTimeMillis();

										Long idx = (Long) idxs.get(i);
										SimpleFeature jijukSf = jijukLayer.readFeature(idx);
										Geometry jijukGeo = (Geometry) jijukSf.getDefaultGeometry();

										String jpnu = (String) jijukSf.getAttribute("pnu");
										if (jpnu == null || !jpnu.startsWith(code)) {
											continue;
										}

										String md = "";

										if (job.selectAdmin.equals("fc")) {
											if (subG.intersects(jijukGeo)) {
												md = "intersects";
												if (pnuTemp.length() == 0) {
													pnuTemp = jpnu;
												} else {
													if (pnuTemp.length() + jpnu.length() > this.maxStringLength) {
														pnuV.add(pnuTemp);
														pnuTemp = "";
														pnuTemp = jpnu;
													} else {
														pnuTemp += ("," + jpnu);
													}
												}
											}
										} else {

											if (subG.contains(jijukGeo)) {
												md = "contains";
												if (pnuTemp.length() == 0) {
													pnuTemp = jpnu;
												} else {
													if (pnuTemp.length() + jpnu.length() > this.maxStringLength) {
														pnuV.add(pnuTemp);
														pnuTemp = "";
														pnuTemp = jpnu;
													} else {
														pnuTemp += ("," + jpnu);
													}
												}
											} else if (subG.intersects(jijukGeo)) {
												md = "intersect";
												Geometry intertemp = null;
												try {
													intertemp = jijukGeo.intersection(subG);
												} catch (Exception e) {
													try {
														jijukGeo = jijukGeo.buffer(0.0000001);
														Geometry intersTp = subG.buffer(0.0000001);
														intertemp = jijukGeo.intersection(intersTp);
													} catch (Exception e1) {

													}
												}

												if (intertemp != null) {
													Geometry temp = this.getPolygon2(intertemp);
													if (temp != null && temp.isEmpty() == false) {

														if (pnuTemp.length() == 0) {
															pnuTemp = jpnu;
														} else {
															if (pnuTemp.length()
																	+ jpnu.length() > this.maxStringLength) {
																pnuV.add(pnuTemp);
																pnuTemp = "";
																pnuTemp = jpnu;
															} else {
																pnuTemp += ("," + jpnu);
															}
														}

													}
												}
											}

										}
										long jet = System.currentTimeMillis();
									}
									if (pnuTemp.length() > 0) {
										pnuV.add(pnuTemp);
									}
								}

								savesfb.reset();
								savesfb.set("the_geom", subG);
								savesfb.set("ctime", job.createDay);
								savesfb.set("jobNum", job.id);
								savesfb.set("code", code);
								savesfb.set("cdn", name);
								savesfb.set("p_area", subG.getArea());
								savesfb.set("distance", subG.getLength());

								String textSavePnu = "";
								String pnus = "";
								int pnusCnt = 0;
								if (pnuV.size() > 0) {
									for (int k = 0; k < pnuV.size(); k++) {
										String pp = pnuV.get(k);

										textSavePnu += ("\t" + pp);

										if (pnus.length() == 0) {
											pnus = pp;
										} else {
											pnus += ("," + pp);
										}

										pnusCnt += (pp.split(",").length);
									}
								}

								if (job.pnu && bwa != null) {
									bwa.write(job.createDay + "\t" + ucode + "\t" + code + "\t" + id + textSavePnu
											+ "\n");
								}

								savesfb.set("pnus", pnus);
								savesfb.set("pnusCnt", pnusCnt);

								savesfb.set("id", (id++) + "");
								savesfb.set("ctDate", createDate);

								SimpleFeature saveFeature = savesfb.buildFeature(null);

								fc.add(saveFeature);
								long pnet = System.currentTimeMillis();//String.format("%.3f",n)
//								System.out.println(mm+", pnu intersect = " + String.format("%.5f",(pnet-pnst)/1000.0/60.0)  
//								+" min, area = " + String.format("%.2f",subG.getArea()));
							}
							
							//System.out.println("All , pnu intersect = " + (System.currentTimeMillis()-eet)/1000/60 +" min");
							
							
						} else if(job.selectAdmin.equals("fc")) {
							long sst = System.currentTimeMillis();
							Geometry all = null;

							boolean isRun = true;

							for (int k = 0; k < job.codes.length; k++) {
								long cst = System.currentTimeMillis();
								// 하나의 코드에 교차하는 영역
								Geometry unionG = null;

								Vector<Geometry> codeAll = new Vector();

								for (FileLayer layer : layers) {

									SimpleFeatureCollection sfc = null;

									try {
										sfc = layer.getFeatures(env.getMinX(), env.getMinY(), env.getMaxX(),
												env.getMaxY(), job.codes[k]);
									} catch (Exception e) {
										System.out.println(
												"error layerName = " + layer.getName() + ", code = " + job.codes[0]);
										e.printStackTrace();
										continue;
									}

									org.geotools.data.simple.SimpleFeatureIterator sfi = sfc.features();

									while (sfi.hasNext()) {
										SimpleFeature feature = sfi.next();
										Geometry jiguG = (Geometry) feature.getDefaultGeometry();

										if (jiguG instanceof LineString || jiguG instanceof MultiLineString) {
											continue;
										}

										Geometry temp = null;

										try {
											temp = jiguG.intersection(geoDong);
										} catch (Exception e) {
											jiguG = jiguG.buffer(0.0);
											temp = jiguG.intersection(geoDong.buffer(0.0000001));
										}

										Geometry inters = this.getPolygon2(temp);

										if (inters == null || inters.isEmpty()) {
											continue;
										}

										inters = this.getPolygon2(inters.buffer(0.0));

										codeAll.add(inters);

									}

								}

								if (codeAll.size() > 0) {
									// System.out.println("codeAll size = " + codeAll.size()+", layerName=");
									Geometry[] array = new Geometry[codeAll.size()];

									for (int kk = 0; kk < codeAll.size(); kk++) {
										array[kk] = codeAll.get(kk);
									}

									GeometryCollection gc = gft.createGeometryCollection(array);

									Geometry tempp = gc.buffer(0.0);
									// unionG = this.getPolygon(tempp, -1, job.exceptionArea);
									unionG = this.getPolygon2(tempp);
								}

								if (unionG == null) {
									isRun = false;
									break;
								}

								if (all == null) {
									all = unionG;
								} else {
									try {
										Geometry allGeo = all.intersection(unionG);
										all = this.getPolygon2(allGeo);
									} catch (Exception e) {
										unionG = unionG.buffer(0.0000001);
										Geometry allGeo = all.intersection(unionG);
										all = this.getPolygon2(allGeo);
									}
								}
								long cet = System.currentTimeMillis();
								//System.out.println("code = " + (cet - cst) / 1000.0 + " sec");
							}

							if (isRun == false) {
								continue;
							}

							if (all == null || all.isEmpty() || all.getArea() == 0) {
								continue;
							}

							long eet = System.currentTimeMillis();

							//System.out.println(ucode + ", layer create time = " + (eet - sst) / 1000.0 / 60.0 + " min" + ", size=" + all.getNumGeometries()+", code="+code);

							if (all == null || all.isEmpty()) {
								continue;
							}
							
							int pcnt = all.getNumGeometries();
							for (int mm = 0; mm < pcnt; mm++) {
								long pnst = System.currentTimeMillis();
								
								
								Geometry subG = all.getGeometryN(mm);

								if (subG.isEmpty()) {
									continue;
								}

								String pnuTemp = "";
								Vector<String> pnuV = new Vector();
								
								
								
								if (job.pnu) {
									Envelope allEnv = subG.getEnvelopeInternal();

									Vector idxs = jijukLayer.getFeatureIdxs(allEnv.getMinX(), allEnv.getMinY(),
											allEnv.getMaxX(), allEnv.getMaxY());
									
									List list = new ArrayList();
									Vector<String> addPnus = new Vector();
									for (int i = 0; i < idxs.size(); i++) {
										
										long jst = System.currentTimeMillis();
										Long idx = (Long) idxs.get(i);
										
										CarMultiThread cmt = (CarMultiThread) cmpm.getThreadObject();
										cmt.init(jijukLayer, idx, subG, addPnus, code);
										cmt.notifyThread();
										
										long jet = System.currentTimeMillis();
									}

									
									while(cmpm.getUsedCount() != 0) {
										Thread.sleep(100);
									}
									
									for(String jpnu : addPnus) {
										if (pnuTemp.length() == 0) {
											pnuTemp = jpnu;
										} else {
											if (pnuTemp.length() + jpnu.length() > this.maxStringLength) {
												pnuV.add(pnuTemp);
												pnuTemp = "";
												pnuTemp = jpnu;
											} else {
												pnuTemp += ("," + jpnu);
											}
										}
									}
									if (pnuTemp.length() > 0) {
										pnuV.add(pnuTemp);
									}
								}
								

								
								
								savesfb.reset();
								savesfb.set("the_geom", subG);
								savesfb.set("ctime", job.createDay);
								savesfb.set("jobNum", job.id);
								savesfb.set("code", code);
								savesfb.set("cdn", name);
								savesfb.set("p_area", subG.getArea());
								savesfb.set("distance", subG.getLength());

								String textSavePnu = "";
								String pnus = "";
								int pnusCnt = 0;
								if (pnuV.size() > 0) {
									for (int k = 0; k < pnuV.size(); k++) {
										String pp = pnuV.get(k);

										textSavePnu += ("\t" + pp);

										if (pnus.length() == 0) {
											pnus = pp;
										} else {
											pnus += ("," + pp);
										}

										pnusCnt += (pp.split(",").length);
									}
								}

								if (job.pnu && bwa != null) {
									bwa.write(job.createDay + "\t" + ucode + "\t" + code + "\t" + id + textSavePnu
											+ "\n");
								}

								savesfb.set("pnus", pnus);
								savesfb.set("pnusCnt", pnusCnt);

								savesfb.set("id", (id++) + "");
								savesfb.set("ctDate", createDate);

								SimpleFeature saveFeature = savesfb.buildFeature(null);

								fc.add(saveFeature);
								long pnet = System.currentTimeMillis();//String.format("%.3f",n)
//								System.out.println(mm+", pnu intersect = " + String.format("%.5f",(pnet-pnst)/1000.0/60.0)  
//								+" min, area = " + String.format("%.2f",subG.getArea()));
							}
							String ttime = format.format(new Date(System.currentTimeMillis()));
							System.out.println(ttime+","+ucode+","+code+"," + (System.currentTimeMillis()-sst)/1000/60 +" min, "+dongIdx+"/"+dongCnt);
							
						} else if(job.selectAdmin.equals("ff")) {

							WKTWriter writer = new WKTWriter();

							long st1 = System.currentTimeMillis();
							for (int k = 0; k < job.codes.length; k++) {

								FileLayer fl = null;

								Geometry unionG = null;

								for (FileLayer layer : layers) {

									SimpleFeatureCollection sfc = null;

									try {
										sfc = layer.getFeatures(env.getMinX(), env.getMinY(), env.getMaxX(),
												env.getMaxY(), job.codes[k]);
									} catch (Exception e) {
										System.out.println(
												"error layerName = " + layer.getName() + ", code = " + job.codes[0]);
										e.printStackTrace();
										continue;
									}

									// System.out.println("size=" + sfc.size());

									if (sfc.size() > 0) {
										if (fl == null) {

											// layer.getLayerInfo();
											LayerInfo li = layer.cloneLayerInfo(layer.getLayerInfo());

											Field fd = new Field();
											fd.setName("the_geom");
											fd.setType(FieldType.GEOMETRY);

											List<Field> fds = li.getSchema();
											fds.clear();

											fds.add(fd);

											UUID one = UUID.randomUUID();

											// String tempDir = System.getProperty("java.io.tmpdir");

											String tempDir = System.getProperty("user.dir") + "\\temp";

											File tempF = new File(tempDir);

											if (!tempF.exists()) {
												tempF.mkdirs();
											}

											// System.out.println("temp path = " + tempF.getAbsolutePath());
											fl = new FileLayer(tempF.getAbsolutePath() + "/" + one + "_" + ucode, li);
										}

										SimpleFeatureBuilder sftb = new SimpleFeatureBuilder(fl.getSimpleFeatureType());

										org.geotools.data.simple.SimpleFeatureIterator sfi = sfc.features();

										int idx = 0;

										while (sfi.hasNext()) {
											SimpleFeature sf1 = sfi.next();
											Geometry ge = (Geometry) sf1.getDefaultGeometry();

//											ge = ge.buffer(0);

											Envelope eng = ge.getEnvelopeInternal();
											SimpleFeatureCollection flsfc = fl.getFeatures(eng.getMinX(), eng.getMinY(),
													eng.getMaxX(), eng.getMaxY());

											org.geotools.data.simple.SimpleFeatureIterator sfi2 = flsfc.features();

											while (sfi2.hasNext()) {
												SimpleFeature sf2 = sfi2.next();
												Geometry g21 = (Geometry) sf2.getDefaultGeometry();
												try {
													ge = ge.difference(g21);
													ge = this.getPolygon2(ge);
													if (ge == null || ge.isEmpty()) {
														break;
													}
												} catch (Exception e) {

													String g21s = writer.write(g21);
													String ges = writer.write(ge);

													Geometry bg21 = g21.buffer(0.00001);

													bg21 = this.getPolygon2(bg21);

													Geometry bge = ge.buffer(0.00001);

													bge = this.getPolygon2(bge);

													try {
														ge = bge.difference(bg21);
														ge = this.getPolygon2(ge);
														if (ge == null || ge.isEmpty()) {
															break;
														}
													} catch (Exception e1) {
														// e1.printStackTrace();
														PrecisionModel pm = new PrecisionModel(PrecisionModel.FIXED);

														GeometryFactory gf1 = new GeometryFactory(pm);

														Geometry ag21 = gf1.createGeometry(g21);
														Geometry age = gf1.createGeometry(ge);

														ag21 = this.getPolygon2(ag21);
														age = this.getPolygon2(age);

														String ag21s = writer.write(ag21);
														String ages = writer.write(age);
//														FileUtils.write(new File("d://g21.txt"), g21s,+6+6+6+6+6+6+6+6+6+6+6+6+6+6+6+6+6+6+6+6+6+ true);
//														FileUtils.write(new File("d://ge.txt"), ges, true);
														try {
															ge = age.difference(ag21);
															ge = this.getPolygon2(ge);
															if (ge == null || ge.isEmpty()) {
																break;
															}
														} catch (Exception e2) {
//															FileUtils.write(new File("d://g21.txt"), g21s, true);
//															FileUtils.write(new File("d://ge.txt"), ges, true);														
//															
//															
//															FileUtils.write(new File("d://g21.txt"), ag21s, true);
//															FileUtils.write(new File("d://ge.txt"), ages, true);
//															
//															System.out.println("idx="+idx);
//															e2.printStackTrace();

															Geometry bbg21 = g21.buffer(0.1);

															bbg21 = this.getPolygon2(bbg21);

															Geometry bbge = ge.buffer(0.1);

															bbge = this.getPolygon2(bbge);
															try {
																ge = bbge.difference(bbg21);
																ge = this.getPolygon2(ge);
																if (ge == null || ge.isEmpty()) {
																	break;
																}
															} catch (Exception ee) {
																FileUtils.write(new File("d://g21.txt"), g21s, true);
																FileUtils.write(new File("d://ge.txt"), ges, true);
																ee.printStackTrace();
															}

														}
//														System.out.println();
													}

												}
												// ge = this.getPolygon(ge, 2.2, 1);
											}

											sftb.reset();

											Geometry geom = this.getPolygon(ge, 2.2, 1);
											// this.getP
											if (geom != null) {
												Geometry geoms = TopologyPreservingSimplifier.simplify(geom, 0.5);
												Geometry geomss = this.getPolygon2(geom);
												sftb.set("the_geom", geomss);
												SimpleFeature savesf = sftb.buildFeature(null);
												fl.writeFeature(savesf);
											}
											idx++;

											if ((idx % 1000) == 0) {
												long now = System.currentTimeMillis();

												double time = (now - st1) / 1000.0 / 60.0;
												double ing = idx * 100.0 / sfc.size();

												double totalTime = 100.0 * time / ing;
												// System.out.println(ucode+" 진행율="+ing+", time="+time+" min"+", 나머지
												// 시간="+totalTime+" min");
											}

										}
									}
								}

//								if(fl != null) {
//									fl.close();
//								}
//								fl = null;

								long et1 = System.currentTimeMillis();
//								System.out.println("layer create time = " + (et1-st1)/1000.0);

								if (fl != null) {

									com.data.file.FileLayer.SimpleFeatureIterator sfi = fl.iterator();

									while (sfi.hasNext()) {
										unionG = (Geometry) sfi.next().getDefaultGeometry();

										int pcnt = unionG.getNumGeometries();

										for (int m = 0; m < pcnt; m++) {
											Geometry subG = unionG.getGeometryN(m);

											if (subG.isEmpty()) {
												continue;
											}

											String pnuTemp = "";
											Vector<String> pnuV = new Vector();

											int pnusCnt = 0;
											if (job.pnu) {

												Envelope allEnv = subG.getEnvelopeInternal();

												Vector idxs = jijukLayer.getFeatureIdxs(allEnv.getMinX(),
														allEnv.getMinY(), allEnv.getMaxX(), allEnv.getMaxY());

												for (int i = 0; i < idxs.size(); i++) {
													Long idx = (Long) idxs.get(i);
													SimpleFeature jijukSf = jijukLayer.readFeature(idx);

													Geometry jijukGeo = (Geometry) jijukSf.getDefaultGeometry();

													if (subG.contains(jijukGeo)) {
														String jpnu = (String) jijukSf.getAttribute("pnu");
														if (jpnu != null) {
															pnusCnt++;
														}
													} else if (subG.intersects(jijukGeo)) {
														Geometry intertemp = null;
														try {
															intertemp = jijukGeo.intersection(subG);
														} catch (Exception e) {
															try {
																jijukGeo = jijukGeo.buffer(0.0000001);
																Geometry intersTp = subG.buffer(0.0000001);
																intertemp = jijukGeo.intersection(intersTp);
															} catch (Exception e1) {

															}
														}

														if (intertemp != null) {
															Geometry temp1 = this.getPolygon(intertemp, -1,
																	job.pnuArea);
															if (temp1 != null && temp1.isEmpty() == false) {
																String jpnu = (String) jijukSf.getAttribute("pnu");
																if (jpnu != null) {
																	pnusCnt++;
																}
															}

														}

													}
												}

											}

											savesfb.reset();
											savesfb.set("the_geom", subG);
											savesfb.set("ctime", job.createDay);
											savesfb.set("jobNum", job.id);
											savesfb.set("code", code);
											savesfb.set("cdn", name);
											savesfb.set("p_area", subG.getArea());
											savesfb.set("distance", subG.getLength());
											savesfb.set("pnus", "");
											savesfb.set("pnusCnt", pnusCnt);
											savesfb.set("ctDate", createDate);
											savesfb.set("id", (id++) + "");

											SimpleFeature saveFeature = savesfb.buildFeature(null);

											fc.add(saveFeature);

										}
									}
									fl.close();
									FileUtils.deleteDirectory(fl.getPath());
								}

								// fl.getPath();
							}
						}
					}

					if (shapeSave) {
						featureStore.addFeatures(fc);
						transaction.commit();

						transaction.close();

						if (job.pnu) {
							if (bwa != null) {
								bwa.flush();
								bwa.close();
							}
							if (osra != null) {
								// osra.flush();
								osra.close();
							}
							if (fosa != null) {
								// fosa.flush();
								fosa.close();
							}
						}

					}

					if (geojsonSave) {
						String value = MapService.getGeoJson(fc);

						FileOutputStream fis = new FileOutputStream(new File(job.dstPath + ".geojson"));

						OutputStreamWriter isr = new OutputStreamWriter(fis, "utf-8");
						BufferedWriter br = new BufferedWriter(isr);

						br.write(value);

						br.flush();

						br.close();

						isr.close();
						fis.close();
					}

					long et = System.currentTimeMillis();

					String codeS = "";
					for (String cd : job.codes) {
						codeS += ("," + cd);
					}

					String endDateString = format.format(new Date(System.currentTimeMillis()));
					System.out.println(endDateString + ", dstFile = " + job.dstPath + ", code = " + codeS + ", time = "
							+ (et - st) / 1000.0 + " sec, " + fc.size());
					fc.clear();

					logData = "";
					ingString = "완료";
					for (int i = 0; i < jobs.size(); i++) {
						Job tjob = jobs.get(i);
						String joblog = tjob.id + "\t" + tjob.dstPath + "\t";

						int nowJobId = Integer.parseInt(job.id);
						int jobId = Integer.parseInt(tjob.id);

						if (jobId == nowJobId) {
							joblog += "완료";
						} else if (jobId < nowJobId) {
							joblog += "완료";
						} else {
							joblog += "대기";
						}
						joblog += "\n";
						logData += joblog;
					}
//					File logFile = new File(this.logPath);
//					File parentLogFile = logFile.getParentFile();
//					if (!parentLogFile.exists()) {
//						parentLogFile.mkdirs();
//					}
					FileUtils.write(logFile, logData, Charset.forName("utf-8"), false);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("end run");


	}

//	public void writeLog() {
//		String logData = "";
//
//		String ingString = "완료";
//		for (int i = 0; i < jobs.size(); i++) {
//			Job tjob = jobs.get(i);
//			String joblog = tjob.id + "\t" + tjob.dstPath + "\t";
//
//			int nowJobId = Integer.parseInt(job.id);
//			int jobId = Integer.parseInt(tjob.id);
//
//			if (jobId == nowJobId) {
//				joblog += "진행";
//			} else if (jobId < nowJobId) {
//				joblog += "완료";
//			} else {
//				joblog += "대기";
//			}
//			joblog += "\n";
//			logData += joblog;
//		}
//
//		File logFile = new File(this.logPath);
//
//		File parentLogFile = logFile.getParentFile();
//
//		if (!parentLogFile.exists()) {
//			parentLogFile.mkdirs();
//		}
//
//		FileUtils.write(logFile, logData, Charset.forName("utf-8"), false);
//
//	}

	public Geometry getPolygon2(Geometry geo) {

		// double lenArea = 2.2;

		if (geo instanceof Polygon || geo instanceof MultiPolygon) {
			return geo;
		} else if (geo instanceof GeometryCollection) {
			Vector<Polygon> newps = new Vector();
			GeometryCollection geoc = (GeometryCollection) geo;
			int cnt = geoc.getNumGeometries();
			for (int i = 0; i < cnt; i++) {
				Geometry subGeo = geoc.getGeometryN(i);
				if (subGeo instanceof Polygon) {
					newps.add((Polygon) subGeo);
				} else if (subGeo instanceof MultiPolygon) {
					int mcnt = subGeo.getNumGeometries();
					for (int k = 0; k < mcnt; k++) {
						Geometry subPg = subGeo.getGeometryN(k);
						newps.add((Polygon) subPg);
					}
				}
			}

			Polygon[] pls = new Polygon[newps.size()];
			for (int m = 0; m < newps.size(); m++) {
				pls[m] = newps.get(m);
			}
			return gft.createMultiPolygon(pls);

		}
		return null;
	}

	GeometryFactory gft = new GeometryFactory();

	public Geometry getPolygon(Geometry geo, double lenArea, double area) {

		// double lenArea = 2.2;

		if (geo instanceof Polygon) {
			return getSimplePolygon((Polygon) geo, lenArea, area);
		} else if (geo instanceof MultiPolygon) {
			Vector<Polygon> newps = new Vector();
			int cnt = geo.getNumGeometries();
			for (int i = 0; i < cnt; i++) {
				Geometry pl = geo.getGeometryN(i);
				Geometry ppl = this.getSimplePolygon((Polygon) pl, lenArea, area);
				if (ppl != null) {
					newps.add((Polygon) ppl);
				}
			}

			Polygon[] pls = new Polygon[newps.size()];
			for (int m = 0; m < newps.size(); m++) {
				pls[m] = newps.get(m);
			}

			return gft.createMultiPolygon(pls);
		} else if (geo instanceof GeometryCollection) {
			Vector<Polygon> newps = new Vector();
			GeometryCollection geoc = (GeometryCollection) geo;
			int cnt = geoc.getNumGeometries();
			for (int i = 0; i < cnt; i++) {
				Geometry subGeo = geoc.getGeometryN(i);
				if (subGeo instanceof Polygon) {

					Geometry subGeoo = this.getSimplePolygon((Polygon) subGeo, lenArea, area);
					if (subGeoo != null) {
						newps.add((Polygon) subGeoo);
					}
				} else if (subGeo instanceof MultiPolygon) {
					int mcnt = subGeo.getNumGeometries();
					for (int k = 0; k < mcnt; k++) {
						Geometry subPg = this.getSimplePolygon((Polygon) subGeo.getGeometryN(k), lenArea, area);
						if (subPg != null) {
							newps.add((Polygon) subPg);
						}
					}
				}
			}

			Polygon[] pls = new Polygon[newps.size()];
			for (int m = 0; m < newps.size(); m++) {
				pls[m] = newps.get(m);
			}
			return gft.createMultiPolygon(pls);

		}
		return null;
	}

	public Geometry getSimplePolygon(Polygon _geo, double distance, double area) {

		Vector<LineString> interV = new Vector();
		Polygon geo = _geo;
		LineString rl = geo.getExteriorRing();

		double rlArea = Math.abs(CGAlgorithms.signedArea(rl.getCoordinates()));
		double rlAreaTemp = rl.getArea();
		double geoArea = geo.getArea();
		double rlLen = rl.getLength();

		if (distance > 0) {
			if ((rlLen / rlArea) > distance || rlArea < area) {
				return null;
			}
		} else {
			if (rlArea < area) {
				return null;
			}

		}

		int interCnt = geo.getNumInteriorRing();
		for (int i = 0; i < interCnt; i++) {
			LineString inter = geo.getInteriorRingN(i);
			double itArea = Math.abs(CGAlgorithms.signedArea(inter.getCoordinates()));
			double itLen = inter.getLength();

			if (distance > 0) {
				if ((rlLen / rlArea) < distance) {

					if (itArea > area) {
						interV.add(inter);
					}

				}
			} else {
				if (itArea > area) {
					interV.add(inter);
				}
			}

		}
		LinearRing[] lrs = new LinearRing[interV.size()];
		for (int i = 0; i < lrs.length; i++) {
			lrs[i] = (LinearRing) interV.get(i);
		}
		return gft.createPolygon((LinearRing) rl, lrs);
	}

	public Vector<Job> read(String file, String data) {
		Vector<Job> jobs = new Vector();
		try {
			String line = "";
			Vector<String> lines = new Vector();

			FileInputStream fis = null;
			InputStreamReader isr = null;
			BufferedReader br = null;

			if (file != null && data == null) {

				fis = new FileInputStream(file);
				isr = new InputStreamReader(fis, "utf-8");
				br = new BufferedReader(isr);

				while ((line = br.readLine()) != null) {
					lines.add(line);
				}
			} else {
				String[] lineArray = data.split("\\^");
				for (String temp : lineArray) {
					lines.add(temp);
				}
			}

			int idx = 0;
			String sp = "\\|";

			for (int i = 0; i < lines.size(); i++) {
				// while ((line = br.readLine()) != null) {
				line = lines.get(i);
				if (i == 0) {
					this.logPath = line;
					continue;
				}

				String[] words = line.split(sp);

				String isTrue = words[0];
				String code = words[1];
				String isPnu = words[2];
				String srcPath = words[3];
				String createDay = words[4];
				String dstPath = words[5];
				String saveType = words[6];
				String dong = words[7];
				String selectAdmin = words[8];
				String exceptionArea = words[9];
				String pnuArea = words[10];

				Job job = new Job();

				if (selectAdmin.equals("1")) {
					job.selectAdmin = "fd";
				} else if (selectAdmin.equals("2")) {
					job.selectAdmin = "fc";
				} else if (selectAdmin.equals("3")) {
					job.selectAdmin = "ff";
				}

				job.exceptionArea = Double.parseDouble(exceptionArea);

				job.pnuArea = Double.parseDouble(pnuArea);

				if (isTrue.equals("1")) {
					job.ing = true;
				}

				if (isPnu.equals("1")) {
					job.pnu = true;
				}

				// job.id = (i+1)+"";
				job.id = (i) + "";

				String[] codes = code.split(",");
				job.codes = codes;

				job.srcPath = srcPath;

				job.createDay = createDay;

				job.dstPath = dstPath + "/" + createDay + "-" + (i) + "-" + code.replaceAll(",", "_");

				if (saveType.equals("1")) {
					job.isGeoJson = true;
					job.isShape = false;
				} else if (saveType.equals("2")) {
					job.isGeoJson = false;
					job.isShape = true;
				} else if (saveType.equals("3")) {
					job.isGeoJson = true;
					job.isShape = true;
				}

				job.hcode = dong.replaceAll("\\*", "");

				jobs.add(job);
				// }
			}
			if (br != null) {
				br.close();
			}
			if (isr != null) {
				isr.close();
			}
			if (fis != null) {
				fis.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jobs;
	}

	public class Job {

		public String id = "";

		public boolean ing = false;
		public String[] codes = null;
		public String srcPath = "";
		public String dstPath = "";
		public boolean pnu = false;
		public String createDay = "";
		public String hcode = "";

		public boolean isGeoJson = false;
		public boolean isShape = false;

		public String ingString = "";

		public String selectAdmin = "";
		public double exceptionArea = 0.0;

		// public int exceptionMode = 1;

		public double pnuArea = 0.0;
	}

	public static String s1 = "진행";
	public static String s2 = "완료";
	public static String s3 = "대기";

	public String logPath = "";

	public SimpleFeatureType getType() {

		SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();

		b.setName("pnu");

		AttributeTypeBuilder build1 = new AttributeTypeBuilder();
		build1.setNillable(true);
		build1.setBinding(String.class);
		build1.setLength(256);
		AttributeDescriptor ctimeA = build1.buildDescriptor("ctime");
		b.add(ctimeA);

		AttributeTypeBuilder build2 = new AttributeTypeBuilder();
		build2.setNillable(true);
		build2.setBinding(String.class);
		build2.setLength(256);
		AttributeDescriptor jobNumA = build2.buildDescriptor("jobNum");
		b.add(jobNumA);

		AttributeTypeBuilder build3 = new AttributeTypeBuilder();
		build3.setNillable(true);
		build3.setBinding(String.class);
		build3.setLength(256);
		AttributeDescriptor idA = build3.buildDescriptor("id");
		b.add(idA);

		AttributeTypeBuilder build4 = new AttributeTypeBuilder();
		build4.setNillable(true);
		build4.setBinding(String.class);
		build4.setLength(256);
		AttributeDescriptor codeA = build4.buildDescriptor("code");
		b.add(codeA);

		AttributeTypeBuilder build5 = new AttributeTypeBuilder();
		build5.setNillable(true);
		build5.setBinding(String.class);
		build5.setLength(256);
		AttributeDescriptor cdnA = build5.buildDescriptor("cdn");
		b.add(cdnA);

		AttributeTypeBuilder build6 = new AttributeTypeBuilder();
		build6.setNillable(true);
		build6.setBinding(Double.class);
		AttributeDescriptor p_areaA = build6.buildDescriptor("p_area");
		b.add(p_areaA);

		AttributeTypeBuilder build7 = new AttributeTypeBuilder();
		build7.setNillable(true);
		build7.setBinding(Double.class);
		AttributeDescriptor distanceA = build7.buildDescriptor("distance");
		b.add(distanceA);

		AttributeTypeBuilder build8 = new AttributeTypeBuilder();
		build8.setNillable(true);
		build8.setBinding(String.class);
		build8.setLength(maxStringLength);
		build8.setMaxOccurs(maxStringLength);
		build8.setMinOccurs(maxStringLength);
		AttributeDescriptor pnusA = build8.buildDescriptor("pnus");
		b.add(pnusA);

		AttributeTypeBuilder build9 = new AttributeTypeBuilder();
		build9.setNillable(true);
		build9.setBinding(String.class);
		build9.setLength(maxStringLength);

		AttributeDescriptor pnus2A = build9.buildDescriptor("pnus2");
		b.add(pnus2A);

		AttributeTypeBuilder build10 = new AttributeTypeBuilder();
		build10.setNillable(true);
		build10.setBinding(String.class);
		build10.setLength(maxStringLength);
		AttributeDescriptor pnus3A = build10.buildDescriptor("pnus3");
		b.add(pnus3A);

		AttributeTypeBuilder build11 = new AttributeTypeBuilder();
		build11.setNillable(true);
		build11.setBinding(String.class);
		build11.setLength(maxStringLength);
		AttributeDescriptor pnus4A = build11.buildDescriptor("pnus4");
		b.add(pnus4A);

		AttributeTypeBuilder build12 = new AttributeTypeBuilder();
		build12.setNillable(true);
		build12.setBinding(String.class);
		build12.setLength(256);
		AttributeDescriptor ctDateA = build12.buildDescriptor("ctDate");
		b.add(ctDateA);

		AttributeTypeBuilder build13 = new AttributeTypeBuilder();
		build13.setNillable(true);
		build13.setBinding(Integer.class);
		AttributeDescriptor pnusCntA = build13.buildDescriptor("pnusCnt");
		b.add(pnusCntA);

		AttributeTypeBuilder build14 = new AttributeTypeBuilder();
		AttributeDescriptor geom = build13.buildDescriptor("the_geom", SimpleSchema.MULTIPOLYGON);
		b.add(geom);

		SimpleFeatureType sftt = b.buildFeatureType();

		return sftt;
		// GeometryType gt = SimpleSchema.MULTIPOLYGON;

//		SimpleFeatureType type =
//		// DataUtilities.createType(dstFile.getName(),
//		// "the_geom:MultiPolygon,code:String,cdn:String,area:Double,pnus:String");
//		DataUtilities.createType(dstFile.getName(),
//				"the_geom:MultiPolygon," + "ctime:String," + "jobNum:String," + "id:String,"
//						+ "code:String," + "cdn:String," + "p_area:Double," + "distance:Double,"
//						+ "pnus:String," + "ctDate:String,"+"pnusCnt:Integer");

	}
}
