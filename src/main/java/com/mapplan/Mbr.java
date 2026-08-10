//
// 이 파일은 JAXB(JavaTM Architecture for XML Binding) 참조 구현 2.2.8-b130911.1802 버전을 통해 생성되었습니다. 
// <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>를 참조하십시오. 
// 이 파일을 수정하면 소스 스키마를 재컴파일할 때 수정 사항이 손실됩니다. 
// 생성 날짜: 2026.06.22 시간 02:58:37 PM KST 
//


package com.mapplan;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 				최소, 최대 x축 y축 좌표 값
 * 			
 * 
 * <p>mbr complex type에 대한 Java 클래스입니다.
 * 
 * <p>다음 스키마 단편이 이 클래스에 포함되는 필요한 콘텐츠를 지정합니다.
 * 
 * <pre>
 * &lt;complexType name="mbr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="minx" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="miny" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="maxx" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="maxy" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mbr", propOrder = {
    "minx",
    "miny",
    "maxx",
    "maxy"
})
public class Mbr {

    protected double minx;
    protected double miny;
    protected double maxx;
    protected double maxy;

    /**
     * minx 속성의 값을 가져옵니다.
     * 
     */
    public double getMinx() {
        return minx;
    }

    /**
     * minx 속성의 값을 설정합니다.
     * 
     */
    public void setMinx(double value) {
        this.minx = value;
    }

    /**
     * miny 속성의 값을 가져옵니다.
     * 
     */
    public double getMiny() {
        return miny;
    }

    /**
     * miny 속성의 값을 설정합니다.
     * 
     */
    public void setMiny(double value) {
        this.miny = value;
    }

    /**
     * maxx 속성의 값을 가져옵니다.
     * 
     */
    public double getMaxx() {
        return maxx;
    }

    /**
     * maxx 속성의 값을 설정합니다.
     * 
     */
    public void setMaxx(double value) {
        this.maxx = value;
    }

    /**
     * maxy 속성의 값을 가져옵니다.
     * 
     */
    public double getMaxy() {
        return maxy;
    }

    /**
     * maxy 속성의 값을 설정합니다.
     * 
     */
    public void setMaxy(double value) {
        this.maxy = value;
    }

}
