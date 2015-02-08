/*
 * Copyright 2012 - 2015 Manuel Laggner
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

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * The class EncryptedStringXmlAdapter. Used to encrypt (obfuscate) fields in the config.xml
 * 
 * @author Manuel Laggner
 */
public class EncryptedStringXmlAdapter extends XmlAdapter<String, String> {
  private static final String  encryptVector = "727DEC2725991751BDFE3DBD0C6BF137";
  private static final String  encryptSalt   = "19CAD45282FBC7627B91A57F201B69E0359AFEB15DE328A88C0A2E05585F84C9";
  private static final AesUtil aesUtil       = new AesUtil(128, 10);

  /**
   * Encrypts the value to be placed back in XML
   */
  @Override
  public String marshal(String plaintext) throws Exception {
    return aesUtil.encrypt(encryptSalt, encryptVector, encryptVector, plaintext);
  }

  /**
   * Decrypts the string value
   */
  @Override
  public String unmarshal(String cyphertext) throws Exception {
    return aesUtil.decrypt(encryptSalt, encryptVector, encryptVector, cyphertext);
  }
}