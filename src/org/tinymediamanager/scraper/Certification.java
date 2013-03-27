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

import org.tinymediamanager.Globals;

/**
 * The Enum Certification.
 * 
 * @author Manuel Laggner / Myron Boyle
 */
public enum Certification {

  /** US certifications. */
  US_G(CountryCode.US, "G", new String[] { "G", "Rated G" }),
  /** The us pg. */
  US_PG(CountryCode.US, "PG", new String[] { "PG", "Rated PG" }),
  /** The U s_ p g13. */
  US_PG13(CountryCode.US, "PG-13", new String[] { "PG-13", "Rated PG-13" }),
  /** The us r. */
  US_R(CountryCode.US, "R", new String[] { "R", "Rated R" }),
  /** The U s_ n c17. */
  US_NC17(CountryCode.US, "NC-17", new String[] { "NC-17", "Rated NC-17" }),

  /** DE certifications. */
  DE_FSK0(CountryCode.DE, "FSK 0", new String[] { "FSK 0", "FSK0", "0" }),
  /** The D e_ fs k6. */
  DE_FSK6(CountryCode.DE, "FSK 6", new String[] { "FSK 6", "FSK6", "6", "ab 6" }),
  /** The D e_ fs k12. */
  DE_FSK12(CountryCode.DE, "FSK 12", new String[] { "FSK 12", "FSK12", "12", "ab 12" }),
  /** The D e_ fs k16. */
  DE_FSK16(CountryCode.DE, "FSK 16", new String[] { "FSK 16", "FSK16", "16", "ab 16" }),
  /** The D e_ fs k18. */
  DE_FSK18(CountryCode.DE, "FSK 18", new String[] { "FSK 18", "FSK18", "18", "ab 18" }),

  /** UK/GB certifications. */
  GB_UC(CountryCode.GB, "UC", new String[] { "UC" }),
  /** The gb u. */
  GB_U(CountryCode.GB, "U", new String[] { "U" }),
  /** The gb pg. */
  GB_PG(CountryCode.GB, "PG", new String[] { "PG" }),
  /** The G b_12 a. */
  GB_12A(CountryCode.GB, "12A", new String[] { "12A" }),
  /** The G b_12. */
  GB_12(CountryCode.GB, "12", new String[] { "12" }),
  /** The G b_15. */
  GB_15(CountryCode.GB, "15", new String[] { "15" }),
  /** The G b_18. */
  GB_18(CountryCode.GB, "18", new String[] { "18" }),
  /** The G b_ r18. */
  GB_R18(CountryCode.GB, "R18", new String[] { "R18" }),

  /** RU certifications. */
  RU_Y(CountryCode.RU, "Y", new String[] { "Y" }),
  /** The R u_6. */
  RU_6(CountryCode.RU, "6+", new String[] { "6+" }),
  /** The R u_12. */
  RU_12(CountryCode.RU, "12+", new String[] { "12+" }),
  /** The R u_14. */
  RU_14(CountryCode.RU, "14+", new String[] { "14+" }),
  /** The R u_16. */
  RU_16(CountryCode.RU, "16+", new String[] { "16+" }),
  /** The R u_18. */
  RU_18(CountryCode.RU, "18+", new String[] { "18+" }),

  /** NL certifications. */
  NL_AL(CountryCode.NL, "AL", new String[] { "AL" }),
  /** The N l_6. */
  NL_6(CountryCode.NL, "6", new String[] { "6" }),

  /** The N l_9. */
  NL_9(CountryCode.NL, "9", new String[] { "9" }),
  /** The N l_12. */
  NL_12(CountryCode.NL, "12", new String[] { "12" }),
  /** The N l_16. */
  NL_16(CountryCode.NL, "16", new String[] { "16" }),

  /** JP certifications. */
  JP_G(CountryCode.JP, "G", new String[] { "G" }),
  /** The J p_ p g12. */
  JP_PG12(CountryCode.JP, "PG-12", new String[] { "PG-12" }),
  /** The J p_ r15. */
  JP_R15(CountryCode.JP, "R15+", new String[] { "R15+" }),
  /** The J p_ r18. */
  JP_R18(CountryCode.JP, "R18+", new String[] { "R18+" }),

  /** IT certifications. */
  IT_T(CountryCode.IT, "T", new String[] { "T" }),
  /** The I t_ v m14. */
  IT_VM14(CountryCode.IT, "V.M.14", new String[] { "V.M.14", "VM14" }),
  /** The I t_ v m18. */
  IT_VM18(CountryCode.IT, "V.M.18", new String[] { "V.M.18", "VM18" }),

  /** IN certifications. */
  IN_U(CountryCode.IN, "U", new String[] { "U" }),
  /** The in ua. */
  IN_UA(CountryCode.IN, "UA", new String[] { "UA" }),

  /** The in a. */
  IN_A(CountryCode.IN, "A", new String[] { "A" }),
  /** The in s. */
  IN_S(CountryCode.IN, "S", new String[] { "S" }),

  /** GR certifications. */
  GR_K(CountryCode.GR, "K", new String[] { "K" }),
  /** The G r_ k13. */
  GR_K13(CountryCode.GR, "K-13", new String[] { "K-13", "K13" }),
  /** The G r_ k17. */
  GR_K17(CountryCode.GR, "K-17", new String[] { "K-17", "K17" }),
  /** The gr e. */
  GR_E(CountryCode.GR, "E", new String[] { "E" }),

  /** FR certifications. */
  FR_U(CountryCode.FR, "U", new String[] { "U" }),
  /** The F r_10. */
  FR_10(CountryCode.FR, "10", new String[] { "10" }),
  /** The F r_12. */
  FR_12(CountryCode.FR, "12", new String[] { "12" }),
  /** The F r_16. */
  FR_16(CountryCode.FR, "16", new String[] { "16" }),
  /** The F r_18. */
  FR_18(CountryCode.FR, "18", new String[] { "18" }),

  /** CA certifications. */
  CA_G(CountryCode.CA, "G", new String[] { "G" }),
  /** The ca pg. */
  CA_PG(CountryCode.CA, "PG", new String[] { "PG" }),
  /** The C a_14 a. */
  CA_14A(CountryCode.CA, "14A", new String[] { "14A" }),
  /** The C a_18 a. */
  CA_18A(CountryCode.CA, "18A", new String[] { "18A" }),
  /** The ca r. */
  CA_R(CountryCode.CA, "R", new String[] { "R" }),
  /** The ca a. */
  CA_A(CountryCode.CA, "A", new String[] { "A" }),

  /** AU certifications. */
  AU_E(CountryCode.AU, "E", new String[] { "E" }),
  /** The au g. */
  AU_G(CountryCode.AU, "G", new String[] { "G" }),

  /** The au pg. */
  AU_PG(CountryCode.AU, "PG", new String[] { "PG" }),
  /** The au m. */
  AU_M(CountryCode.AU, "M", new String[] { "M" }),
  /** The A u_ m a15. */
  AU_MA15(CountryCode.AU, "MA15+", new String[] { "MA15+" }),
  /** The A u_ r18. */
  AU_R18(CountryCode.AU, "R18+", new String[] { "R18+" }),
  /** The A u_ x18. */
  AU_X18(CountryCode.AU, "X18+", new String[] { "X18+" }),

  /** The au rc. */
  AU_RC(CountryCode.AU, "RC", new String[] { "RC" }),

  /** CZ certifications. */
  CZ_U(CountryCode.CZ, "U", new String[] { "U" }),
  /** The cz pg. */
  CZ_PG(CountryCode.CZ, "PG", new String[] { "PG" }),
  /** The C z_12. */
  CZ_12(CountryCode.CZ, "12", new String[] { "12" }),
  /** The C z_15. */
  CZ_15(CountryCode.CZ, "15", new String[] { "15" }),
  /** The C z_18. */
  CZ_18(CountryCode.CZ, "18", new String[] { "18" }),
  /** The cz e. */
  CZ_E(CountryCode.CZ, "E", new String[] { "E" }),

  /** DK certifications. */
  DK_A(CountryCode.DK, "A", new String[] { "A" }),
  /** The D k_7. */
  DK_7(CountryCode.DK, "7", new String[] { "7" }),

  /** The D k_11. */
  DK_11(CountryCode.DK, "11", new String[] { "11" }),
  /** The D k_15. */
  DK_15(CountryCode.DK, "15", new String[] { "15" }),
  /** The dk f. */
  DK_F(CountryCode.DK, "F", new String[] { "F" }),

  /** EE certifications. */
  EE_PERE(CountryCode.EE, "PERE", new String[] { "PERE" }),
  /** The ee l. */
  EE_L(CountryCode.EE, "L", new String[] { "L" }),
  /** The E e_ m s6. */
  EE_MS6(CountryCode.EE, "MS-6", new String[] { "MS-6" }),
  /** The E e_ m s12. */
  EE_MS12(CountryCode.EE, "MS-12", new String[] { "MS-12" }),
  /** The E e_ k12. */
  EE_K12(CountryCode.EE, "K-12", new String[] { "K-12" }),

  /** The E e_ k14. */
  EE_K14(CountryCode.EE, "K-14", new String[] { "K-14" }),
  /** The E e_ k16. */
  EE_K16(CountryCode.EE, "K-16", new String[] { "K-16" }),

  /** FI certifications. */
  FI_S(CountryCode.FI, "S", new String[] { "S" }),
  /** The F i_ k7. */
  FI_K7(CountryCode.FI, "K-7", new String[] { "K-7" }),
  /** The F i_ k12. */
  FI_K12(CountryCode.FI, "K-12", new String[] { "K-12" }),
  /** The F i_ k16. */
  FI_K16(CountryCode.FI, "K-16", new String[] { "K-16" }),
  /** The F i_ k18. */
  FI_K18(CountryCode.FI, "K-18", new String[] { "K-18" }),

  /** The fi ke. */
  FI_KE(CountryCode.FI, "K-E", new String[] { "K-E" }),

  /** HU certifications. */
  HU_KN(CountryCode.HU, "KN", new String[] { "KN" }),
  /** The H u_6. */
  HU_6(CountryCode.HU, "6", new String[] { "6" }),
  /** The H u_12. */
  HU_12(CountryCode.HU, "12", new String[] { "12" }),
  /** The H u_16. */
  HU_16(CountryCode.HU, "16", new String[] { "16" }),
  /** The H u_18. */
  HU_18(CountryCode.HU, "18", new String[] { "18" }),
  /** The hu x. */
  HU_X(CountryCode.HU, "X", new String[] { "X" }),

  /** IS certifications. */
  IS_L(CountryCode.IS, "L", new String[] { "L" }),
  /** The I s_7. */
  IS_7(CountryCode.IS, "7", new String[] { "7" }),

  /** The I s_10. */
  IS_10(CountryCode.IS, "10", new String[] { "10" }),
  /** The I s_12. */
  IS_12(CountryCode.IS, "12", new String[] { "12" }),
  /** The I s_14. */
  IS_14(CountryCode.IS, "14", new String[] { "14" }),
  /** The I s_16. */
  IS_16(CountryCode.IS, "16", new String[] { "16" }),
  /** The I s_18. */
  IS_18(CountryCode.IS, "18", new String[] { "18" }),

  /** IE certifications. */
  IE_G(CountryCode.IE, "G", new String[] { "G" }),
  /** The ie pg. */
  IE_PG(CountryCode.IE, "PG", new String[] { "PG" }),
  /** The I e_12 a. */
  IE_12A(CountryCode.IE, "12A", new String[] { "12A" }),
  /** The I e_15 a. */
  IE_15A(CountryCode.IE, "15A", new String[] { "15A" }),
  /** The I e_16. */
  IE_16(CountryCode.IE, "16", new String[] { "16" }),
  /** The I e_18. */
  IE_18(CountryCode.IE, "18", new String[] { "18" }),

  /** NZ certifications. */
  NZ_G(CountryCode.NZ, "G", new String[] { "G" }),
  /** The nz pg. */
  NZ_PG(CountryCode.NZ, "PG", new String[] { "PG" }),

  /** The nz m. */
  NZ_M(CountryCode.NZ, "M", new String[] { "M" }),
  /** The N z_ r13. */
  NZ_R13(CountryCode.NZ, "R13", new String[] { "R13" }),
  /** The N z_ r16. */
  NZ_R16(CountryCode.NZ, "R16", new String[] { "R16" }),
  /** The N z_ r18. */
  NZ_R18(CountryCode.NZ, "R18", new String[] { "R18" }),
  /** The N z_ r15. */
  NZ_R15(CountryCode.NZ, "R15", new String[] { "R15" }),
  /** The N z_ r p13. */
  NZ_RP13(CountryCode.NZ, "RP13", new String[] { "RP13" }),
  /** The N z_ r p16. */
  NZ_RP16(CountryCode.NZ, "RP16", new String[] { "RP16" }),
  /** The nz r. */
  NZ_R(CountryCode.NZ, "R", new String[] { "R" }),

  /** NO certifications. */
  NO_A(CountryCode.NO, "A", new String[] { "A" }),
  /** The N o_7. */
  NO_7(CountryCode.NO, "7", new String[] { "7" }),

  /** The N o_11. */
  NO_11(CountryCode.NO, "11", new String[] { "11" }),
  /** The N o_15. */
  NO_15(CountryCode.NO, "15", new String[] { "15" }),
  /** The N o_18. */
  NO_18(CountryCode.NO, "18", new String[] { "18" }),

  /** PL certifications. */
  PL_AL(CountryCode.PL, "AL", new String[] { "AL" }),
  /** The P l_7. */
  PL_7(CountryCode.PL, "7", new String[] { "7" }),
  /** The P l_12. */
  PL_12(CountryCode.PL, "12", new String[] { "12" }),
  /** The P l_15. */
  PL_15(CountryCode.PL, "15", new String[] { "15" }),
  /** The pl ap. */
  PL_AP(CountryCode.PL, "AP", new String[] { "AP" }),
  /** The P l_21. */
  PL_21(CountryCode.PL, "21", new String[] { "21" }),

  /** RO certifications. */
  RO_AP(CountryCode.RO, "A.P.", new String[] { "A.P.", "AP" }),
  /** The R o_12. */
  RO_12(CountryCode.RO, "12", new String[] { "12" }),
  /** The R o_15. */
  RO_15(CountryCode.RO, "15", new String[] { "15" }),
  /** The R o_18. */
  RO_18(CountryCode.RO, "18", new String[] { "18" }),
  /** The R o_18 x. */
  RO_18X(CountryCode.RO, "18*", new String[] { "18*" }),

  /** ES certifications. */
  ES_APTA(CountryCode.ES, "APTA", new String[] { "APTA" }),
  /** The es er. */
  ES_ER(CountryCode.ES, "ER", new String[] { "ER" }),
  /** The E s_7. */
  ES_7(CountryCode.ES, "7", new String[] { "7" }),
  /** The E s_12. */
  ES_12(CountryCode.ES, "12", new String[] { "12" }),
  /** The E s_16. */
  ES_16(CountryCode.ES, "16", new String[] { "16" }),
  /** The E s_18. */
  ES_18(CountryCode.ES, "18", new String[] { "18" }),
  /** The es px. */
  ES_PX(CountryCode.ES, "PX", new String[] { "PX" }),

  /** SE certifications. */
  SE_BTL(CountryCode.SE, "BTL", new String[] { "BTL" }),
  /** The S e_7. */
  SE_7(CountryCode.SE, "7", new String[] { "7" }),
  /** The S e_11. */
  SE_11(CountryCode.SE, "11", new String[] { "11" }),
  /** The S e_15. */
  SE_15(CountryCode.SE, "15", new String[] { "15" }),

  /** CH certifications. */
  CH_0(CountryCode.CH, "0", new String[] { "0" }),
  /** The C h_7. */
  CH_7(CountryCode.CH, "7", new String[] { "7" }),

  /** The C h_10. */
  CH_10(CountryCode.CH, "10", new String[] { "10" }),
  /** The C h_12. */
  CH_12(CountryCode.CH, "12", new String[] { "12" }),
  /** The C h_14. */
  CH_14(CountryCode.CH, "14", new String[] { "14" }),
  /** The C h_16. */
  CH_16(CountryCode.CH, "16", new String[] { "16" }),
  /** The C h_18. */
  CH_18(CountryCode.CH, "18", new String[] { "18" }),

  /** TH certifications. */
  TH_P(CountryCode.TH, "P", new String[] { "P" }),
  /** The th g. */
  TH_G(CountryCode.TH, "G", new String[] { "G" }),
  /** The T h_13. */
  TH_13(CountryCode.TH, "13+", new String[] { "13+" }),
  /** The T h_15. */
  TH_15(CountryCode.TH, "15+", new String[] { "15+" }),
  /** The T h_18. */
  TH_18(CountryCode.TH, "18+", new String[] { "18+" }),
  /** The T h_20. */
  TH_20(CountryCode.TH, "20+", new String[] { "20+" }),
  /** The T h_ banned. */
  TH_Banned(CountryCode.TH, "Banned", new String[] { "Banned" }),

  /** initial value. */
  NOT_RATED(CountryCode.US, "not rated", new String[] { "not rated" });

  /** The country. */
  private CountryCode country;

  /** The name. */
  private String      name;

  /** The possible notations. */
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

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName() {
    return this.name;
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

  /**
   * Gets the certification.
   * 
   * @param country
   *          the country
   * @param name
   *          the name
   * @return the certification
   */
  public static Certification getCertification(String country, String name) {
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
  public static String generateCertificationStringFromList(ArrayList<Certification> certs) {
    if (certs == null || certs.isEmpty()) {
      return "";
    }
    String certstring = "";
    for (Certification c : certs) {
      certstring += " / " + c.getCountry().getAlpha2() + ":" + c.getName();
      if (c.getCountry() == CountryCode.GB) {
        // GB is official, but skins often parse UK
        certstring += " / UK:" + c.getName();
      }
    }
    return certstring.substring(3).trim(); // strip off first slash
  }

  /**
   * generates a certification string for country alpha2 (including all
   * different variants); so skins parsing with substr will find them :)<br>
   * eg: "DE:FSK 16 / DE:FSK16 / DE:16 / DE:ab 16".
   * 
   * @param cert
   *          the cert
   * @return certification string like "US:R / UK:15 / SW:15"
   */
  public static String generateCertificationStringWithAlternateNames(Certification cert) {
    if (cert == null) {
      return "";
    }
    String certstring = "";
    for (String notation : cert.getPossibleNotations()) {
      certstring += " / " + cert.getCountry().getAlpha2() + ":" + notation;
      if (cert.getCountry() == CountryCode.GB) {
        certstring += " / UK:" + notation;
      }
    }
    return certstring.substring(3).trim(); // strip off first slash
  }

  /**
   * Parses a given certification string for the localized country setup in
   * setting.
   * 
   * @param name
   *          certification string like "USA:R / UK:15 / Sweden:15"
   * @return the localized certification if found, else *ANY* language cert
   *         found
   */
  // <certification>USA:R / UK:15 / Sweden:15 / Spain:18 / South Korea:15 /
  // Singapore:NC-16 / Portugal:M/16 / Philippines:R-18 / Norway:15 / New
  // Zealand:M / Netherlands:16 / Malaysia:U / Malaysia:18PL / Ireland:18 /
  // Iceland:16 / Hungary:18 / Germany:16 / Finland:K-15 / Canada:18A /
  // Canada:18+ / Brazil:16 / Australia:M / Argentina:16</certification>
  public static Certification parseCertificationStringForSetupCountry(String name) {
    Certification cert = NOT_RATED;
    name = name.trim();
    if (name.contains("/")) {
      // multiple countries
      String[] countries = name.split("/");
      // first try to find by setup CertLanguage
      for (String c : countries) {
        c = c.trim();
        if (c.contains(":")) {
          String[] cs = c.split(":");
          cert = getCertification(Globals.settings.getCertificationCountry(), cs[1]);
          if (cert != NOT_RATED) {
            return cert;
          }
        }
        else {
          cert = getCertification(Globals.settings.getCertificationCountry(), c);
          if (cert != NOT_RATED) {
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
          cert = findCertification(cs[1]);
          if (cert != NOT_RATED) {
            return cert;
          }
        }
        else {
          cert = findCertification(c);
          if (cert != NOT_RATED) {
            return cert;
          }
        }
      }
    }
    else {
      // no slash, so only one country
      if (name.contains(":")) {
        String[] cs = name.split(":");
        cert = getCertification(Globals.settings.getCertificationCountry(), cs[1]);
      }
      else {
        // no country? try to find only by name
        cert = getCertification(Globals.settings.getCertificationCountry(), name);
      }
    }
    // still not found localized cert? parse the name to find *ANY* certificate
    if (cert == Certification.NOT_RATED) {
      cert = findCertification(name);
    }
    return cert;
  }

  /**
   * Find certification.
   * 
   * @param name
   *          the name
   * @return the certification
   */
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

  /**
   * Gets the certification.
   * 
   * @param country
   *          the country
   * @param name
   *          the name
   * @return the certification
   */
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
