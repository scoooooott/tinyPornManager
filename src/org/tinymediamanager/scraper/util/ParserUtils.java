package org.tinymediamanager.scraper.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserUtils {
    private static Pattern dateInBrackets = Pattern.compile("(.*)\\s+\\(([0-9]{4})\\)");
    /**
     * return a 2 element array.  0 = title; 1=date 
     * 
     * parses the title in the format Title YEAR or Title (YEAR)
     * 
     * @param title
     * @return
     */
    public static String[] parseTitle(String title) {
        String v[] = {"",""};
        if (title==null) return v;
        
        Pattern p = Pattern.compile("(.*)\\s+\\(?([0-9]{4})\\)?", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(title);
        if (m.find()) {
            v[0]=m.group(1);
            v[1]=m.group(2);
        } else {
            v[0] = title;
        }
        return v;
    }

    /**
     * Parses titles if they are in the form Title (Year).  The first element is the title, and
     * the second element is the date, both can be null.  If the matcher fails to find the
     * pattern, then the passed in title is set as the first element, which is the title.
     * 
     * @param title
     * @return
     */
    public static Pair<String, String> parseTitleAndDateInBrackets(String title) {
        if (title==null) return new Pair<String, String>(null, null);
        
        Pattern p = Pattern.compile("(.*)\\s+\\(?([0-9]{4})\\)?", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(title);
        if (m.find()) {
            return new Pair<String, String>(m.group(1), m.group(2));
        }
        
        return new Pair<String, String>(title, null);
    }
}

