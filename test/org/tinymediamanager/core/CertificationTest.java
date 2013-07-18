package org.tinymediamanager.core;

import org.junit.Test;
import org.tinymediamanager.scraper.Certification;

public class CertificationTest {

  @Test
  public void testCertificationNames() {
    for (Certification cert : Certification.values()) {
      System.out.println(cert.name() + "\t" + cert.toString());
    }
  }

}
