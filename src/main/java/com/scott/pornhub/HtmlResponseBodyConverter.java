package com.scott.pornhub;

import java.io.BufferedInputStream;
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
    public Document convert(ResponseBody value) throws IOException {
        BufferedInputStream myStream = new BufferedInputStream(value.byteStream());
        try {
            return Jsoup.parse(myStream, "UTF-8", baseUrl);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            value.close();
        }
        return new Document(baseUrl);
    }
}