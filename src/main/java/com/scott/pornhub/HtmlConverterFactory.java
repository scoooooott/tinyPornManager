package com.scott.pornhub;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import okhttp3.ResponseBody;
import org.jsoup.nodes.Document;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class HtmlConverterFactory extends Converter.Factory {
    private String baseUrl;

    private HtmlConverterFactory(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public static HtmlConverterFactory create(String baseUrl) {
        return new HtmlConverterFactory(baseUrl);
    }

    @Override
    public Converter<ResponseBody, Document> responseBodyConverter(Type type,
        Annotation[] annotations, Retrofit retrofit) {
        return new HtmlResponseBodyConverter(this.baseUrl);
    }
}