/*
 * Copyright 2012 - 2015 Manuel Laggner
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
package org.tinymediamanager.scraper.moviemeternl.model;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import redstone.xmlrpc.XmlRpcArray;
import redstone.xmlrpc.XmlRpcStruct;

/**
 * @author Myron Boyle
 */
public class FilmDetail {
  private String              url;
  private String              thumbnail;
  private String              title;
  private ArrayList<Title>    alternative_titles;
  private String              year;
  private String              imdb;
  private String              plot;
  private String              duration;
  private ArrayList<Duration> durations;
  private ArrayList<Actor>    actors;
  private String              actors_text;
  private ArrayList<Director> directors;
  private String              directors_text;
  private ArrayList<Country>  countries;
  private String              countries_text;
  private ArrayList<Genre>    genres;
  private String              genres_text;
  private ArrayList<Date>     dates_cinema;
  private ArrayList<Date>     dates_video;
  private String              average;
  private String              votes_count;
  private int                 filmId;

  public FilmDetail(XmlRpcStruct struct) {
    this.url = struct.getString("url");
    this.thumbnail = struct.getString("thumbnail");
    this.title = struct.getString("title");
    this.alternative_titles = getInner(struct.getArray("alternative_titles"), Title.class);
    this.year = struct.getString("year");
    this.imdb = struct.getString("imdb");
    this.plot = struct.getString("plot");
    this.duration = struct.getString("duration");
    this.durations = getInner(struct.getArray("durations"), Duration.class);
    this.actors = getInner(struct.getArray("actors"), Actor.class);
    this.actors_text = struct.getString("actors_text");
    this.directors = getInner(struct.getArray("directors"), Director.class);
    this.directors_text = struct.getString("directors_text");
    this.countries = getInner(struct.getArray("countries"), Country.class);
    this.countries_text = struct.getString("countries_text");
    this.genres = getSeparated(struct.getArray("genres"));
    this.genres_text = struct.getString("genres_text");
    this.dates_cinema = getInner(struct.getArray("dates_cinema"), Date.class);
    this.dates_video = getInner(struct.getArray("dates_video"), Date.class);
    this.average = struct.getString("average");
    this.votes_count = struct.getString("votes_count");
    this.filmId = struct.getInteger("filmId");
  }

  public ArrayList getInner(XmlRpcArray arr, Class child) {
    ArrayList al = new ArrayList();

    for (int i = 0; i < arr.size(); i++) {
      XmlRpcStruct str = arr.getStruct(i);
      Constructor con;
      Object object = null;
      try {
        con = child.getDeclaredConstructor(new Class[] { XmlRpcStruct.class });
        object = con.newInstance(new Object[] { str });
        al.add(object);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
    return al;
  }

  public ArrayList<Genre> getSeparated(XmlRpcArray arr) {
    ArrayList al = new ArrayList();
    for (int i = 0; i < arr.size(); i++) {
      al.add(new Genre(arr.get(i).toString()));
    }
    return al;
  }

  public static class Duration {
    public String duration;
    public String description;

    public Duration(XmlRpcStruct struct) {
      this.duration = struct.getString("duration");
      this.description = struct.getString("description");
    }

    public Duration(String duration, String description) {
      this.duration = duration;
      this.description = description;
    }

    public String getDuration() {
      return this.duration;
    }

    public String getDescription() {
      return this.description;
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
  }

  public static class Actor {
    public String name;
    public String voice;

    public Actor(XmlRpcStruct struct) {
      this.name = struct.getString("name");
      this.voice = struct.getString("voice");
    }

    public String getName() {
      return this.name;
    }

    public String getVoice() {
      return this.voice;
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
  }

  public static class Director {
    public String id;
    public String name;

    public Director(XmlRpcStruct struct) {
      this.id = struct.getString("id");
      this.name = struct.getString("name");
    }

    public String getId() {
      return this.id;
    }

    public String getName() {
      return this.name;
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
  }

  public static class Country {
    public String iso_3166_1;
    public String name;

    public Country(XmlRpcStruct struct) {
      this.iso_3166_1 = struct.getString("iso_3166_1");
      this.name = struct.getString("name");
    }

    public String getIso_3166_1() {
      return this.iso_3166_1;
    }

    public String getName() {
      return this.name;
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
  }

  public static class Date {
    public String date;

    public Date(XmlRpcStruct struct) {
      this.date = struct.getString("date");
    }

    public String getDate() {
      return this.date;
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
  }

  public static class Genre {
    public String name;

    public Genre(String name) {
      this.name = name;
    }

    public String getName() {
      return this.name;
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
  }

  public static class Title {
    public String title;

    public Title(XmlRpcStruct struct) {
      this.title = struct.getString("title");
    }

    public String getTitle() {
      return this.title;
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
  }

  public String getUrl() {
    return this.url;
  }

  public String getThumbnail() {
    return this.thumbnail;
  }

  public String getTitle() {
    return this.title;
  }

  public ArrayList<Title> getAlternative_titles() {
    return this.alternative_titles;
  }

  public String getYear() {
    return this.year;
  }

  public String getImdb() {
    return this.imdb;
  }

  public String getPlot() {
    return this.plot;
  }

  public String getDuration() {
    return this.duration;
  }

  public ArrayList<Duration> getDurations() {
    return this.durations;
  }

  public ArrayList<Actor> getActors() {
    return this.actors;
  }

  public String getActors_text() {
    return this.actors_text;
  }

  public ArrayList<Director> getDirectors() {
    return this.directors;
  }

  public String getDirectors_text() {
    return this.directors_text;
  }

  public ArrayList<Country> getCountries() {
    return this.countries;
  }

  public String getCountries_text() {
    return this.countries_text;
  }

  public ArrayList<Genre> getGenres() {
    return this.genres;
  }

  public String getGenres_text() {
    return this.genres_text;
  }

  public ArrayList<Date> getDates_cinema() {
    return this.dates_cinema;
  }

  public ArrayList<Date> getDates_video() {
    return this.dates_video;
  }

  public String getAverage() {
    return this.average;
  }

  public String getVotes_count() {
    return this.votes_count;
  }

  public int getFilmId() {
    return this.filmId;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
