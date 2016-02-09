package org.tinymediamanager.core.movie;

import org.junit.Assert;
import org.junit.Test;
import org.tinymediamanager.core.MediaSource;

public class MovieMediaSourceTest {

  @Test
  public void performTest() {
    // bluray from different releases
    Assert.assertEquals(MediaSource.BLURAY, MediaSource.parseMediaSource("Godzilla.German.AC3.Dubbed.720p.BluRay.x264"));
    Assert.assertEquals(MediaSource.BLURAY, MediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.BDRip.x264"));
    Assert.assertEquals(MediaSource.BLURAY, MediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.BRRip.x264"));
    Assert.assertEquals(MediaSource.BLURAY, MediaSource.parseMediaSource("/media/movies/Night.on.Earth.1991.German.Subbed.BDRip.x264"));
    Assert.assertEquals(MediaSource.BLURAY, MediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.HDRIP.x264"));

    // dvd/dvdrip
    Assert.assertEquals(MediaSource.DVD, MediaSource.parseMediaSource("DrugStore.Cowboy.1989.German.AC3.DVDRiP.x264"));
    Assert.assertEquals(MediaSource.DVD, MediaSource.parseMediaSource("Hannibal 2001 AC3 German XviD DVDR"));
    Assert.assertEquals(MediaSource.DVD, MediaSource.parseMediaSource("/media/jets/movies/Planes.dvdrip.avi"));
    Assert.assertEquals(MediaSource.DVD, MediaSource.parseMediaSource("/media/jets/movies/Planes.(dvdrip).avi"));

    // TS
    Assert.assertEquals(MediaSource.TS,
        MediaSource.parseMediaSource("Planet.der.Affen.Revolution.2014.German.AC3D.HDTS.720p.NEW.SOURCE.x264"));
    Assert.assertEquals(MediaSource.TS, MediaSource.parseMediaSource("/media/jets/movies/Planes.ts.avi"));
    Assert.assertEquals(MediaSource.TS, MediaSource.parseMediaSource("/media/jets/movies/Planes.[ts].avi"));
    Assert.assertEquals(MediaSource.TS, MediaSource.parseMediaSource("/media/jets/movies/Planes-ts.avi"));

    // TV
    Assert.assertEquals(MediaSource.TV, MediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.TVRIP"));
    Assert.assertEquals(MediaSource.TV, MediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.HDTVRIP.x264"));

    // web-dl
    Assert.assertEquals(MediaSource.WEB_DL, MediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.web-dl"));
    Assert.assertEquals(MediaSource.WEB_DL, MediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.webdl"));
  }

}
