package org.tinymediamanager.scraper.pornhub.v1;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.script.ScriptException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.tinymediamanager.scraper.pornhub.util.RandomStringUtils;
import org.tinymediamanager.scraper.pornhub.util.RnkeyUtils;

/**
 * @author anony
 */
public class JsoupClient {

    static String host = "www.pornhub.com";
    static String url = "https://" + host + "/";

    /**
     * @param path
     * @param params
     * @return
     * @throws IOException
     * @throws ScriptException
     */
    public static Document getForDocument(String path, Map<String, String> params) throws IOException, ScriptException {
        Document a = Jsoup.connect(url + path)
            .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
            .cookies(getCookies())
            .referrer("https://www.google.com")
            .data(params)
            .proxy("127.0.0.1", 10809)
            .get();
        System.out.println(a.outerHtml());
        if (a.html().contains("RNKEY")) {
            RnkeyUtils.genRnKey(a.outerHtml());
            return getForDocument(path, params);
        }
        return a;
    }

    private static Map<String, String> getCookies() {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("bs", RandomStringUtils.random(32));
        cookies.put("platform", "pc");
        cookies.put("ua", "b5b29e4074b1362df9783c4beff7fc0f");
        cookies.put("RNLBSERVERID", "ded6646");
        cookies.put("g36FastPopSessionRequestNumber", "1");
        cookies.put("FastPopSessionRequestNumber", "1");
        cookies.put("FPSRN", "1");
        cookies.put("performance_timing", "home");
        cookies.put("RNKEY", RnkeyUtils.nextKey());
        return cookies;
    }
}
