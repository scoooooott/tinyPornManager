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
  US_G(CountryCode.US, "G", new String[] { "G" }), US_PG(CountryCode.US, "PG", new String[] { "PG" }), US_PG13(CountryCode.US, "PG-13",
      new String[] { "PG-13" }), US_R(CountryCode.US, "R", new String[] { "R" }), US_NC17(CountryCode.US, "NC-17", new String[] { "NC-17" }),

  /** DE certifications. */
  DE_FSK0(CountryCode.DE, "FSK 0", new String[] { "FSK 0", "FSK0", "0" }), DE_FSK6(CountryCode.DE, "FSK 6", new String[] { "FSK 6", "FSK6", "6",
      "ab 6" }), DE_FSK12(CountryCode.DE, "FSK 12", new String[] { "FSK 12", "FSK12", "12", "ab 12" }), DE_FSK16(CountryCode.DE, "FSK 16",
      new String[] { "FSK 16", "FSK16", "16", "ab 16" }), DE_FSK18(CountryCode.DE, "FSK 18", new String[] { "FSK 18", "FSK18", "18", "ab 18" }),

  /** UK/GB certifications. */
  GB_UC(CountryCode.GB, "UC", new String[] { "UC" }), GB_U(CountryCode.GB, "U", new String[] { "U" }), GB_PG(CountryCode.GB, "PG",
      new String[] { "PG" }), GB_12A(CountryCode.GB, "12A", new String[] { "12A" }), GB_12(CountryCode.DE, "12", new String[] { "12" }), GB_15(
      CountryCode.GB, "15", new String[] { "15" }), GB_18(CountryCode.GB, "18", new String[] { "18" }), GB_R18(CountryCode.GB, "R18",
      new String[] { "R18" }),

  /** RU certifications. */
  RU_Y(CountryCode.RU, "Y", new String[] { "Y" }), RU_6(CountryCode.RU, "6+", new String[] { "6+" }), RU_12(CountryCode.RU, "12+",
      new String[] { "12+" }), RU_14(CountryCode.RU, "14+", new String[] { "14+" }), RU_16(CountryCode.RU, "16+", new String[] { "16+" }), RU_18(
      CountryCode.RU, "18+", new String[] { "18+" }),

  /** NL certifications. */
  NL_AL(CountryCode.NL, "NL", new String[] { "AL" }), NL_6(CountryCode.NL, "6", new String[] { "6" }),
  NL_9(CountryCode.NL, "9", new String[] { "9" }), NL_12(CountryCode.NL, "12", new String[] { "12" }), NL_16(CountryCode.NL, "16",
      new String[] { "16" }),

  /** JP certifications. */
  JP_G(CountryCode.JP, "G", new String[] { "G" }), JP_PG12(CountryCode.JP, "PG-12", new String[] { "PG-12" }), JP_R15(CountryCode.JP, "R15+",
      new String[] { "R15+" }), JP_R18(CountryCode.JP, "R18+", new String[] { "R18+" }),

  /** IT certifications. */
  IT_T(CountryCode.IT, "T", new String[] { "T" }), IT_VM14(CountryCode.IT, "V.M.14", new String[] { "V.M.14", "VM14" }), IT_VM18(CountryCode.IT,
      "V.M.18", new String[] { "V.M.18", "VM18" }),

  /** IN certifications. */
  IN_U(CountryCode.IN, "U", new String[] { "U" }), IN_UA(CountryCode.IN, "UA", new String[] { "UA" }),
  IN_A(CountryCode.IN, "A", new String[] { "A" }), IN_S(CountryCode.IN, "S", new String[] { "S" }),

  /** GR certifications. */
  GR_K(CountryCode.GR, "K", new String[] { "K" }), GR_K13(CountryCode.GR, "K-13", new String[] { "K-13", "K13" }), GR_K17(CountryCode.GR, "K-17",
      new String[] { "K-17", "K17" }), GR_E(CountryCode.GR, "E", new String[] { "E" }),

  /** FR certifications. */
  FR_U(CountryCode.FR, "U", new String[] { "U" }), FR_10(CountryCode.FR, "10", new String[] { "10" }), FR_12(CountryCode.FR, "12",
      new String[] { "12" }), FR_16(CountryCode.FR, "16", new String[] { "16" }), FR_18(CountryCode.FR, "18", new String[] { "18" }),

  /** CA certifications. */
  CA_G(CountryCode.CA, "G", new String[] { "G" }), CA_PG(CountryCode.CA, "PG", new String[] { "PG" }), CA_14A(CountryCode.CA, "14A",
      new String[] { "14A" }), CA_18A(CountryCode.CA, "18A", new String[] { "18A" }), CA_R(CountryCode.CA, "R", new String[] { "R" }), CA_A(
      CountryCode.CA, "A", new String[] { "A" }),

  /** AU certifications. */
  AU_E(CountryCode.AU, "E", new String[] { "E" }), AU_G(CountryCode.AU, "G", new String[] { "G" }),
  AU_PG(CountryCode.AU, "PG", new String[] { "PG" }), AU_M(CountryCode.AU, "M", new String[] { "M" }), AU_MA15(CountryCode.AU, "MA15+",
      new String[] { "MA15+" }), AU_R18(CountryCode.AU, "R18+", new String[] { "R18+" }), AU_X18(CountryCode.AU, "X18+", new String[] { "X18+" }),
  AU_RC(CountryCode.AU, "RC", new String[] { "RC" }),

  /** CZ certifications. */
  CZ_U(CountryCode.CZ, "U", new String[] { "U" }), CZ_PG(CountryCode.CZ, "PG", new String[] { "PG" }), CZ_12(CountryCode.CZ, "12",
      new String[] { "12" }), CZ_15(CountryCode.CZ, "15", new String[] { "15" }), CZ_18(CountryCode.CZ, "18", new String[] { "18" }), CZ_E(
      CountryCode.CZ, "E", new String[] { "E" }),

  /** DK certifications. */
  DK_A(CountryCode.DK, "A", new String[] { "A" }), DK_7(CountryCode.DK, "7", new String[] { "7" }),
  DK_11(CountryCode.DK, "11", new String[] { "11" }), DK_15(CountryCode.DK, "15", new String[] { "15" }), DK_F(CountryCode.DK, "F",
      new String[] { "F" }),

  /** EE certifications. */
  EE_PERE(CountryCode.EE, "PERE", new String[] { "PERE" }), EE_L(CountryCode.EE, "L", new String[] { "L" }), EE_MS6(CountryCode.EE, "MS-6",
      new String[] { "MS-6" }), EE_MS12(CountryCode.EE, "MS-12", new String[] { "MS-12" }), EE_K12(CountryCode.EE, "K-12", new String[] { "K-12" }),
  EE_K14(CountryCode.EE, "K-14", new String[] { "K-14" }), EE_K16(CountryCode.EE, "K-16", new String[] { "K-16" }),

  /** FI certifications. */
  FI_S(CountryCode.FI, "S", new String[] { "S" }), FI_K7(CountryCode.FI, "K-7", new String[] { "K-7" }), FI_K12(CountryCode.FI, "K-12",
      new String[] { "K-12" }), FI_K16(CountryCode.FI, "K-16", new String[] { "K-16" }), FI_K18(CountryCode.FI, "K-18", new String[] { "K-18" }),
  FI_KE(CountryCode.FI, "K-E", new String[] { "K-E" }),

  /** HU certifications. */
  HU_KN(CountryCode.HU, "KN", new String[] { "KN" }), HU_6(CountryCode.HU, "6", new String[] { "6" }), HU_12(CountryCode.HU, "12",
      new String[] { "12" }), HU_16(CountryCode.HU, "16", new String[] { "16" }), HU_18(CountryCode.HU, "18", new String[] { "18" }), HU_X(
      CountryCode.HU, "X", new String[] { "X" }),

  /** IS certifications. */
  IS_L(CountryCode.IS, "L", new String[] { "L" }), IS_7(CountryCode.IS, "7", new String[] { "7" }),
  IS_10(CountryCode.IS, "10", new String[] { "10" }), IS_12(CountryCode.IS, "12", new String[] { "12" }), IS_14(CountryCode.IS, "14",
      new String[] { "14" }), IS_16(CountryCode.IS, "16", new String[] { "16" }), IS_18(CountryCode.IS, "18", new String[] { "18" }),

  /** IE certifications. */
  IE_G(CountryCode.IE, "G", new String[] { "G" }), IE_PG(CountryCode.IE, "PG", new String[] { "PG" }), IE_12A(CountryCode.IE, "12A",
      new String[] { "12A" }), IE_15A(CountryCode.IE, "15A", new String[] { "15A" }), IE_16(CountryCode.IE, "16", new String[] { "16" }), IE_18(
      CountryCode.IE, "18", new String[] { "18" }),

  /** NZ certifications. */
  NZ_G(CountryCode.NZ, "G", new String[] { "G" }), NZ_PG(CountryCode.NZ, "PG", new String[] { "PG" }),
  NZ_M(CountryCode.NZ, "M", new String[] { "M" }), NZ_R13(CountryCode.NZ, "R13", new String[] { "R13" }), NZ_R16(CountryCode.NZ, "R16",
      new String[] { "R16" }), NZ_R18(CountryCode.NZ, "R18", new String[] { "R18" }), NZ_R15(CountryCode.NZ, "R15", new String[] { "R15" }), NZ_RP13(
      CountryCode.NZ, "RP13", new String[] { "RP13" }), NZ_RP16(CountryCode.NZ, "RP16", new String[] { "RP16" }), NZ_R(CountryCode.NZ, "R",
      new String[] { "R" }),

  /** NO certifications. */
  NO_A(CountryCode.NO, "A", new String[] { "A" }), NO_7(CountryCode.NO, "7", new String[] { "7" }),
  NO_11(CountryCode.NO, "11", new String[] { "11" }), NO_15(CountryCode.NO, "15", new String[] { "15" }), NO_18(CountryCode.NO, "18",
      new String[] { "18" }),

  /** PL certifications. */
  PL_AL(CountryCode.PL, "AL", new String[] { "AL" }), PL_7(CountryCode.PL, "7", new String[] { "7" }), PL_12(CountryCode.PL, "12",
      new String[] { "12" }), PL_15(CountryCode.PL, "15", new String[] { "15" }), PL_AP(CountryCode.PL, "AP", new String[] { "AP" }), PL_21(
      CountryCode.PL, "21", new String[] { "21" }),

  /** RO certifications. */
  RO_AP(CountryCode.RO, "A.P.", new String[] { "A.P.", "AP" }), RO_12(CountryCode.RO, "12", new String[] { "12" }), RO_15(CountryCode.RO, "15",
      new String[] { "15" }), RO_18(CountryCode.RO, "18", new String[] { "18" }), RO_18X(CountryCode.RO, "18*", new String[] { "18*" }),

  /** ES certifications. */
  ES_APTA(CountryCode.ES, "APTA", new String[] { "APTA" }), ES_ER(CountryCode.ES, "ER", new String[] { "ER" }), ES_7(CountryCode.ES, "7",
      new String[] { "7" }), ES_12(CountryCode.ES, "12", new String[] { "12" }), ES_16(CountryCode.ES, "16", new String[] { "16" }), ES_18(
      CountryCode.ES, "18", new String[] { "18" }), ES_PX(CountryCode.ES, "PX", new String[] { "PX" }),

  /** SE certifications. */
  SE_BTL(CountryCode.SE, "BTL", new String[] { "BTL" }), SE_7(CountryCode.SE, "7", new String[] { "7" }), SE_11(CountryCode.SE, "11",
      new String[] { "11" }), SE_15(CountryCode.SE, "15", new String[] { "15" }),

  /** CH certifications. */
  CH_0(CountryCode.CH, "0", new String[] { "0" }), CH_7(CountryCode.CH, "7", new String[] { "7" }),
  CH_10(CountryCode.CH, "10", new String[] { "10" }), CH_12(CountryCode.CH, "12", new String[] { "12" }), CH_14(CountryCode.CH, "14",
      new String[] { "14" }), CH_16(CountryCode.CH, "16", new String[] { "16" }), CH_18(CountryCode.CH, "18", new String[] { "18" }),

  /** TH certifications. */
  TH_P(CountryCode.TH, "P", new String[] { "P" }), TH_G(CountryCode.TH, "G", new String[] { "G" }), TH_13(CountryCode.TH, "13+",
      new String[] { "13+" }), TH_15(CountryCode.TH, "15+", new String[] { "15+" }), TH_18(CountryCode.TH, "18+", new String[] { "18+" }), TH_20(
      CountryCode.TH, "20+", new String[] { "20+" }), TH_Banned(CountryCode.TH, "Banned", new String[] { "Banned" }),

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
