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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.CodeSource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.LaunchUtil;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.scraper.util.StrgUtils;

/**
 * The Class Utils.
 * 
 * @author Manuel Laggner / Myron Boyle
 */
public class Utils {
  private static final Logger  LOGGER                = LoggerFactory.getLogger(Utils.class);
  private static final Pattern localePattern         = Pattern.compile("messages_(.{2})_?(.{2,4})?\\.properties", Pattern.CASE_INSENSITIVE);

  // <cd/dvd/part/pt/disk/disc> <0-N>
  private static final Pattern stackingPattern1      = Pattern.compile("(.*?)[ _.-]+((?:cd|dvd|p(?:ar)?t|dis[ck])[ _.-]*[1-9][0-9]?)(\\.[^.]+)$",
      Pattern.CASE_INSENSITIVE);

  // <cd/dvd/part/pt/disk/disc> <a-d>
  private static final Pattern stackingPattern2      = Pattern.compile("(.*?)[ _.-]+((?:cd|dvd|p(?:ar)?t|dis[ck])[ _.-]*[a-d])(\\.[^.]+)$",
      Pattern.CASE_INSENSITIVE);

  // moviename-a.avi // modified mandatory delimiter (but no space), and A-D must be at end!
  private static final Pattern stackingPattern3      = Pattern.compile("(.*?)[_.-]+([a-d])(\\.[^.]+)$", Pattern.CASE_INSENSITIVE);

  // moviename-1of2.avi, moviename-1 of 2.avi
  private static final Pattern stackingPattern4      = Pattern.compile("(.*?)[ \\(_.-]+([1-9][0-9]?[ .]?of[ .]?[1-9][0-9]?)[ \\)_-]?(\\.[^.]+)$",
      Pattern.CASE_INSENSITIVE);

  // folder stacking marker <cd/dvd/part/pt/disk/disc> <0-N> - must be last part
  private static final Pattern folderStackingPattern = Pattern.compile("(.*?)[ _.-]*((?:cd|dvd|p(?:ar)?t|dis[ck])[ _.-]*[1-9][0-9]?)$",
      Pattern.CASE_INSENSITIVE);

  private static List<Locale>  availableLocales      = new ArrayList<>();

  private static String        tempFolder;

  static {
    // get the systems default temp folder
    try {
      String temp = System.getProperty("java.io.tmpdir");
      Path tempFolder = Paths.get(temp);
      if (Files.exists(tempFolder) && Files.isWritable(tempFolder)) {
        Utils.tempFolder = temp;
      }
      else {
        Utils.tempFolder = "tmp";
      }
    }
    catch (Exception | Error ignored) {
      if (tempFolder != null) {
        tempFolder = "tmp";
      }
    }
  }

  private Utils() {
    // hide public constructor for utility classes
  }

  /**
   * gets the filename part, and returns last extension
   * 
   * @param path
   *          the path to get the last extension for
   * @return the last extension found
   */
  public static String getExtension(Path path) {
    String ext = "";
    String fn = path.getFileName().toString();
    int i = fn.lastIndexOf('.');
    if (i > 0) {
      ext = fn.substring(i + 1);
    }
    return ext;
  }

  /**
   * this is the TMM variant of isRegularFiles()<br>
   * because deduplication creates windows junction points, we check here if it is<br>
   * not a directory, and either a regular file or "other" one.<br>
   * see http://serverfault.com/a/667220
   * 
   * @param file
   * @return
   */
  public static boolean isRegularFile(Path file) {
    // see windows impl http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/7u40-b43/sun/nio/fs/WindowsFileAttributes.java#451
    try {
      BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
      return (attr.isRegularFile() || attr.isOther()) && !attr.isDirectory();
    }
    catch (IOException e) {
      return false;
    }
  }

  /**
   * this is the TMM variant of isRegularFiles()<br>
   * because deduplication creates windows junction points, we check here if it is<br>
   * not a directory, and either a regular file or "other" one.<br>
   * see http://serverfault.com/a/667220
   * 
   * @param attr
   * @return
   */
  public static boolean isRegularFile(BasicFileAttributes attr) {
    // see windows impl http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/7u40-b43/sun/nio/fs/WindowsFileAttributes.java#451
    return (attr.isRegularFile() || attr.isOther()) && !attr.isDirectory();
  }

  /**
   * dumps a complete Object (incl sub-classes 5 levels deep) to System.out
   * 
   * @param o
   *          the object to dump
   */
  public static void dumpObject(Object o) {
    RecursiveToStringStyle style = new RecursiveToStringStyle(5);
    System.out.println(ReflectionToStringBuilder.toString(o, style)); // NOSONAR
    style.cleanup();
  }

  /**
   * returns the relative path of 2 absolute file paths<br>
   * "/a/b & /a/b/c/d -> c/d
   * 
   * @param parent
   *          the directory
   * @param child
   *          the subdirectory
   * @return relative path
   */
  public static String relPath(String parent, String child) {
    return relPath(Paths.get(parent), Paths.get(child));
  }

  /**
   * returns the relative path of 2 absolute file paths<br>
   * "/a/b & /a/b/c/d -> c/d
   * 
   * @param parent
   *          the directory
   * @param child
   *          the subdirectory
   * @return relative path
   */
  public static String relPath(Path parent, Path child) {
    return parent.relativize(child).toString();
  }

  /**
   * Returns the sortable variant of title/originaltitle<br>
   * eg "The Bourne Legacy" -> "Bourne Legacy, The".
   * 
   * @param title
   *          the title
   * @return the title/originaltitle in its sortable format
   */
  public static String getSortableName(String title) {
    if (title == null || title.isEmpty()) {
      return "";
    }
    if (title.toLowerCase(Locale.ROOT).matches("^die hard$") || title.toLowerCase(Locale.ROOT).matches("^die hard[:\\s].*")) {
      return title;
    }
    if (title.toLowerCase(Locale.ROOT).matches("^die another day$") || title.toLowerCase(Locale.ROOT).matches("^die another day[:\\s].*")) {
      return title;
    }
    for (String prfx : Settings.getInstance().getTitlePrefix()) {
      String delim = "\\s+"; // one or more spaces needed
      if (prfx.matches(".*['`´]$")) { // ends with hand-picked delim, so no space might be possible
        delim = "";
      }

      // only move the first found prefix
      if (title.matches("(?i)^" + Pattern.quote(prfx) + delim + "(.*)")) {
        title = title.replaceAll("(?i)^" + Pattern.quote(prfx) + delim + "(.*)", "$1, " + prfx);
        break;
      }
    }
    return title.trim();
  }

  /**
   * Returns the common name of title/originaltitle when it is named sortable<br>
   * eg "Bourne Legacy, The" -> "The Bourne Legacy".
   * 
   * @param title
   *          the title
   * @return the original title
   */
  public static String removeSortableName(String title) {
    if (title == null || title.isEmpty()) {
      return "";
    }
    for (String prfx : Settings.getInstance().getTitlePrefix()) {
      String delim = " "; // one spaces as delim
      if (prfx.matches(".*['`´]$")) { // ends with hand-picked delim, so no space between prefix and title
        delim = "";
      }
      title = title.replaceAll("(?i)(.*), " + prfx + "$", prfx + delim + "$1");
    }
    return title.trim();
  }

  /**
   * Clean stacking markers.<br>
   * Same logic as detection, but just returning string w/o
   * 
   * @param filename
   *          the filename WITH extension
   * @return the string
   */
  public static String cleanStackingMarkers(String filename) {
    if (!StringUtils.isEmpty(filename)) {
      // see http://kodi.wiki/view/Advancedsettings.xml#moviestacking
      // basically returning <regexp>(Title)(Stacking)(Ignore)(Extension)</regexp>

      // <cd/dvd/part/pt/disk/disc> <0-N>
      Matcher m = stackingPattern1.matcher(filename);
      if (m.matches()) {
        return m.group(1) + m.group(3); // just return String w/o stacking
      }

      // <cd/dvd/part/pt/disk/disc> <a-d>
      m = stackingPattern2.matcher(filename);
      if (m.matches()) {
        return m.group(1) + m.group(3); // just return String w/o stacking
      }

      // moviename-2.avi // modified mandatory delimiter, and AD must be at end!
      m = stackingPattern3.matcher(filename);
      if (m.matches()) {
        return m.group(1) + m.group(3); // just return String w/o stacking
      }

      // moviename-1of2.avi, moviename-1 of 2.avi
      m = stackingPattern4.matcher(filename);
      if (m.matches()) {
        return m.group(1) + m.group(3); // just return String w/o stacking
      }
    }
    return filename; // no cleanup, return 1:1
  }

  /**
   * Clean stacking markers.<br>
   * Same logic as detection, but just returning string w/o
   * 
   * @param filename
   *          the filename WITH extension
   * @return the string
   */
  public static String cleanFolderStackingMarkers(String filename) {
    if (!StringUtils.isEmpty(filename)) {
      Matcher m = folderStackingPattern.matcher(filename);
      if (m.matches()) {
        return m.group(1); // just return String w/o stacking
      }
    }
    return filename;
  }

  /**
   * Returns the stacking information from FOLDER name
   * 
   * @param filename
   *          the filename
   * @return the stacking information
   */
  public static String getFolderStackingMarker(String filename) {
    if (!StringUtils.isEmpty(filename)) {
      // see http://kodi.wiki/view/Advancedsettings.xml#moviestacking
      // basically returning <regexp>(Title)(Volume)(Ignore)(Extension)</regexp>

      // <cd/dvd/part/pt/disk/disc> <0-N> // FIXME: check for first delimiter (optional/mandatory)!
      Matcher m = folderStackingPattern.matcher(filename);
      if (m.matches()) {
        return m.group(2);
      }
    }
    return "";
  }

  /**
   * Returns the stacking information from filename
   * 
   * @param filename
   *          the filename
   * @return the stacking information
   */
  public static String getStackingMarker(String filename) {
    if (!StringUtils.isEmpty(filename)) {
      // see http://kodi.wiki/view/Advancedsettings.xml#moviestacking
      // basically returning <regexp>(Title)(Stacking)(Ignore)(Extension)</regexp>

      // <cd/dvd/part/pt/disk/disc> <0-N>
      Matcher m = stackingPattern1.matcher(filename);
      if (m.matches()) {
        return m.group(2);
      }

      // <cd/dvd/part/pt/disk/disc> <a-d>
      m = stackingPattern2.matcher(filename);
      if (m.matches()) {
        return m.group(2);
      }

      // moviename-a.avi // modified mandatory delimiter, and AD must be at end!
      m = stackingPattern3.matcher(filename);
      if (m.matches()) {
        return m.group(2);
      }

      // moviename-1of2.avi, moviename-1 of 2.avi
      m = stackingPattern4.matcher(filename);
      if (m.matches()) {
        return m.group(2);
      }
    }
    return "";
  }

  public static String substr(String str, String pattern) {
    Pattern regex = Pattern.compile(pattern);
    Matcher m = regex.matcher(str);
    if (m.find()) {
      return m.group(1);
    }
    else {
      return "";
    }
  }

  /**
   * Returns the stacking prefix
   * 
   * @param filename
   *          the filename
   * @return the stacking prefix - might be empty
   */
  public static String getStackingPrefix(String filename) {
    String stack = getStackingMarker(filename).replaceAll("[0-9]", "");
    if (stack.length() == 1 || stack.contains("of")) {
      // A-D and (X of Y) - no prefix here
      stack = "";
    }
    return stack;
  }

  /**
   * Returns the stacking information from filename
   * 
   * @param filename
   *          the filename
   * @return the stacking information
   */
  public static int getStackingNumber(String filename) {
    if (!StringUtils.isEmpty(filename)) {
      String stack = getStackingMarker(filename);
      if (!stack.isEmpty()) {
        if (stack.equalsIgnoreCase("a")) {
          return 1;
        }
        else if (stack.equalsIgnoreCase("b")) {
          return 2;
        }
        else if (stack.equalsIgnoreCase("c")) {
          return 3;
        }
        else if (stack.equalsIgnoreCase("d")) {
          return 4;
        }
        if (stack.contains("of")) {
          stack = stack.replaceAll("of.*", ""); // strip all after "of", so we have the first number
        }

        try {
          return Integer.parseInt(stack.replaceAll("[^0-9]", ""));
        }
        catch (Exception e) {
          return 0;
        }
      }
    }
    return 0;
  }

  /**
   * Checks if is valid imdb id.
   * 
   * @param imdbId
   *          the imdb id
   * @return true, if is valid imdb id
   */
  public static boolean isValidImdbId(String imdbId) {
    if (StringUtils.isEmpty(imdbId)) {
      return false;
    }

    return imdbId.matches("tt\\d{6,}");
  }

  /**
   * Unquote.
   * 
   * @param str
   *          the str
   * @return the string
   */
  public static String unquote(String str) {
    if (str == null)
      return null;
    return str.replaceFirst("^\\\"(.*)\\\"$", "$1");
  }

  /**
   * gets the UTF-8 encoded System property.
   * 
   * @param prop
   *          the property to fetch
   * @return the enc prop
   */
  @SuppressWarnings("deprecation")
  private static String getEncProp(String prop) {
    String property = System.getProperty(prop);
    if (StringUtils.isBlank(property)) {
      return "";
    }

    try {
      return URLEncoder.encode(property, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      return URLEncoder.encode(property);
    }
  }

  public static void removeEmptyStringsFromList(List<String> list) {
    list.removeAll(Collections.singleton(null));
    list.removeAll(Collections.singleton(""));
  }

  /**
   * replaces a string with placeholder ({}) with the string from the replacement array the strings in the replacement array have to be in the same
   * order as the placeholder in the source string
   * 
   * @param source
   *          string
   * @param replacements
   *          array
   * @return replaced string
   */
  public static String replacePlaceholders(String source, String[] replacements) {
    String result = source;
    int index = 0;

    Pattern pattern = Pattern.compile("\\{\\}");
    while (true) {
      Matcher matcher = pattern.matcher(result);
      if (matcher.find()) {
        try {
          if (replacements.length > index) {
            result = result.replaceFirst(pattern.pattern(), StringEscapeUtils.escapeJava(replacements[index]));
          }
          else {
            result = result.replaceFirst(pattern.pattern(), "");
          }
        }
        catch (Exception e) {
          result = result.replaceFirst(pattern.pattern(), "");
        }
        index++;
      }
      else {
        break;
      }
    }
    return StrgUtils.removeDuplicateWhitespace(result);
  }

  /**
   * modified version of commons-io FileUtils.moveDirectory(); adapted to Java 7 NIO<br>
   * since renameTo() might not work in first place, retry it up to 5 times.<br>
   * (better wait 5 sec for success, than always copying a 50gig directory ;)<br>
   * <b>And NO, we're NOT doing a copy+delete as fallback!</b>
   * 
   * @param srcDir
   *          the directory to be moved
   * @param destDir
   *          the destination directory
   * @return true, if successful
   * @throws IOException
   *           if an IO error occurs moving the file
   */
  public static boolean moveDirectorySafe(Path srcDir, Path destDir) throws IOException {
    // rip-off from
    // http://svn.apache.org/repos/asf/commons/proper/io/trunk/src/main/java/org/apache/commons/io/FileUtils.java
    if (srcDir == null) {
      throw new NullPointerException("Source must not be null"); // NOSONAR
    }
    if (destDir == null) {
      throw new NullPointerException("Destination must not be null"); // NOSONAR
    }
    if (!srcDir.toAbsolutePath().toString().equals(destDir.toAbsolutePath().toString())) {
      LOGGER.debug("try to move folder {} to {}", srcDir, destDir);
      if (!Files.isDirectory(srcDir)) {
        throw new FileNotFoundException("Source '{}" + srcDir + "' does not exist, or is not a directory"); // NOSONAR
      }
      if (Files.exists(destDir) && !Files.isSameFile(destDir, srcDir)) {
        // extra check for Windows/OSX, where the File.equals is case insensitive
        // so we know now, that the Dir is the same, but the absolute name does not match
        throw new FileExistsException("Destination '" + destDir + "' already exists"); // NOSONAR
      }
      if (destDir.getParent() != null && !Files.exists(destDir.getParent())) {
        // create parent folder structure, else renameTo does not work
        // NULL parent means, that we just have one relative folder like Paths.get("bla") - no need to create anything
        try {
          Files.createDirectories(destDir.getParent());
        }
        catch (Exception e) {
          LOGGER.error("could not create directory structure {}", destDir.getParent());
          // but we try a move anyway...
        }
      }

      // rename folder; try 5 times and wait a sec
      boolean rename = false;
      for (int i = 0; i < 5; i++) {
        try {
          // need atomic fs move for changing cASE
          Files.move(srcDir, destDir, StandardCopyOption.ATOMIC_MOVE);
          rename = true;// no exception
        }
        catch (AtomicMoveNotSupportedException a) {
          // if it fails (b/c not on same file system) use that; original documentation
          /*
           * When moving a directory requires that its entries be moved then this method fails (by throwing an {@code IOException}). To move a <i>file
           * tree</i> may involve copying rather than moving directories and this can be done using the {@link #copy copy} method in conjunction with
           * the {@link #walkFileTree Files.walkFileTree} utility method.
           */
          // in this case we do a recursive copy & delete
          // copy all files (with re-creating symbolic links if there are some)
          try (Stream<Path> stream = Files.walk(srcDir)) {
            Iterator<Path> srcFiles = stream.iterator();
            while (srcFiles.hasNext()) {
              Path source = srcFiles.next();
              Path destination = destDir.resolve(srcDir.relativize(source));
              if (Files.isSymbolicLink(source)) {
                Files.createSymbolicLink(destination, source.toRealPath());
                continue;
              }
              if (Files.isDirectory(source)) {
                if (!Files.exists(destination)) {
                  Files.createDirectory(destination);
                }
                continue;
              }
              Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            // delete source files
            Utils.deleteDirectoryRecursive(srcDir);
            rename = true;
          }
          catch (IOException e) {
            LOGGER.warn("rename problem (fallback): {}", e.getMessage()); // NOSONAR
          }
        }
        catch (IOException e) {
          LOGGER.warn("rename problem: {}", e.getMessage()); // NOSONAR
        }
        if (rename) {
          break; // ok it worked, step out
        }
        try {
          LOGGER.debug("rename did not work - sleep a while and try again..."); // NOSONAR
          Thread.sleep(1000);
        }
        catch (InterruptedException e) { // NOSONAR
          // we will not let the JVM abort the thread here -> just finish the logic without waiting any longer
          LOGGER.warn("I'm so excited - could not sleep"); // NOSONAR
          break;
        }
      }

      // ok, we tried it 5 times - it still seems to be locked somehow. Continue
      // with copying as fallback
      // NOOO - we don't like to have some files copied and some not.

      if (!rename) {
        LOGGER.error("Failed to rename directory {} to {}", srcDir, destDir);
        LOGGER.error("Movie renaming aborted.");
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, srcDir, "message.renamer.failedrename")); // NOSONAR
        return false;
      }
      else {
        LOGGER.info("Successfully moved folder {} to {}", srcDir, destDir);
        return true;
      }
    }
    return true; // dir are equal
  }

  /**
   * modified version of commons-io FileUtils.moveFile(); adapted to Java 7 NIO<br>
   * since renameTo() might not work in first place, retry it up to 5 times.<br>
   * (better wait 5 sec for success, than always copying a 50gig directory ;)<br>
   * <b>And NO, we're NOT doing a copy+delete as fallback!</b>
   * 
   * @param srcFile
   *          the file to be moved
   * @param destFile
   *          the destination file
   * @throws NullPointerException
   *           if source or destination is {@code null}
   * @throws FileExistsException
   *           if the destination file exists
   * @throws IOException
   *           if source or destination is invalid
   * @throws IOException
   *           if an IO error occurs moving the file
   */
  public static boolean moveFileSafe(final Path srcFile, final Path destFile) throws IOException {
    if (srcFile == null) {
      throw new NullPointerException("Source must not be null");
    }
    if (destFile == null) {
      throw new NullPointerException("Destination must not be null");
    }
    if (!srcFile.toAbsolutePath().toString().equals(destFile.toAbsolutePath().toString())) {
      LOGGER.debug("try to move file {} to {}", srcFile, destFile);
      if (!Files.exists(srcFile)) {
        // allow moving of symlinks
        // https://github.com/tinyMediaManager/tinyMediaManager/issues/410
        if (!Files.isSymbolicLink(srcFile)) {
          throw new FileNotFoundException("Source '" + srcFile + "' does not exist"); // NOSONAR
        }
      }
      if (Files.isDirectory(srcFile)) {
        throw new IOException("Source '" + srcFile + "' is a directory"); // NOSONAR
      }
      if (Files.exists(destFile) && !Files.isSameFile(destFile, srcFile)) {
        // extra check for windows, where the File.equals is case insensitive
        // so we know now, that the File is the same, but the absolute name does not match
        throw new FileExistsException("Destination '" + destFile + "' already exists");
      }
      if (Files.isDirectory(destFile)) {
        throw new IOException("Destination '" + destFile + "' is a directory");
      }

      // rename folder; try 5 times and wait a sec
      boolean rename = false;
      for (int i = 0; i < 5; i++) {
        try {
          // need atomic fs move for changing cASE
          Files.move(srcFile, destFile, StandardCopyOption.ATOMIC_MOVE);
          rename = true;// no exception
        }
        catch (AtomicMoveNotSupportedException a) {
          // if it fails (b/c not on same file system) use that
          try {
            Files.copy(srcFile, destFile, StandardCopyOption.REPLACE_EXISTING);
            Files.delete(srcFile);
            rename = true; // no exception
          }
          catch (IOException e) {
            LOGGER.warn("rename problem (fallback): {}", e.getMessage()); // NOSONAR
          }
        }
        catch (IOException e) {
          LOGGER.warn("rename problem: {}", e.getMessage()); // NOSONAR
        }
        if (rename) {
          break; // ok it worked, step out
        }
        try {
          LOGGER.debug("rename did not work - sleep a while and try again...");
          Thread.sleep(1000);
        }
        catch (InterruptedException e) { // NOSONAR
          // we will not let the JVM abort the thread here -> just finish the logic without waiting any longer
          LOGGER.warn("I'm so excited - could not sleep");
          break;
        }
      }

      if (!rename) {
        LOGGER.error("Failed to rename file {} to {}", srcFile, destFile);
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, srcFile, "message.renamer.failedrename")); // NOSONAR
        return false;
      }
      else {
        LOGGER.info("Successfully moved file from {} to {}", srcFile, destFile);
        return true;
      }
    }
    return true; // files are equal
  }

  /**
   * copy a file, preserving the attributes, but NOT overwrite it
   * 
   * @param srcFile
   *          the file to be copied
   * @param destFile
   *          the target
   * @return true/false
   * @throws NullPointerException
   *           if source or destination is {@code null}
   * @throws FileExistsException
   *           if the destination file exists
   * @throws IOException
   *           if source or destination is invalid
   * @throws IOException
   *           if an IO error occurs moving the file
   */
  public static boolean copyFileSafe(final Path srcFile, final Path destFile) throws IOException {
    return copyFileSafe(srcFile, destFile, false);
  }

  /**
   * copy a file, preserving the attributes
   *
   * @param srcFile
   *          the file to be copied
   * @param destFile
   *          the target
   * @param overwrite
   *          overwrite the target?
   * @return true/false
   * @throws NullPointerException
   *           if source or destination is {@code null}
   * @throws FileExistsException
   *           if the destination file exists
   * @throws IOException
   *           if source or destination is invalid
   * @throws IOException
   *           if an IO error occurs moving the file
   */
  public static boolean copyFileSafe(final Path srcFile, final Path destFile, boolean overwrite) throws IOException {
    if (srcFile == null) {
      throw new NullPointerException("Source must not be null");
    }
    if (destFile == null) {
      throw new NullPointerException("Destination must not be null");
    }
    if (!srcFile.toAbsolutePath().toString().equals(destFile.toAbsolutePath().toString())) {
      LOGGER.debug("try to copy file {} to {}", srcFile, destFile);
      if (!Files.exists(srcFile)) {
        throw new FileNotFoundException("Source '" + srcFile + "' does not exist");
      }
      if (Files.isDirectory(srcFile)) {
        throw new IOException("Source '" + srcFile + "' is a directory");
      }
      if (!overwrite) {
        if (Files.exists(destFile) && !Files.isSameFile(destFile, srcFile)) {
          // extra check for windows, where the File.equals is case insensitive
          // so we know now, that the File is the same, but the absolute name does not match
          throw new FileExistsException("Destination '" + destFile + "' already exists");
        }
      }
      if (Files.isDirectory(destFile)) {
        throw new IOException("Destination '" + destFile + "' is a directory");
      }

      // rename folder; try 5 times and wait a sec
      boolean rename = false;
      for (int i = 0; i < 5; i++) {
        try {
          // replace existing for changing cASE
          Files.copy(srcFile, destFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
          rename = true;// no exception
        }
        catch (UnsupportedOperationException u) {
          // maybe copy with attributes does not work here (across file systems), just try without file attributes
          try {
            // replace existing for changing cASE
            Files.copy(srcFile, destFile, StandardCopyOption.REPLACE_EXISTING);
            rename = true;// no exception
          }
          catch (IOException e) {
            LOGGER.warn("copy did not work (fallback): {}", e.getMessage());
          }
        }
        catch (IOException e) {
          LOGGER.warn("copy did not work: {}", e.getMessage());
        }

        if (rename) {
          break; // ok it worked, step out
        }
        try {
          LOGGER.debug("copy did not work - sleep a while and try again..."); // NOSONAR
          Thread.sleep(1000);
        }
        catch (InterruptedException e) { // NOSONAR
          // we will not let the JVM abort the thread here -> just finish the logic without waiting any longer
          LOGGER.warn("I'm so excited - could not sleep"); // NOSONAR
          break;
        }
      }

      if (!rename) {
        LOGGER.error("Failed to copy file {} to {}", srcFile, destFile);
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, srcFile, "message.renamer.failedrename")); // NOSONAR
        return false;
      }
      else {
        LOGGER.info("Successfully copied file from {} to {}", srcFile, destFile);
        return true;
      }
    }
    return true; // files are equal
  }

  /**
   * <b>PHYSICALLY</b> deletes a file by moving it to datasource backup folder<br>
   * DS\.backup\&lt;filename&gt;<br>
   * maintaining its originating directory
   * 
   * @param file
   *          the file to be deleted
   * @param datasource
   *          the data source (for the location of the backup folder)
   * @return true/false if successful
   */
  public static boolean deleteFileWithBackup(Path file, String datasource) {
    Path ds = Paths.get(datasource);

    if (!file.startsWith(ds)) { // safety
      LOGGER.warn("could not delete file '{}': datasource '{}' does not match", file, datasource);
      return false;
    }
    if (Files.isDirectory(file)) {
      LOGGER.warn("could not delete file '{}': file is a directory!", file);
      return false;
    }

    // check if the file exists; if it does not exist any more we won't need to delete it ;)
    if (!Files.exists(file)) {
      // this file is no more here - just return "true"
      return true;
    }

    // backup
    try {
      // create path
      Path backup = Paths.get(ds.toAbsolutePath().toString(), Constants.BACKUP_FOLDER, ds.relativize(file).toString());
      if (!Files.exists(backup.getParent())) {
        Files.createDirectories(backup.getParent());
      }
      // overwrite backup file by deletion prior
      Files.deleteIfExists(backup);
      return moveFileSafe(file, backup);
    }
    catch (IOException e) {
      LOGGER.warn("Could not delete file: {}", e.getMessage());
      return false;
    }
  }

  /**
   * <b>PHYSICALLY</b> deletes a file (w/o backup)<br>
   * only doing a check if it is not a directory
   * 
   * @param file
   *          the file to be deleted
   * @return true/false if successful
   */
  public static boolean deleteFileSafely(Path file) {
    file = file.toAbsolutePath();
    if (Files.isDirectory(file)) {
      LOGGER.warn("Will not delete file '{}': file is a directory!", file);
      return false;
    }
    try {
      Files.deleteIfExists(file);
    }
    catch (Exception e) {
      LOGGER.warn("Could not delete file: {}", e.getMessage());
      return false;
    }
    return true;
  }

  /**
   * <b>PHYSICALLY</b> deletes a complete directory by moving it to datasource backup folder<br>
   * DS\.backup\&lt;foldername&gt;<br>
   * maintaining its originating directory
   * 
   * @param folder
   *          the folder to be deleted
   * @param datasource
   *          the datasource of this folder
   * @return true/false if successful
   */
  public static boolean deleteDirectorySafely(Path folder, String datasource) {
    folder = folder.toAbsolutePath();
    Path ds = Paths.get(datasource);

    if (!Files.isDirectory(folder)) {
      LOGGER.warn("Will not delete folder '{}': folder is a file, NOT a directory!", folder);
      return false;
    }
    if (!folder.startsWith(ds)) { // safety
      LOGGER.warn("Will not delete folder '{}': datasource '{}' does not match", folder, datasource);
      return false;
    }

    // backup
    try {
      Instant instant = Instant.now();
      long timeStampSeconds = instant.getEpochSecond();
      // create path
      Path backup = Paths.get(ds.toAbsolutePath().toString(), Constants.BACKUP_FOLDER, ds.relativize(folder).toString() + timeStampSeconds);
      if (!Files.exists(backup.getParent())) {
        Files.createDirectories(backup.getParent());
      }
      // overwrite backup file by deletion prior
      // deleteDirectoryRecursive(backup); // we timestamped our folder - no need for it
      return moveDirectorySafe(folder, backup);
    }
    catch (IOException e) {
      LOGGER.warn("could not delete directory: {}", e.getMessage());
      return false;
    }
  }

  /**
   * returns a list of all available GUI languages
   * 
   * @return List of Locales
   */
  public static List<Locale> getLanguages() {
    if (!availableLocales.isEmpty()) {
      // do not return the original list to avoid external manipulation
      return new ArrayList<>(availableLocales);
    }

    availableLocales.add(getLocaleFromLanguage(Locale.ENGLISH.getLanguage()));
    try {
      // list all properties files from the classpath
      InputStream is = Utils.class.getResourceAsStream("/");
      if (is != null) {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String resource;
        while ((resource = br.readLine()) != null) {
          parseLocaleFromFilename(resource);
        }
      }

      if (availableLocales.size() == 1) {
        // we may be in a .jar file
        CodeSource src = Utils.class.getProtectionDomain().getCodeSource();
        if (src != null) {
          URL jar = src.getLocation();
          try (InputStream jarInputStream = jar.openStream(); ZipInputStream zip = new ZipInputStream(jarInputStream)) {
            while (true) {
              ZipEntry e = zip.getNextEntry();
              if (e == null) {
                break;
              }
              parseLocaleFromFilename(e.getName());
            }
          }
        }
      }
    }
    catch (Exception e) {
      LOGGER.warn("could not read locales: " + e.getMessage(), e);
    }

    // do not return the original list to avoid external manipulation
    return new ArrayList<>(availableLocales);
  }

  private static void parseLocaleFromFilename(String filename) {
    Matcher matcher = localePattern.matcher(filename);
    if (matcher.matches()) {
      Locale myloc;

      String language = matcher.group(1);
      String country = matcher.group(2);

      if (country != null) {
        // found language & country
        myloc = new Locale(language, country);
      }
      else {
        // found only language
        myloc = getLocaleFromLanguage(language);
      }
      if (myloc != null && !availableLocales.contains(myloc)) {
        availableLocales.add(myloc);
      }
    }
  }

  /**
   * Gets a correct Locale (language + country) from given language.
   * 
   * @param language
   *          as 2char
   * @return Locale
   */
  public static Locale getLocaleFromLanguage(String language) {
    if (StringUtils.isBlank(language)) {
      return Locale.getDefault();
    }
    // do we have a newer locale settings style?
    if (language.length() > 2) {
      try {
        return LocaleUtils.toLocale(language);
      }
      catch (Exception e) {
        // Whoopsie. try to fix string....
        if (language.matches("^\\w\\w_\\w\\w.*")) {
          return LocaleUtils.toLocale(language.substring(0, 5));
        }
      }
    }
    if (language.equalsIgnoreCase("en")) {
      return Locale.US; // don't mess around; at least fixate this
    }

    // try to find country based locale
    Locale l = null;
    List<Locale> countries = LocaleUtils.countriesByLanguage(language.toLowerCase(Locale.ROOT));
    for (Locale locale : countries) {
      if (locale.getCountry().equalsIgnoreCase(language)) {
        // map to main countries; de->de_DE (and not de_CH)
        l = locale;
      }
    }
    if (l == null && !countries.isEmpty()) {
      // well, take the first one
      l = countries.get(0);
    }

    if (l == null) {
      l = new Locale(language); // let java decide..?
    }
    return l;
  }

  /**
   * creates a zipped backup of file in backup folder with yyyy-MM-dd timestamp<br>
   * <b>does overwrite already existing file from today!</b>
   * 
   * @param file
   *          the file to backup
   */
  public static void createBackupFile(Path file) {
    createBackupFile(file, true);
  }

  /**
   * creates a zipped backup of file in backup folder with yyyy-MM-dd timestamp
   * 
   * @param file
   *          the file to backup
   * @param overwrite
   *          if file is already there, ignore that and overwrite with new copy
   */
  public static void createBackupFile(Path file, boolean overwrite) {
    Path backup = Paths.get(Globals.BACKUP_FOLDER);
    try {
      if (!Files.exists(backup)) {
        Files.createDirectory(backup);
      }
      if (!Files.exists(file)) {
        return;
      }
      DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
      String date = formatter.format(new Date());
      backup = backup.resolve(file.getFileName() + "." + date + ".zip");
      if (!Files.exists(backup) || overwrite) {
        createZip(backup, file, "/" + file.getFileName().toString()); // just put in main dir
      }
    }
    catch (IOException e) {
      LOGGER.error("Could not backup file {}: {}", file, e.getMessage());
    }

  }

  /**
   * Deletes old backup files in backup folder; keep only last X files
   * 
   * @param file
   *          the file of backup to be deleted
   * @param keep
   *          keep last X versions
   */
  public static void deleteOldBackupFile(Path file, int keep) {
    ArrayList<Path> al = new ArrayList<>();
    String fname = file.getFileName().toString();
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(Globals.BACKUP_FOLDER))) {
      for (Path path : directoryStream) {
        if (path.getFileName().toString().matches(fname + "\\.\\d{4}\\-\\d{2}\\-\\d{2}\\.zip") || // name.ext.yyyy-mm-dd.zip
            path.getFileName().toString().matches(fname + "\\.\\d{4}\\-\\d{2}\\-\\d{2}")) { // old name.ext.yyyy-mm-dd
          al.add(path);
        }
      }
    }
    catch (IOException e) {
      LOGGER.error("could not list files from the backup folder: {}", e.getMessage());
      return;
    }

    // sort files by creation date
    al.sort((o1, o2) -> (int) (o1.toFile().lastModified() - o2.toFile().lastModified()));

    for (int i = 0; i < al.size() - keep; i++) {
      Path backupFile = al.get(i);
      LOGGER.debug("deleting old backup file {}", backupFile.getFileName());
      deleteFileSafely(backupFile);
    }

  }

  /**
   * Sends a wake-on-lan packet for specified MAC address across subnet
   * 
   * @param macAddr
   *          the mac address to 'wake up'
   */
  public static void sendWakeOnLanPacket(String macAddr) {
    // Broadcast IP address
    final String IP = "255.255.255.255";
    final int port = 7;

    try {
      final byte[] MACBYTE = new byte[6];
      final String[] hex = macAddr.split("(\\:|\\-)");

      for (int i = 0; i < 6; i++) {
        MACBYTE[i] = (byte) Integer.parseInt(hex[i], 16);
      }
      final byte[] bytes = new byte[6 + 16 * MACBYTE.length];
      for (int i = 0; i < 6; i++) {
        bytes[i] = (byte) 0xff;
      }
      for (int i = 6; i < bytes.length; i += MACBYTE.length) {
        System.arraycopy(MACBYTE, 0, bytes, i, MACBYTE.length);
      }

      // Send UDP packet here
      final InetAddress address = InetAddress.getByName(IP);
      final DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);
      try (DatagramSocket socket = new DatagramSocket()) {
        socket.send(packet);
      }

      LOGGER.info("Sent WOL packet to {}", macAddr);
    }
    catch (final Exception e) {
      LOGGER.error("Error sending WOL packet to {} - {}", macAddr, e.getMessage());
    }
  }

  /**
   * create a ProcessBuilder for restarting TMM
   * 
   * @return the process builder
   */
  public static ProcessBuilder getPBforTMMrestart() {
    Path f = Paths.get("tmm.jar");
    if (!Files.exists(f)) {
      LOGGER.error("cannot restart TMM - tmm.jar not found.");
      return null; // when we are in GIT, return null = normal close
    }
    List<String> arguments = getJVMArguments();
    arguments.add(0, LaunchUtil.getJVMPath()); // java exe before JVM args
    arguments.add("-Dsilent=noupdate"); // start GD.jar instead of TMM.jar, since we don't have the libs in manifest
    arguments.add("-jar");
    arguments.add("getdown.jar"); // NOSONAR
    arguments.add(".");
    ProcessBuilder pb = new ProcessBuilder(arguments);
    pb.directory(Paths.get("").toAbsolutePath().toFile()); // set working directory (current TMM dir)
    return pb;
  }

  /**
   * create a ProcessBuilder for restarting TMM to the updater
   * 
   * @return the process builder
   */
  public static ProcessBuilder getPBforTMMupdate() {
    Path f = Paths.get("getdown.jar");
    if (!Files.exists(f)) {
      LOGGER.error("cannot start updater - getdown.jar not found.");
      return null; // when we are in GIT, return null = normal close
    }
    List<String> arguments = getJVMArguments();
    arguments.add(0, LaunchUtil.getJVMPath()); // java exe before JVM args
    arguments.add("-jar");
    arguments.add("getdown.jar"); // NOSONAR
    arguments.add(".");
    ProcessBuilder pb = new ProcessBuilder(arguments);
    pb.directory(Paths.get("").toAbsolutePath().toFile()); // set working directory (current TMM dir)
    return pb;
  }

  /**
   * gets all the JVM parameters used for starting TMM<br>
   * like -Dfile.encoding=UTF8 or others<br>
   * needed for restarting tmm :)
   * 
   * @return list of jvm parameters
   */
  private static List<String> getJVMArguments() {
    RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
    List<String> arguments = new ArrayList<>(runtimeMxBean.getInputArguments());
    // fixtate some
    if (!arguments.contains("-Djava.net.preferIPv4Stack=true")) {
      arguments.add("-Djava.net.preferIPv4Stack=true");
    }
    if (!arguments.contains("-Dfile.encoding=UTF-8")) {
      arguments.add("-Dfile.encoding=UTF-8");
    }
    return arguments;
  }

  /**
   * Deletes a complete directory recursively, using Java NIO
   * 
   * @param dir
   *          directory to delete
   * @throws IOException
   */
  public static void deleteDirectoryRecursive(Path dir) throws IOException {
    if (!Files.exists(dir) || !Files.isDirectory(dir)) {
      return;
    }

    LOGGER.info("Deleting complete directory: {}", dir);
    Files.walkFileTree(dir, new FileVisitor<Path>() {

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(Path file, IOException exc) {
        LOGGER.warn("Could not delete {} - {}", file, exc.getMessage());
        return FileVisitResult.CONTINUE;
      }

    });
  }

  /**
   * Deletes a complete directory recursively, but checking if empty (from inside out) - using Java NIO
   *
   * @param dir
   *          directory to delete
   * @throws IOException
   */
  public static void deleteEmptyDirectoryRecursive(Path dir) throws IOException {
    if (!Files.exists(dir) || !Files.isDirectory(dir)) {
      return;
    }

    LOGGER.info("Deleting complete directory: {}", dir);
    Files.walkFileTree(dir, new FileVisitor<Path>() {

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (isFolderEmpty(dir)) {
          Files.delete(dir);
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return FileVisitResult.CONTINUE;
      }
    });
  }

  /**
   * check whether a folder is empty or not
   * 
   * @param folder
   *          the folder to be checked
   * @return true/false
   * @throws IOException
   */
  public static boolean isFolderEmpty(final Path folder) throws IOException {
    try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(folder)) {
      return !dirStream.iterator().hasNext();
    }
  }

  /**
   * Creates (or adds) a file to a ZIP
   * 
   * @param zipFile
   *          Path of zip file
   * @param toBeAdded
   *          Path to be added
   * @param internalPath
   *          the location inside the ZIP like /aa/a.txt
   */
  public static void createZip(Path zipFile, Path toBeAdded, String internalPath) {
    Map<String, String> env = new HashMap<>();
    try {
      // check if file exists
      env.put("create", String.valueOf(!Files.exists(zipFile)));
      // and use temp files rather than everything in memory
      env.put("useTempFile", "true");

      // use a Zip filesystem URI
      URI fileUri = zipFile.toUri(); // here
      URI zipUri = new URI("jar:" + fileUri.getScheme(), fileUri.getPath(), null);

      // zip
      // try with resource
      try (FileSystem zipfs = FileSystems.newFileSystem(zipUri, env)) {
        // Create internal path in the zipfs
        Path internalTargetPath = zipfs.getPath(internalPath);
        if (!Files.exists(internalTargetPath)) {
          // Create directory
          Files.createDirectory(internalTargetPath);
        }
        // copy a file into the zip file
        if (Files.isDirectory(toBeAdded)) {
          try (Stream<Path> files = Files.walk(toBeAdded)) {
            files.forEach(source -> {
              try {
                if (Files.isSameFile(source, toBeAdded)) {
                  return;
                }

                if (Files.isDirectory(source)) {
                  // Create directory
                  Files.createDirectory(internalTargetPath.resolve(toBeAdded.relativize(source)));
                }
                else {
                  Files.copy(source, internalTargetPath.resolve(toBeAdded.relativize(source).toString()), StandardCopyOption.REPLACE_EXISTING);
                }
              }
              catch (Exception e) {
                LOGGER.error("Failed to create zip file: {}", e.getMessage()); // NOSONAR
              }
            });
          }
        }
        else {
          Files.copy(toBeAdded, internalTargetPath, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }
    catch (Exception e) {
      LOGGER.error("Failed to create zip file: {}", e.getMessage()); // NOSONAR
    }
  }

  /**
   * Unzips the specified zip file to the specified destination directory. Replaces any files in the destination, if they already exist.
   * 
   * @param zipFile
   *          the name of the zip file to extract
   * @param destDir
   *          the directory to unzip to
   * @throws IOException
   */
  public static void unzip(Path zipFile, final Path destDir) {
    Map<String, String> env = new HashMap<>();

    try {
      // if the destination doesn't exist, create it
      if (!Files.exists(destDir)) {
        Files.createDirectories(destDir);
      }

      // check if file exists
      env.put("create", String.valueOf(!Files.exists(zipFile)));
      // use a Zip filesystem URI
      URI fileUri = zipFile.toUri(); // here
      URI zipUri = new URI("jar:" + fileUri.getScheme(), fileUri.getPath(), null);

      try (FileSystem zipfs = FileSystems.newFileSystem(zipUri, env)) {
        final Path root = zipfs.getPath("/");

        // walk the zip file tree and copy files to the destination
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            final Path destFile = Paths.get(destDir.toString(), file.toString());
            LOGGER.debug("Extracting file {} to {}", file, destFile);
            Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            final Path dirToCreate = Paths.get(destDir.toString(), dir.toString());
            if (!Files.exists(dirToCreate)) {
              LOGGER.debug("Creating directory {}", dirToCreate);
              Files.createDirectory(dirToCreate);
            }
            return FileVisitResult.CONTINUE;
          }
        });
      }
    }
    catch (Exception e) {
      LOGGER.error("Failed to create zip file: {}", e.getMessage()); // NOSONAR
    }
  }

  /**
   * extract our templates (only if non existing)
   */
  public static void extractTemplates() {
    extractTemplates(false);
  }

  /**
   * extract our templates (use force to overwrite)
   */
  public static void extractTemplates(boolean force) {
    Path dest = Paths.get("templates");
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dest)) {
      // count the directory amount if not forced
      int dirCount = 0;
      if (!force) {
        for (Path path : directoryStream) {
          if (Files.isDirectory(path)) {
            dirCount++;
          }
        }
      }

      // if forced or the directory count is zero, we will extract the templates.zip
      if (dirCount == 0 || force) {
        Utils.unzip(dest.resolve("templates.jar"), dest);
      }
    }
    catch (IOException e) {
      LOGGER.warn("failed to extract templates: {}", e.getMessage());
    }
  }

  /**
   * Java NIO replacement of commons-io
   * 
   * @param file
   *          the file to write the string to
   * @param text
   *          the text to be written into the file
   * @throws IOException
   *           any {@link IOException} thrown
   */
  public static void writeStringToFile(Path file, String text) throws IOException {
    byte[] buf = text.getBytes(StandardCharsets.UTF_8);
    Files.write(file, buf);
  }

  /**
   * Java NIO replacement of commons-io
   * 
   * @param file
   *          the file to read the string from
   * @return the read string
   * @throws IOException
   *           any {@link IOException} thrown
   */
  public static String readFileToString(Path file) throws IOException {
    byte[] fileArray = Files.readAllBytes(file);
    return new String(fileArray, StandardCharsets.UTF_8);
  }

  /**
   * Copies a complete directory recursively, using Java NIO
   * 
   * @param from
   *          source
   * @param to
   *          destination
   * @throws IOException
   *           any {@link IOException} thrown
   */
  public static void copyDirectoryRecursive(Path from, Path to) throws IOException {
    LOGGER.info("Copyin complete directory from {} to {}", from, to);
    Files.walkFileTree(from, new CopyFileVisitor(to));
  }

  /**
   * logback does not clean older log files than 32 days in the past. We have to clean the log files too
   */
  public static void cleanOldLogs() {
    Pattern pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, -30);
    Date dateBefore30Days = cal.getTime();

    // the log file pattern is logs/tmm.%d.%i.log.gz
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get("logs"))) {
      for (Path path : directoryStream) {
        Matcher matcher = pattern.matcher(path.getFileName().toString());
        if (matcher.find()) {
          try {
            Date date = StrgUtils.parseDate(matcher.group());
            if (dateBefore30Days.after(date)) {
              Utils.deleteFileSafely(path);
            }
          }
          catch (Exception e) {
            LOGGER.debug("could not clean old logs: {}", e.getMessage());
          }
        }
      }
    }
    catch (IOException e) {
      LOGGER.debug("could not clean old logs: {}", e.getMessage());
    }
  }

  public static String getArtworkExtension(String url) {
    String ext = FilenameUtils.getExtension(url);
    if (StringUtils.isBlank(ext) || "tbn".equals(ext)) {
      // no extension or tbn? fall back to jpg
      ext = "jpg";
    }

    return ext.toLowerCase(Locale.ROOT);
  }

  /**
   * get all files from the given path
   *
   * @param root
   *          the root folder to search files for
   * @return a list of all found files
   */
  public static List<Path> listFiles(Path root) {
    final List<Path> filesFound = new ArrayList<>();
    if (!Files.isDirectory(root)) {
      return filesFound;
    }
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(root)) {
      for (Path path : directoryStream) {
        if (Files.isRegularFile(path)) {
          filesFound.add(path);
        }
      }
    }
    catch (IOException e) {
      LOGGER.warn("could not get a file listing: {}", e.getMessage());
    }

    return filesFound;
  }

  /**
   * get all files from the given path recursive
   *
   * @param root
   *          the root folder to search files for
   * @return a list of all found files
   */
  public static List<Path> listFilesRecursive(Path root) {
    final List<Path> filesFound = new ArrayList<>();
    if (!Files.isDirectory(root)) {
      return filesFound;
    }
    try {
      Files.walkFileTree(root, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new AbstractFileVisitor() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
          if (Utils.isRegularFile(file)) {
            filesFound.add(file);
          }
          return FileVisitResult.CONTINUE;
        }
      });
    }
    catch (Exception e) {
      LOGGER.warn("could not get a file listing: {}", e.getMessage());
    }

    return filesFound;
  }

  /**
   * flush the {@link FileOutputStream} to the disk
   */
  public static void flushFileOutputStreamToDisk(FileOutputStream fileOutputStream) {
    if (fileOutputStream == null) {
      return;
    }

    try {
      fileOutputStream.flush();
      fileOutputStream.getFD().sync(); // wait until file has been completely written
      // give it a few milliseconds
      Thread.sleep(150);
    }
    catch (Exception e) {
      LOGGER.error("could not flush to disk: {}", e.getMessage());
    }
  }

  /**
   * Visitor for copying a directory recursively<br>
   * Usage: Files.walkFileTree(sourcePath, new CopyFileVisitor(targetPath));
   */
  public static class CopyFileVisitor extends SimpleFileVisitor<Path> {
    private final Path targetPath;
    private Path       sourcePath = null;

    public CopyFileVisitor(Path targetPath) {
      this.targetPath = targetPath;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
      if (sourcePath == null) {
        sourcePath = dir;
      }
      Path target = targetPath.resolve(sourcePath.relativize(dir));
      if (!Files.exists(target)) {
        try {
          Files.createDirectories(target);
        }
        catch (FileAlreadyExistsException e) {
          // ignore
        }
        catch (IOException x) {
          return FileVisitResult.SKIP_SUBTREE;
        }
      }
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
      Files.copy(file, targetPath.resolve(sourcePath.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
      return FileVisitResult.CONTINUE;
    }
  }

  /**
   * get the temporary folder for this tmm instance
   * 
   * @return a string to the temporary folder
   */
  public static String getTempFolder() {
    return tempFolder;
  }

  /**
   * Method to get a list of files with the given regular expression
   *
   * @param regexList
   *          list of regular expression
   * @return a list of files
   */
  public static HashSet<Path> getUnknownFilesByRegex(Path folder, List<String> regexList) {

    GetUnknownFilesVisitor visitor = new GetUnknownFilesVisitor(regexList);

    try {
      Files.walkFileTree(folder, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, visitor);
    }
    catch (IOException e) {
      LOGGER.error("could not get unknown files: {}", e.getMessage());
    }

    return visitor.fileList;
  }

  private static class GetUnknownFilesVisitor extends AbstractFileVisitor {

    private HashSet<Path> fileList = new HashSet<>();
    private List<String>  regexList;

    GetUnknownFilesVisitor(List<String> regexList) {
      this.regexList = regexList;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {

      for (String regex : regexList) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(file.getFileName().toString());
        if (m.find()) {
          fileList.add(file);
        }
      }
      return CONTINUE;
    }
  }
}
