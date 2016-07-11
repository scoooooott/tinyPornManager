package com.github.stephenc.javaisotools.loopfs.iso9660;

import java.io.IOException;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.thirdparty.MediaInfo;

/**
 * Workaround for protected classes
 * 
 * @author Myron Boyle
 *
 */
public class ISO {

  private static final Logger LOGGER      = LoggerFactory.getLogger(ISO.class);
  private static final int    BUFFER_SIZE = 64 * 1024;

  public static MediaFile getMediaInfoFromISO(MediaFile file, MediaInfo mediaInfo) {
    Iso9660FileSystem image;
    MediaFile isomf = new MediaFile(); // just for MI infos

    try {
      image = new Iso9660FileSystem(file.getFileAsPath().toFile(), true);

      for (Iso9660FileEntry entry : image) {
        if (entry.getSize() <= 5000) { // small files and "." entries
          continue;
        }
        else if (entry.getSize() > Integer.MAX_VALUE) {
          // API mostly works with integer :/
          // we need to integrate https://github.com/stephenc/java-iso-tools/pull/14
          LOGGER.warn("Cannot get mediainfo from file " + file.getFileAsPath().toString() + "" + entry.getPath() + " - file to big!");
          continue;
        }
        MediaFile mf = new MediaFile(Paths.get(file.getFileAsPath().toString(), entry.getPath())); // set ISO as MF path
        mf.setMediaInfo(mediaInfo);
        if (mf.getType() == MediaFileType.VIDEO) {
          mf.setFilesize(entry.getSize());

          try {
            // mediaInfo.option("File_IsSeekable", "0");
            byte[] From_Buffer = new byte[BUFFER_SIZE];
            int From_Buffer_Size; // The size of the read file buffer

            // Preparing to fill MediaInfo with a buffer
            mediaInfo.openBufferInit(entry.getSize(), 0);

            long pos = 0L;
            // The parsing loop
            do {
              // Reading data somewhere, do what you want for this.
              // From_Buffer_Size = is.read(From_Buffer);
              From_Buffer_Size = image.readBytes(entry, (int) pos, From_Buffer, 0, BUFFER_SIZE); // aww... USE LONG!
              pos += From_Buffer_Size; // add bytes read to file position

              // Sending the buffer to MediaInfo
              int Result = mediaInfo.openBufferContinue(From_Buffer, From_Buffer_Size);
              if ((Result & 8) == 8) { // Status.Finalized
                break;
              }

              // Testing if MediaInfo request to go elsewhere
              if (mediaInfo.openBufferContinueGoToGet() != -1) {
                long newPos = mediaInfo.openBufferContinueGoToGet();
                // System.out.println("seek to " + newPos);
                From_Buffer_Size = image.readBytes(entry, (int) newPos, From_Buffer, 0, BUFFER_SIZE); // aww... USE LONG!
                pos = newPos + From_Buffer_Size; // add bytes read to file position
                mediaInfo.openBufferInit(entry.getSize(), newPos); // Informing MediaInfo we have seek
              }

            } while (From_Buffer_Size > 0);

            // Finalizing
            mediaInfo.openBufferFinalize(); // This is the end of the stream, MediaInfo must finish some work
            mf.setMiSnapshot(mediaInfo.snapshot()); // set ours to MI for standard gathering
            mf.gatherMediaInformation(); // normal gather from snapshot

            // copy/accumulate from first MF
            isomf.setDuration(isomf.getDuration() + mf.getDuration()); // accumulate

            if (isomf.getVideoCodec().isEmpty()) {
              isomf.setVideoCodec(mf.getVideoCodec());
            }
            if (isomf.getExactVideoFormat().isEmpty()) {
              isomf.setExactVideoFormat(mf.getExactVideoFormat());
            }
            if (isomf.getVideo3DFormat().isEmpty()) {
              isomf.setVideo3DFormat(mf.getVideo3DFormat());
            }
            if (isomf.getVideoHeight() == 0) {
              isomf.setVideoHeight(mf.getVideoHeight());
            }
            if (isomf.getVideoWidth() == 0) {
              isomf.setVideoWidth(mf.getVideoWidth());
            }
            if (isomf.getOverallBitRate() == 0) {
              isomf.setOverallBitRate(mf.getOverallBitRate());
            }
            if (isomf.getAudioStreams().size() == 0) {
              isomf.setAudioStreams(mf.getAudioStreams());
            }
            if (isomf.getSubtitles().size() == 0) {
              isomf.setSubtitles(mf.getSubtitles());
            }
            // System.out.println(mf);
          }
          // sometimes also an error is thrown
          catch (Exception | Error e) {
            LOGGER.error("Mediainfo could not open file STREAM");

          }
        } // end VIDEO
      } // end entry
      image.close();
    }
    catch (IOException e) {
      LOGGER.error("Mediainfo could not open STREAM");
    }
    return isomf;
  }
}
