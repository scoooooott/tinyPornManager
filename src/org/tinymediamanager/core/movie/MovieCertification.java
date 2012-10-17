package org.tinymediamanager.core.movie;

import javax.persistence.Entity;

import org.tinymediamanager.core.AbstractModelObject;

@Entity
public class MovieCertification extends AbstractModelObject {

  private String country;

  private String certification;

  public MovieCertification(String country, String certification) {
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
