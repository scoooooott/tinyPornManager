package org.tinymediamanager.scraper;

import java.util.*;

class ApiResourceBundle extends ResourceBundle {
  private static ResourceBundle INSTANCE = new ApiResourceBundle();

  @SuppressWarnings("unchecked")
  public static ResourceBundle getResourceBundle() {
    try {
      Class<Control> clazz = (Class<Control>) Class.forName("org.tinymediamanager.ui.UTF8Control");
      return ResourceBundle.getBundle("messages", clazz.newInstance());
    }
    catch (Exception e) {
      return INSTANCE;
    }
  }

  @SuppressWarnings("unchecked")
  public static ResourceBundle getResourceBundle(Locale loc) {
    try {
      Class<Control> clazz = (Class<Control>) Class.forName("org.tinymediamanager.ui.UTF8Control");
      return ResourceBundle.getBundle("messages", loc, clazz.newInstance());
    }
    catch (Exception e) {
      return INSTANCE;
    }
  }

  @Override
  protected Object handleGetObject(String key) {
    return "";
  }

  @Override
  public Enumeration<String> getKeys() {
    return Collections.enumeration(new ArrayList<String>(0));
  }
}
