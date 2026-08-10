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
 *         &lt;element name="extensionArea" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="deleteOverlapMode" type="{}deleteOverlapMode" minOccurs="0"/>
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
    "extensionArea",
    "deleteOverlapMode"
})
@XmlRootElement(name = "overlapConfig")
public class OverlapConfig {

    protected Integer extensionArea;
    @XmlSchemaType(name = "string")
    protected DeleteOverlapMode deleteOverlapMode;

    /**
     * extensionArea 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getExtensionArea() {
        return extensionArea;
    }

    /**
     * extensionArea 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setExtensionArea(Integer value) {
        this.extensionArea = value;
    }

    /**
     * deleteOverlapMode 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link DeleteOverlapMode }
     *     
     */
    public DeleteOverlapMode getDeleteOverlapMode() {
        return deleteOverlapMode;
    }

    /**
     * deleteOverlapMode 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link DeleteOverlapMode }
     *     
     */
    public void setDeleteOverlapMode(DeleteOverlapMode value) {
        this.deleteOverlapMode = value;
    }

}
