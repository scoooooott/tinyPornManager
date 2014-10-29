/*
 * Copyright 2012 - 2014 Manuel Laggner
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
package org.tinymediamanager.scraper.opensubtitles.model;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import redstone.xmlrpc.XmlRpcArray;
import redstone.xmlrpc.XmlRpcStruct;

/**
 * @author Myron Boyle
 */
public class Info {
  private double               seconds;
  private String               status;
  private ArrayList<MovieInfo> movieInfo = new ArrayList<Info.MovieInfo>();

  public Info(XmlRpcStruct struct) {
    this.seconds = struct.getDouble("seconds");
    this.status = struct.getString("status");
    XmlRpcStruct data = struct.getStruct("data");
    for (Object entry : data.values()) {
      XmlRpcArray arr = (XmlRpcArray) entry;
      movieInfo.add((MovieInfo) getInner(arr, MovieInfo.class).get(0));
    }
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

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  public ArrayList<MovieInfo> getMovieInfo() {
    return movieInfo;
  }

  public double getSeconds() {
    return seconds;
  }

  public String getStatus() {
    return status;
  }

  public static class MovieInfo {
    public String MovieKind;
    public String SubCount;
    public String SeenCount;
    public String MovieImdbID;
    public String MovieYear;
    public String MovieHash;
    public String SeriesEpisode;
    public String MovieName;
    public String SeriesSeason;

    public MovieInfo(XmlRpcStruct struct) {
      this.MovieKind = struct.getString("MovieKind");
      this.SubCount = struct.getString("SubCount");
      this.SeenCount = struct.getString("SeenCount");
      this.MovieImdbID = struct.getString("MovieImdbID");
      this.MovieYear = struct.getString("MovieYear");
      this.MovieHash = struct.getString("MovieHash");
      this.SeriesEpisode = struct.getString("SeriesEpisode");
      this.MovieName = struct.getString("MovieName");
      this.SeriesSeason = struct.getString("SeriesSeason");
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
  }
}
