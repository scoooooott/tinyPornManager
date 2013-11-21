///*
// * Copyright 2012 - 2013 Manuel Laggner
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package org.tinymediamanager.scraper.anidb;
//
//import static org.junit.Assert.*;
//
//import java.util.List;
//
//import org.junit.Test;
//import org.tinymediamanager.scraper.ITvShowMetadataProvider;
//import org.tinymediamanager.scraper.MediaCastMember;
//import org.tinymediamanager.scraper.MediaMetadata;
//import org.tinymediamanager.scraper.MediaScrapeOptions;
//import org.tinymediamanager.scraper.MediaSearchOptions;
//import org.tinymediamanager.scraper.MediaSearchOptions.SearchParam;
//import org.tinymediamanager.scraper.MediaSearchResult;
//import org.tinymediamanager.scraper.MediaType;
//
///**
// * @author Manuel Laggner
// * 
// */
//public class AniDBTest {
//
//  @Test
//  public void testSearch() {
////    AniDBMetadataProvider mp = new AniDBMetadataProvider();
//
//    MediaSearchOptions options = new MediaSearchOptions(MediaType.TV_SHOW);
//    options.set(SearchParam.TITLE, "Spider Riders");
//    try {
//      List<MediaSearchResult> results = mp.search(options);
//
//      for (MediaSearchResult result : results) {
//        System.out.println(result.getTitle() + " " + result.getId() + " " + result.getScore());
//      }
//
//      options.set(SearchParam.TITLE, "Spice and Wolf");
//      results = mp.search(options);
//
//      for (MediaSearchResult result : results) {
//        System.out.println(result.getTitle() + " " + result.getId() + " " + result.getScore());
//      }
//    }
//    catch (Exception e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
//  }
//
//  @Test
//  public void testScrapeTvShow() {
////    ITvShowMetadataProvider mp = new AniDBMetadataProvider();
//    MediaScrapeOptions options = new MediaScrapeOptions();
//    options.setType(MediaType.TV_SHOW);
//    options.setId("anidb", "4242");
//    try {
//      MediaMetadata md = mp.getTvShowMetadata(options);
//      assertEquals("2006-03-25", md.getFirstAired());
//      assertEquals("2006", md.getYear());
//      assertEquals("Spider Riders", md.getTitle());
//      assertEquals(
//          "In this Earth, there exists unknown underground world, the Inner World. In the world, there are braves who fight with large spiders, and they are called Spider Riders. According to his grandfather`s diary, a boy, Hunter Steel is traveling around. Meanwhile he happens to enter the Inner World from a pyramid. There, the war between the insect squad that aims at conquest of the Inner World and Spider Riders continues. Oracle, the fairly of the Inner World, summons Hunter because he thinks Hunter will be the messiah of the world. However, the powers of Oracle are sealed by Mantid who is the rule of the Insecter. For the peace of the Inner World, he has to find four sealed keys of Oracle to retrieve Oracle`s power. Hunter asks Spider Shadow, which is a big spider chosen by Oracle, to become a member of Spider Riders to fight against enemies. Source: AnimeNfo Note: The first three episodes premiered in North America with a 2 month hiatus between episodes 3 and 4, after which the series continued without a break between seasons. Episodes 4-26 aired first in Japan.",
//          md.getPlot());
//      assertEquals(5.66f, md.getRating(), 0.1);
//      assertEquals(48, md.getVoteCount(), 5);
//      assertEquals("http://img7.anidb.net/pics/anime/11059.jpg", md.getPosterUrl());
//      assertEquals("Anime", md.getGenres().get(0).toString());
//
//      // first actor
//      MediaCastMember member = md.getCastMembers().get(0);
//      assertEquals("Hunter Steele", member.getCharacter());
//      assertEquals("Kumai Motoko", member.getName());
//      assertEquals("http://img7.anidb.net/pics/anime/25956.jpg", member.getImageUrl());
//
//      // second
//      member = md.getCastMembers().get(1);
//      assertEquals("Corona", member.getCharacter());
//      assertEquals("Chiba Saeko", member.getName());
//      assertEquals("http://img7.anidb.net/pics/anime/25957.jpg", member.getImageUrl());
//    }
//    catch (Exception e) {
//      e.printStackTrace();
//      fail();
//    }
//  }
//
//  @Test
//  public void testScrapeEpisode() {
//    ITvShowMetadataProvider mp = new AniDBMetadataProvider();
//    MediaScrapeOptions options = new MediaScrapeOptions();
//    options.setType(MediaType.TV_SHOW);
//    options.setId("anidb", "4242");
//    options.setId("episodeNr", "1");
//    options.setId("seasonNr", "1");
//
//    try {
//      MediaMetadata md = mp.getEpisodeMetadata(options);
//    }
//    catch (Exception e) {
//      e.printStackTrace();
//      fail();
//    }
//  }
// }
