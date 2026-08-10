//
// 이 파일은 JAXB(JavaTM Architecture for XML Binding) 참조 구현 2.2.8-b130911.1802 버전을 통해 생성되었습니다. 
// <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>를 참조하십시오. 
// 이 파일을 수정하면 소스 스키마를 재컴파일할 때 수정 사항이 손실됩니다. 
// 생성 날짜: 2026.06.22 시간 02:12:30 PM KST 
//


package com.gis.protocol.freegis3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>deleteOverlapMode에 대한 Java 클래스입니다.
 * 
 * <p>다음 스키마 단편이 이 클래스에 포함되는 필요한 콘텐츠를 지정합니다.
 * <p>
 * <pre>
 * &lt;simpleType name="deleteOverlapMode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="OnlyStyle"/>
 *     &lt;enumeration value="AllLStyle"/>
 *     &lt;enumeration value="NotDelete"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "deleteOverlapMode")
@XmlEnum
public enum DeleteOverlapMode {


    /**
     * 
     * 				현재 스타일로 정의 된 그룹하고만 겹침 처리를 한다. ex) 반투명 행정동 경계
     * 				
     * 
     */
    @XmlEnumValue("OnlyStyle")
    ONLY_STYLE("OnlyStyle"),

    /**
     * 
     * 				AllStyle로 정의된 그룹하고만 겹침 처리를 한다.
     * 				
     * 
     */
    @XmlEnumValue("AllLStyle")
    ALL_L_STYLE("AllLStyle"),

    /**
     * 
     * 				현재 스타일로 정의 된 그룹은 겹침 처리를 하지 않는다. ex) 지하철 출구
     * 				
     * 
     */
    @XmlEnumValue("NotDelete")
    NOT_DELETE("NotDelete");
    private final String value;

    DeleteOverlapMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DeleteOverlapMode fromValue(String v) {
        for (DeleteOverlapMode c: DeleteOverlapMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
