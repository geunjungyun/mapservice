//
// 이 파일은 JAXB(JavaTM Architecture for XML Binding) 참조 구현 2.2.8-b130911.1802 버전을 통해 생성되었습니다. 
// <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>를 참조하십시오. 
// 이 파일을 수정하면 소스 스키마를 재컴파일할 때 수정 사항이 손실됩니다. 
// 생성 날짜: 2020.05.03 시간 01:42:16 PM KST 
//


package com.mapplan;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>status에 대한 Java 클래스입니다.
 * 
 * <p>다음 스키마 단편이 이 클래스에 포함되는 필요한 콘텐츠를 지정합니다.
 * <p>
 * <pre>
 * &lt;simpleType name="status">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="not_exist_image"/>
 *     &lt;enumeration value="not_exist_path"/>
 *     &lt;enumeration value="not_read_path"/>
 *     &lt;enumeration value="registered"/>
 *     &lt;enumeration value="running"/>
 *     &lt;enumeration value="completed"/>
 *     &lt;enumeration value="not_exist_tile"/>
 *     &lt;enumeration value="not_exist_directory"/>
 *     &lt;enumeration value="user_ceate_ok"/>
 *     &lt;enumeration value="user_ceate_fail_id"/>
 *     &lt;enumeration value="user_ceate_fail_domain"/>
 *     &lt;enumeration value="user_ceate_fail_maxReqCnt"/>
 *     &lt;enumeration value="ok"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "status")
@XmlEnum
public enum Status {

    @XmlEnumValue("not_exist_image")
    NOT_EXIST_IMAGE("not_exist_image"),
    @XmlEnumValue("not_exist_path")
    NOT_EXIST_PATH("not_exist_path"),
    @XmlEnumValue("not_read_path")
    NOT_READ_PATH("not_read_path"),
    @XmlEnumValue("registered")
    REGISTERED("registered"),
    @XmlEnumValue("running")
    RUNNING("running"),
    @XmlEnumValue("completed")
    COMPLETED("completed"),
    @XmlEnumValue("not_exist_tile")
    NOT_EXIST_TILE("not_exist_tile"),
    @XmlEnumValue("not_exist_directory")
    NOT_EXIST_DIRECTORY("not_exist_directory"),
    @XmlEnumValue("user_ceate_ok")
    USER_CEATE_OK("user_ceate_ok"),
    @XmlEnumValue("user_ceate_fail_id")
    USER_CEATE_FAIL_ID("user_ceate_fail_id"),
    @XmlEnumValue("user_ceate_fail_domain")
    USER_CEATE_FAIL_DOMAIN("user_ceate_fail_domain"),
    @XmlEnumValue("user_ceate_fail_maxReqCnt")
    USER_CEATE_FAIL_MAX_REQ_CNT("user_ceate_fail_maxReqCnt"),
    @XmlEnumValue("ok")
    OK("ok");
    private final String value;

    Status(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Status fromValue(String v) {
        for (Status c: Status.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
