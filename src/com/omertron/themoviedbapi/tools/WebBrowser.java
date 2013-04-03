/*
 *      Copyright (c) 2004-2013 Stuart Boston
 *
 *      This file is part of TheMovieDB API.
 *
 *      TheMovieDB API is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      any later version.
 *
 *      TheMovieDB API is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with TheMovieDB API.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.omertron.themoviedbapi.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.tinymediamanager.Globals;
import org.tinymediamanager.scraper.util.Url;

import com.omertron.themoviedbapi.MovieDbException;

/**
 * Web browser with simple cookies support
 */
public final class WebBrowser {

  private static final Logger logger = Logger.getLogger(WebBrowser.class);

  // Hide the constructor
  protected WebBrowser() {
    // prevents calls from subclass
    throw new UnsupportedOperationException();
  }

  public static String request(String url) throws MovieDbException {
    try {
      return request(new URL(url));
    }
    catch (MalformedURLException ex) {
      throw new MovieDbException(MovieDbException.MovieDbExceptionType.INVALID_URL, null, ex);
    }
  }

  public static String request(URL requestUrl) throws MovieDbException {
    StringWriter content = null;

    try {
      content = new StringWriter();

      BufferedReader in = null;
      Url url = null;
      try {
        url = new Url(requestUrl.toString());
        url.addHeader("Accept", "application/json");
        url.addHeader("Content-Type", "application/json");

        in = new BufferedReader(new InputStreamReader(url.getInputStream(), url.getCharset()));
        String line;
        while ((line = in.readLine()) != null) {
          content.write(line);
        }
      }
      finally {
        if (in != null) {
          in.close();
        }
      }
      System.out.println(content);
      return content.toString();
    }
    catch (IOException ex) {
      throw new MovieDbException(MovieDbException.MovieDbExceptionType.CONNECTION_ERROR, null, ex);
    }
    finally {
      if (content != null) {
        try {
          content.close();
        }
        catch (IOException ex) {
          logger.debug("Failed to close connection: " + ex.getMessage());
        }
      }
    }
  }

  public static String getProxyHost() {
    return Globals.settings.getProxyHost();
  }

  public static void setProxyHost(String myProxyHost) {
  }

  public static String getProxyPort() {
    return Globals.settings.getProxyPort();
  }

  public static void setProxyPort(String myProxyPort) {
  }

  public static String getProxyUsername() {
    return Globals.settings.getProxyUsername();
  }

  public static void setProxyUsername(String myProxyUsername) {
  }

  public static String getProxyPassword() {
    return Globals.settings.getProxyPassword();
  }

  public static void setProxyPassword(String myProxyPassword) {
  }

  public static int getWebTimeoutConnect() {
    return 0;
  }

  public static int getWebTimeoutRead() {
    return 0;
  }

  public static void setWebTimeoutConnect(int webTimeoutConnect) {
  }

  public static void setWebTimeoutRead(int webTimeoutRead) {
  }
}