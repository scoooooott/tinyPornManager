package org.tinymediamanager.scraper.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

public class UrlUtil {
    private static final Logger log = Logger.getLogger(UrlUtil.class);

    /**
     * Returns the the entire Url Path except the filename, like doing a basedir
     * on a filename.
     * 
     * @param url
     * @return
     */
    public static String getBaseUrl(String url) {
        String path = getPathName(url);
        if (path != null && path.contains("/")) {
            path = path.substring(0, path.lastIndexOf("/"));
        }
        return getDomainUrl(url) + path;
    }

    public static String getDomainUrl(String url) {
        URL u;
        try {
            u = new URL(url);
            return String.format("%s://%s/", u.getProtocol(), u.getHost());
        } catch (MalformedURLException e) {
            log.error("Failed to get domain url for: " + url);
        }
        return null;
    }

    public static String joinUrlPath(String baseUrl, String path) {
        StringBuffer sb = new StringBuffer(baseUrl);
        if (baseUrl.endsWith("/") && path.startsWith("/")) {
            path = path.substring(1);
        }
        sb.append(path);

        return sb.toString();
    }

    public static String getPathName(String url) {
        URL u;
        try {
            u = new URL(url);
            return u.getPath();
        } catch (MalformedURLException e) {
            log.error("getPathName() Failed! " + url, e);
        }
        return null;
    }

    public static String encode(String data) {
        if (data==null) return "";
        try {
            return URLEncoder.encode(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.warn("Failed to url encode data: " + data + " as UTF-8; will try again using default encoding", e);
            return URLEncoder.encode(data);
        }
    }
    
}

