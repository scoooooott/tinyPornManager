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
package org.tinymediamanager.core.movie;

import javax.persistence.Entity;

import org.tinymediamanager.core.AbstractModelObject;

/**
 * The Class MovieCertification.
 */
@Entity
public class MovieCertification extends AbstractModelObject {

  /** The country. */
  private String country;

  /** The certification. */
  private String certification;

  /**
   * Instantiates a new movie certification.
   * 
   * @param country
   *          the country
   * @param certification
   *          the certification
   */
  public MovieCertification(String country, String certification) {
    this.country = country;
    this.certification = certification;
  }

  /**
   * Gets the country.
   * 
   * @return the country
   */
  public String getCountry() {
    return country;
  }

  /**
   * Sets the country.
   * 
   * @param country
   *          the new country
   */
  public void setCountry(String country) {
    this.country = country;
  }

  /**
   * Gets the certification.
   * 
   * @return the certification
   */
  public String getCertification() {
    return certification;
  }

  /**
   * Sets the certification.
   * 
   * @param certification
   *          the new certification
   */
  public void setCertification(String certification) {
    this.certification = certification;
  }

}
