package org.tinymediamanager.scraper.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.security.MessageDigest;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubtitleUtils {

  private static final Logger LOGGER          = LoggerFactory.getLogger(SubtitleUtils.class);
  /**
   * Size of the chunks that will be hashed in bytes (64 KB)
   */
  private static final int    HASH_CHUNK_SIZE = 64 * 1024;

  /**
   * Returns SubDB hash or empty string if error
   * 
   * @param file
   * @return hash
   */
  @SuppressWarnings("resource")
  public static String computeSubDBHash(File file) {
    long size = file.length();
    long chunkSizeForFile = Math.min(HASH_CHUNK_SIZE, size);

    FileChannel fileChannel = null;
    try {
      fileChannel = new FileInputStream(file).getChannel();

      ByteBuffer head = fileChannel.map(MapMode.READ_ONLY, 0, chunkSizeForFile);
      ByteBuffer tail = fileChannel.map(MapMode.READ_ONLY, Math.max(size - HASH_CHUNK_SIZE, 0), chunkSizeForFile);

      // md.digest(ByteBuffer.array()) always error *grml
      final byte[] hbytes = new byte[head.remaining()];
      head.duplicate().get(hbytes);
      final byte[] tbytes = new byte[tail.remaining()];
      tail.duplicate().get(tbytes);

      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(hbytes);
      md.update(tbytes);

      return Hex.encodeHexString(md.digest());
    }
    catch (Exception e) {
      LOGGER.error("Error computing SubDB hash", e);
    }
    finally {
      try {
        fileChannel.close();
      }
      catch (IOException e) {
        LOGGER.error("Error closing file stream", e);
      }
    }
    return "";
  }

  /**
   * Returns OpenSubtitle hash or empty string if error
   * 
   * @param file
   * @return hash
   */
  @SuppressWarnings("resource")
  public static String computeOpenSubtitlesHash(File file) {
    long size = file.length();
    long chunkSizeForFile = Math.min(HASH_CHUNK_SIZE, size);

    FileChannel fileChannel = null;
    try {
      fileChannel = new FileInputStream(file).getChannel();
      long head = computeOpenSubtitlesHashForChunk(fileChannel.map(MapMode.READ_ONLY, 0, chunkSizeForFile));
      long tail = computeOpenSubtitlesHashForChunk(fileChannel.map(MapMode.READ_ONLY, Math.max(size - HASH_CHUNK_SIZE, 0), chunkSizeForFile));

      return String.format("%016x", size + head + tail);
    }
    catch (Exception e) {
      LOGGER.error("Error computing OpenSubtitles hash", e);
    }
    finally {
      try {
        fileChannel.close();
      }
      catch (IOException e) {
        LOGGER.error("Error closing file stream", e);
      }
    }
    return "";
  }

  private static long computeOpenSubtitlesHashForChunk(ByteBuffer buffer) {

    LongBuffer longBuffer = buffer.order(ByteOrder.LITTLE_ENDIAN).asLongBuffer();
    long hash = 0;

    while (longBuffer.hasRemaining()) {
      hash += longBuffer.get();
    }

    return hash;
  }
}
