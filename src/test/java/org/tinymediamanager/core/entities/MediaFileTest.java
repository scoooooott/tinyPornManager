package org.tinymediamanager.core.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;

import org.junit.Test;
import org.tinymediamanager.BasicTest;

public class MediaFileTest extends BasicTest {

  @Test
  public void testLanguageDetection() {
    MediaFile mf = new MediaFile(Paths.get("target/test-classes/testmovies.Subtitle/Django Unchained Special Edition.pt-br.sub"));
    mf.gatherMediaInformation();
    assertThat(mf.getSubtitles()).isNotEmpty();
    assertThat(mf.getSubtitles().get(0).getLanguage()).isEqualTo("por");
  }
}
