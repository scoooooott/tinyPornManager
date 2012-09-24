package org.tinymediamanager.scraper.util;

import java.util.Map;
import java.util.Map.Entry;

public class StringUtils {
    public static String removeHtml(String html) {
        if (html==null) return null;
        return html.replaceAll("<[^>]+>", "");
    }
    
    public static String unquote(String str) {
        if (str==null) return null;
        return str.replaceFirst("^\\\"(.*)\\\"$", "$1");
    }
    
    public static String mapToString(Map map) {
        if (map == null) return "null";
        if (map.size()==0) return "empty";
        
        StringBuilder sb = new StringBuilder();
        for (Object o : map.entrySet()) {
            Map.Entry me = (Entry) o;
            sb.append(me.getKey()).append(": ").append(me.getValue()).append(",");
        }
        return sb.toString();
    }
    
    public static String zeroPad(String encodeString, int padding) {
        try {
            int v = Integer.parseInt(encodeString);
            String format = "%0" + padding + "d";
            return String.format(format, v);
        } catch (Exception e) {
            return encodeString;
        }
    }
    

}