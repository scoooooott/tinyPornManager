/*
 * Copyright 2012 - 2018 Manuel Laggner
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

public class CertificationTest {

  @Test
  public void testCertificationNames() {
    for (Certification cert : Certification.values()) {
      System.out.println(cert.name() + "\t" + cert.toString());
    }
  }

  @Test
  public void testParseCertification() {
    assertThat(Certification.findCertification("FSK12")).isEqualTo(Certification.DE_FSK12);
    assertThat(Certification.findCertification("PG")).isEqualTo(Certification.US_PG);
    assertThat(Certification.findCertification("NR")).isEqualTo(Certification.NOT_RATED);
    assertThat(Certification.findCertification("not rated")).isEqualTo(Certification.NOT_RATED);
    assertThat(Certification.findCertification("V.M.14")).isEqualTo(Certification.IT_VM14);
    assertThat(Certification.findCertification("ab 18")).isEqualTo(Certification.DE_FSK18);

    assertThat(Certification.findCertification("")).isEqualTo(Certification.UNKNOWN);
    assertThat(Certification.findCertification("asdf")).isEqualTo(Certification.UNKNOWN);
  }

}
