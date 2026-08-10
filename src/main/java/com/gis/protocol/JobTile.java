//
// 이 파일은 JAXB(JavaTM Architecture for XML Binding) 참조 구현 2.2.8-b130911.1802 버전을 통해 생성되었습니다. 
// <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>를 참조하십시오. 
// 이 파일을 수정하면 소스 스키마를 재컴파일할 때 수정 사항이 손실됩니다. 
// 생성 날짜: 2025.12.08 시간 01:50:29 PM KST 
//


package com.gis.protocol;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>jobTile complex type에 대한 Java 클래스입니다.
 * 
 * <p>다음 스키마 단편이 이 클래스에 포함되는 필요한 콘텐츠를 지정합니다.
 * 
 * <pre>
 * &lt;complexType name="jobTile">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="run" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="priority" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="desc" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="tileMapServiceName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="levelSet" type="{}levelSet" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="threadCnt" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="drawCanvasRatio" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="distribute" type="{}distribute" minOccurs="0"/>
 *         &lt;element name="update" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="crs" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "jobTile", propOrder = {
    "run",
    "priority",
    "name",
    "desc",
    "tileMapServiceName",
    "levelSet",
    "threadCnt",
    "drawCanvasRatio",
    "distribute",
    "update",
    "crs"
})
public class JobTile {

    protected boolean run;
    protected int priority;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String desc;
    @XmlElement(required = true)
    protected String tileMapServiceName;
    protected List<LevelSet> levelSet;
    protected int threadCnt;
    protected Integer drawCanvasRatio;
    protected Distribute distribute;
    protected Boolean update;
    protected String crs;

    /**
     * run 속성의 값을 가져옵니다.
     * 
     */
    public boolean isRun() {
        return run;
    }

    /**
     * run 속성의 값을 설정합니다.
     * 
     */
    public void setRun(boolean value) {
        this.run = value;
    }

    /**
     * priority 속성의 값을 가져옵니다.
     * 
     */
    public int getPriority() {
        return priority;
    }

    /**
     * priority 속성의 값을 설정합니다.
     * 
     */
    public void setPriority(int value) {
        this.priority = value;
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
     * tileMapServiceName 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTileMapServiceName() {
        return tileMapServiceName;
    }

    /**
     * tileMapServiceName 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTileMapServiceName(String value) {
        this.tileMapServiceName = value;
    }

    /**
     * Gets the value of the levelSet property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the levelSet property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLevelSet().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LevelSet }
     * 
     * 
     */
    public List<LevelSet> getLevelSet() {
        if (levelSet == null) {
            levelSet = new ArrayList<LevelSet>();
        }
        return this.levelSet;
    }

    /**
     * threadCnt 속성의 값을 가져옵니다.
     * 
     */
    public int getThreadCnt() {
        return threadCnt;
    }

    /**
     * threadCnt 속성의 값을 설정합니다.
     * 
     */
    public void setThreadCnt(int value) {
        this.threadCnt = value;
    }

    /**
     * drawCanvasRatio 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getDrawCanvasRatio() {
        return drawCanvasRatio;
    }

    /**
     * drawCanvasRatio 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDrawCanvasRatio(Integer value) {
        this.drawCanvasRatio = value;
    }

    /**
     * distribute 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Distribute }
     *     
     */
    public Distribute getDistribute() {
        return distribute;
    }

    /**
     * distribute 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Distribute }
     *     
     */
    public void setDistribute(Distribute value) {
        this.distribute = value;
    }

    /**
     * update 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isUpdate() {
        return update;
    }

    /**
     * update 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUpdate(Boolean value) {
        this.update = value;
    }

    /**
     * crs 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCrs() {
        return crs;
    }

    /**
     * crs 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCrs(String value) {
        this.crs = value;
    }

}
