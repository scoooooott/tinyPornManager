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
package org.tinymediamanager.core.movie.tasks;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.nio.file.FileVisitResult.TERMINATE;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.threading.TmmThreadPool;

/**
 * The Class MissingMovieTask.
 * 
 * @author Myron Boyle
 */

public class MovieFindMissingTask extends TmmThreadPool {
  private static final Logger         LOGGER = LoggerFactory.getLogger(MovieFindMissingTask.class);
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control());

  private List<String>                dataSources;
  private MovieList                   movieList;

  public MovieFindMissingTask() {
    super(BUNDLE.getString("movie.findmissing"));
    movieList = MovieList.getInstance();
    dataSources = new ArrayList<>(MovieModuleManager.SETTINGS.getMovieDataSource());
  }

  public MovieFindMissingTask(String datasource) {
    super(BUNDLE.getString("movie.findmissing") + " (" + datasource + ")");
    movieList = MovieList.getInstance();
    dataSources = new ArrayList<>(1);
    dataSources.add(datasource);
  }

  @Override
  public void doInBackground() {
    try {
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();
      start();

      // build MF list
      ArrayList<MediaFile> mfs = new ArrayList<>();
      for (Movie movie : movieList.getMovies()) {
        mfs.addAll(movie.getMediaFiles());
      }

      for (String ds : dataSources) {
        if (cancel) {
          break;
        }

        HashSet<Path> bigFiles = getBigFilesRecursive(Paths.get(ds));
        if (cancel) {
          break;
        }

        for (Path file : bigFiles) {

          MediaFile mf = new MediaFile(file);
          if (!mfs.contains(mf)) {
            LOGGER.info("found possible movie file {}", file);
            MessageManager.instance
                .pushMessage(new Message(MessageLevel.ERROR, "possible movie", "found possible movie " + file, new String[] { ds }));
          }
        }
      }

      stopWatch.stop();
      LOGGER.info("Done finding missing movies :) - took {}", stopWatch);
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "message.update.threadcrashed"));
    }
  }

  public HashSet<Path> getBigFilesRecursive(Path folder) {
    BigFilesRecursive visitor = new BigFilesRecursive();
    try {
      Files.walkFileTree(folder, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, visitor);
    }
    catch (IOException ignored) {
      // can not happen, since we overridden visitFileFailed, which throws no exception ;)
    }
    return visitor.fFound;
  }

  private class BigFilesRecursive extends SimpleFileVisitor<Path> {
    private HashSet<Path> fFound = new HashSet<>();

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
      if (Utils.isRegularFile(attr) && attr.size() > 1024 * 1024 * 100) {
        fFound.add(file.toAbsolutePath());
      }
      return CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
      // getFilename returns null on DS root!
      if (dir.getFileName() != null && dir.getFileName().toString().equals(Constants.BACKUP_FOLDER)) {
        LOGGER.debug("Skipping backup folder: {}", dir);
        // but not any other well known
        return SKIP_SUBTREE;
      }
      return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
      if (cancel) {
        return TERMINATE;
      }
      return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
      LOGGER.error("visitFile failed: {}", exc.getMessage());
      return CONTINUE;
    }
  }

  @Override
  public void callback(Object obj) {
    publishState((String) obj, progressDone);
  }
}
