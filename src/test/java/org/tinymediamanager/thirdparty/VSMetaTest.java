package org.tinymediamanager.thirdparty;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;

public class VSMetaTest extends BasicTest {

  @Test
  public void checkEmpty() {
    setTraceLogging();
    Path file = Paths.get("src/test/resources/syno_vsmeta/empty.vsmeta");
    VSMeta vsmeta = new VSMeta(file);
    vsmeta.parseFile();
    assertEqual("1", vsmeta.getMovie().getTitle());
    assertEqual("", vsmeta.getTvShowEpisode().getTitle());
  }

  @Test
  public void check101() {
    setTraceLogging();
    Path file = Paths.get("src/test/resources/syno_vsmeta/101 Dalmatiner.avi.vsmeta");
    VSMeta vsmeta = new VSMeta(file);
    vsmeta.parseFile();
    assertEqual("101 Dalmatiner", vsmeta.getMovie().getTitle());
    assertEqual("", vsmeta.getTvShowEpisode().getTitle());
  }

  @Test
  public void checkAvatar() {
    setTraceLogging();
    Path file = Paths.get("src/test/resources/syno_vsmeta/Avatar.mkv.vsmeta");
    VSMeta vsmeta = new VSMeta(file);
    vsmeta.parseFile();
    Movie m = vsmeta.getMovie();
    assertEqual("Avatar - Aufbruch nach Pandora", m.getTitle());
  }

  @Test
  public void checkBieneMaia() {
    setTraceLogging();

    Path file = Paths.get("src/test/resources/syno_vsmeta/Die Biene Maja - S01E01 - Maja wird geboren.avi.vsmeta");
    VSMeta vsmeta = new VSMeta(file);
    vsmeta.parseFile();
    TvShowEpisode ep = vsmeta.getTvShowEpisode();
    assertEqual("Maja wird geboren", ep.getTitle());
    assertEqual(1, ep.getSeason());
    assertEqual(1, ep.getEpisode());
    assertEqual("117124", ep.getTvdbId());
    System.out.println(ep);

    file = Paths.get("src/test/resources/syno_vsmeta/Die Biene Maja - S01E02 - Maja lernt Fliegen.avi.vsmeta");
    vsmeta = new VSMeta(file);
    vsmeta.parseFile();
    ep = vsmeta.getTvShowEpisode();
    assertEqual(1, ep.getSeason());
    assertEqual(2, ep.getEpisode());
    assertEqual("117125", ep.getTvdbId());
    System.out.println(ep);
  }

}
