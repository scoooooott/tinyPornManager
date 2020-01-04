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

package org.tinymediamanager.core;

import java.util.ArrayList;
import java.util.List;

import org.tinymediamanager.scraper.entities.CountryCode;

/**
 * The enum Certification. This enum holds all (to tinyMediaManager) known certifications including some parsing information. You can parse a string
 * with {@link #findCertification(String) Certification.findCertification} or {@link #getCertification(String, String) Certification.getCertification}
 * to the corresponding enum.
 * 
 * @author Manuel Laggner / Myron Boyle
 * @since 1.0
 */
public enum MediaCertification {

  // @formatter:off
    US_G(CountryCode.US, "G", new String[] { "G", "Rated G" }),
    US_PG(CountryCode.US, "PG", new String[] { "PG", "Rated PG" }),
    US_PG13(CountryCode.US, "PG-13", new String[] { "PG-13", "Rated PG-13" }),
    US_R(CountryCode.US, "R", new String[] { "R", "Rated R" }),
    US_NC17(CountryCode.US, "NC-17", new String[] { "NC-17", "Rated NC-17" }),

    US_TVY(CountryCode.US, "TV-Y", new String[] { "TV-Y" }),
    US_TVY7(CountryCode.US, "TV-Y7", new String[] { "TV-Y7" }),
    US_TVG(CountryCode.US, "TV-G", new String[] { "TV-G" }),
    US_TVPG(CountryCode.US, "TV-PG", new String[] { "TV-PG" }),
    US_TV14(CountryCode.US, "TV-14", new String[] { "TV-14" }),
    US_TVMA(CountryCode.US, "TV-MA", new String[] { "TV-MA" }),

    DE_FSK0(CountryCode.DE, "FSK 0", new String[] { "FSK 0", "FSK-0", "FSK0", "0" }),
    DE_FSK6(CountryCode.DE, "FSK 6", new String[] { "FSK 6", "FSK-6", "FSK6", "6", "ab 6" }),
    DE_FSK12(CountryCode.DE, "FSK 12", new String[] { "FSK 12", "FSK-12", "FSK12", "12", "ab 12" }),
    DE_FSK16(CountryCode.DE, "FSK 16", new String[] { "FSK 16", "FSK-16", "FSK16", "16", "ab 16" }),
    DE_FSK18(CountryCode.DE, "FSK 18", new String[] { "FSK 18", "FSK-18", "FSK18", "18", "ab 18" }),

    GB_UC(CountryCode.GB, "UC", new String[] { "UC" }),
    GB_U(CountryCode.GB, "U", new String[] { "U" }),
    GB_PG(CountryCode.GB, "PG", new String[] { "PG" }),
    GB_12A(CountryCode.GB, "12A", new String[] { "12A" }),
    GB_12(CountryCode.GB, "12", new String[] { "12" }),
    GB_15(CountryCode.GB, "15", new String[] { "15" }),
    GB_18(CountryCode.GB, "18", new String[] { "18" }),
    GB_R18(CountryCode.GB, "R18", new String[] { "R18" }),

    RU_Y(CountryCode.RU, "Y", new String[] { "Y" }),
    RU_6(CountryCode.RU, "6+", new String[] { "6+" }),
    RU_12(CountryCode.RU, "12+", new String[] { "12+" }),
    RU_14(CountryCode.RU, "14+", new String[] { "14+" }),
    RU_16(CountryCode.RU, "16+", new String[] { "16+" }),
    RU_18(CountryCode.RU, "18+", new String[] { "18+" }),

    NL_AL(CountryCode.NL, "AL", new String[] { "AL" }),
    NL_6(CountryCode.NL, "6", new String[] { "6" }),
    NL_9(CountryCode.NL, "9", new String[] { "9" }),
    NL_12(CountryCode.NL, "12", new String[] { "12" }),
    NL_16(CountryCode.NL, "16", new String[] { "16" }),

    JP_G(CountryCode.JP, "G", new String[] { "G" }),
    JP_PG12(CountryCode.JP, "PG-12", new String[] { "PG-12" }),
    JP_R15(CountryCode.JP, "R15+", new String[] { "R15+" }),
    JP_R18(CountryCode.JP, "R18+", new String[] { "R18+" }),

    IT_T(CountryCode.IT, "T", new String[] { "T" }),
    IT_VM14(CountryCode.IT, "V.M.14", new String[] { "V.M.14", "VM14" }),
    IT_VM18(CountryCode.IT, "V.M.18", new String[] { "V.M.18", "VM18" }),

    IN_U(CountryCode.IN, "U", new String[] { "U" }),
    IN_UA(CountryCode.IN, "UA", new String[] { "UA" }),
    IN_A(CountryCode.IN, "A", new String[] { "A" }),
    IN_S(CountryCode.IN, "S", new String[] { "S" }),

    GR_K(CountryCode.GR, "K", new String[] { "K" }),
    GR_K13(CountryCode.GR, "K-13", new String[] { "K-13", "K13" }),
    GR_K17(CountryCode.GR, "K-17", new String[] { "K-17", "K17" }),
    GR_E(CountryCode.GR, "E", new String[] { "E" }),

    FR_U(CountryCode.FR, "U", new String[] { "U" }),
    FR_10(CountryCode.FR, "10", new String[] { "10" }),
    FR_12(CountryCode.FR, "12", new String[] { "12" }),
    FR_16(CountryCode.FR, "16", new String[] { "16" }),
    FR_18(CountryCode.FR, "18", new String[] { "18" }),

    CA_G(CountryCode.CA, "G", new String[] { "G" }),
    CA_PG(CountryCode.CA, "PG", new String[] { "PG" }),
    CA_14A(CountryCode.CA, "14A", new String[] { "14A" }),
    CA_18A(CountryCode.CA, "18A", new String[] { "18A" }),
    CA_R(CountryCode.CA, "R", new String[] { "R" }),
    CA_A(CountryCode.CA, "A", new String[] { "A" }),

    AU_E(CountryCode.AU, "E", new String[] { "E" }),
    AU_G(CountryCode.AU, "G", new String[] { "G" }),
    AU_PG(CountryCode.AU, "PG", new String[] { "PG" }),
    AU_M(CountryCode.AU, "M", new String[] { "M" }),
    AU_MA15(CountryCode.AU, "MA15+", new String[] { "MA15+" }),
    AU_R18(CountryCode.AU, "R18+", new String[] { "R18+" }),
    AU_X18(CountryCode.AU, "X18+", new String[] { "X18+" }),
    AU_RC(CountryCode.AU, "RC", new String[] { "RC" }),

    CZ_U(CountryCode.CZ, "U", new String[] { "U" }),
    CZ_PG(CountryCode.CZ, "PG", new String[] { "PG" }),
    CZ_12(CountryCode.CZ, "12", new String[] { "12" }),
    CZ_15(CountryCode.CZ, "15", new String[] { "15" }),
    CZ_18(CountryCode.CZ, "18", new String[] { "18" }),
    CZ_E(CountryCode.CZ, "E", new String[] { "E" }),

    DK_A(CountryCode.DK, "A", new String[] { "A" }),
    DK_7(CountryCode.DK, "7", new String[] { "7" }),
    DK_11(CountryCode.DK, "11", new String[] { "11" }),
    DK_15(CountryCode.DK, "15", new String[] { "15" }),
    DK_F(CountryCode.DK, "F", new String[] { "F" }),

    EE_PERE(CountryCode.EE, "PERE", new String[] { "PERE" }),
    EE_L(CountryCode.EE, "L", new String[] { "L" }),
    EE_MS6(CountryCode.EE, "MS-6", new String[] { "MS-6" }),
    EE_MS12(CountryCode.EE, "MS-12", new String[] { "MS-12" }),
    EE_K12(CountryCode.EE, "K-12", new String[] { "K-12" }),
    EE_K14(CountryCode.EE, "K-14", new String[] { "K-14" }),
    EE_K16(CountryCode.EE, "K-16", new String[] { "K-16" }),

    FI_S(CountryCode.FI, "S", new String[] { "S" }),
    FI_K7(CountryCode.FI, "K-7", new String[] { "K-7" }),
    FI_K12(CountryCode.FI, "K-12", new String[] { "K-12" }),
    FI_K16(CountryCode.FI, "K-16", new String[] { "K-16" }),
    FI_K18(CountryCode.FI, "K-18", new String[] { "K-18" }),
    FI_KE(CountryCode.FI, "K-E", new String[] { "K-E" }),

    HU_KN(CountryCode.HU, "KN", new String[] { "KN" }),
    HU_6(CountryCode.HU, "6", new String[] { "6" }),
    HU_12(CountryCode.HU, "12", new String[] { "12" }),
    HU_16(CountryCode.HU, "16", new String[] { "16" }),
    HU_18(CountryCode.HU, "18", new String[] { "18" }),
    HU_X(CountryCode.HU, "X", new String[] { "X" }),

    IS_L(CountryCode.IS, "L", new String[] { "L" }),
    IS_7(CountryCode.IS, "7", new String[] { "7" }),
    IS_10(CountryCode.IS, "10", new String[] { "10" }),
    IS_12(CountryCode.IS, "12", new String[] { "12" }),
    IS_14(CountryCode.IS, "14", new String[] { "14" }),
    IS_16(CountryCode.IS, "16", new String[] { "16" }),
    IS_18(CountryCode.IS, "18", new String[] { "18" }),

    IE_G(CountryCode.IE, "G", new String[] { "G" }),
    IE_PG(CountryCode.IE, "PG", new String[] { "PG" }),
    IE_12A(CountryCode.IE, "12A", new String[] { "12A" }),
    IE_15A(CountryCode.IE, "15A", new String[] { "15A" }),
    IE_16(CountryCode.IE, "16", new String[] { "16" }),
    IE_18(CountryCode.IE, "18", new String[] { "18" }),

    NZ_G(CountryCode.NZ, "G", new String[] { "G" }),
    NZ_PG(CountryCode.NZ, "PG", new String[] { "PG" }),
    NZ_M(CountryCode.NZ, "M", new String[] { "M" }),
    NZ_R13(CountryCode.NZ, "R13", new String[] { "R13" }),
    NZ_R16(CountryCode.NZ, "R16", new String[] { "R16" }),
    NZ_R18(CountryCode.NZ, "R18", new String[] { "R18" }),
    NZ_R15(CountryCode.NZ, "R15", new String[] { "R15" }),
    NZ_RP13(CountryCode.NZ, "RP13", new String[] { "RP13" }),
    NZ_RP16(CountryCode.NZ, "RP16", new String[] { "RP16" }),
    NZ_R(CountryCode.NZ, "R", new String[] { "R" }),

    NO_A(CountryCode.NO, "A", new String[] { "A" }),
    NO_6(CountryCode.NO, "6", new String[] { "6" }),
    NO_7(CountryCode.NO, "7", new String[] { "7" }),
    NO_9(CountryCode.NO, "9", new String[] { "9" }),
    NO_11(CountryCode.NO, "11", new String[] { "11" }),
    NO_12(CountryCode.NO, "12", new String[] { "12" }),
    NO_15(CountryCode.NO, "15", new String[] { "15" }),
    NO_18(CountryCode.NO, "18", new String[] { "18" }),

    PL_AL(CountryCode.PL, "AL", new String[] { "AL" }),
    PL_7(CountryCode.PL, "7", new String[] { "7" }),
    PL_12(CountryCode.PL, "12", new String[] { "12" }),
    PL_15(CountryCode.PL, "15", new String[] { "15" }),
    PL_AP(CountryCode.PL, "AP", new String[] { "AP" }),
    PL_21(CountryCode.PL, "21", new String[] { "21" }),

    RO_AP(CountryCode.RO, "A.P.", new String[] { "A.P.", "AP" }),
    RO_12(CountryCode.RO, "12", new String[] { "12" }),
    RO_15(CountryCode.RO, "15", new String[] { "15" }),
    RO_18(CountryCode.RO, "18", new String[] { "18" }),
    RO_18X(CountryCode.RO, "18*", new String[] { "18*" }),

    ES_APTA(CountryCode.ES, "APTA", new String[] { "APTA" }),
    ES_ER(CountryCode.ES, "ER", new String[] { "ER" }),
    ES_7(CountryCode.ES, "7", new String[] { "7" }),
    ES_12(CountryCode.ES, "12", new String[] { "12" }),
    ES_16(CountryCode.ES, "16", new String[] { "16" }),
    ES_18(CountryCode.ES, "18", new String[] { "18" }),
    ES_PX(CountryCode.ES, "PX", new String[] { "PX" }),

    SE_BTL(CountryCode.SE, "BTL", new String[] { "BTL" }),
    SE_7(CountryCode.SE, "7", new String[] { "7" }),
    SE_11(CountryCode.SE, "11", new String[] { "11" }),
    SE_15(CountryCode.SE, "15", new String[] { "15" }),

    CH_0(CountryCode.CH, "0", new String[] { "0" }),
    CH_7(CountryCode.CH, "7", new String[] { "7" }),
    CH_10(CountryCode.CH, "10", new String[] { "10" }),
    CH_12(CountryCode.CH, "12", new String[] { "12" }),
    CH_14(CountryCode.CH, "14", new String[] { "14" }),
    CH_16(CountryCode.CH, "16", new String[] { "16" }),
    CH_18(CountryCode.CH, "18", new String[] { "18" }),

    TH_P(CountryCode.TH, "P", new String[] { "P" }),
    TH_G(CountryCode.TH, "G", new String[] { "G" }),
    TH_13(CountryCode.TH, "13+", new String[] { "13+" }),
    TH_15(CountryCode.TH, "15+", new String[] { "15+" }),
    TH_18(CountryCode.TH, "18+", new String[] { "18+" }),
    TH_20(CountryCode.TH, "20+", new String[] { "20+" }),
    TH_Banned(CountryCode.TH, "Banned", new String[] { "Banned" }), //NOSONAR

    PT_0(CountryCode.PT, "Para todos os públicos", new String[] { "Para todos os públicos" }),
    PT_M3(CountryCode.PT, "M/3", new String[] { "M/3", "M_3" }),
    PT_M6(CountryCode.PT, "M/6", new String[] { "M/6", "M_6" }),
    PT_M12(CountryCode.PT, "M/12", new String[] { "M/12", "M_12" }),
    PT_M14(CountryCode.PT, "M/14", new String[] { "M/14", "M_14" }),
    PT_M16(CountryCode.PT, "M/16", new String[] { "M/16", "M_16" }),
    PT_M18(CountryCode.PT, "M/18", new String[] { "M/18", "M_18" }),
    PT_P(CountryCode.PT, "P", new String[] { "P" }),

    NOT_RATED(CountryCode.US, "not rated", new String[] { "not rated", "NR" }),
    UNKNOWN(null, "unknown", new String[] { "unknown" });
  // @formatter:on

  private CountryCode country;
  private String      name;
  private String[]    possibleNotations;

  /**
   * Instantiates a new certification.
   * 
   * @param country
   *          the country
   * @param name
   *          the name
   * @param possibleNotations
   *          the possible notations
   */
  MediaCertification(CountryCode country, String name, String[] possibleNotations) {
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
    return country;
  }

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the possible notations.
   * 
   * @return the possible notations
   */
  public String[] getPossibleNotations() {
    return possibleNotations;
  }

  /**
   * Get the certifications for the given country.
   * 
   * @param country
   *          the country
   * @return the certifications for the given country
   */
  public static List<MediaCertification> getCertificationsforCountry(CountryCode country) {
    List<MediaCertification> certifications = new ArrayList<>();

    for (MediaCertification cert : MediaCertification.values()) {
      if (cert.getCountry() == country) {
        certifications.add(cert);
      }
    }

    // at last - add unknown
    if (!certifications.contains(UNKNOWN)) {
      certifications.add(UNKNOWN);
    }

    return certifications;
  }

  @Override
  public String toString() {
    return name;
  }

  /**
   * Gets the certification.
   * 
   * @param country
   *          the country
   * @param name
   *          the name
   * @return the certification
   */
  public static MediaCertification getCertification(String country, String name) {
    CountryCode countryCode = CountryCode.getByCode(country);
    return getCertification(countryCode, name);
  }

  /**
   * generates a certification string from certs list, country alpha2.
   * 
   * @param certs
   *          list of certifications
   * @return certification string like "US:R / UK:15 / SW:15"
   */
  public static String generateCertificationStringFromList(ArrayList<MediaCertification> certs) {
    if (certs == null || certs.isEmpty()) {
      return "";
    }
    String certstring = "";
    for (MediaCertification c : certs) {
      if (c.getCountry() == CountryCode.GB) {
        certstring += " / UK:" + c.getName();
      }
      else {
        certstring += " / " + c.getCountry().getAlpha2() + ":" + c.getName();
        certstring += " / " + c.getCountry().getName() + ":" + c.getName();
      }
    }
    return certstring.substring(3).trim(); // strip off first slash
  }

  /**
   * generates a certification string for country alpha2 (including all different variants); so skins parsing with substr will find them :)<br>
   * eg: "DE:FSK 16 / DE:FSK16 / DE:16 / DE:ab 16".
   * 
   * @param cert
   *          the cert
   * @return certification string like "US:R / UK:15 / SW:15"
   */
  public static String generateCertificationStringWithAlternateNames(MediaCertification cert) {
    return generateCertificationStringWithAlternateNames(cert, false);
  }

  /**
   * generates a certification string for country alpha2 or country name (including all different variants); so skins parsing with substr will find
   * them :)<br>
   * eg: "DE:FSK 16 / DE:FSK16 / DE:16 / DE:ab 16". eg: "Germany:FSK 16 / Germany:FSK16 / Germany:16 / Germany:ab 16".
   *
   * @param cert
   *          the cert
   * @param withCountryName
   *          true/false
   * @return certification string like "US:R / UK:15 / SW:15"
   */
  public static String generateCertificationStringWithAlternateNames(MediaCertification cert, boolean withCountryName) {
    if (cert == null) {
      return "";
    }
    if (cert == UNKNOWN) {
      return cert.name;
    }
    if (cert == NOT_RATED) {
      return "NR";
    }
    String certstring = "";
    for (String notation : cert.getPossibleNotations()) {
      if (withCountryName) {
        certstring += " / " + cert.getCountry().getName() + ":" + notation;
      }
      else {
        if (cert.getCountry() == CountryCode.GB) {
          certstring += " / UK:" + notation;
        }
        else {
          certstring += " / " + cert.getCountry().getAlpha2() + ":" + notation;
        }
      }
    }
    return certstring.substring(3).trim(); // strip off first slash
  }

  /**
   * Find certification.
   * 
   * @param name
   *          the name
   * @return the certification
   */
  public static MediaCertification findCertification(String name) {
    for (MediaCertification cert : MediaCertification.values()) {
      // check if the ENUM name matches
      if (cert.name().equalsIgnoreCase(name)) {
        return cert;
      }
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
    return UNKNOWN;
  }

  /**
   * Gets the certification.
   * 
   * @param country
   *          the country
   * @param name
   *          the name
   * @return the certification
   */
  public static MediaCertification getCertification(CountryCode country, String name) {
    // try to find the certification
    for (MediaCertification cert : MediaCertification.getCertificationsforCountry(country)) {
      // check if the ENUM name matches
      if (cert.name().equalsIgnoreCase(name)) {
        return cert;
      }
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
    return UNKNOWN;
  }

  /**
   * gets the MPAA String from any US (!) movie/TV show certification<br>
   */
  public static String getMPAAString(MediaCertification cert) {
    // http://en.wikipedia.org/wiki/Motion_picture_rating_system#Comparison
    switch (cert) {
      // movies
      case US_G:
        return "Rated G";
      case US_PG:
        return "Rated PG";
      case US_PG13:
        return "Rated PG-13";
      case US_R:
        return "Rated R";
      case US_NC17:
        return "Rated NC-17";
      case NOT_RATED:
        return "NR";

      // TV shows
      case US_TVY7:
      case US_TV14:
      case US_TVPG:
      case US_TVMA:
      case US_TVG:
      case US_TVY:
        return cert.getName();

      default:
        return "";
    }
  }
}
