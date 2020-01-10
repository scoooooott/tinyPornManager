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
package org.tinymediamanager.scraper.util;

/**
 * A helper class to encrypt/decrypt API keys
 * 
 * @author Manuel Laggner
 * @since 1.0
 */
public class ApiKey {
  private static final String  SALT     = "3FF2EC019C627B9652257EBAD71812269851E84295370EB132882F88C0A59A76";
  private static final String  IV       = "E17D2C9927726ACEFE7510B1BDD3D439";

  private static final AesUtil AES_UTIL = new AesUtil(128, 100);

  private ApiKey() {
    // hide the public constructor for utility classes
  }

  /**
   * decrypt the cryted API key
   * 
   * @param cryptedApiKey
   *          the crypted API key
   * @return the decrypted API key
   */
  public static String decryptApikey(String cryptedApiKey) {
    return AES_UTIL.decrypt(SALT, IV, "", cryptedApiKey);
  }

  /**
   * encrypt the API key
   * 
   * @param apiKey
   *          the API key
   * @return the encrypted API key
   */
  public static String encryptApikey(String apiKey) {
    return AES_UTIL.encrypt(SALT, IV, "", apiKey);
  }
}
