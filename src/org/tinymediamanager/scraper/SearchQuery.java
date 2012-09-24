package org.tinymediamanager.scraper;

import java.util.HashMap;
import java.util.Map;

public class SearchQuery{
    private static final long serialVersionUID = 1L;
   
    /*
        we are going to add IMDBID and ID to the search query
        remove metadata id and series id since they are basically the same
        provider will no longer have search by id, just a searchQUery, and
        then the provider can determine how to search, etc.
    */
    public enum Field { QUERY, RAW_TITLE, CLEAN_TITLE, SEASON, EPISODE, DISC, EPISODE_TITLE, EPISODE_DATE, YEAR, FILE, URL, PROVIDER, ID};
   
    private Map<Field, String> fields = new HashMap<Field, String>();
    private MediaType type = MediaType.MOVIE;
   
    public SearchQuery() {
        // empty
    }
   
    public SearchQuery(SearchQuery query) {
        this.type=query.getMediaType();
        for (Field f : query.fields.keySet()) {
            fields.put(f, query.get(f));
        }
    }

    public SearchQuery(MediaType type, String title) {
        this(type, Field.RAW_TITLE, title);
    }
   
    public SearchQuery(MediaType type, Field field, String value) {
        this.type=type;
        set(field, value);
    }
   
    public MediaType getMediaType() {
        return type;
    }
   
    public SearchQuery setMediaType(MediaType type) {
        this.type=type;
        return this;
    }
   
    public SearchQuery set(Field field, String value) {
        fields.put(field, value);
        return this;
    }
   
    public String get(Field field) {
        return fields.get(field);
    }
   
    @Override
    public String toString() {
       StringBuffer sb =  new StringBuffer("SearchQuery; Type: ").append(type.name()).append("; ");;
       for (Field k : fields.keySet()) {
           sb.append(k.name()).append(":").append(fields.get(k)).append(";");
       }
       return sb.toString();
    }
   
    public static SearchQuery copy(SearchQuery q) {
        return new SearchQuery(q);
    }
}
