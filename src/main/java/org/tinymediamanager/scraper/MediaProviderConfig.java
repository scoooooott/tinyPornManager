package org.tinymediamanager.scraper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediaProviderConfig {
  private static final Logger           LOGGER        = LoggerFactory.getLogger(MediaProviderConfig.class);
  private static final String           CONFIG_FOLDER = "data";
  private HashMap<String, ConfigObject> settings      = new HashMap<String, ConfigObject>();
  private String                        id            = "";

  public MediaProviderConfig(MediaProviderInfo mpi) {
    this.id = mpi.getId();
  }

  public void load() {
    loadFromDir(CONFIG_FOLDER);
  }

  /**
   * convenient method for unit testing - should not be used otherwise
   */
  @Deprecated
  public void loadFromDir(String folder) {
    if (settings.isEmpty()) {
      return;
    }
    Properties p = new Properties();
    File conf = new File(folder, "scraper_" + id + ".conf");
    try {
      p.load(new FileInputStream(conf));
      for (ConfigObject co : settings.values()) {
        String value = p.getProperty(co.getKey());
        co.setValue(value == null ? co.getDefaultValue() : value);
      }
    }
    catch (Exception e) {
      LOGGER.warn("Cannot load settings '" + conf + "' - using defaults");
    }
  }

  public void save() {
    saveToDir(CONFIG_FOLDER);
  }

  /**
   * convenient method for unit testing - should not be used otherwise
   */
  @Deprecated
  public void saveToDir(String folder) {
    if (settings.isEmpty()) {
      return;
    }
    Properties p = new Properties();
    for (ConfigObject co : settings.values()) {
      p.setProperty(co.getKey(), co.getValue());
    }
    File conf = new File(folder, "scraper_" + id + ".conf");
    try {
      p.store(new FileOutputStream(conf), "");
    }
    catch (IOException e) {
      LOGGER.warn("Cannot write settings " + conf);
    }
  }

  public Set<String> getAllEntries() {
    return settings.keySet();
  }

  /**
   * gets the config value as string<br>
   * You might want to parse it to boolean if it is true|false<br>
   * You might get a number if it was setup to return the index
   * 
   * @param key
   * @return
   */
  public String getValue(String key) {
    ConfigObject co = settings.get(key);
    if (co == null) {
      LOGGER.warn("Could not get value for key '" + key + "' - key not defined!");
      return "";
    }
    return co.getValue();
  }

  /**
   * If you know that this key is a boolean, use that :)<br>
   * will return NULL if it cannot be parsed as boolean
   * 
   * @param key
   * @return true|false or NULL
   */
  public Boolean getValueAsBool(String key) {
    Boolean bool = null;
    String val = getValue(key);
    if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")) { // always false when unparseable :/
      bool = Boolean.valueOf(val);
    }
    else {
      LOGGER.warn("This is not a Boolean '" + key + "=" + val + "' - returning NULL");
    }
    return bool;
  }

  public void setValue(String key, String value) {
    ConfigObject co = settings.get(key);
    if (co == null) {
      LOGGER.warn("Could not set '" + key + "=" + value + "' - key not defined!");
      return;
    }
    if (co.getPossibleValues().size() > 0 && !co.getPossibleValues().contains(value)) {
      // possible values set, but ours isn't in? just return...
      LOGGER.warn("Could not set '" + key + "=" + value + "' - not in defined range!");
      return;
    }
    co.setValue(value);
  }

  public void setValue(String key, boolean value) {
    ConfigObject co = settings.get(key);
    if (co == null) {
      LOGGER.warn("Could not set '" + key + "=" + value + "' - key not defined!");
      return;
    }
    co.setValue(String.valueOf(value));
  }

  public void addBoolean(String key, boolean defaultValue) {
    ConfigObject co = new ConfigObject();
    co.setKey(key);
    co.setValue(String.valueOf(defaultValue));
    co.setDefaultValue(String.valueOf(defaultValue));
    settings.put(key, co);
  }

  public void addText(String key, String defaultValue) {
    ConfigObject co = new ConfigObject();
    co.setKey(key);
    co.setValue(defaultValue);
    co.setDefaultValue(defaultValue);
    settings.put(key, co);
  }

  public void addSelect(String key, String[] possibleValues, String defaultValue) {
    ConfigObject co = new ConfigObject();
    co.setKey(key);
    for (String s : possibleValues) {
      co.addPossibleValues(s);
    }
    if (!co.possibleValues.contains(defaultValue)) {
      LOGGER.warn("Will not set defaultValue '" + key + "=" + defaultValue + "' - since it is not in the list of possible values!");
    }
    else {
      co.setDefaultValue(defaultValue);
      co.setValue(defaultValue);
    }
    settings.put(key, co);
  }

  public void addSelectIndex(String key, String[] possibleValues, String defaultValue) {
    ConfigObject co = new ConfigObject();
    co.setKey(key);
    co.setReturnListAsInt(true);
    for (String s : possibleValues) {
      co.addPossibleValues(s);
    }
    if (!co.possibleValues.contains(defaultValue)) {
      LOGGER.warn("Will not set defaultValue '" + key + "=" + defaultValue + "' - since it is not in the list of possible values!");
    }
    else {
      co.setDefaultValue(defaultValue);
      co.setValue(defaultValue);
    }
    settings.put(key, co);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  static class ConfigObject {
    String            key             = "";
    String            value           = "";
    String            defaultValue    = "";
    boolean           returnListAsInt = false;
    ArrayList<String> possibleValues  = new ArrayList<String>();

    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    /**
     * gets the configured value, or the index of the value
     * 
     * @return
     */
    public String getValue() {
      if (this.returnListAsInt) {
        if (!possibleValues.contains(this.value)) {
          LOGGER.warn("Could not get INT value for key '" + this.key + "' - not in range!");
          return "";
        }
        else {
          return String.valueOf(possibleValues.indexOf(value));
        }
      }
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public String getDefaultValue() {
      return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
      this.defaultValue = defaultValue;
    }

    public boolean isReturnListAsInt() {
      return returnListAsInt;
    }

    public void setReturnListAsInt(boolean returnListAsInt) {
      this.returnListAsInt = returnListAsInt;
    }

    public ArrayList<String> getPossibleValues() {
      return possibleValues;
    }

    public void setPossibleValues(ArrayList<String> possibleValues) {
      this.possibleValues = possibleValues;
    }

    public void addPossibleValues(String possibleValue) {
      this.possibleValues.add(possibleValue);
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

  }

}
