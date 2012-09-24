package org.tinymediamanager.scraper.util;

/**
 * A Pair is simple container containing 2 paired values and types.
 * 
 * @author seans
 *
 * @param <First>
 * @param <Second>
 */
public class Pair<First, Second> {
    private First first;
    private Second second;
    public Pair(First f, Second s) {
        this.first = f;
        this.second=s;
    }
    
    public First first() {
        return first;
    }
    public Second second() {
        return second;
    }
}

