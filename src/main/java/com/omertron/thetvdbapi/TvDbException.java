/*
 *      Copyright (c) 2004-2015 Matthew Altman & Stuart Boston
 *
 *      This file is part of TheTVDB API.
 *
 *      TheTVDB API is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      any later version.
 *
 *      TheTVDB API is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with TheTVDB API.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.omertron.thetvdbapi;

import java.net.MalformedURLException;
import java.net.URL;

import org.tinymediamanager.scraper.util.ApiKey;

public class TvDbException extends ApiException {

  public TvDbException(ApiExceptionType exceptionType, String response) {
    super(exceptionType, response);
  }

  public TvDbException(ApiExceptionType exceptionType, String response, URL url) {
    super(exceptionType, response, cleanApiKey(url));
  }

  public TvDbException(ApiExceptionType exceptionType, String response, int responseCode, URL url) {
    super(exceptionType, response, responseCode, cleanApiKey(url));
  }

  public TvDbException(ApiExceptionType exceptionType, String response, String url) {
    super(exceptionType, response, cleanApiKey(url));
  }

  public TvDbException(ApiExceptionType exceptionType, String response, int responseCode, String url) {
    super(exceptionType, response, responseCode, cleanApiKey(url));
  }

  public TvDbException(ApiExceptionType exceptionType, String response, URL url, Throwable cause) {
    super(exceptionType, response, cleanApiKey(url), cause);
  }

  public TvDbException(ApiExceptionType exceptionType, String response, int responseCode, URL url, Throwable cause) {
    super(exceptionType, response, responseCode, cleanApiKey(url), cause);
  }

  public TvDbException(ApiExceptionType exceptionType, String response, String url, Throwable cause) {
    super(exceptionType, response, cleanApiKey(url), cause);
  }

  public TvDbException(ApiExceptionType exceptionType, String response, int responseCode, String url, Throwable cause) {
    super(exceptionType, response, responseCode, cleanApiKey(url), cause);
  }

  // strips out our API key from any exception log
  private static String cleanApiKey(String url) {
    return url.replaceAll(ApiKey.decryptApikey("7bHHg4k0XhRERM8xd3l+ElhMUXOA5Ou4vQUEzYLGHt8="), "<API_KEY>");
  }

  private static URL cleanApiKey(URL url) {
    try {
      return new URL(url.toString().replaceAll(ApiKey.decryptApikey("7bHHg4k0XhRERM8xd3l+ElhMUXOA5Ou4vQUEzYLGHt8="), "<API_KEY>"));
    }
    catch (MalformedURLException e) {
      return url;
    }
  }
}
