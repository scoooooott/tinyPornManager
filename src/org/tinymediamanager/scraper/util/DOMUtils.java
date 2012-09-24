package org.tinymediamanager.scraper.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMUtils {
    public static String getElementValue(Element el, String tag) {
        if (el==null || tag==null) return null;
        NodeList nl = el.getElementsByTagName(tag);
        if (nl.getLength() > 0) {
            Node n = nl.item(0);
            return StringUtils.trim(n.getTextContent());
        }
        return null;
    }
    
    public static int getElementIntValue(Element el, String tag) {
        NodeList nl = el.getElementsByTagName(tag);
        if (nl.getLength() > 0) {
            Node n = nl.item(0);
            return NumberUtils.toInt(StringUtils.trim(n.getTextContent()));
        }
        return 0;
    }
    
    public static String getMaxElementValue(Element el, String tag) {
        NodeList nl = el.getElementsByTagName(tag);
        String retVal = null;
        for (int i = 0; i < nl.getLength(); i++) {
            String s = nl.item(i).getTextContent();
            if (retVal == null) {
                retVal = s;
            } else {
                if (s != null && s.length() > retVal.length()) {
                    retVal = s;
                }
            }
        }
        return retVal;
    }
    
    public static Element getElementByTagName(Element el, String tag) {
        NodeList nl = el.getElementsByTagName(tag);
        if (nl.getLength() > 0) {
            return (Element)nl.item(0);
        }
        return null;
    }
}