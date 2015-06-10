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
package org.tinymediamanager.core.threading;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.scraper.util.StreamingUrl;
import org.tinymediamanager.scraper.util.UrlUtil;
import org.tinymediamanager.ui.UTF8Control;

import com.squareup.okhttp.Headers;

/**
 * DownloadTask for bigger downloads with status updates
 * 
 * @author Myron Boyle, Manuel Laggner
 */
public class DownloadTask extends TmmTask {
  private static final Logger         LOGGER    = LoggerFactory.getLogger(DownloadTask.class);
  private static final ResourceBundle BUNDLE    = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private String                      url;
  private File                        file;
  private MediaEntity                 media;
  private MediaFileType               fileType;
  private String                      userAgent = "";

  /**
   * Downloads an url to a file, and does correct http encoding on querystring.<br>
   * Downloads to cache file first, and does then the renaming.
   * 
   * @param url
   *          the http url as string
   * @param toFile
   *          the file to save to
   */
  public DownloadTask(String url, File toFile) {
    this(url, toFile, null, null);
  }

  /**
   * Downloads an url to a file, and does correct http encoding on querystring.<br>
   * Adds file as new MediaFile to submitted MediaEntity<br>
   * Downloads to cache file first, and does then the renaming.
   * 
   * @param url
   *          the http url as string
   * @param toFile
   *          the file to save to
   * @param addToMe
   *          the MediaEntity (like movie) where to add this file
   * @param expectedFiletype
   *          the MediaFileType what we expect (video, trailer, graphic), so we can react on it
   */
  public DownloadTask(String url, File toFile, MediaEntity addToMe, MediaFileType expectedFiletype) {
    super(BUNDLE.getString("task.download") + " " + toFile.getName(), 100, TaskType.BACKGROUND_TASK);

    this.url = url;
    this.file = toFile;
    this.media = addToMe;
    this.fileType = expectedFiletype;

    setTaskDescription(file.getName());
  }

  /**
   * Set a special user agent which is needed for the download
   * 
   * @param userAgent
   */
  public void setSpecialUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  @Override
  protected void doInBackground() {
    try {
      // verify the url is not empty and starts with at least
      if (StringUtils.isBlank(url) || !url.toLowerCase().startsWith("http")) {
        return;
      }

      // if file extension is empty, detect from url, or content type
      String ext = FilenameUtils.getExtension(file.getName());
      if (ext != null && ext.length() > 4) {
        ext = ""; // no extension when longer than 4 chars!
      }
      if (ext == null || ext.isEmpty()) {
        ext = UrlUtil.getExtension(url);
        if (!ext.isEmpty()) {
          if (Globals.settings.getAllSupportedFileTypes().contains("." + ext)) {
            file = new File(file.getParent(), file.getName() + "." + ext);
          }
          else {
            // unsupported filetype, eg php/asp/cgi script
            ext = "";
          }
        }
      }

      LOGGER.info("Downloading " + url);
      StreamingUrl u = new StreamingUrl(UrlUtil.getURIEncoded(url).toASCIIString());
      if (StringUtils.isNotBlank(userAgent)) {
        u.setUserAgent(userAgent);
      }
      InputStream is = u.getInputStream();

      // trace server headers
      LOGGER.debug("Server returned: " + u.getStatusLine());
      Headers headers = u.getHeadersResponse();
      for (String name : headers.names()) {
        LOGGER.debug(" < " + name + ": " + headers.get(name));
      }

      if (u.isFault()) {
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, u.getUrl(), u.getStatusLine()));
        is.close();
        return;
      }

      long length = u.getContentLength();
      String type = u.getContentEncoding();
      if (ext.isEmpty()) {
        // still empty? try to parse from mime header
        if (type.startsWith("video/") || type.startsWith("audio/") || type.startsWith("image/")) {
          ext = type.split("/")[1];
          ext = ext.replaceAll("x-", ""); // x-wmf and others
          file = new File(file.getParent(), file.getName() + "." + ext);
        }
      }

      LOGGER.info("Downloading to " + file);

      File tempFile = new File(file.getAbsolutePath() + ".part");
      BufferedInputStream bufferedInputStream = new BufferedInputStream(is);
      FileOutputStream outputStream = new FileOutputStream(tempFile);
      int count = 0;
      byte buffer[] = new byte[2048];
      Long timestamp1 = System.nanoTime();
      Long timestamp2;
      long bytesDone = 0;
      long bytesDonePrevious = 0;
      double speed = 0;

      while ((count = bufferedInputStream.read(buffer, 0, buffer.length)) != -1) {
        if (cancel) {
          break;
        }

        outputStream.write(buffer, 0, count);
        bytesDone += count;

        // we push the progress only once per 250ms (to use less performance and get a better download speed)
        timestamp2 = System.nanoTime();
        if (timestamp2 - timestamp1 > 250000000) {
          // avg. speed between the actual and the previous
          speed = (speed + (bytesDone - bytesDonePrevious) / ((double) (timestamp2 - timestamp1) / 1000000000)) / 2;

          timestamp1 = timestamp2;
          bytesDonePrevious = bytesDone;

          if (length > 0) {
            publishState(formatBytesForOutput(bytesDone) + "/" + formatBytesForOutput(length) + " @" + formatSpeedForOutput(speed),
                (int) (bytesDone * 100 / length));
          }
          else {
            setWorkUnits(0);
            publishState(formatBytesForOutput(bytesDone) + " @" + formatSpeedForOutput(speed), 0);
          }

        }
      }

      outputStream.close();

      // we must not close the input stream on cancel(the rest will be downloaded if we close it on cancel)
      if (!cancel) {
        is.close();
      }

      if (cancel) {
        // delete half downloaded file
        FileUtils.deleteQuietly(tempFile);
      }
      else {
        if (ext.isEmpty()) {
          // STILL empty? hmpf...
          // now we have a chicken-egg problem:
          // MediaInfo needs MF type to correctly fetch extension
          // to detect MF type, we need the extension
          // so we are forcing to read the container type direct on tempFile
          MediaFile mf = new MediaFile(tempFile);
          mf.setContainerFormatDirect(); // force direct read of mediainfo - regardless of filename!!!
          ext = mf.getContainerFormat();
          if (!ext.isEmpty()) {
            file = new File(file.getParent(), file.getName() + "." + ext);
          }
        }

        FileUtils.deleteQuietly(file); // delete existing file
        boolean ok = Utils.moveFileSafe(tempFile, file);
        if (ok) {
          FileUtils.deleteQuietly(tempFile);
          if (media != null) {
            MediaFile mf = new MediaFile(file, fileType);
            mf.gatherMediaInformation();
            media.removeFromMediaFiles(mf); // remove old (possibly same) file
            media.addToMediaFiles(mf); // add file, but maybe with other MI values
            media.saveToDb();
          }
        }
        else {
          LOGGER.warn("Download to '" + tempFile + "' was ok, but couldn't move to '" + file + "'");
        }
      } // end isCancelled
    }
    catch (Exception e) {
      LOGGER.error("problem downloading: ", e);
    }
  }

  private String formatBytesForOutput(long bytes) {
    return String.format("%.2fM", (double) bytes / (1024d * 1024d));
  }

  private String formatSpeedForOutput(double speed) {
    return String.format("%.2fkB/s", speed / 1024d);
  }
}
