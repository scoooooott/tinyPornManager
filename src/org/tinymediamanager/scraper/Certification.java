package org.tinymediamanager.scraper;

public class Certification {

  // private static final String[] countryCodes = Locale.getISOCountries();

  private String country;

  private String certification;

  public Certification(String country, String certification) {
    this.country = country;
    this.certification = certification;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getCertification() {
    return certification;
  }

  public void setCertification(String certification) {
    this.certification = certification;
  }

}
