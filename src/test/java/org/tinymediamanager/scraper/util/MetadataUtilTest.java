package org.tinymediamanager.scraper.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MetadataUtilTest {

  @Test
  public void testParseInt() {
    assertThat(MetadataUtil.parseInt("2000")).isEqualTo(2000);
    assertThat(MetadataUtil.parseInt("2.000")).isEqualTo(2000);
    assertThat(MetadataUtil.parseInt("2,000")).isEqualTo(2000);
    assertThat(MetadataUtil.parseInt("2 000")).isEqualTo(2000);
  }
}
