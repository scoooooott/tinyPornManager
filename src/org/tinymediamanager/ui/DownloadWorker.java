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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.scraper.util.Url;
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
  private static final Logger LOGGER = LoggerFactory.getLogger(DownloadWorker.class);

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
    this.url = url;
    this.file = toFile;
  }

  // http://docs.oracle.com/javase/6/docs/api/javax/swing/SwingWorker.html
  // http://stackoverflow.com/questions/2908306/how-to-delegate-swingworkers-publish-to-other-methods

  @Override
  protected Void doInBackground() {
    long bytesDone = 0;
    try {
      LOGGER.info("Downloading " + url + " to " + file);
      Url u = new Url(UrlUtil.getURIEncoded(url).toASCIIString());
      File tempFile = new File(file.getAbsolutePath() + ".part");

      InputStream is = u.getInputStream();

      long length = u.getContentLength();
      LOGGER.debug("Content length: " + length);

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
        boolean ok = Utils.moveFileSafe(tempFile, file);
        if (ok) {
          FileUtils.deleteQuietly(tempFile);
        }
        else {
          // TODO: well, yes, what to do? Download was ok, but moving failed...
        }
      }
    }
    catch (Exception e) {
      // muh
    }
    return null;
  }

  @Override
  protected void process(List<ProgressType> chunks) {
    // just get last - always cumulative
    ProgressType p = chunks.get(chunks.size() - 1);
    System.out.println(p.done);
    System.out.println(p.total); // if total = 0, we don't have the size of the download; setIndeterminate
    System.out.println(p.percent);
  }

  @Override
  public void cancel() {
    // TODO Auto-generated method stub
  }

  @Override
  public void done() {
    // TODO Auto-generated method stub
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
