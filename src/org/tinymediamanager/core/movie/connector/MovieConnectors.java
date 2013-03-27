/**
 * 
 */
package org.tinymediamanager.core.movie.connector;

/**
 * The Enum MovieConnectors.
 * 
 * @author Manuel Laggner
 */
public enum MovieConnectors {

  /** The xbmc. */
  XBMC("XBMC"),
  /** The mp. */
  MP("MediaPortal");

  /** The title. */
  private String title;

  /**
   * Instantiates a new movie connectors.
   * 
   * @param title
   *          the title
   */
  private MovieConnectors(String title) {
    this.title = title;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Enum#toString()
   */
  public String toString() {
    return this.title;
  }

}
