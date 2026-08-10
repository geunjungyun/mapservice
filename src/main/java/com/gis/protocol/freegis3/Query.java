//
// 이 파일은 JAXB(JavaTM Architecture for XML Binding) 참조 구현 2.2.8-b130911.1802 버전을 통해 생성되었습니다. 
// <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>를 참조하십시오. 
// 이 파일을 수정하면 소스 스키마를 재컴파일할 때 수정 사항이 손실됩니다. 
// 생성 날짜: 2026.06.22 시간 02:12:30 PM KST 
//


package com.gis.protocol.freegis3;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>anonymous complex type에 대한 Java 클래스입니다.
 * 
 * <p>다음 스키마 단편이 이 클래스에 포함되는 필요한 콘텐츠를 지정합니다.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}name" minOccurs="0"/>
 *         &lt;element ref="{}value" minOccurs="0"/>
 *         &lt;element name="cql" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element ref="{}styleName" minOccurs="0"/>
 *         &lt;element ref="{}overlapConfig" minOccurs="0"/>
 *         &lt;element ref="{}priority" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "name",
    "value",
    "cql",
    "styleName",
    "overlapConfig",
    "priority"
})
@XmlRootElement(name = "query")
public class Query {

    protected String name;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String value;
    protected String cql;
    protected String styleName;
    protected OverlapConfig overlapConfig;
    protected Integer priority;

    /**
     * 조건을 비교하기 위한 컬럼 이름
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * name 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * 조건을 비교하기 위한 해당 컬럼의 값, int형 범위 조건시 '|'구분 (예: 0이상~10이하는 0|10)
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * value 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * cql 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCql() {
        return cql;
    }

    /**
     * cql 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCql(String value) {
        this.cql = value;
    }

    /**
     * 스타일 이름
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStyleName() {
        return styleName;
    }

    /**
     * styleName 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStyleName(String value) {
        this.styleName = value;
    }

    /**
     * overlapConfig 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link OverlapConfig }
     *     
     */
    public OverlapConfig getOverlapConfig() {
        return overlapConfig;
    }

    /**
     * overlapConfig 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link OverlapConfig }
     *     
     */
    public void setOverlapConfig(OverlapConfig value) {
        this.overlapConfig = value;
    }

    /**
     * 그리기 위한 우선 순위로 후순위이고 겹칠 경우 그리지 않는다.
     * 						
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * priority 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPriority(Integer value) {
        this.priority = value;
    }

}
