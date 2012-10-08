/*
 * Copyright 2012 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.scraper;

/**
 * The Enum MediaType.
 */
public enum MediaType {
    
    /** The tv. */
    TV("TV","TV"),
    
    /** The movie. */
    MOVIE("Movies","Movie"),
    
    /** The music. */
    MUSIC("Music", "Music"),
    
    /** The music videos. */
    MUSIC_VIDEOS("MusicVideos","MusicVideo"),
    
    // Genre and Actor are special types
    /** The genre. */
    GENRE("Genres", "Genre"),
    
    /** The actor. */
    ACTOR("Actors", "Actor");
    
    /** The dir. */
    private String dir = null;
    
    /** The sage value. */
    private String sageValue;
    
    /**
     * Instantiates a new media type.
     *
     * @param dir the dir
     * @param sageValue the sage value
     */
    private MediaType(String dir, String sageValue) {
        this.dir=dir;
        this.sageValue = sageValue;
    }
    
    /**
     * Dir name.
     *
     * @return the string
     */
    public String dirName() {
        return dir;
    }
    
    /**
     * Sage value.
     *
     * @return the string
     */
    public String sageValue() {
        return sageValue;
    }
    
    /**
     * To media type.
     *
     * @param id the id
     * @return the media type
     */
    public static MediaType toMediaType(String id) {
        if (id==null) return null;
        
        id = id.toLowerCase();
        if ("movie".equals(id) || "movies".equals(id)) {
            return MOVIE;
        }
        
        if ("tv".equals(id)) {
            return TV;
        }

        if ("genre".equals(id) || "genres".equals(id)) {
            return GENRE;
        }

        if ("actor".equals(id) || "actors".equals(id)) {
            return ACTOR;
        }

        if ("music".equals(id)) {
            return MUSIC;
        }

        if ("musicvideos".equals(id)) {
            return MUSIC;
        }
        
        return null;
    }
}