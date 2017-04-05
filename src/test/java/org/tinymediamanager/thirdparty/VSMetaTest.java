package org.tinymediamanager.thirdparty;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.movie.entities.Movie;

public class VSMetaTest extends BasicTest {

  @Test
  public void checkEmpty() {
    Path file = Paths.get("src/test/resources/empty.vsmeta");
    VSMeta vsmeta = new VSMeta();
    vsmeta.parseFile(file);
    assertEqual("1", vsmeta.getMovie().getTitle());
    assertEqual("", vsmeta.getTvShowEpisode().getTitle());
  }

  @Test
  public void checkAvatar() {
    Path file = Paths.get("src/test/resources/Avatar.mkv.vsmeta");
    VSMeta vsmeta = new VSMeta();
    vsmeta.parseFile(file);
    Movie m = vsmeta.getMovie();
    assertEqual("Avatar - Aufbruch nach Pandora", m.getTitle());
  }
}
