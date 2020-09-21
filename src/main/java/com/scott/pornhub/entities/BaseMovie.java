package com.scott.pornhub.entities;

import java.util.Date;
import java.util.List;

public class BaseMovie extends BaseRatingObject {

    public String id;

    public Boolean adult;
    public String backdrop_path;
    public List<Genre> genres;
    public List<Integer> genre_ids;
    public String original_title;
    public String original_language;
    /**
     * 概览
     */
    public String overview;
    public Double popularity;
    public String poster_path;
    public Date release_date;
    public String title;
    public Double vote_average;
    public Integer vote_count;

    public String media_type;

}
