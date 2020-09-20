package com.scott.pornhub.entities;

import com.scott.pornhub.PornhubHelper;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PornhubDate {

    // can not use ThreadLocal.withInitial as it requires Java 8
    private static final ThreadLocal<DateFormat> TMDB_DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        public DateFormat initialValue() {
            return new SimpleDateFormat(PornhubHelper.TMDB_DATE_PATTERN);
        }
    };

    private final Date date;

    public PornhubDate(Date date) {
        this.date = date;
    }

    public PornhubDate(String date) {
        Date parsedDate;
        try {
            parsedDate = TMDB_DATE_FORMAT.get().parse(date);
        } catch (ParseException e) {
            parsedDate = null;
        }
        this.date = parsedDate;
    }

    @Override
    public String toString() {
        return TMDB_DATE_FORMAT.get().format(date);
    }

}
