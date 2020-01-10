/*
 * Copyright 2012 - 2020 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tinymediamanager.scraper.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.tinymediamanager.core.MediaCertification;

public class MediaCertificationTest {

  // @Test
  // public void testCertificationNames() {
  // for (Certification cert : Certification.values()) {
  // System.out.println(cert.name() + "\t" + cert.toString());
  // }
  // }

  @Test
  public void testParseCertification() {
    assertThat(MediaCertification.findCertification("FSK12")).isEqualTo(MediaCertification.DE_FSK12);
    assertThat(MediaCertification.findCertification("PG")).isEqualTo(MediaCertification.US_PG);
    assertThat(MediaCertification.findCertification("NR")).isEqualTo(MediaCertification.NOT_RATED);
    assertThat(MediaCertification.findCertification("not rated")).isEqualTo(MediaCertification.NOT_RATED);
    assertThat(MediaCertification.findCertification("V.M.14")).isEqualTo(MediaCertification.IT_VM14);
    assertThat(MediaCertification.findCertification("ab 18")).isEqualTo(MediaCertification.DE_FSK18);

    assertThat(MediaCertification.findCertification("")).isEqualTo(MediaCertification.UNKNOWN);
    assertThat(MediaCertification.findCertification("asdf")).isEqualTo(MediaCertification.UNKNOWN);
  }

}
