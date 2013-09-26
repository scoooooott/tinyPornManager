/*
 * Copyright 2012 - 2013 Manuel Laggner
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
package org.tinymediamanager.scraper.moviemeternl;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.moviemeternl.model.ApiStartSession;
import org.tinymediamanager.scraper.moviemeternl.model.Film;
import org.tinymediamanager.scraper.moviemeternl.model.FilmDetail;

import redstone.xmlrpc.XmlRpcArray;
import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcFault;
import redstone.xmlrpc.XmlRpcStruct;

public class MoviemeterApi {

  private static final Logger    LOGGER  = LoggerFactory.getLogger(MoviemeterApi.class); ;

  private static final String    SERVICE = "http://www.moviemeter.nl/ws";
  private static String          APIKEY  = "";

  private static ApiStartSession session = null;
  private static XmlRpcClient    client  = null;

  public MoviemeterApi(String apiKey) {
    APIKEY = apiKey;
    if (client == null) {
      try {
        client = new XmlRpcClient(SERVICE, false);
      }
      catch (MalformedURLException e) {
        LOGGER.error("cannot create XmlRpcClient", e);
      }
    }
  }

  // system.listMethods()
  // system.methodHelp(string method_name)
  // system.methodSignature(string method_name)
  //
  // api.startSession(string apikey), returns array with sessionkey and unix timestamp for session's expiration date
  // api.closeSession(string sessionkey), returns boolean
  //
  // film.search(string sessionkey, string search), returns array with films
  // film.retrieveScore(string sessionkey, int filmId), returns array with information about the current score (average, total, amount of votes)
  // film.retrieveImdb(string sessionkey, int filmId), returns array with imdb code, url, score and votes for this film
  // film.retrieveByImdb(string sessionkey, string imdb code), returns filmId corresponding to the imdb code supplied
  // film.retrieveDetails(string sessionkey, int filmId), returns array with information about the film
  // film.retrieveImage(string sessionkey, int filmId), returns array with information and base64 encoded contents of poster and its thumbnail
  // film.retrieveReviews(string sessionkey, int filmId), retrieve summarized reviews for the film
  // film.retrieveReview(string sessionkey, int filmId, int messageId), retrieves full review
  //
  // film.retrieveCinema(string sessionkey)
  // film.retrieveVideo(string sessionkey)
  // film.retrieveTv(string sessionkey)
  // film.retrieveTvAll(string sessionkey)
  //
  // director.search(string sessionkey, string search), returns array with directors
  // director.retrieveDetails(string sessionkey, int directorId), returns array with director's information
  // director.retrieveFilms(string sessionkey, int directorId), returns array with director's films
  // director.retrieveImage(string sessionkey, int directorId), returns array with information and base64 encoded contents of director image

  /**
   * 
   * calls the specific method with params...
   * 
   * @param method
   *          the method
   * @param params
   *          the params
   * @return return value
   * @throws XmlRpcFault
   * @throws XmlRpcException
   */
  public Object methodCall(String method, Object params) throws XmlRpcException, XmlRpcFault {
    startSession();
    Object token = null;
    if (params != null) {
      token = client.invoke(method, new Object[] { session.getSession_key(), params });
    }
    else {
      token = client.invoke(method, new Object[] { session.getSession_key() });
    }
    return token;

  }

  public ArrayList filmImages(int filmId) {
    ArrayList al = new ArrayList();
    try {
      XmlRpcStruct token = (XmlRpcStruct) methodCall("film.retrieveImage", filmId);
      for (int i = 0; i < token.size(); i++) {
        // TODO: gets base64 encoded images inline
      }
    }
    catch (Exception e) {
      LOGGER.error("Error getting images: " + filmId);
    }
    return al;
  }

  public ArrayList<Film> filmSearch(String search) {
    ArrayList<Film> al = new ArrayList<Film>();
    try {
      XmlRpcArray token = (XmlRpcArray) methodCall("film.search", search);
      for (int i = 0; i < token.size(); i++) {
        Film film = new Film(token.getStruct(i));
        al.add(film);
      }
    }
    catch (Exception e) {
      LOGGER.error("Error searching movie: " + search);
    }
    return al;
  }

  public FilmDetail filmDetail(int filmId) {
    FilmDetail film = null;
    try {
      XmlRpcStruct token = (XmlRpcStruct) methodCall("film.retrieveDetails", filmId);
      film = new FilmDetail(token);
    }
    catch (Exception e) {
      LOGGER.error("Error getting movie details for id: " + filmId);
    }
    return film;
  }

  public FilmDetail filmSearchImdb(String imdb) {
    FilmDetail film = null;
    try {
      String id = (String) methodCall("film.retrieveByImdb", imdb);
      film = filmDetail(Integer.valueOf(id));
    }
    catch (Exception e) {
      LOGGER.error("Cannot find movie for IMDBid: " + imdb);
    }
    return film;
  }

  /**
   * starts a new session, if we don't have any; or if it is expired
   */
  private void startSession() {
    if (session == null || session.getSession_key().isEmpty() || new Date().compareTo(session.getValid_till()) > 0) {
      Object token = null;
      try {
        token = client.invoke("api.startSession", new Object[] { APIKEY });
      }
      catch (Exception e) {
        LOGGER.error("Could not start session!", e);
      }
      XmlRpcStruct response = (XmlRpcStruct) token;
      session = new ApiStartSession(response);
      // System.out.println(session.getSession_key());
    }
    else {
      // System.out.println("session still valid till " + session.getValid_till());
    }
  }

  public void closeSession() {
    try {
      methodCall("api.closeSession", null);
    }
    catch (Exception e) {
      LOGGER.error("Cannot close session.");
    }
    session = null;
  }

}
