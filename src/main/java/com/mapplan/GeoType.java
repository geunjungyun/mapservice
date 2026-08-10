//
// 이 파일은 JAXB(JavaTM Architecture for XML Binding) 참조 구현 2.2.8-b130911.1802 버전을 통해 생성되었습니다. 
// <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>를 참조하십시오. 
// 이 파일을 수정하면 소스 스키마를 재컴파일할 때 수정 사항이 손실됩니다. 
// 생성 날짜: 2026.06.22 시간 02:58:37 PM KST 
//


package com.mapplan;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>geoType에 대한 Java 클래스입니다.
 * 
 * <p>다음 스키마 단편이 이 클래스에 포함되는 필요한 콘텐츠를 지정합니다.
 * <p>
 * <pre>
 * &lt;simpleType name="geoType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Point"/>
 *     &lt;enumeration value="LineString"/>
 *     &lt;enumeration value="Polygon"/>
 *     &lt;enumeration value="MultiPoint"/>
 *     &lt;enumeration value="MultiLineString"/>
 *     &lt;enumeration value="MultiPolygon"/>
 *     &lt;enumeration value="GeometryCollection"/>
 *     &lt;enumeration value="Raster"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "geoType")
@XmlEnum
public enum GeoType {

    @XmlEnumValue("Point")
    POINT("Point"),
    @XmlEnumValue("LineString")
    LINE_STRING("LineString"),
    @XmlEnumValue("Polygon")
    POLYGON("Polygon"),
    @XmlEnumValue("MultiPoint")
    MULTI_POINT("MultiPoint"),
    @XmlEnumValue("MultiLineString")
    MULTI_LINE_STRING("MultiLineString"),
    @XmlEnumValue("MultiPolygon")
    MULTI_POLYGON("MultiPolygon"),
    @XmlEnumValue("GeometryCollection")
    GEOMETRY_COLLECTION("GeometryCollection"),
    @XmlEnumValue("Raster")
    RASTER("Raster");
    private final String value;

    GeoType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static GeoType fromValue(String v) {
        for (GeoType c: GeoType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
