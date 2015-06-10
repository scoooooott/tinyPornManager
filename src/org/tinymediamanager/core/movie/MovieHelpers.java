package org.tinymediamanager.core.movie;

import org.tinymediamanager.scraper.Certification;

public class MovieHelpers {
  /**
   * Parses a given certification string for the localized country setup in setting.
   *
   * @param name
   *          certification string like "USA:R / UK:15 / Sweden:15"
   * @return the localized certification if found, else *ANY* language cert found
   */
  // <certification>USA:R / UK:15 / Sweden:15 / Spain:18 / South Korea:15 /
  // Singapore:NC-16 / Portugal:M/16 / Philippines:R-18 / Norway:15 / New
  // Zealand:M / Netherlands:16 / Malaysia:U / Malaysia:18PL / Ireland:18 /
  // Iceland:16 / Hungary:18 / Germany:16 / Finland:K-15 / Canada:18A /
  // Canada:18+ / Brazil:16 / Australia:M / Argentina:16</certification>

  public static Certification parseCertificationStringForMovieSetupCountry(String name) {
    Certification cert = Certification.NOT_RATED;
    name = name.trim();
    if (name.contains("/")) {
      // multiple countries
      String[] countries = name.split("/");
      // first try to find by setup CertLanguage
      for (String c : countries) {
        c = c.trim();
        if (c.contains(":")) {
          String[] cs = c.split(":");
          cert = Certification.getCertification(MovieModuleManager.MOVIE_SETTINGS.getCertificationCountry(), cs[1]);
          if (cert != Certification.NOT_RATED) {
            return cert;
          }
        }
        else {
          cert = Certification.getCertification(MovieModuleManager.MOVIE_SETTINGS.getCertificationCountry(), c);
          if (cert != Certification.NOT_RATED) {
            return cert;
          }
        }
      }
      // still not found localized cert? parse the name to find *ANY*
      // certificate
      for (String c : countries) {
        c = c.trim();
        if (c.contains(":")) {
          String[] cs = c.split(":");
          cert = Certification.findCertification(cs[1]);
          if (cert != Certification.NOT_RATED) {
            return cert;
          }
        }
        else {
          cert = Certification.findCertification(c);
          if (cert != Certification.NOT_RATED) {
            return cert;
          }
        }
      }
    }
    else {
      // no slash, so only one country
      if (name.contains(":")) {
        String[] cs = name.split(":");
        cert = Certification.getCertification(MovieModuleManager.MOVIE_SETTINGS.getCertificationCountry(), cs[1]);
      }
      else {
        // no country? try to find only by name
        cert = Certification.getCertification(MovieModuleManager.MOVIE_SETTINGS.getCertificationCountry(), name);
      }
    }
    // still not found localized cert? parse the name to find *ANY* certificate
    if (cert == Certification.NOT_RATED) {
      cert = Certification.findCertification(name);
    }
    return cert;
  }
}
