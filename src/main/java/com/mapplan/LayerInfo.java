//
// 이 파일은 JAXB(JavaTM Architecture for XML Binding) 참조 구현 2.2.8-b130911.1802 버전을 통해 생성되었습니다. 
// <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>를 참조하십시오. 
// 이 파일을 수정하면 소스 스키마를 재컴파일할 때 수정 사항이 손실됩니다. 
// 생성 날짜: 2026.06.22 시간 02:58:37 PM KST 
//


package com.mapplan;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * 
 * 			FreeLayer 파일의 메타 정보
 * 		
 * 
 * <p>layerInfo complex type에 대한 Java 클래스입니다.
 * 
 * <p>다음 스키마 단편이 이 클래스에 포함되는 필요한 콘텐츠를 지정합니다.
 * 
 * <pre>
 * &lt;complexType name="layerInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mbr" type="{}mbr"/>
 *         &lt;element name="schema" type="{}field" maxOccurs="unbounded"/>
 *         &lt;element name="charSet" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="projection" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="desc" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="createDate" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *         &lt;element name="modifyDate" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *         &lt;element name="user" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="objectCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="maxObjMbr" type="{}mbr"/>
 *         &lt;element name="maxObjSize" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="gridSize" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "layerInfo", propOrder = {
    "mbr",
    "schema",
    "charSet",
    "projection",
    "name",
    "desc",
    "createDate",
    "modifyDate",
    "user",
    "objectCount",
    "maxObjMbr",
    "maxObjSize",
    "gridSize"
})
public class LayerInfo {

    @XmlElement(required = true)
    protected Mbr mbr;
    @XmlElement(required = true)
    protected List<Field> schema;
    @XmlElement(required = true)
    protected String charSet;
    @XmlElement(required = true)
    protected String projection;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String desc;
    @XmlElement(required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar createDate;
    @XmlElement(required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar modifyDate;
    @XmlElement(required = true)
    protected String user;
    protected int objectCount;
    @XmlElement(required = true)
    protected Mbr maxObjMbr;
    protected int maxObjSize;
    protected Integer gridSize;

    /**
     * mbr 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Mbr }
     *     
     */
    public Mbr getMbr() {
        return mbr;
    }

    /**
     * mbr 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Mbr }
     *     
     */
    public void setMbr(Mbr value) {
        this.mbr = value;
    }

    /**
     * Gets the value of the schema property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the schema property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSchema().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Field }
     * 
     * 
     */
    public List<Field> getSchema() {
        if (schema == null) {
            schema = new ArrayList<Field>();
        }
        return this.schema;
    }

    /**
     * charSet 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCharSet() {
        return charSet;
    }

    /**
     * charSet 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCharSet(String value) {
        this.charSet = value;
    }

    /**
     * projection 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProjection() {
        return projection;
    }

    /**
     * projection 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProjection(String value) {
        this.projection = value;
    }

    /**
     * name 속성의 값을 가져옵니다.
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
     * desc 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDesc() {
        return desc;
    }

    /**
     * desc 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDesc(String value) {
        this.desc = value;
    }

    /**
     * createDate 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCreateDate() {
        return createDate;
    }

    /**
     * createDate 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCreateDate(XMLGregorianCalendar value) {
        this.createDate = value;
    }

    /**
     * modifyDate 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getModifyDate() {
        return modifyDate;
    }

    /**
     * modifyDate 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setModifyDate(XMLGregorianCalendar value) {
        this.modifyDate = value;
    }

    /**
     * user 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUser() {
        return user;
    }

    /**
     * user 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUser(String value) {
        this.user = value;
    }

    /**
     * objectCount 속성의 값을 가져옵니다.
     * 
     */
    public int getObjectCount() {
        return objectCount;
    }

    /**
     * objectCount 속성의 값을 설정합니다.
     * 
     */
    public void setObjectCount(int value) {
        this.objectCount = value;
    }

    /**
     * maxObjMbr 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Mbr }
     *     
     */
    public Mbr getMaxObjMbr() {
        return maxObjMbr;
    }

    /**
     * maxObjMbr 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Mbr }
     *     
     */
    public void setMaxObjMbr(Mbr value) {
        this.maxObjMbr = value;
    }

    /**
     * maxObjSize 속성의 값을 가져옵니다.
     * 
     */
    public int getMaxObjSize() {
        return maxObjSize;
    }

    /**
     * maxObjSize 속성의 값을 설정합니다.
     * 
     */
    public void setMaxObjSize(int value) {
        this.maxObjSize = value;
    }

    /**
     * gridSize 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getGridSize() {
        return gridSize;
    }

    /**
     * gridSize 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setGridSize(Integer value) {
        this.gridSize = value;
    }

}
