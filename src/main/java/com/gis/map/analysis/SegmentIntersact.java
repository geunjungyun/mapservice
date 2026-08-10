package com.gis.map.analysis;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.data.file.ShapeFile;

public class SegmentIntersact {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		/*
        Double[] array = {1.0, 3.0, 7.0};
        System.out.println("원래 배열: " + Arrays.toString(array));

        // 배열을 리스트로 변환
        ArrayList<Double> list = new ArrayList<>(Arrays.asList(array));

        // 중간 값을 삽입
        for (int i = 0; i < list.size() - 1; i += 2) {
            double current = list.get(i);
            double next = list.get(i + 1);

            // 중간 값 계산
            double midpoint = (current + next) / 2.0;

            // 중간 값 삽입
            list.add(i + 1, midpoint);
            System.out.println(i+", size=" + list.size());
        }
		

        Coordinate start = new Coordinate(0.5, 0.5);
        Coordinate end = new Coordinate(1, 1);
        LineSegment segment = new LineSegment(start, end);

        // 1미터 간격으로 좌표 추출
        List<Coordinate> points = extractPoints(segment, 1.5);

        // 결과 출력
        System.out.println("추출된 좌표:");
        for (Coordinate point : points) {
            System.out.println(point);
        }		
		*/
		
		//3611012100107130000
		
		
		SegmentIntersact si = new SegmentIntersact();
		
		
		Coordinate sc = new Coordinate(1020064.9726877332, 1984540.2163141782);
		
		
		Coordinate[] roadCoords = new Coordinate[4];
		roadCoords[0] = new Coordinate(1020138.5470539973, 1984500.8844671098);
		roadCoords[1] = new Coordinate(1020138.5470539973, 1984500.8844671098);
		roadCoords[2] = new Coordinate(1020138.5470539973, 1984500.8844671098);
		roadCoords[3] = new Coordinate(1020138.5470539973, 1984500.8844671098);
		
		
		Coordinate nearsc = si.getNearCoord(sc, roadCoords);
		
		
		
		
		SimpleFeature jijuk  = null;
		
		Vector<SimpleFeature> list = new Vector();
		
		ShapeFile shp = new ShapeFile(new File("D:\\coops\\test.shp"), null, "utf-8");
		
		try {
			SimpleFeatureIterator sfi = shp.getReader();
			while(sfi.hasNext()) {
				SimpleFeature sf = sfi.next();
				
				String pnu  = (String)sf.getAttribute("PNU");
				if(pnu.equals("3611033021104800000")) {
					jijuk = sf;
				}
				else {
					list.add(sf);
				}
				//System.out.println("sf="+sf.toString());
			}
			sfi.close();
			
			shp.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		ListFeatureCollection lineSf = si.getIntersaction(jijuk, list, 0.5);
		
		
		
		/*
		
		GeometryFactory gf = new GeometryFactory();
		SegmentIntersact si = new SegmentIntersact();
		
		for(int i=0; i< ((Geometry)jijuk.getDefaultGeometry()).getNumGeometries() ; i++) {
			
			Polygon pg = (Polygon) ((Geometry)jijuk.getDefaultGeometry()).getGeometryN(i);
			
			for(SimpleFeature road : list) {
				
				Add result = new Add();
				
				for(int k=0; k<((Geometry)road.getDefaultGeometry()).getNumGeometries(); k++) {
					Polygon roadpg = (Polygon) ((Geometry)road.getDefaultGeometry()).getGeometryN(i);
					
					si.getLine(pg, roadpg, result, road);
				}
				
				Geometry resgeo = null;
				
				Vector<Coordinate[]> cdl = result.getList();
				
				if(cdl == null) {
					continue;
				}
				else if(cdl.size() == 1) {
					LineString ls = gf.createLineString(cdl.get(0));
					resgeo = ls;
				}
				else if(cdl.size() > 1){
					LineString[] lss = new LineString[cdl.size()];
					
					for(int kk=0; kk<cdl.size(); kk++) {
						lss[kk] = gf.createLineString(cdl.get(kk));
					}
					
					resgeo = gf.createMultiLineString(lss);
				}
				
				if(resgeo != null) {
					System.out.println(road.getAttribute("PNU")+" = " + resgeo.toText());
				}
			}
		}
		*/
	}
	
	
	
    public SimpleFeatureType modifyFeatureType(SimpleFeatureType originalType) {
        // 새로운 SimpleFeatureType 생성
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();

        // 기존 이름 복사
        typeBuilder.setName(originalType.getName());

        // 속성 복사 및 geometry 타입 변경
        originalType.getAttributeDescriptors().forEach(attribute -> {
            if ("the_geom".equalsIgnoreCase(attribute.getLocalName()) 
                    && attribute.getType().getBinding().isAssignableFrom(org.locationtech.jts.geom.MultiPolygon.class)) {
                // geometry 속성을 MultiPolygon -> MultiLineString으로 변경
                typeBuilder.add(attribute.getLocalName(), org.locationtech.jts.geom.MultiLineString.class);
            } else {
                // 다른 속성은 그대로 복사
                typeBuilder.add(attribute);
            }
        });

        // CRS 설정 복사
        typeBuilder.setCRS(originalType.getCoordinateReferenceSystem());

        // 새로운 SimpleFeatureType 반환
        return typeBuilder.buildFeatureType();
    }	
	
	public ListFeatureCollection getIntersaction(SimpleFeature jijuk, Vector<SimpleFeature> roads, double snap){
		
		//Vector<SimpleFeature> sfs = new Vector();
		
		SimpleFeatureType sft = roads.get(0).getFeatureType();
		
		SimpleFeatureType nsft = this.modifyFeatureType(sft);
		
		SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(nsft);
		
		ListFeatureCollection sfs = new ListFeatureCollection(nsft);
		
		GeometryFactory gf = new GeometryFactory();
		SegmentIntersact si = new SegmentIntersact();
		
		for(int i=0; i< ((Geometry)jijuk.getDefaultGeometry()).getNumGeometries() ; i++) {
			
			Polygon pg = (Polygon) ((Geometry)jijuk.getDefaultGeometry()).getGeometryN(i);
			
			for(SimpleFeature road : roads) {
				
				
//				if(!road.getAttribute("PNU").equals("3611033021111460001")) {
//					continue;
//				}
				
				Add result = new Add();
				
				for(int k=0; k<((Geometry)road.getDefaultGeometry()).getNumGeometries(); k++) {
					Polygon roadpg = (Polygon) ((Geometry)road.getDefaultGeometry()).getGeometryN(k);
					
					si.getLine(pg, roadpg, result, road, snap);
				}
				
				Geometry resgeo = null;
				
				Vector<Coordinate[]> cdl = result.getList();
				
				if(cdl == null) {
					continue;
				}
				else if(cdl.size() == 1) {
					LineString ls = gf.createLineString(cdl.get(0));
					resgeo = ls;
				}
				else if(cdl.size() > 1){
					LineString[] lss = new LineString[cdl.size()];
					
					for(int kk=0; kk<cdl.size(); kk++) {
						lss[kk] = gf.createLineString(cdl.get(kk));
					}
					
					resgeo = gf.createMultiLineString(lss);
				}
				
				if(resgeo != null) {
					
					
					sfb.reset();
					
					for(int m=0; m<road.getAttributeCount(); m++) {
						sfb.set(m, road.getAttribute(m));
					}
					
					SimpleFeature newSf = sfb.buildFeature(null);
					
					newSf.setDefaultGeometry(resgeo);
					
					sfs.add(newSf);
					
					//System.out.println(road.getAttribute("PNU")+" = " + resgeo.toText());
				}
			}
		}
		
		
		return sfs;
	}
	
	
	public void getLine(Polygon jijuk, Polygon road, Add result, SimpleFeature roadFeature, double snap_) {
		
		double snap = snap_;
		
		LineString ls = jijuk.getExteriorRing();
		
		Coordinate[] jijukCoords = ls.getCoordinates();
		
		ArrayList<Coordinate> jijukCdList = new ArrayList<>(Arrays.asList(jijukCoords));
		
		Vector<Coordinate[]> cdv = new Vector();
		
		//Coordinate[] roadCoords = road.getExteriorRing().getCoordinates();
		
		cdv.add(road.getExteriorRing().getCoordinates());
		
		for(int i=0; i<road.getNumInteriorRing(); i++) {
			LineString roadls = road.getInteriorRingN(i);
			cdv.add(roadls.getCoordinates());
		}
		
		
		for(Coordinate[] roadCoords : cdv) {
			
		//지적좌표 리시트에 도로 좌표와 가장 가까운 좌표에 버텍스가 없을 경우 추가함. 단 snap 거리에 지적의 좌표가 존재할 경우 추가 안함  
		for(Coordinate roadCoord : roadCoords) {
			
			int addIdx = -1;
			
			double nowDistance = Double.MAX_VALUE;
			Coordinate addCoord = null;
			
			
			for(int i=0; i<jijukCdList.size()-1; i++) {
				
				Coordinate sc = jijukCdList.get(i);
				Coordinate ed = jijukCdList.get(i+1);
				
				
				Coordinate closed = closestPointOnSegment(sc, ed, roadCoord);
				
				double dis = closed.distance(roadCoord);
				
				if(closed.distance(roadCoord) < snap) {
					if(dis < nowDistance) {
							addCoord = closed;
							nowDistance = dis;
							if(closed.distance(sc) > snap && closed.distance(ed) > snap) {
								addIdx = i;								
							}
					}
				}
			}
			if(addIdx > -1) {
				//지적에 추가되는 좌표
				//System.out.println("add coord = " + addCoord.toString()+", roadpnu = " + roadFeature.getAttribute("PNU")+", roadCoord="+roadCoord.toString());
				jijukCdList.add(addIdx+1,addCoord);
			}
			
		}
		
		for(int i=0; i<jijukCdList.size()-1; i++) {
			
			Coordinate sc = jijukCdList.get(i);
			Coordinate ed = jijukCdList.get(i+1);
			
			Coordinate nearsc = this.getNearCoord(sc, roadCoords);
			
			Coordinate neared = this.getNearCoord(ed, roadCoords);
			
			if(nearsc == null) {
				int k=0; 
			}
			
			double scDistance = sc.distance(nearsc);
			double edDistance = ed.distance(neared);
			
			if(scDistance < snap && edDistance < snap) {
				
				//시작 포인트에 가장 가까운 도로상의 포인트로 지적과 교차하여 현재 지적보다 가까운 지적이 있으면 
				//시작 포인트와 도로 사이에 지적의 라인이 존재한다고 판단
				
				boolean isAdd = true;
				
				for(int k=0; k<jijukCdList.size(); k++) {
					Coordinate jijukCoord =  jijukCdList.get(k);
					if(jijukCoord.distance(nearsc) < scDistance) {
						isAdd = false;
						break;
					}
				}
				
				for(int k=0; k<jijukCdList.size(); k++) {
					Coordinate jijukCoord =  jijukCdList.get(k);
					if(jijukCoord.distance(neared) < edDistance) {
						isAdd = false;
						break;
					}
				}
				
				if(isAdd) {
					result.add(sc, ed);
				}
				
				
			}
		}
		
		}
		
	}
	
	
//	public double distance(Coordinate coord, Coordinate[] roadCoords, double snap) {
//		for(int i=0; i<roadCoords.length-1; i++) {
//			
//			Coordinate cd =closestPointOnSegment(roadCoords[i], roadCoords[i+1], coord );
//			
//			if(cd.distance(coord) < snap) {
//				return true;
//			}
//		}
//		return false;
//		
//	}
	
	
	
	public Coordinate getNearCoord(Coordinate coord, Coordinate[] roadCoords) {
		
		Coordinate nearCoord = null;
		
		double nowDistance = Double.MAX_VALUE;
		for(int i=0; i<roadCoords.length-1; i++) {
			
			Coordinate cd = null;
			
			if(roadCoords[i].equals(roadCoords[i+1])) {
				cd = roadCoords[i];
			}
			else {
				cd = closestPointOnSegment(roadCoords[i], roadCoords[i+1], coord );
			}
			//Coordinate cd = closestPointOnSegment(roadCoords[i], roadCoords[i+1], coord );
			
			double distance = coord.distance(cd);
			
			if(distance < nowDistance) {
				nearCoord = cd;
				nowDistance = distance;
			}
		}
		
		return nearCoord;
	}
	
    // 선분과 주어진 점 사이의 가장 가까운 점을 찾는 함수
    private Coordinate closestPointOnSegment(Coordinate startPoint, Coordinate endPoint, Coordinate targetCoord) {
        // 선분의 방향 벡터 계산
        double dx = endPoint.x - startPoint.x;
        double dy = endPoint.y - startPoint.y;
        
        // 선분의 길이 제곱 계산
        double segmentLengthSquared = dx * dx + dy * dy;
        
        // 선분 상에서 주어진 점에 가장 가까운 점의 위치 계산
        double t = ((targetCoord.x - startPoint.x) * dx + (targetCoord.y - startPoint.y) * dy) / segmentLengthSquared;
        t = Math.max(0, Math.min(1, t)); // 선분을 벗어나지 않도록 t 값을 조정
        double closestX = startPoint.x + t * dx;
        double closestY = startPoint.y + t * dy;
        
        return new Coordinate(closestX, closestY);
    } 	
	
	
	public boolean inDistance(Coordinate coord, Coordinate[] roadCoords, double snap) {
		for(int i=0; i<roadCoords.length-1; i++) {
			
			Coordinate cd =closestPointOnSegment(roadCoords[i], roadCoords[i+1], coord );
			
			if(cd.distance(coord) < snap) {
				return true;
			}
		}
		return false;
		
	}
	

	
    public static List<Coordinate> extractPoints(LineSegment segment, double interval) {
        List<Coordinate> points = new ArrayList<>();

        // 선분 길이 계산
        double length = segment.getLength();

        // 시작점을 추가
        points.add(segment.getCoordinate(0));

        // 1미터 단위로 점 추가
        for (double fraction = interval / length; fraction < 1.0; fraction += interval / length) {
            Coordinate interpolated = segment.pointAlong(fraction);
            points.add(interpolated);
        }

        // 끝점을 추가
        points.add(segment.getCoordinate(1));

        return points;
    }
    
	
}
