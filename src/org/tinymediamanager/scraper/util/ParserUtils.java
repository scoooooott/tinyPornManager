/*
 * Copyright 2012 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.scraper.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Class ParserUtils.
 */
public class ParserUtils {
    
    /** The date in brackets. */
    private static Pattern dateInBrackets = Pattern.compile("(.*)\\s+\\(([0-9]{4})\\)");
    
    /**
     * return a 2 element array.  0 = title; 1=date
     * 
     * parses the title in the format Title YEAR or Title (YEAR)
     *
     * @param title the title
     * @return the string[]
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
     * @param title the title
     * @return the pair
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

