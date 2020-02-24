package org.tinymediamanager.core.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.MediaFileHelper;
import org.tinymediamanager.core.MediaFileType;

public class MediaFileTest extends BasicTest {

  @Test
  public void testLanguageDetection() {
    MediaFile mf = new MediaFile(Paths.get("target/test-classes/testmovies.Subtitle/Django Unchained Special Edition.pt-br.sub"));
    mf.gatherMediaInformation();
    assertThat(mf.getSubtitles()).isNotEmpty();
    assertThat(mf.getSubtitles().get(0).getLanguage()).isEqualTo("por");
  }

  @Test
  public void mediaFileTypeNoTrailer() {
    Path filename = Paths.get("South Park - S00E00 - The New Terrance and Phillip Movie Trailer.avi");
    MediaFileType mft = MediaFileHelper.parseMediaFileType(filename);
    assertEqual(MediaFileType.VIDEO, mft);
  }
}
