/*
 * Copyright 2012 Manuel Laggner
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

package org.tinymediamanager.scraper;

import java.util.ArrayList;
import java.util.List;

/**
 * The Enum Certification.
 */
public enum Certification {

  /** US certifications. */
  G(CountryCode.US, "G", new String[] { "G" }), PG(CountryCode.US, "PG", new String[] { "PG" }), PG13(CountryCode.US, "PG-13",
      new String[] { "PG-13" }), R(CountryCode.US, "R", new String[] { "R" }), NC17(CountryCode.US, "NC-17", new String[] { "NC-17" }),

  /** DE certifications. */
  FSK0(CountryCode.DE, "FSK 0", new String[] { "FSK 0", "FSK0", "0" }), FSK6(CountryCode.DE, "FSK 6", new String[] { "FSK 6", "FSK6", "6", "ab 6" }),
  FSK12(CountryCode.DE, "FSK 12", new String[] { "FSK 12", "FSK12", "12", "ab 12" }), FSK16(CountryCode.DE, "FSK 16", new String[] { "FSK 16",
      "FSK16", "16", "ab 16" }), FSK18(CountryCode.DE, "FSK 18", new String[] { "FSK 18", "FSK18", "18", "ab 18" }),

  /** initial value. */
  NOT_RATED(CountryCode.US, "not rated", new String[] { "not rated" });

  /** The country. */
  private CountryCode country;

  /** The name. */
  private String      name;

  private String[]    possibleNotations;

  /**
   * Instantiates a new certification.
   * 
   * @param country
   *          the country
   * @param name
   *          the name
   */
  private Certification(CountryCode country, String name, String[] possibleNotations) {
    this.country = country;
    this.name = name;
    this.possibleNotations = possibleNotations;
  }

  /**
   * Gets the country.
   * 
   * @return the country
   */
  public CountryCode getCountry() {
    return this.country;
  }

  public String getName() {
    return this.name;
  }

  /**
   * Gets the certificationsfor country.
   * 
   * @param country
   *          the country
   * @return the certificationsfor country
   */
  public static List<Certification> getCertificationsforCountry(CountryCode country) {
    List<Certification> certifications = new ArrayList<Certification>();

    for (Certification cert : Certification.values()) {
      if (cert.getCountry() == country) {
        certifications.add(cert);
      }
    }

    // at last - add NOT_RATED
    if (!certifications.contains(NOT_RATED)) {
      certifications.add(NOT_RATED);
    }

    return certifications;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Enum#toString()
   */
  public String toString() {
    return this.name;
  }

  public static Certification getCertification(String country, String name) {
    CountryCode countryCode = CountryCode.getByCode(country);
    return getCertification(countryCode, name);
  }

  public static Certification findCertification(String name) {
    for (Certification cert : Certification.values()) {
      // check if the name matches
      if (cert.getName().equalsIgnoreCase(name)) {
        return cert;
      }
      // check if one of the possible notations matches
      for (String notation : cert.possibleNotations) {
        if (notation.equalsIgnoreCase(name)) {
          return cert;
        }
      }
    }
    return NOT_RATED;
  }

  public static Certification getCertification(CountryCode country, String name) {
    // try to find the certification
    for (Certification cert : Certification.getCertificationsforCountry(country)) {
      // check if the name matches
      if (cert.getName().equalsIgnoreCase(name)) {
        return cert;
      }
      // check if one of the possible notations matches
      for (String notation : cert.possibleNotations) {
        if (notation.equalsIgnoreCase(name)) {
          return cert;
        }
      }
    }
    return NOT_RATED;
  }
}
