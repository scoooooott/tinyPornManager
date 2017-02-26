/*
 * Copyright 2012 - 2017 Manuel Laggner
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
package org.tinymediamanager.core.movie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.MediaGenres;

/**
 * This enum is used to provide all options for movie filtering (in UI)
 * 
 * @author Manuel Laggner
 */
public enum MovieSearchOptions {
  // hint: whenever a non String search option is added, you have to add it to the (un)marshaller below too
  DUPLICATES,
  WATCHED,
  GENRE,
  CERTIFICATION,
  CAST,
  TAG,
  MOVIESET,
  VIDEO_FORMAT,
  VIDEO_CODEC,
  AUDIO_CODEC,
  DATASOURCE,
  MISSING_METADATA,
  MISSING_ARTWORK,
  MISSING_SUBTITLES,
  NEW_MOVIES,
  MEDIA_SOURCE,
  YEAR,
  VIDEO_3D,
  OFFLINE;

  /*
   * helper classes for transforming entities to writeable strings
   */
  public static class MovieSearchOptionsAdapter extends XmlAdapter<MovieSearchOptionsMapType, Map<MovieSearchOptions, Object>> {
    @Override
    public Map<MovieSearchOptions, Object> unmarshal(MovieSearchOptionsMapType v) throws Exception {
      Map<MovieSearchOptions, Object> options = new HashMap<>();
      if (v == null || v.entries == null) {
        return options;
      }

      // try to unmarshal the values
      for (MovieSearchOptionsEntryType entry : v.entries) {
        try {
          switch (entry.key) {
            // bool
            case "DUPLICATES":
            case "WATCHED":
            case "MOVIESET":
            case "MISSING_METADATA":
            case "MISSING_ARTWORK":
            case "MISSING_SUBTITLES":
            case "NEW_MOVIES":
            case "VIDEO_3D":
            case "OFFLINE":
              options.put(MovieSearchOptions.valueOf(entry.key), Boolean.valueOf(entry.value));
              break;

            // integer
            case "YEAR":
              options.put(MovieSearchOptions.valueOf(entry.key), Integer.valueOf(entry.value));
              break;

            // special cases
            case "GENRE":
              options.put(MovieSearchOptions.valueOf(entry.key), MediaGenres.getGenre(entry.value));
              break;

            case "CERTIFICATION":
              options.put(MovieSearchOptions.valueOf(entry.key), Certification.valueOf(entry.value));
              break;

            case "MEDIA_SOURCE":
              options.put(MovieSearchOptions.valueOf(entry.key), MediaSource.valueOf(entry.value));
              break;

            default:
              options.put(MovieSearchOptions.valueOf(entry.key), entry.value);
              break;
          }
        }
        catch (Exception ignored) {
        }
      }

      return options;
    }

    @Override
    public MovieSearchOptionsMapType marshal(Map<MovieSearchOptions, Object> v) throws Exception {
      MovieSearchOptionsMapType output = new MovieSearchOptionsMapType();

      for (Entry<MovieSearchOptions, Object> entry : v.entrySet()) {
        switch (entry.getKey()) {
          case GENRE:
            MediaGenres genre = (MediaGenres) entry.getValue();
            output.entries.add(new MovieSearchOptionsEntryType(entry.getKey().name(), genre.name()));
            break;

          case CERTIFICATION:
            Certification cert = (Certification) entry.getValue();
            output.entries.add(new MovieSearchOptionsEntryType(entry.getKey().name(), cert.name()));
            break;

          case MEDIA_SOURCE:
            MediaSource source = (MediaSource) entry.getValue();
            output.entries.add(new MovieSearchOptionsEntryType(entry.getKey().name(), source.name()));
            break;

          default:
            output.entries.add(new MovieSearchOptionsEntryType(entry.getKey().name(), String.valueOf(entry.getValue())));
            break;
        }
      }

      return output;
    }
  }

  public static class MovieSearchOptionsMapType {
    public List<MovieSearchOptionsEntryType> entries = new ArrayList<>();
  }

  public static class MovieSearchOptionsEntryType {
    @XmlAttribute
    public String key;

    @XmlValue
    public String value;

    public MovieSearchOptionsEntryType() {
    }

    public MovieSearchOptionsEntryType(String key, String value) {
      this.key = key;
      this.value = value;
    }
  }
}
