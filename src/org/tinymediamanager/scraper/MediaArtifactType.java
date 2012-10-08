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
 * The Enum MediaArtifactType.
 */
public enum MediaArtifactType {
    
    /** The background. */
    BACKGROUND("Backgrounds"),
    
    /** The banner. */
    BANNER("Banners"),
    
    /** The poster. */
    POSTER("Posters"),
    
    /** The actor. */
    ACTOR("Actors");
    
    /** The dir. */
    private String dir;
    
    /**
     * Instantiates a new media artifact type.
     *
     * @param dir the dir
     */
    MediaArtifactType(String dir) {
        this.dir=dir;
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
     * To media artifact type.
     *
     * @param artifactType the artifact type
     * @return the media artifact type
     */
    public static MediaArtifactType toMediaArtifactType(String artifactType) {
        if (artifactType==null) return null;
        artifactType=artifactType.toLowerCase();
        if (artifactType.startsWith("background")) return BACKGROUND;
        if (artifactType.startsWith("banner")) return BANNER;
        if (artifactType.startsWith("poster")) return POSTER;
        if (artifactType.startsWith("actor")) return ACTOR;
        return null;
    }
}

