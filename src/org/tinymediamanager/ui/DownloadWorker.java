/*
 * Copyright 2012 - 2013 Manuel Laggner
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
package org.tinymediamanager.ui;

import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.TmmTaskManager;
import org.tinymediamanager.core.MediaEntity;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.scraper.util.StreamingUrl;
import org.tinymediamanager.scraper.util.UrlUtil;
import org.tinymediamanager.ui.DownloadWorker.ProgressType;

/**
 * DownloadWorker for bigger downloads with progressbar
 * 
 * @author Myron Boyle
 */
public class DownloadWorker extends TmmSwingWorker<Void, ProgressType> {
  private String              url;
  private File                file;
  private MediaEntity         media;
  private MediaFileType       fileType;
  private static final Logger LOGGER = LoggerFactory.getLogger(DownloadWorker.class);
  private boolean             cancel = false;

  /**
   * Downloads an url to a file, and does correct http encoding on querystring.<br>
   * Downloads to cache file first, and does then the renaming.
   * 
   * @param url
   *          the http url as string
   * @param toFile
   *          the file to save to
   */
  public DownloadWorker(String url, File toFile) {
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
  public DownloadWorker(String url, File toFile, MediaEntity addToMe, MediaFileType expectedFiletype) {
    this.url = url;
    this.file = toFile;
    this.media = addToMe;
    this.fileType = expectedFiletype;
    if (!GraphicsEnvironment.isHeadless()) {
      JButton btnCancel = new JButton(IconManager.PROCESS_STOP);
      btnCancel.setContentAreaFilled(false);
      btnCancel.setBorderPainted(false);
      btnCancel.setBorder(null);
      btnCancel.setMargin(new Insets(0, 2, 0, 2));
      btnCancel.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
          DownloadWorker.this.cancel();
        }
      });
      setUIElements(new JLabel("Download queued"), new JProgressBar(0, 0), btnCancel);
    }
  }

  // http://docs.oracle.com/javase/6/docs/api/javax/swing/SwingWorker.html
  // http://stackoverflow.com/questions/2908306/how-to-delegate-swingworkers-publish-to-other-methods

  @Override
  protected Void doInBackground() {
    long bytesDone = 0;

    try {
      // if file extension is empty, detect from url, or content type
      String ext = FilenameUtils.getExtension(file.getName());
      if (ext != null && ext.length() > 4) {
        ext = ""; // no extension when longer than 4 chars!
      }
      if (ext.isEmpty()) {
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

      LOGGER.info("Downloading " + url + " to " + file);
      StreamingUrl u = new StreamingUrl(UrlUtil.getURIEncoded(url).toASCIIString());
      File tempFile = new File(file.getAbsolutePath() + ".part");

      URLConnection connection = u.getUrl().openConnection();
      connection.connect();

      long length = connection.getContentLength();
      LOGGER.debug("Content length: " + length);
      String type = connection.getContentType();
      LOGGER.debug("Content type: " + type);
      if (ext.isEmpty()) {
        // still empty? try to parse from mime header
        if (type.startsWith("video/")) {
          ext = type.split("/")[1];
          file = new File(file.getParent(), file.getName() + "." + ext);
        }
      }

      // trace server headers - config.xml loglevel=5000
      if (LOGGER.isTraceEnabled()) {
        Map<String, List<String>> headerfields = connection.getHeaderFields();
        Set<Entry<String, List<String>>> headers = headerfields.entrySet();
        for (Iterator<Entry<String, List<String>>> i = headers.iterator(); i.hasNext();) {
          Entry<String, List<String>> map = i.next();
          LOGGER.trace(map.getKey() + " : " + map.getValue());
        }
      }

      InputStream is = u.getInputStream();

      BufferedInputStream bufferedInputStream = new BufferedInputStream(is);
      FileOutputStream outputStream = new FileOutputStream(tempFile);
      int count = 0;
      int percent = 0;
      byte buffer[] = new byte[1024];

      while ((count = bufferedInputStream.read(buffer, 0, buffer.length)) != -1 && !isCancelled()) {
        outputStream.write(buffer, 0, count);
        bytesDone += count;
        if (length > 0) {
          percent = Math.round(bytesDone * 100 / length);
        }
        publish(new ProgressType(length, bytesDone, percent)); // publish to EDT for async GUI update
      }
      outputStream.close();
      is.close();

      if (isCancelled()) {
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

      // close the url connections
      if (u != null) {
        u.closeConnection();
      }
    }
    catch (Exception e) {
      // muh
      stopProgressBar();
    }
    return null;
  }

  @Override
  protected void process(List<ProgressType> chunks) {
    // just get last - always cumulative
    ProgressType p = chunks.get(chunks.size() - 1);

    if (p.total == 0) {
      // we don't have the size of the download; setIndeterminate
      startProgressBar("downloaded " + p.total / 1024 + "k");
    }
    else {
      startProgressBar("Download " + p.percent + "%", 100, p.percent);
    }

    System.out.println(p.done + "/" + p.total + " (" + p.percent + "%)");
  }

  @Override
  public void cancel() {
    this.cancel = true;
    stopProgressBar();
    TmmTaskManager.removeDownloadTask(this);
  }

  @Override
  public void done() {
    stopProgressBar();
    TmmTaskManager.removeDownloadTask(this);
  }

  /**
   * our progressbar result type
   * 
   * @author Myron Boyle
   * 
   */
  protected static class ProgressType {
    long total;
    long done;
    int  percent;

    public ProgressType(long total, long done, int percent) {
      this.done = done;
      this.total = total;
      this.percent = percent;
    }
  }
}
