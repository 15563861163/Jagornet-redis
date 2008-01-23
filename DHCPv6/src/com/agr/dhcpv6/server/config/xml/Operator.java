//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-520 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.01.22 at 06:43:49 PM EST 
//


package com.agr.dhcpv6.server.config.xml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for operator.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="operator">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="equals"/>
 *     &lt;enumeration value="startsWith"/>
 *     &lt;enumeration value="endsWith"/>
 *     &lt;enumeration value="contains"/>
 *     &lt;enumeration value="regExp"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "operator")
@XmlEnum
public enum Operator {

    @XmlEnumValue("equals")
    EQUALS("equals"),
    @XmlEnumValue("startsWith")
    STARTS_WITH("startsWith"),
    @XmlEnumValue("endsWith")
    ENDS_WITH("endsWith"),
    @XmlEnumValue("contains")
    CONTAINS("contains"),
    @XmlEnumValue("regExp")
    REG_EXP("regExp");
    private final String value;

    Operator(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Operator fromValue(String v) {
        for (Operator c: Operator.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
