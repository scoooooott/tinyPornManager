package org.tinymediamanager;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.tinymediamanager.core.Settings;

public class Globals {
  public static Settings      settings      = Settings.getInstance();
  public static EntityManager entityManager = Persistence.createEntityManagerFactory("$objectdb/db/tmm.odb").createEntityManager();
}
