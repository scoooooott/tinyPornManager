package org.tinymediamanager.core.movie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.tinymediamanager.core.movie.MovieEdition.DIRECTORS_CUT;
import static org.tinymediamanager.core.movie.MovieEdition.EXTENDED_EDITION;
import static org.tinymediamanager.core.movie.MovieEdition.IMAX;
import static org.tinymediamanager.core.movie.MovieEdition.NONE;
import static org.tinymediamanager.core.movie.MovieEdition.SPECIAL_EDITION;
import static org.tinymediamanager.core.movie.MovieEdition.THEATRICAL_EDITION;
import static org.tinymediamanager.core.movie.MovieEdition.UNCUT;
import static org.tinymediamanager.core.movie.MovieEdition.UNRATED;

import org.junit.Test;

/**
 * @author Manuel Laggner
 */
public class MovieEditionTest {

  @Test
  public void testMovieEditionRegexp() {
    // DIRECTORS_CUT
    assertThat(parse("Halloween.Directors.Cut.German.AC3D.HDRip.x264-xx")).isEqualTo(DIRECTORS_CUT);
    assertThat(parse("Saw.Directors.Cut.2004.French.HDRiP.H264-xx")).isEqualTo(DIRECTORS_CUT);
    assertThat(parse("The.Grudge.Unrated.Directors.Cut.2004.AC3D.HDRip.x264")).isEqualTo(DIRECTORS_CUT);
    assertThat(parse("Straight Outta Compton Directors Cut 2015 German AC3 BDRiP XViD-abc")).isEqualTo(DIRECTORS_CUT);

    // EXTENDED
    assertThat(parse("Lord.Of.The.Rings.Extended.Edition")).isEqualTo(EXTENDED_EDITION);
    assertThat(parse("Vikings.S03E10.EXTENDED.720p.BluRay.x264-xyz")).isEqualTo(EXTENDED_EDITION);
    assertThat(parse("Taken.3.EXTENDED.2014.BRRip.XviD.AC3-xyz")).isEqualTo(EXTENDED_EDITION);
    assertThat(parse("Coyote.Ugly.UNRATED.EXTENDED.CUT.2000.German.AC3D.HDRip.x264")).isEqualTo(EXTENDED_EDITION);
    assertThat(parse("Project X EXTENDED CUT Italian AC3 BDRip XviD-xxx")).isEqualTo(EXTENDED_EDITION);

    // THEATRICAL
    assertThat(parse("The.Lord.of.the.Rings.The.Return.of.the.King.THEATRICAL.EDITION.2003.720p.BrRip.x264.mp4")).isEqualTo(THEATRICAL_EDITION);
    assertThat(parse("Movie.43.2013.American.Theatrical.Version.DVDRip.XViD.avi")).isEqualTo(THEATRICAL_EDITION);

    // UNRATED
    assertThat(parse("Get.Hard.2015.UNRATED.720p.BluRay.DTS.x264-xyz")).isEqualTo(UNRATED);
    assertThat(parse("Curse.Of.Chucky.2013.UNRATED.1080p.WEB-DL.H264-xyz")).isEqualTo(UNRATED);
    assertThat(parse("Men.Of.War.UNRATED.1994.AC3.HDRip.x264")).isEqualTo(UNRATED);
    assertThat(parse("Men Of War UNRATED 1994 AC3 HDRip x264")).isEqualTo(UNRATED);

    // UNCUT
    assertThat(parse("12 Monkeys [Uncut] [3D]")).isEqualTo(UNCUT);
    assertThat(parse("Creep.UNCUT.2004.AC3.HDTVRip.x264")).isEqualTo(UNCUT);
    assertThat(parse("Dragonball.Z.COMPLETE.Dutch.Dubbed.UNCUT.1989.ANiME.WS.DVDRiP.XviD")).isEqualTo(UNCUT);
    assertThat(parse("Rest Stop Dead Ahead 2006 UNCUT 720p BluRay H264 AAC-xxx")).isEqualTo(UNCUT);

    // IMAX
    assertThat(parse("IMAX.Sharks.2004.BDRip.XviD-xyz")).isEqualTo(IMAX);
    assertThat(parse("IMAX.Sea.Rex.GERMAN.DOKU.BDRip.XviD-xyz")).isEqualTo(IMAX);
    assertThat(parse("IMAX.Alaska.Spirit.of.the.Wild.1997.FRENCH.AC3.DOKU.DL.720p.BluRay.x264-xxx")).isEqualTo(IMAX);
    assertThat(parse("The.Hunger.Games.Catching.Fire.2013.IMAX.720p.BluRay.H264.AAC-xxx")).isEqualTo(IMAX);
    assertThat(parse("Transformers Revenge Of The Fallen 2009 IMAX 1080p BluRay x264-abc")).isEqualTo(IMAX);

    // SPECIAL_EDITION
    assertThat(parse("Blade.Runner.The.Final.Cut.1982.BluRay.1080p")).isEqualTo(SPECIAL_EDITION);

    // NORMAL
    assertThat(parse("Boomerang.1992.Incl.Directors.Commentary.DVDRip.x264-xyz")).isEqualTo(NONE);
    assertThat(parse("Unrated.The.Movie.2009.720p.BluRay.x264-xyz")).isEqualTo(NONE);
    assertThat(parse("The Lion Guard Return Of The Roar 2015 DVDRip x264-aaa")).isEqualTo(NONE);
    assertThat(parse("Spies 1928 720p BluRay x264-hhh")).isEqualTo(NONE);
    assertThat(parse("Rodeo Girl 2016 DVDRip x264-yxc")).isEqualTo(NONE);
    assertThat(parse("Climax")).isEqualTo(NONE);
  }

  private MovieEdition parse(String name) {
    return MovieEdition.getMovieEditionFromString(name);
  }
}
