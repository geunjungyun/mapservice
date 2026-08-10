//
// 이 파일은 JAXB(JavaTM Architecture for XML Binding) 참조 구현 2.2.8-b130911.1802 버전을 통해 생성되었습니다. 
// <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>를 참조하십시오. 
// 이 파일을 수정하면 소스 스키마를 재컴파일할 때 수정 사항이 손실됩니다. 
// 생성 날짜: 2020.05.03 시간 01:42:16 PM KST 
//


package com.mapplan;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>statisticReq complex type에 대한 Java 클래스입니다.
 * 
 * <p>다음 스키마 단편이 이 클래스에 포함되는 필요한 콘텐츠를 지정합니다.
 * 
 * <pre>
 * &lt;complexType name="statisticReq">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="startYear" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="endYear" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="startMonth" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="endMonth" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="startDay" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="endDay" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="startHour" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="endHour" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="interval" type="{}interval"/>
 *         &lt;element name="userId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="pageId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "statisticReq", propOrder = {
    "startYear",
    "endYear",
    "startMonth",
    "endMonth",
    "startDay",
    "endDay",
    "startHour",
    "endHour",
    "interval",
    "userId",
    "pageId"
})
public class StatisticReq {

    protected int startYear;
    protected int endYear;
    protected int startMonth;
    protected int endMonth;
    protected int startDay;
    protected int endDay;
    protected int startHour;
    protected int endHour;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected Interval interval;
    protected String userId;
    protected String pageId;

    /**
     * startYear 속성의 값을 가져옵니다.
     * 
     */
    public int getStartYear() {
        return startYear;
    }

    /**
     * startYear 속성의 값을 설정합니다.
     * 
     */
    public void setStartYear(int value) {
        this.startYear = value;
    }

    /**
     * endYear 속성의 값을 가져옵니다.
     * 
     */
    public int getEndYear() {
        return endYear;
    }

    /**
     * endYear 속성의 값을 설정합니다.
     * 
     */
    public void setEndYear(int value) {
        this.endYear = value;
    }

    /**
     * startMonth 속성의 값을 가져옵니다.
     * 
     */
    public int getStartMonth() {
        return startMonth;
    }

    /**
     * startMonth 속성의 값을 설정합니다.
     * 
     */
    public void setStartMonth(int value) {
        this.startMonth = value;
    }

    /**
     * endMonth 속성의 값을 가져옵니다.
     * 
     */
    public int getEndMonth() {
        return endMonth;
    }

    /**
     * endMonth 속성의 값을 설정합니다.
     * 
     */
    public void setEndMonth(int value) {
        this.endMonth = value;
    }

    /**
     * startDay 속성의 값을 가져옵니다.
     * 
     */
    public int getStartDay() {
        return startDay;
    }

    /**
     * startDay 속성의 값을 설정합니다.
     * 
     */
    public void setStartDay(int value) {
        this.startDay = value;
    }

    /**
     * endDay 속성의 값을 가져옵니다.
     * 
     */
    public int getEndDay() {
        return endDay;
    }

    /**
     * endDay 속성의 값을 설정합니다.
     * 
     */
    public void setEndDay(int value) {
        this.endDay = value;
    }

    /**
     * startHour 속성의 값을 가져옵니다.
     * 
     */
    public int getStartHour() {
        return startHour;
    }

    /**
     * startHour 속성의 값을 설정합니다.
     * 
     */
    public void setStartHour(int value) {
        this.startHour = value;
    }

    /**
     * endHour 속성의 값을 가져옵니다.
     * 
     */
    public int getEndHour() {
        return endHour;
    }

    /**
     * endHour 속성의 값을 설정합니다.
     * 
     */
    public void setEndHour(int value) {
        this.endHour = value;
    }

    /**
     * interval 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Interval }
     *     
     */
    public Interval getInterval() {
        return interval;
    }

    /**
     * interval 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Interval }
     *     
     */
    public void setInterval(Interval value) {
        this.interval = value;
    }

    /**
     * userId 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserId() {
        return userId;
    }

    /**
     * userId 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserId(String value) {
        this.userId = value;
    }

    /**
     * pageId 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPageId() {
        return pageId;
    }

    /**
     * pageId 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPageId(String value) {
        this.pageId = value;
    }

}
