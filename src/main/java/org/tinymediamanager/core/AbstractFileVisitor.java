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

package org.tinymediamanager.core;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * the class {@link AbstractFileVisitor} is used to provide a robust {@link FileVisitor} which is capable to parse Google Drive File Stream paths
 * 
 * @author Manuel Laggner
 */
public abstract class AbstractFileVisitor implements FileVisitor<Path> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFileVisitor.class);

  // copied the implementation from the class {@link java.nio.file.SimpleFileVisitor}
  @Override
  public FileVisitResult preVisitDirectory(Path var1, BasicFileAttributes var2) throws IOException {
    Objects.requireNonNull(var1);
    Objects.requireNonNull(var2);
    return FileVisitResult.CONTINUE;
  }

  // copied the implementation from the class {@link java.nio.file.SimpleFileVisitor}
  public FileVisitResult postVisitDirectory(Path var1, IOException var2) throws IOException {
    Objects.requireNonNull(var1);
    if (var2 != null) {
      throw var2;
    }
    else {
      return FileVisitResult.CONTINUE;
    }
  }

  // If there is some error accessing the file, let the user know.
  // If you don't override this method and an error occurs, an IOException is thrown.
  @Override
  public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
    if (e instanceof FileSystemLoopException) {
      LOGGER.trace("FileSystemLoopException detected: {}", path);
      LOGGER.trace("Problematic path: {} - isDir: {} - isFile: {}", path, path.toFile().isDirectory(), path.toFile().isFile());
      // in some cases (e.g. Google Drive File Stream) loop detection for directories works incorrectly
      // fallback: try to traverse in an alternate way
      if (path.toFile().isDirectory()) {
        traverseTreeAlternate(path);
      }
      else {
        traverseTreeAlternate(path.getParent());
      }
      return FileVisitResult.SKIP_SUBTREE;
    }
    else {
      // any other problem here
      LOGGER.error("visit file failed: {}", e.getMessage());
      // add some more trace infos to get a clue what exactly failed
      LOGGER.trace("visit file failed", e);
      return CONTINUE;
    }
  }

  // an alternate way to traverse the tree
  protected void traverseTreeAlternate(Path parent) {
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(parent)) {
      BasicFileAttributes pathAttr = Files.readAttributes(parent, BasicFileAttributes.class);
      preVisitDirectory(parent, pathAttr);

      for (Path path : directoryStream) {
        if (path.toFile().isDirectory()) {
          traverseTreeAlternate(path);
        }
        else {
          BasicFileAttributes fileAttr = Files.readAttributes(path, BasicFileAttributes.class);
          visitFile(path, fileAttr);
        }
      }

      postVisitDirectory(parent, null);
    }
    catch (IOException e) {
      LOGGER.error("error on traverseTreeAlternate: {}", e.getMessage());
    }
  }
}
