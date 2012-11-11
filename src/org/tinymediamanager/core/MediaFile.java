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
package org.tinymediamanager.core;

import java.io.File;
import java.text.DecimalFormat;

import javax.persistence.Entity;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class MediaFile.
 * 
 * @author manuel
 */
@Entity
public class MediaFile extends AbstractModelObject {

  /** The Constant PATH. */
  private static final String PATH           = "path";

  /** The Constant FILENAME. */
  private static final String FILENAME       = "filename";

  /** The Constant FILESIZE. */
  private static final String FILESIZE       = "filesize";

  private static final String FILESIZE_IN_MB = "filesizeInMegabytes";

  /** The path. */
  private String              path;

  /** The filename. */
  private String              filename;

  /** The filesize. */
  private long                filesize;

  public MediaFile() {
    this.path = "";
    this.filename = "";
  }

  /**
   * Instantiates a new media file.
   * 
   * @param path
   *          the path
   * @param filename
   *          the filename
   */
  public MediaFile(String path, String filename) {
    this.path = path;
    this.filename = filename;

    File file = new File(this.path, this.filename);
    if (file.exists()) {
      setFilesize(FileUtils.sizeOf(file));
    }
  }

  /**
   * Instantiates a new media file.
   * 
   * @param pathAndFilename
   *          the path and filename
   */
  public MediaFile(String pathAndFilename) {
    this.path = FilenameUtils.getPath(pathAndFilename);
    this.filename = FilenameUtils.getName(pathAndFilename);

    File file = new File(this.path, this.filename);
    if (file.exists()) {
      setFilesize(FileUtils.sizeOf(file));
    }
  }

  /**
   * Gets the path.
   * 
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * Sets the path.
   * 
   * @param newValue
   *          the new path
   */
  public void setPath(String newValue) {
    String oldValue = this.path;
    this.path = newValue;
    firePropertyChange(PATH, oldValue, newValue);
  }

  /**
   * Gets the filename.
   * 
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }

  /**
   * Sets the filename.
   * 
   * @param newValue
   *          the new filename
   */
  public void setFilename(String newValue) {
    String oldValue = this.filename;
    this.filename = newValue;
    firePropertyChange(FILENAME, oldValue, newValue);
  }

  /**
   * Gets the filesize.
   * 
   * @return the filesize
   */
  public long getFilesize() {
    return filesize;
  }

  /**
   * Sets the filesize.
   * 
   * @param newValue
   *          the new filesize
   */
  public void setFilesize(long newValue) {
    long oldValue = this.filesize;
    this.filesize = newValue;
    firePropertyChange(FILESIZE, oldValue, newValue);
    firePropertyChange(FILESIZE_IN_MB, oldValue, newValue);
  }

  public String getFilesizeInMegabytes() {
    DecimalFormat df = new DecimalFormat("#0.00");
    return df.format(filesize / (1024.0 * 1024.0)) + " M";
  }

}
