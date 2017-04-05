package org.tinymediamanager.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.scraper.entities.Certification;

public class CertificationTest extends BasicTest {

  @Test
  public void testCertificationNames() {
    for (Certification cert : Certification.values()) {
      System.out.println(cert.name() + "\t" + cert.toString());
    }
  }

  @Test
  public void testCertificationTemplate() {
    // assertEqual(expected, actual);
    assertThat(CertificationStyle.formatCertification(Certification.DE_FSK16, CertificationStyle.SHORT)).isEqualTo("FSK 16");
    assertThat(CertificationStyle.formatCertification(Certification.US_PG13, CertificationStyle.MEDIUM)).isEqualTo("US: PG-13");
    assertThat(CertificationStyle.formatCertification(Certification.DE_FSK16, CertificationStyle.LARGE))
        .isEqualTo("DE:FSK 16 / DE:FSK16 / DE:16 / DE:ab 16");
    assertThat(CertificationStyle.formatCertification(Certification.DE_FSK16, CertificationStyle.LARGE_FULL))
        .isEqualTo("Germany:FSK 16 / Germany:FSK16 / Germany:16 / Germany:ab 16");
    assertThat(CertificationStyle.formatCertification(Certification.DE_FSK16, CertificationStyle.TECHNICAL)).isEqualTo("DE_FSK16");
  }
}
