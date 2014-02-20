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
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.TmmTaskManager;
import org.tinymediamanager.core.MediaEntity;
import org.tinymediamanager.core.MediaFile;
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
    this(url, toFile, null);
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
   */
  public DownloadWorker(String url, File toFile, MediaEntity addToMe) {
    this.url = url;
    this.file = toFile;
    this.media = addToMe;
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
      // if we can detect the extension frum url, set it already
      String ext = UrlUtil.getExtension(url);
      if (!ext.isEmpty()) {
        file = new File(file.getParent(), file.getName() + "." + ext);
      }

      LOGGER.info("Downloading " + url + " to " + file + ext);
      StreamingUrl u = new StreamingUrl(UrlUtil.getURIEncoded(url).toASCIIString());
      File tempFile = new File(file.getAbsolutePath() + ".part");

      URLConnection connection = u.getUrl().openConnection();
      connection.connect();

      long length = connection.getContentLength();
      LOGGER.debug("Content length: " + length);

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
        // generate MF + MI
        MediaFile mf = new MediaFile(tempFile);
        mf.gatherMediaInformation();
        if (ext.isEmpty()) { // no extension from url? take from MI (hopefully it is a supported file ;)
          file = new File(file.getParent(), file.getName() + "." + mf.getContainerFormat());
        }

        boolean ok = Utils.moveFileSafe(tempFile, file);
        if (ok) {
          FileUtils.deleteQuietly(tempFile);
          if (media != null) {
            mf.setFile(file);
            mf.setType(mf.parseType()); // reparse with actual file
            media.addToMediaFiles(mf);
            media.saveToDb();
          }
        }
        else {
          // TODO: well, yes, what to do? Download was ok, but moving failed...
        }
      }

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

    System.out.println(p.done);
    System.out.println(p.total); // if total = 0, we don't have the size of the download; setIndeterminate
    System.out.println(p.percent);
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
