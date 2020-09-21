package com.scott.pornhub;

import java.io.IOException;
import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import retrofit2.Converter;

public class HtmlResponseBodyConverter implements Converter<ResponseBody, Document> {
    private String baseUrl;

    public HtmlResponseBodyConverter(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public Document convert(ResponseBody value) throws
        IOException {
        try {
            return Jsoup.parse(value.byteStream(), "UTF-8", baseUrl);
        } finally {
            value.close();
        }
    }
}