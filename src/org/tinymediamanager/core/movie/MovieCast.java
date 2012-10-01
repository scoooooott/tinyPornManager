package org.tinymediamanager.core.movie;

import javax.persistence.Entity;

import org.tinymediamanager.core.AbstractModelObject;

@Entity
public class MovieCast extends AbstractModelObject {

  public enum CastType {
    ACTOR
  }

  private String   name;
  private String   character;

  private CastType type;

  public MovieCast() {
    this.name = new String();
    this.character = new String();
  }

  public MovieCast(String name, CastType castType) {
    this.name = name;
    this.type = castType;
    this.character = new String();
  }

  public MovieCast(String name, String character) {
    this.name = name;
    this.character = character;
    this.type = CastType.ACTOR;
  }

  public void setName(String newValue) {
    String oldValue = name;
    name = newValue;
    firePropertyChange("name", oldValue, newValue);
  }

  public String getName() {
    return name;
  }

  public void setType(CastType newValue) {
    CastType oldValue = type;
    type = newValue;
    firePropertyChange("type", oldValue, newValue);
  }

  public CastType getType() {
    return type;
  }

  public String getCharacter() {
    return character;
  }

  public void setCharacter(String newValue) {
    String oldValue = character;
    character = newValue;
    firePropertyChange("name", oldValue, newValue);
  }

}
