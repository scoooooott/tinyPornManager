package com.scott.pornhub.entities;

import com.scott.pornhub.enumerations.VideoType;
import java.util.List;

public class Videos {

    public static class Video {

        public String id;
        public String iso_639_1;
        public String iso_3166_1;
        public String key;
        public String name;
        public String site;
        public Integer size;
        public VideoType type;

    }

    public Integer id;
    public List<Video> results;

}
