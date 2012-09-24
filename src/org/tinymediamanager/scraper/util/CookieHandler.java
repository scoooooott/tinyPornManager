package org.tinymediamanager.scraper.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class CookieHandler {
    private static final Logger log               = Logger.getLogger(CookieHandler.class);

    private Map<String, String> cookies           = new HashMap<String, String>();
    private String              cookieUrl         = null;
    private boolean             lookingForCookies = true;

    public CookieHandler(String cookieUrl) {
        this.cookieUrl = cookieUrl;
    }

    public Map<String, String> getCookiesToSend(String url) {
        log.debug("getCookies called on url: " + url);
        if (cookies.size() == 0 && lookingForCookies) {
            log.debug("We don't have a cookie, so we'll try and get them from: " + cookieUrl);
            // this happens when we are fetching a document from a result that
            // is prev cached.
            // we need to connect to the site url, grab the cookie, and then
            // we'll be ok.

            lookingForCookies = false;
            Url u = new Url(cookieUrl);
            try {
                // this should call us, with the main cookie fetching url, so
                // that we get populated.
                u.getInputStream(this, false);
            } catch (Exception e) {
                // don't care
            }
        }
        return cookies;
    }

    public void handleSetCookie(String url, String cookie) {
        log.debug(String.format("Handlin Cookies: Url: %s; Cookie: %s\n", url, cookie));
        Pattern p = Pattern.compile("([^ =:]+)=([^;]+)");
        Matcher m = p.matcher(cookie);
        // Goup[0]: [ASP.NET_SessionId=v411dwiwwnb04ifq24avpeet]
        // Goup[1]: [ASP.NET_SessionId]
        // Goup[2]: [v411dwiwwnb04ifq24avpeet]
        if (m.find()) {
            cookies.put(m.group(1), m.group(2));
        }
    }

}

