/*
 * Copyright 2012 - 2017 Manuel Laggner
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
package org.tinymediamanager.core.movie.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FilenameUtils;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.threading.DownloadTask;

/**
 * This class handles the download and additional unpacking of a subtitle
 * 
 * @author Manuel Laggner
 */
public class MovieSubtitleDownloadTask extends DownloadTask {

  private final Movie  movie;
  private final String languageTag;
  private final Path   videoFilePath;

  public MovieSubtitleDownloadTask(String url, Path videoFilePath, String languageTag, Movie movie) {
    super(url, movie.getPathNIO().resolve(FilenameUtils.getBaseName(videoFilePath.getFileName().toString()) + "." + languageTag));
    this.movie = movie;
    this.languageTag = languageTag;
    this.videoFilePath = videoFilePath;
  }

  @Override
  protected void doInBackground() {
    // let the DownloadTask handle the whole download
    super.doInBackground();

    MediaFile mf = new MediaFile(file);

    if (mf.getType() != MediaFileType.SUBTITLE) {
      String basename = FilenameUtils.getBaseName(videoFilePath.toString()) + "." + languageTag;

      // try to decompress
      try {
        byte[] buffer = new byte[1024];

        ZipInputStream is = new ZipInputStream(new FileInputStream(file.toFile()));

        // get the zipped file list entry
        ZipEntry ze = is.getNextEntry();

        while (ze != null) {
          String zipEntryFilename = ze.getName();
          String extension = FilenameUtils.getExtension(zipEntryFilename);

          // check is that is a valid file type
          if (!Globals.settings.getSubtitleFileType().contains("." + extension) && !"idx".equals(extension)) {
            ze = is.getNextEntry();
            continue;
          }

          File destination = new File(file.getParent().toFile(), basename + "." + extension);
          FileOutputStream os = new FileOutputStream(destination);

          int len;
          while ((len = is.read(buffer)) > 0) {
            os.write(buffer, 0, len);
          }

          os.close();
          mf = new MediaFile(destination.toPath());

          // only take the first subtitle
          break;
        }
        is.closeEntry();
        is.close();

        Utils.deleteFileSafely(file);
      }
      catch (Exception e) {
      }
    }

    mf.gatherMediaInformation();
    movie.removeFromMediaFiles(mf); // remove old (possibly same) file
    movie.addToMediaFiles(mf); // add file, but maybe with other MI values
    movie.saveToDb();
  }
}
