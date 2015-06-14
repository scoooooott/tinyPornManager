package org.tinymediamanager.core.movie;

import org.junit.Assert;
import org.junit.Test;

public class MovieMediaSourceTest {

  @Test
  public void performTest() {
    // bluray from different releases
    Assert.assertEquals(MovieMediaSource.BLURAY, MovieMediaSource.parseMediaSource("Godzilla.German.AC3.Dubbed.720p.BluRay.x264"));
    Assert.assertEquals(MovieMediaSource.BLURAY, MovieMediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.BDRip.x264"));
    Assert.assertEquals(MovieMediaSource.BLURAY, MovieMediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.BRRip.x264"));
    Assert.assertEquals(MovieMediaSource.BLURAY, MovieMediaSource.parseMediaSource("/media/movies/Night.on.Earth.1991.German.Subbed.BDRip.x264"));
    Assert.assertEquals(MovieMediaSource.BLURAY, MovieMediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.HDRIP.x264"));

    // dvd/dvdrip
    Assert.assertEquals(MovieMediaSource.DVD, MovieMediaSource.parseMediaSource("DrugStore.Cowboy.1989.German.AC3.DVDRiP.x264"));
    Assert.assertEquals(MovieMediaSource.DVD, MovieMediaSource.parseMediaSource("Hannibal 2001 AC3 German XviD DVDR"));
    Assert.assertEquals(MovieMediaSource.DVD, MovieMediaSource.parseMediaSource("/media/jets/movies/Planes.dvdrip.avi"));
    Assert.assertEquals(MovieMediaSource.DVD, MovieMediaSource.parseMediaSource("/media/jets/movies/Planes.(dvdrip).avi"));

    // TS
    Assert.assertEquals(MovieMediaSource.TS,
        MovieMediaSource.parseMediaSource("Planet.der.Affen.Revolution.2014.German.AC3D.HDTS.720p.NEW.SOURCE.x264"));
    Assert.assertEquals(MovieMediaSource.TS, MovieMediaSource.parseMediaSource("/media/jets/movies/Planes.ts.avi"));
    Assert.assertEquals(MovieMediaSource.TS, MovieMediaSource.parseMediaSource("/media/jets/movies/Planes.[ts].avi"));
    Assert.assertEquals(MovieMediaSource.TS, MovieMediaSource.parseMediaSource("/media/jets/movies/Planes-ts.avi"));

    // TV
    Assert.assertEquals(MovieMediaSource.TV, MovieMediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.TVRIP"));
    Assert.assertEquals(MovieMediaSource.TV, MovieMediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.HDTVRIP.x264"));

  }

}
