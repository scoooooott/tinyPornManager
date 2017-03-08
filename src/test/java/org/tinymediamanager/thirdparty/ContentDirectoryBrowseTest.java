/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.tinymediamanager.thirdparty;

import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.SortCriterion;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.thirdparty.upnp.ContentDirectoryService;

public class ContentDirectoryBrowseTest extends BasicTest {

  private static final String                  KODI_FILTER = "dc:date,dc:description,upnp:longDescription,upnp:genre,res,res@duration,res@size,upnp:albumArtURI,upnp:rating,upnp:lastPlaybackPosition,upnp:lastPlaybackTime,upnp:playbackCount,upnp:originalTrackNumber,upnp:episodeNumber,upnp:programTitle,upnp:seriesTitle,upnp:album,upnp:artist,upnp:author,upnp:director,dc:publisher,searchable,childCount,dc:title,dc:creator,upnp:actor,res@resolution,upnp:episodeCount,upnp:episodeSeason,xbmc:dateadded,xbmc:rating,xbmc:votes,xbmc:artwork,xbmc:uniqueidentifier,xbmc:country,xbmc:userrating";
  private static final ContentDirectoryService CDS         = new ContentDirectoryService();

  // =====================================================
  // directory browsing
  // =====================================================
  @Test
  public void browseRoot() throws ContentDirectoryException {
    browse("0", BrowseFlag.DIRECT_CHILDREN);
  }

  @Test
  public void browseMovies() throws ContentDirectoryException {
    browse("1", BrowseFlag.DIRECT_CHILDREN);
  }

  @Test
  public void browseTvShow() throws ContentDirectoryException {
    browse("2", BrowseFlag.DIRECT_CHILDREN);
  }

  @Test
  public void browseEpisode() throws ContentDirectoryException {
    browse("2/d5e46aef-e85a-4a30-a486-9343d1060f7a", BrowseFlag.DIRECT_CHILDREN);
  }

  // =====================================================
  // meta data information
  // =====================================================
  @Test
  public void metadataMovie() throws ContentDirectoryException {
    browse("1/2a46cf62-df0e-4fb5-86d4-0ce4d67325e2", BrowseFlag.METADATA);
  }

  @Test
  public void metadataEpisode() throws ContentDirectoryException {
    browse("2/d5e46aef-e85a-4a30-a486-9343d1060f7a/1/6", BrowseFlag.METADATA);
  }

  // =====================================================
  // INVALID exception tests
  // =====================================================
  @Test(expected = ContentDirectoryException.class)
  public void metadataRootContainer() throws ContentDirectoryException {
    browse("0", BrowseFlag.METADATA);
  }

  @Test(expected = ContentDirectoryException.class)
  public void metadataMovieContainer() throws ContentDirectoryException {
    browse("1", BrowseFlag.METADATA);
  }

  @Test(expected = ContentDirectoryException.class)
  public void metadataTvShowContainer() throws ContentDirectoryException {
    browse("2", BrowseFlag.METADATA);
  }

  @Test(expected = ContentDirectoryException.class)
  public void invalidMovieUUID() throws ContentDirectoryException {
    browse("1/00000000-0000-0000-0000-000000000000", BrowseFlag.METADATA);
  }

  @Test(expected = ContentDirectoryException.class)
  public void invalidShowUUID() throws ContentDirectoryException {
    browse("2/00000000-0000-0000-0000-000000000000/1/2", BrowseFlag.METADATA);
  }

  @Test(expected = ContentDirectoryException.class)
  public void invalidEpisodeSE() throws ContentDirectoryException {
    browse("2/d5e46aef-e85a-4a30-a486-9343d1060f7a/17/23", BrowseFlag.METADATA);
  }

  private void browse(String s, BrowseFlag b) throws ContentDirectoryException {
    CDS.browse(s, b, "", 0, 200, new SortCriterion[] {});
  }

  @BeforeClass
  public static void init() throws Exception {
    TmmModuleManager.getInstance().startUp();
    MovieModuleManager.getInstance().startUp();
    TvShowModuleManager.getInstance().startUp();
  }

  @AfterClass
  public static void shutdown() throws Exception {
    TvShowModuleManager.getInstance().shutDown();
    MovieModuleManager.getInstance().shutDown();
    TmmModuleManager.getInstance().shutDown();
  }

}
