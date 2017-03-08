package org.tinymediamanager.thirdparty;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.tinymediamanager.BasicTest;

public class VSMetaTest extends BasicTest {

  @Test
  public void parseVSMeta() throws IOException {
    Path file = Paths.get("src/test/resources/empty.vsmeta");
    VSMeta vsmeta = new VSMeta();
    vsmeta.parseFile(file);
    System.out.println(vsmeta.getMovie());
    System.out.println(vsmeta.getTvShowEpisode());
  }
}
