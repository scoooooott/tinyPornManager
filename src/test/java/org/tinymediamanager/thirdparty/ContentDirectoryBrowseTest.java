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

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.SortCriterion;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.thirdparty.upnp.ContentDirectoryService;

public class ContentDirectoryBrowseTest extends BasicTest {

  private static final String                  KODI_FILTER = "dc:date,dc:description,upnp:longDescription,upnp:genre,res,res@duration,res@size,upnp:albumArtURI,upnp:rating,upnp:lastPlaybackPosition,upnp:lastPlaybackTime,upnp:playbackCount,upnp:originalTrackNumber,upnp:episodeNumber,upnp:programTitle,upnp:seriesTitle,upnp:album,upnp:artist,upnp:author,upnp:director,dc:publisher,searchable,childCount,dc:title,dc:creator,upnp:actor,res@resolution,upnp:episodeCount,upnp:episodeSeason,xbmc:dateadded,xbmc:rating,xbmc:votes,xbmc:artwork,xbmc:uniqueidentifier,xbmc:country,xbmc:userrating";
  private static final ContentDirectoryService CDS         = new ContentDirectoryService();

  private String getValidMovieID() {
    return MovieList.getInstance().getMovies().get(0).getDbId().toString();
  }

  private String getValidShowID() {
    return TvShowList.getInstance().getTvShows().get(0).getDbId().toString();
  }

  @Test
  public void browseStructure() throws ContentDirectoryException {
    CDS.browse("0", BrowseFlag.METADATA, "*", 0, 0, SortCriterion.valueOf("")); // 1 result / full meta
    CDS.browse("0", BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf("")); // list of needed meta
    CDS.browse("0", BrowseFlag.DIRECT_CHILDREN, "*", 0, 1, SortCriterion.valueOf("")); // list of needed meta (filtered for 1 result)

    CDS.browse("1", BrowseFlag.METADATA, "*", 0, 0, SortCriterion.valueOf(""));
    CDS.browse("1", BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf(""));
    CDS.browse("1", BrowseFlag.DIRECT_CHILDREN, "*", 0, 1, SortCriterion.valueOf(""));

    CDS.browse("1/t", BrowseFlag.METADATA, "*", 0, 0, SortCriterion.valueOf(""));
    CDS.browse("1/t", BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf(""));
    CDS.browse("1/t", BrowseFlag.DIRECT_CHILDREN, "*", 0, 1, SortCriterion.valueOf(""));

    CDS.browse("1/t/" + getUUID("AnotherMovie"), BrowseFlag.METADATA, "*", 0, 0, SortCriterion.valueOf(""));
    CDS.browse("1/t/" + getUUID("AnotherMovie"), BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf(""));
    CDS.browse("1/t/" + getUUID("AnotherMovie"), BrowseFlag.DIRECT_CHILDREN, "*", 0, 1, SortCriterion.valueOf(""));

    CDS.browse("1/g", BrowseFlag.METADATA, "*", 0, 0, SortCriterion.valueOf(""));
    CDS.browse("1/g", BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf(""));
    CDS.browse("1/g", BrowseFlag.DIRECT_CHILDREN, "*", 0, 1, SortCriterion.valueOf(""));

    CDS.browse("1/g/Abenteuer", BrowseFlag.METADATA, "*", 0, 0, SortCriterion.valueOf(""));
    CDS.browse("1/g/Abenteuer", BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf(""));
    CDS.browse("1/g/Abenteuer", BrowseFlag.DIRECT_CHILDREN, "*", 0, 1, SortCriterion.valueOf(""));

    CDS.browse("1/g/Abenteuer/" + getUUID("AnotherMovie"), BrowseFlag.METADATA, "*", 0, 0, SortCriterion.valueOf(""));
    CDS.browse("1/g/Abenteuer/" + getUUID("AnotherMovie"), BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf(""));
    CDS.browse("1/g/Abenteuer/" + getUUID("AnotherMovie"), BrowseFlag.DIRECT_CHILDREN, "*", 0, 1, SortCriterion.valueOf(""));

    CDS.browse("1/g/invalid", BrowseFlag.METADATA, "*", 0, 0, SortCriterion.valueOf(""));
    CDS.browse("1/g/invalid", BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf(""));
    CDS.browse("1/g/invalid", BrowseFlag.DIRECT_CHILDREN, "*", 0, 1, SortCriterion.valueOf(""));

    CDS.browse("2", BrowseFlag.METADATA, "*", 0, 0, SortCriterion.valueOf(""));
    CDS.browse("2", BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf(""));
    CDS.browse("2", BrowseFlag.DIRECT_CHILDREN, "*", 0, 1, SortCriterion.valueOf(""));

    CDS.browse("2/" + getUUID("UPNPShow3"), BrowseFlag.METADATA, "*", 0, 0, SortCriterion.valueOf("")); // show
    CDS.browse("2/" + getUUID("UPNPShow3") + "/1", BrowseFlag.METADATA, "*", 0, 0, SortCriterion.valueOf("")); // episode
    CDS.browse("2/" + getUUID("UPNPShow3") + "/1/2", BrowseFlag.METADATA, "*", 0, 0, SortCriterion.valueOf("")); // season
    //
    CDS.browse("2/" + getUUID("UPNPShow3"), BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf("")); // show
    CDS.browse("2/" + getUUID("UPNPShow3") + "/1", BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf("")); // episode
    CDS.browse("2/" + getUUID("UPNPShow3") + "/1/2", BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf("")); // season
    //
    CDS.browse("0", BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf(""));
    CDS.browse("0", "BrowseDirectChildren", "*", new UnsignedIntegerFourBytes(0), new UnsignedIntegerFourBytes(0), "");
    //
    CDS.browse("1", BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf(""));
    CDS.browse("1", BrowseFlag.DIRECT_CHILDREN, "*", 1, 0, SortCriterion.valueOf(""));
  }

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
    browse("2/" + getValidShowID(), BrowseFlag.DIRECT_CHILDREN);
  }

  // =====================================================
  // meta data information
  // =====================================================
  @Test
  public void metadataRootContainer() throws ContentDirectoryException {
    browse("0", BrowseFlag.METADATA);
  }

  @Test
  public void metadataMovie() throws ContentDirectoryException {
    browse("1/" + getValidMovieID(), BrowseFlag.METADATA);
  }

  @Test
  public void metadataEpisode() throws ContentDirectoryException {
    browse("2/" + getValidShowID() + "/1/2", BrowseFlag.METADATA);
  }

  // =====================================================
  // INVALID exception tests / empty responses!
  // =====================================================
  @Test
  public void metadataMovieContainer() throws ContentDirectoryException {
    BrowseResult r = browse("1", BrowseFlag.METADATA);
    assertEqual(Long.valueOf(1), r.getCountLong());
  }

  @Test
  public void metadataTvShowContainer() throws ContentDirectoryException {
    BrowseResult r = browse("2", BrowseFlag.METADATA);
    assertEqual(Long.valueOf(1), r.getCountLong());
  }

  @Test
  public void invalidMovieUUID() throws ContentDirectoryException {
    BrowseResult r = browse("1/00000000-0000-0000-0000-000000000000", BrowseFlag.METADATA);
    assertEqual(Long.valueOf(0), r.getCountLong());
  }

  @Test
  public void invalidShowUUID() throws ContentDirectoryException {
    BrowseResult r = browse("2/00000000-0000-0000-0000-000000000000/1/2", BrowseFlag.METADATA);
    assertEqual(Long.valueOf(0), r.getCountLong());
  }

  @Test
  public void invalidEpisodeSE() throws ContentDirectoryException {
    BrowseResult r = browse("2/" + getValidShowID() + "/10/20", BrowseFlag.METADATA);
    assertEqual(Long.valueOf(0), r.getCountLong());
  }

  private BrowseResult browse(String s, BrowseFlag b) throws ContentDirectoryException {
    return CDS.browse(s, b, "", 0, 200, SortCriterion.valueOf("+dc:date,+dc:title"));
  }

  @BeforeClass
  public static void init() throws Exception {
    setTraceLogging();
    deleteSettingsFolder();
    Settings.getInstance(getSettingsFolder());

    TmmModuleManager.getInstance().startUp();
    MovieModuleManager.getInstance().startUp();
    TvShowModuleManager.getInstance().startUp();

    createFakeMovie("UPNPMovie3");
    createFakeMovie("UPNPMovie2");
    createFakeMovie("UPNPMovie1");
    createFakeMovie("AnotherMovie");

    createFakeShow("UPNPShow3");
    createFakeShow("UPNPShow2");
    createFakeShow("UPNPShow1");
    createFakeShow("AnotherShow");
  }

  @AfterClass
  public static void shutdown() throws Exception {
    TvShowModuleManager.getInstance().shutDown();
    MovieModuleManager.getInstance().shutDown();
    TmmModuleManager.getInstance().shutDown();
  }

}
