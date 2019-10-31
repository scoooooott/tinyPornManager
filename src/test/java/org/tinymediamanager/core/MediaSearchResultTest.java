package org.tinymediamanager.core;

import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaType;

public class MediaSearchResultTest extends BasicTest {

  @Test
  public void test() {

    MediaSearchResult my = new MediaSearchResult("providerID0", MediaType.MOVIE);

    MediaMetadata md = new MediaMetadata("providerId");
    md.setId(MediaMetadata.IMDB, "tt1000000");
    md.addExtraData("key1", "value1");
    md.addGenre(MediaGenres.ACTION);
    md.addGenre(MediaGenres.ACTION);
    md.addGenre(MediaGenres.FAMILY);

    MediaSearchResult s1 = new MediaSearchResult("imdb", MediaType.MOVIE, "tt1651", "movie1", 2014, 0.5f);
    s1.setMetadata(md);

    MediaSearchResult s2 = new MediaSearchResult("TMDB", MediaType.MOVIE, "66666666666", "movie2", 2014, 0.6f);
    md = new MediaMetadata("anotherOne");
    md.setId(MediaMetadata.TMDB, "101010");
    md.addExtraData("key1", "XXXXXXXXXXXnooverwriteXXXXXXXXX");
    md.addGenre(MediaGenres.ACTION);
    md.addGenre(MediaGenres.CRIME);
    s2.setMetadata(md);

    System.out.println(my);
    System.out.println(s1);
    System.out.println(s2);

    System.out.println();
    my.mergeFrom(s1);
    my.mergeFrom(s2);
    System.out.println(my);
  }

}
