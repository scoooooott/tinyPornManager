/*
 * Copyright 2012 - 2020 Manuel Laggner
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
package org.tinymediamanager.core.tasks;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaTrailer;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.scraper.http.StreamingUrl;
import org.tinymediamanager.scraper.util.youtube.YoutubeHelper;
import org.tinymediamanager.scraper.util.youtube.model.Extension;
import org.tinymediamanager.scraper.util.youtube.model.YoutubeMedia;
import org.tinymediamanager.scraper.util.youtube.model.formats.AudioFormat;
import org.tinymediamanager.scraper.util.youtube.model.formats.Format;
import org.tinymediamanager.scraper.util.youtube.model.formats.VideoFormat;
import org.tinymediamanager.scraper.util.youtube.model.quality.VideoQuality;
import org.tinymediamanager.scraper.util.youtube.muxer.TmmMuxer;

/**
 * A task for downloading trailers from youtube
 *
 * @author Wolfgang Janes
 */
public class YoutubeDownloadTask extends TmmTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(YoutubeDownloadTask.class);
  private static final char[] ILLEGAL_FILENAME_CHARACTERS = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"',
      ':' };
  private static final ResourceBundle BUNDLE                      = ResourceBundle.getBundle("messages", new UTF8Control());
  private MediaTrailer mediaTrailer;
  private MediaEntity mediaEntity;
  private YoutubeMedia mediaDetails;
  private String trailerFilename;

  /* helpers for calculating the download speed */
  private long                        timestamp1                  = System.nanoTime();
  private long                        length;
  private long                        bytesDone                   = 0;
  private long                        bytesDonePrevious           = 0;
  private double                      speed                       = 0;

  public YoutubeDownloadTask(MediaTrailer mediaTrailer, MediaEntity mediaEntity, String filename) {
    super(BUNDLE.getString("task.download") + " " + mediaTrailer.getName(), 100, TaskType.BACKGROUND_TASK);
    this.mediaTrailer = mediaTrailer;
    this.mediaEntity = mediaEntity;
    this.trailerFilename = filename;

    setTaskDescription(mediaTrailer.getName());
  }

  @Override
  protected void doInBackground() {
    try {
      mediaDetails = new YoutubeMedia(YoutubeHelper.extractId(mediaTrailer.getUrl()));
      mediaDetails.parseVideo();

      VideoFormat videoFormat = mediaDetails.findVideo(VideoQuality.getVideoQuality(mediaTrailer.getQuality()), Extension.MP4);
      AudioFormat audioFormat = mediaDetails.findBestAudio(Extension.MP4);

      if (videoFormat == null || audioFormat == null) {
        MessageManager.instance.pushMessage(new Message(Message.MessageLevel.ERROR, "Youtube trailer downloader", "message.trailer.unsupported",
                new String[]{mediaEntity.getTitle()}));
        LOGGER.error("Could not download movieTrailer for {}", mediaEntity.getTitle());
        return;
      }

      ExecutorService executorService = Executors.newFixedThreadPool(2);

      // start Futures to download the two streams
      Future<Path> futureVideo = executorService.submit(() -> {
        try {
          LOGGER.debug("Downloading video....");
          return download(videoFormat);
        }
        catch (Exception e) {
          LOGGER.error("Could not download video stream: {}", e.getMessage());
          return null;
        }

      });
      Future<Path> futureAudio = executorService.submit(() -> {
        try {
          LOGGER.debug("Downloading audio....");
          return download(audioFormat);
        }
        catch (Exception e) {
          LOGGER.error("Could not download audio stream: {}", e.getMessage());
          return null;
        }
      });

      Path videoFile = futureVideo.get();
      Path audioFile = futureAudio.get();

      if (videoFile != null && audioFile != null) {

        // Mux the audio and video
        LOGGER.debug("Muxing...");
        TmmMuxer muxer = new TmmMuxer(audioFile, videoFile);
        Path trailer = mediaEntity.getPathNIO().resolve(trailerFilename + ".mp4");
        muxer.mergeAudioVideo(trailer);
        LOGGER.debug("Muxing finished");

        MediaFile mf = new MediaFile(trailer, MediaFileType.TRAILER);

        // Add Media File Information
        mf.gatherMediaInformation();
        mediaEntity.removeFromMediaFiles(mf); // remove old (possibly same) file
        mediaEntity.addToMediaFiles(mf); // add file, but maybe with other MI values
        mediaEntity.saveToDb();

      }

      // Delete the temp audio and video files
      Utils.deleteFileSafely(videoFile);
      Utils.deleteFileSafely(audioFile);

    }
    catch (Exception e) {
      MessageManager.instance.pushMessage(new Message(Message.MessageLevel.ERROR, "Youtube trailer downloader", "message.trailer.downloadfailed",
              new String[]{mediaEntity.getTitle()}));
      LOGGER.error("download of Trailer {} failed", mediaTrailer.getUrl());
    }
  }

  /**
   * Download the given format ( either Audio or Video
   *
   * @param format
   *          Video or Audio Format
   * @return a {@link Path} object of the downloaded file
   * @throws IOException
   *           any {@link Exception} occurred while downloading
   */
  public Path download(Format format) throws Exception {
    String fileName;
    Path tempDir = Paths.get(Utils.getTempFolder());

    if (!Files.exists(tempDir)) {
      Files.createDirectory(tempDir);
    }
    if (format.itag().isVideo()) {
      fileName = mediaDetails.getDetails().getTitle() + "(V)." + format.extension().getText();
    }
    else {
      fileName = mediaDetails.getDetails().getTitle() + "(A)." + format.extension().getText();
    }
    Path outputFile = tempDir.resolve(cleanFilename(fileName));

    StreamingUrl url = new StreamingUrl(format.url());
    try (InputStream is = url.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is);
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile.toFile())) {
      addContentLength(url.getContentLength());

      byte[] buffer = new byte[2048];
      int count;

      while ((count = bis.read(buffer, 0, buffer.length)) != -1) {
        if (cancel) {
          Thread.currentThread().interrupt();
          LOGGER.info("download of {} aborted", url);
          return null;
        }

        fileOutputStream.write(buffer, 0, count);
        addBytesDone(count);
      }

      Utils.flushFileOutputStreamToDisk(fileOutputStream);
      return outputFile;
    }
  }

  private synchronized void addContentLength(long length) {
    this.length += length;
  }

  private synchronized void addBytesDone(long count) {
    bytesDone += count;

    // we push the progress only once per 250ms (to use less performance and get a better download speed)
    long timestamp2 = System.nanoTime();
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

  private String formatBytesForOutput(long bytes) {
    return String.format("%.2fM", (double) bytes / (1000d * 1000d));
  }

  private String formatSpeedForOutput(double speed) {
    return String.format("%.2fkB/s", speed / 1000d);
  }

  private static String cleanFilename(String filename) {
    for (char c : ILLEGAL_FILENAME_CHARACTERS) {
      filename = filename.replace(c, '_');
    }
    return filename;
  }
}
