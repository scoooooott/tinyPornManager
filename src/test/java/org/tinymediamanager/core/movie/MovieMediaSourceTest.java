package org.tinymediamanager.core.movie;

import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.MediaSource;

public class MovieMediaSourceTest extends BasicTest {

  @Test
  public void performTest() {
    // uhd blu rays
    assertEqual(MediaSource.UHD_BLURAY, MediaSource.parseMediaSource("Godzilla.German.AC3.Dubbed.720p.UHD.BluRay.x264"));
    assertEqual(MediaSource.UHD_BLURAY, MediaSource.parseMediaSource("Godzilla.German.AC3.Dubbed.720p.UHDBluRay.x264"));
    assertEqual(MediaSource.UHD_BLURAY, MediaSource.parseMediaSource("/media/jets/movies/Planes 2160p 4K UltraHD BluRay.x265"));
    assertEqual(MediaSource.UHD_BLURAY, MediaSource.parseMediaSource("Arsenal 2017 UHD-Blu-ray.avi"));

    // bluray from different releases
    assertEqual(MediaSource.BLURAY, MediaSource.parseMediaSource("Godzilla.German.AC3.Dubbed.720p.BluRay.x264"));
    assertEqual(MediaSource.BLURAY, MediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.BDRip.x264"));
    assertEqual(MediaSource.BLURAY, MediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.BRRip.x264"));
    assertEqual(MediaSource.BLURAY, MediaSource.parseMediaSource("Night.on.Earth.UHD.1991.German.Subbed.BRRip.x264")); // should not be uhd blu ray!
    assertEqual(MediaSource.BLURAY, MediaSource.parseMediaSource("/media/movies/Night.on.Earth.1991.German.Subbed.BDRip.x264"));

    // hdrip
    assertEqual(MediaSource.HDRIP, MediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.HDRIP.x264"));
    assertEqual(MediaSource.HDRIP, MediaSource.parseMediaSource("Arsenal.2017.HDRip.XviD.AC3.avi"));

    // dvd/dvdrip
    assertEqual(MediaSource.DVD, MediaSource.parseMediaSource("DrugStore.Cowboy.1989.German.AC3.DVDRiP.x264"));
    assertEqual(MediaSource.DVD, MediaSource.parseMediaSource("Hannibal 2001 AC3 German XviD DVDR"));
    assertEqual(MediaSource.DVD, MediaSource.parseMediaSource("/media/jets/movies/Planes.dvdrip.avi"));
    assertEqual(MediaSource.DVD, MediaSource.parseMediaSource("/media/jets/movies/Planes.(dvdrip).avi"));
    assertEqual(MediaSource.DVD, MediaSource.parseMediaSource("Batman.&.Robin[1997]DvDrip-aXXo.avi"));

    // VHS
    assertEqual(MediaSource.VHS, MediaSource.parseMediaSource("Last.Man.Standing.1987.VHS.x264"));
    assertEqual(MediaSource.VHS, MediaSource.parseMediaSource("/media/jets/movies/Twin.Dragon.Encounter.1986.VHSRip.XViD"));


    // TS
    assertEqual(MediaSource.TS, MediaSource.parseMediaSource("Planet.der.Affen.Revolution.2014.German.AC3D.HDTS.720p.NEW.SOURCE.x264"));
    assertEqual(MediaSource.TS, MediaSource.parseMediaSource("/media/jets/movies/Planes.ts.avi"));
    assertEqual(MediaSource.TS, MediaSource.parseMediaSource("/media/jets/movies/Planes.[ts].avi"));
    assertEqual(MediaSource.TS, MediaSource.parseMediaSource("/media/jets/movies/Planes-ts.avi"));

    // TV
    assertEqual(MediaSource.TV, MediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.TVRIP"));
    assertEqual(MediaSource.TV, MediaSource.parseMediaSource("Night.on.Earth.1991.German.Subbed.HDTVRIP.x264"));
    assertEqual(MediaSource.TV, MediaSource.parseMediaSource("Austiros.katallilo.2008.DTB.x264.GrLTv"));
    assertEqual(MediaSource.TV, MediaSource.parseMediaSource("John.Doe.S01E13.PDTV.XviD.Internal-SFM"));
    assertEqual(MediaSource.TV, MediaSource.parseMediaSource("justified.s05e09.hdtv.x264-killers"));
    assertEqual(MediaSource.TV, MediaSource.parseMediaSource("Alpha.Kokkinos.kyklos.S01E27.O.ippoths.DSR.GrLTv.avi"));
    assertEqual(MediaSource.TV, MediaSource.parseMediaSource("Fluss.der.Maya.DOKU.dTV.avi"));
    assertEqual(MediaSource.TV, MediaSource.parseMediaSource("Luckless Pedestrians - 1991 DtT.avi"));
    assertEqual(MediaSource.TV, MediaSource.parseMediaSource("Brave: Neinfricata | Sambata | 21:00 la dtTv.avi"));
    assertEqual(MediaSource.TV, MediaSource.parseMediaSource("Bez.Tajemnic.2013.S03E29.PL.DVBRip.XviD-TROD4T"));
    assertEqual(MediaSource.TV, MediaSource.parseMediaSource("Preacher.S01E01.720p.HDTV.x265.HEVC-MRN.mkv"));


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
