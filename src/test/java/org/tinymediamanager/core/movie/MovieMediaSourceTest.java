package org.tinymediamanager.core.movie;

import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.MediaSource;

public class MovieMediaSourceTest extends BasicTest {

  @Test
  public void performTest() {
    // bluray from different releases
    assertEqual(MediaSource.BLURAY, MediaSource.parseMediaSource("Godzilla.German.AC3.Dubbed.720p.BluRay.x264"));
    assertEqual(MediaSource.BLURAY, MediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.BDRip.x264"));
    assertEqual(MediaSource.BLURAY, MediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.BRRip.x264"));
    assertEqual(MediaSource.BLURAY, MediaSource.parseMediaSource("/media/movies/Night.on.Earth.1991.German.Subbed.BDRip.x264"));

    // hdrip
    assertEqual(MediaSource.HDRIP, MediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.HDRIP.x264"));
    assertEqual(MediaSource.HDRIP, MediaSource.parseMediaSource("Arsenal.2017.HDRip.XviD.AC3.avi"));

    // dvd/dvdrip
    assertEqual(MediaSource.DVD, MediaSource.parseMediaSource("DrugStore.Cowboy.1989.German.AC3.DVDRiP.x264"));
    assertEqual(MediaSource.DVD, MediaSource.parseMediaSource("Hannibal 2001 AC3 German XviD DVDR"));
    assertEqual(MediaSource.DVD, MediaSource.parseMediaSource("/media/jets/movies/Planes.dvdrip.avi"));
    assertEqual(MediaSource.DVD, MediaSource.parseMediaSource("/media/jets/movies/Planes.(dvdrip).avi"));

    // TS
    assertEqual(MediaSource.TS, MediaSource.parseMediaSource("Planet.der.Affen.Revolution.2014.German.AC3D.HDTS.720p.NEW.SOURCE.x264"));
    assertEqual(MediaSource.TS, MediaSource.parseMediaSource("/media/jets/movies/Planes.ts.avi"));
    assertEqual(MediaSource.TS, MediaSource.parseMediaSource("/media/jets/movies/Planes.[ts].avi"));
    assertEqual(MediaSource.TS, MediaSource.parseMediaSource("/media/jets/movies/Planes-ts.avi"));

    // TV
    assertEqual(MediaSource.TV, MediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.TVRIP"));
    assertEqual(MediaSource.TV, MediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.HDTVRIP.x264"));

    // web-dl
    assertEqual(MediaSource.WEB_DL, MediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.web-dl"));
    assertEqual(MediaSource.WEB_DL, MediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.webdl"));
    assertEqual(MediaSource.WEB_DL, MediaSource.parseMediaSource("/media/movies/Tsunami.webdl.avi"));

    // internet stream
    assertEqual(MediaSource.STREAM, MediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.strm"));
    assertEqual(MediaSource.TV, MediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.TVRIP.strm"));

    assertEqual(MediaSource.STREAM, MediaSource.parseMediaSource("some/path/Night.on.Earth.1991.German.Subbed.strm"));

    // DVDSCR
    assertEqual(MediaSource.DVDSCR, MediaSource.parseMediaSource("/media/jets/movies/Planes.DVDSCR.xxx.avi"));
    assertEqual(MediaSource.DVDSCR, MediaSource.parseMediaSource("/media/jets/movies/Planes.dvdscr.avi"));
  }

}
