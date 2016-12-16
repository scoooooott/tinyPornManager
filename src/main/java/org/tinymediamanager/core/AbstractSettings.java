/*
 * Copyright 2012 - 2016 Manuel Laggner
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class AbstractSettings is the base class for our settings structure. Loading/saving is handled by this class
 */
public abstract class AbstractSettings extends AbstractModelObject {
  private static final Logger LOGGER    = LoggerFactory.getLogger(AbstractSettings.class);

  @XmlTransient
  protected boolean           dirty;
  @XmlTransient
  protected boolean           newConfig = false;
  @XmlTransient
  protected String            settingsFolder;

  /**
   * Sets the dirty flag.
   */
  protected void setDirty() {
    dirty = true;
  }

  /**
   * is this config new oder loaded?
   * 
   * @return true/false
   */
  public boolean isNewConfig() {
    return newConfig;
  }

  /**
   * write the default settings values
   */
  abstract protected void writeDefaultSettings();

  /**
   * gets the actual settings folder (used in the getInstance method)
   * 
   * @return the actual settings folder
   */
  public String getSettingsFolder() {
    return settingsFolder;
  }

  /**
   * gets the acutal config filename (used in the getInstance method)
   * 
   * @return the actual config filename
   */
  abstract protected String getConfigFilename();

  /**
   * gets a logger for that class
   * 
   * @return the logger
   */
  abstract protected Logger getLogger();

  /**
   * save the settings to a xml file via JAXB
   */
  public void saveSettings() {
    // is there anything to save?
    if (!dirty) {
      return;
    }

    // create JAXB context and instantiate marshaller
    JAXBContext context;
    Writer w = null;
    try {
      context = JAXBContext.newInstance(this.getClass());
      Marshaller m = context.createMarshaller();
      m.setProperty("jaxb.encoding", "UTF-8");
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      w = new StringWriter();
      m.marshal(this, w);
      StringBuilder sb = new StringBuilder(w.toString());
      w.close();

      // on windows make windows conform linebreaks
      if (SystemUtils.IS_OS_WINDOWS) {
        sb = new StringBuilder(sb.toString().replaceAll("(?<!\r)\n", "\r\n"));
      }

      w = new FileWriter(new File(getSettingsFolder(), getConfigFilename()));
      String xml = sb.toString();
      IOUtils.write(xml, w);

    }
    catch (Exception e) {
      getLogger().error("saveSettings", e);
      MessageManager.instance.pushMessage(new Message(Message.MessageLevel.ERROR, "tmm.settings", "message.config.savesettingserror"));
    }
    finally {
      try {
        w.close();
      }
      catch (Exception e) {
        getLogger().error("saveSettings", e);
        MessageManager.instance.pushMessage(new Message(Message.MessageLevel.ERROR, "tmm.settings", "message.config.savesettingserror"));
      }
    }

    // clear dirty flag
    dirty = false;
  }

  /**
   * get an instance of the desired settings class
   * 
   * @param folder
   *          the folder to load the settings from
   * @param filename
   *          the filename to load the settings from
   * @param clazz
   *          the class we need for unmarshalling
   * @return an instance (loaded or new) for the desired settings class
   */
  protected static AbstractSettings getInstance(String folder, String filename, Class clazz) {
    AbstractSettings instance = null;

    Path cfgFolder = Paths.get(folder);
    if (!Files.exists(cfgFolder)) {
      try {
        Files.createDirectories(cfgFolder);
      }
      catch (IOException e) {
        LOGGER.warn("could not create config folder: " + e.getMessage());
      }
    }

    // try to parse XML
    JAXBContext context;
    try {
      context = JAXBContext.newInstance(clazz);
      Unmarshaller um = context.createUnmarshaller();
      try {
        LOGGER.debug("Loading settings (" + filename + ") from " + folder);
        Reader in = new InputStreamReader(new FileInputStream(new File(folder, filename)), "UTF-8");
        instance = (AbstractSettings) um.unmarshal(in);
        instance.settingsFolder = folder;
      }
      catch (Exception e) {
        LOGGER.warn("could not load settings - creating default ones...");
        instance = (AbstractSettings) clazz.newInstance();
        instance.newConfig = true;
        instance.dirty = true;
        instance.settingsFolder = folder;
        instance.writeDefaultSettings();
        instance.saveSettings();
      }
      instance.dirty = false;
    }
    catch (Exception e) {
      LOGGER.error("getInstance", e);
      MessageManager.instance.pushMessage(new Message(Message.MessageLevel.ERROR, "tmm.settings", "message.config.loadsettingserror"));
    }
    return instance;
  }
}
