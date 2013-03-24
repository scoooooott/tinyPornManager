/**
 * 
 */
package org.tinymediamanager.core.movie;

/**
 * @author Manuel Laggner
 * 
 */
public enum MovieConnectors {

  XBMC("XBMC"), MP("MediaPortal");

  private String title;

  private MovieConnectors(String title) {
    this.title = title;
  }

  public String toString() {
    return this.title;
  }

}
