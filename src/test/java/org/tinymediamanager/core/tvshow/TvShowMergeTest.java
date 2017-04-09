package org.tinymediamanager.core.tvshow;

import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;

public class TvShowMergeTest {

  private static final MediaFile dmf = new MediaFile(Paths.get("/path/to", "video.avi"));

  @Test
  public void testEPmerge() {
    TvShow show = new TvShow();

    TvShowEpisode e1 = new TvShowEpisode();
    e1.setAiredSeason(1);
    e1.setAiredEpisode(2);
    e1.setTvShow(show);

    TvShowEpisode e2 = new TvShowEpisode();
    e2.setTitle("title2");
    e2.setDvdSeason(1);
    e2.setDvdEpisode(2);
    e2.setTvShow(show);

    e1.merge(e2);
    System.out.println(e1);
    Assert.assertEquals(2, e1.getDvdEpisode());
    Assert.assertEquals("title2", e1.getTitle());
  }

  @Test
  public void testShowMerge() {

    // ---------------------------------------
    TvShow show1 = new TvShow();
    show1.setTitle("show1");
    show1.setYear(2009);

    TvShowEpisode ep1 = new TvShowEpisode();
    ep1.setAiredSeason(1);
    ep1.setAiredEpisode(1);
    ep1.setTvShow(show1);
    show1.addEpisode(ep1);

    TvShowEpisode ep2 = new TvShowEpisode();
    ep2.setAiredSeason(1);
    ep2.setAiredEpisode(2);
    ep2.setTvShow(show1);
    show1.addEpisode(ep2);

    // ---------------------------------------
    TvShow show2 = new TvShow();
    show2.setTitle("show2");
    show2.setYear(2009);

    TvShowEpisode ep3 = new TvShowEpisode();
    ep3.setAiredSeason(1);
    ep3.setAiredEpisode(2);
    ep3.setDvdSeason(1); // <-- should be merged
    ep3.setDvdEpisode(2); // <-- should be merged
    ep3.setTvShow(show2);
    show2.addEpisode(ep3);

    TvShowEpisode ep4 = new TvShowEpisode();
    ep4.setAiredSeason(1);
    ep4.setAiredEpisode(3);
    ep4.setTvShow(show2);
    show2.addEpisode(ep4);// <-- should be added
    // ---------------------------------------

    show1.merge(show2);
    System.out.println(show1);

    Assert.assertEquals(2, show1.getEpisode(1, 2).getDvdEpisode());
    Assert.assertEquals(3, show1.getEpisodeCount());
  }

}
