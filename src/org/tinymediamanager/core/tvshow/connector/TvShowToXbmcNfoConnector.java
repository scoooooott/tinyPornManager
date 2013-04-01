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
package org.tinymediamanager.core.tvshow.connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Logger;
import org.tinymediamanager.core.tvshow.TvShow;

/**
 * The Class TvShowToXbmcNfoConnector.
 * 
 * @author Manuel Laggner
 */
@XmlRootElement(name = "tvshow")
public class TvShowToXbmcNfoConnector {

  /** The Constant logger. */
  private static final Logger LOGGER = Logger.getLogger(TvShowToXbmcNfoConnector.class);

  /** The title. */
  private String              title;

  /**
   * Instantiates a new tv show to xbmc nfo connector.
   */
  public TvShowToXbmcNfoConnector() {
  }

  /**
   * Sets the data.
   * 
   * @param tvShow
   *          the tv show
   * @return the string
   */
  public static String setData(TvShow tvShow) {
    TvShowToXbmcNfoConnector xbmc = null;
    JAXBContext context = null;
    String nfoFilename = "tvshow.nfo";
    File nfoFile = new File(tvShow.getPath(), nfoFilename);

    // load existing NFO if possible
    if (nfoFile.exists()) {
      try {
        synchronized (JAXBContext.class) {
          context = JAXBContext.newInstance(TvShowToXbmcNfoConnector.class);
        }
        Unmarshaller um = context.createUnmarshaller();
        Reader in = new InputStreamReader(new FileInputStream(nfoFile), "UTF-8");
        xbmc = (TvShowToXbmcNfoConnector) um.unmarshal(in);
      }
      catch (Exception e) {
        LOGGER.error("failed to parse " + nfoFilename, e);
      }
    }

    // create new
    if (xbmc == null) {
      xbmc = new TvShowToXbmcNfoConnector();
    }

    // set data
    xbmc.setTitle(tvShow.getTitle());

    // and marshall it
    try {

      synchronized (JAXBContext.class) {
        context = JAXBContext.newInstance(TvShowToXbmcNfoConnector.class);
      }
      Marshaller m = context.createMarshaller();
      m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

      Writer w = new StringWriter();
      m.marshal(xbmc, w);
      StringBuilder sb = new StringBuilder(w.toString());
      w.close();

      // on windows make windows conform linebreaks
      if (SystemUtils.IS_OS_WINDOWS) {
        sb = new StringBuilder(sb.toString().replaceAll("(?<!\r)\n", "\r\n"));
      }
      FileUtils.write(nfoFile, sb, "UTF-8");
    }
    catch (JAXBException e) {
      LOGGER.error("setData", e);
    }
    catch (IOException e) {
      LOGGER.error("setData", e);
    }

    // return only the name w/o path
    return nfoFilename;
  }

  /**
   * Gets the title.
   * 
   * @return the title
   */
  @XmlElement(name = "title")
  public String getTitle() {
    return title;
  }

  /**
   * Sets the title.
   * 
   * @param title
   *          the new title
   */
  public void setTitle(String title) {
    this.title = title;
  }

}
