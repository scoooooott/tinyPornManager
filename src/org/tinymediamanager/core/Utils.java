/*
 * Copyright 2012 - 2014 Manuel Laggner
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.LaunchUtil;
import org.tinymediamanager.ReleaseInfo;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.scraper.util.StrgUtils;
import org.tinymediamanager.scraper.util.Url;

/**
 * The Class Utils.
 * 
 * @author Manuel Laggner / Myron Boyle
 */
public class Utils {
  private static final Logger                       LOGGER            = LoggerFactory.getLogger(Utils.class);

  /**
   * Map of all known language/country/abbreviations, key is LOWERCASE
   */
  public static final LinkedHashMap<String, Locale> KEY_TO_LOCALE_MAP = generateSubtitleLanguageArray();

  private static LinkedHashMap<String, Locale> generateSubtitleLanguageArray() {
    Map<String, Locale> langArray = new HashMap<String, Locale>();

    Locale intl = new Locale("en");
    Locale locales[] = Locale.getAvailableLocales();
    // all possible variants of language/country/prefixes/non-iso style
    for (Locale locale : locales) {
      langArray.put(locale.getDisplayLanguage(intl), locale);
      langArray.put(locale.getDisplayLanguage(), locale);
      try {
        langArray.put(locale.getDisplayLanguage(intl).substring(0, 3), locale); // eg German -> Ger, where iso3=deu
      }
      catch (Exception e) {
        // ignore
      }
      langArray.put(locale.getISO3Language(), locale);
      langArray.put(locale.getCountry(), locale);
      try {
        String c = locale.getISO3Country();
        langArray.put(c, locale);
      }
      catch (MissingResourceException e) {
        // tjo... not available, see javadoc
      }
    }
    for (String l : Locale.getISOLanguages()) {
      langArray.put(l, new Locale(l));
    }

    // sort
    List<String> keys = new LinkedList<String>(langArray.keySet());
    Collections.sort(keys, new Comparator<String>() {
      @Override
      public int compare(String s1, String s2) {
        return s2.length() - s1.length();
      }
    });
    LinkedHashMap<String, Locale> sortedMap = new LinkedHashMap<String, Locale>();
    for (String key : keys) {
      if (!key.isEmpty()) {
        sortedMap.put(key.toLowerCase(), langArray.get(key));
      }
    }

    return sortedMap;
  }

  /**
   * returns the relative path of 2 absolute file paths
   * 
   * @param parent
   *          the directory
   * @param child
   *          the subdirectory
   * @return relative path
   */
  public static String relPath(String parent, String child) {
    return relPath(new File(parent), new File(child));
  }

  /**
   * returns the relative path of 2 absolute file paths
   * 
   * @param parent
   *          the directory
   * @param child
   *          the subdirectory
   * @return relative path
   */
  public static String relPath(File parent, String child) {
    return relPath(parent, new File(child));
  }

  /**
   * returns the relative path of 2 absolute file paths
   * 
   * @param parent
   *          the directory
   * @param child
   *          the subdirectory
   * @return relative path
   */
  public static String relPath(String parent, File child) {
    return relPath(new File(parent), child);
  }

  /**
   * returns the relative path of 2 absolute file paths
   * 
   * @param parent
   *          the directory
   * @param child
   *          the subdirectory
   * @return relative path
   */
  public static String relPath(File parent, File child) {
    return parent.toURI().relativize(child.toURI()).getPath();
  }

  /**
   * gets a locale from specific string
   * 
   * @param text
   * @return Locale or NULL
   */
  public static Locale getLocaleFromCountry(String text) {
    String lang = text.toLowerCase().split("[_.-]")[0];
    return KEY_TO_LOCALE_MAP.get(lang);
  }

  /**
   * gets the localized DisplayLanguage , derived from specific string
   * 
   * @param text
   * @return the displayLanguage or empty string
   */
  public static String getDisplayLanguage(String text) {
    Locale l = getLocaleFromCountry(text);
    if (l == null) {
      return "";
    }
    else {
      // return l.getDisplayLanguage(new Locale("en")); // name in english
      return l.getDisplayLanguage(); // local name
    }
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
    if (title.toLowerCase().matches("^die hard$") || title.toLowerCase().matches("^die hard[:\\s].*")) {
      return title;
    }
    for (String prfx : Settings.getInstance().getTitlePrefix()) {
      title = title.replaceAll("(?i)^" + prfx + " (.*)", "$1, " + prfx);
    }
    return title;
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
      title = title.replaceAll("(?i)(.*), " + prfx, prfx + " $1");
    }
    return title;
  }

  /**
   * Read file as string. DEPRECATED: use FileUtils.readFileToString(file)
   * 
   * @param file
   *          the file
   * @return the string
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @Deprecated
  public static String readFileAsString(File file) throws java.io.IOException {
    StringBuffer fileData = new StringBuffer(1000);
    BufferedReader reader = new BufferedReader(new FileReader(file));
    char[] buf = new char[1024];
    int numRead = 0;
    while ((numRead = reader.read(buf)) != -1) {
      String readData = String.valueOf(buf, 0, numRead);
      fileData.append(readData);
      buf = new char[1024];
    }

    reader.close();
    return fileData.toString();
  }

  /**
   * Clean stacking markers.
   * 
   * @param filename
   *          the filename
   * @return the string
   */
  public static String cleanStackingMarkers(String filename) {
    if (!StringUtils.isEmpty(filename)) {
      return filename.replaceAll("(?i)([\\( _.-]*(cd|dvd|part|pt|dis[ck])([0-9])[\\) _.-]*)", "").trim();
    }
    return filename;
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
      return StrgUtils.substr(filename, "(?i)((cd|dvd|part|pt|dis[ck])([0-9]))");
    }
    return "";
  }

  /**
   * Returns the stacking prefix
   * 
   * @param filename
   *          the filename
   * @return the stacking information
   */
  public static String getStackingPrefix(String filename) {
    return getStackingMarker(filename).replaceAll("[0-9]", "");
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
        try {
          int s = Integer.parseInt(stack.replaceAll("[^0-9]", "")); // remove all non numbers
          return s;
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

    return imdbId.matches("tt\\d{7}");
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
   * Starts a thread and does a "ping" on our tracking server, sending the event (and the random UUID + some env vars).
   * 
   * @param event
   *          The event for the GET request
   */
  public static void trackEvent(final String event) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.currentThread().setName("trackEventThread");
          File uuidFile = new File("tmm.uuid");
          File disable = new File("tmm.uuid.disable");
          if (!uuidFile.exists()) {
            FileUtils.write(uuidFile, UUID.randomUUID().toString());
          }

          if (uuidFile.exists() && !disable.exists()) {
            String uuid = FileUtils.readFileToString(uuidFile);
            System.setProperty("tmm.uuid", uuid);

            // 2013-01-29 10:20:43 | event=startup | os=Windows 7 | arch=amd64 | Java=1.6.0_26 | country=DE
            // String nfo = "&os=" + getEncProp("os.name") + "&arch=" + getEncProp("os.arch") + "&java=" + getEncProp("java.version") + "&lang="
            // + getEncProp("user.language") + "_" + getEncProp("user.country");
            // Url url = new Url("http://tracker.tinymediamanager.org/track.php?uuid=" + uuid + "&event=" + event + nfo);

            // https://developers.google.com/analytics/devguides/collection/protocol/v1/parameters
            // @formatter:off
            String ga = "v=1"
                + "&tid=UA-35564534-5"
                + "&cid=" + uuid 
                + "&an=tinyMediaManager" 
                + "&av=" + ReleaseInfo.getBuild() 
                + "&t=event"
                + "&ec=" + event
                + "&ea=" + event 
                + "&je=1"
                + "&ul=" + getEncProp("user.language") + "-" + getEncProp("user.country")  // use real system language
                + "&vp=" + Globals.settings.getWindowConfig().getInteger("mainWindowW") + "x" + Globals.settings.getWindowConfig().getInteger("mainWindowH")
                + "&sr=" + java.awt.Toolkit.getDefaultToolkit().getScreenSize().width + "x" + java.awt.Toolkit.getDefaultToolkit().getScreenSize().height 
                + "&cd1=" + getEncProp("os.name") 
                + "&cd2=" + getEncProp("os.arch") 
                + "&cd3=" + getEncProp("java.version") 
                + "&z=" + System.currentTimeMillis();
            // @formatter:on
            Url url = new Url("http://www.google-analytics.com/collect?" + ga);

            InputStream in = url.getInputStream();
            in.close();
          }
        }
        catch (Exception e) {
          LOGGER.warn("could not ping our update server...");
        }
      }
    }).start();
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
    try {
      return URLEncoder.encode(System.getProperty(prop), "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      return URLEncoder.encode(System.getProperty(prop));
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
          // int index = Integer.parseInt(matcher.group(1));
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
   * modified version of commons-io FileUtils.moveDirectory();<br>
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
  public static boolean moveDirectorySafe(File srcDir, File destDir) throws IOException {
    // rip-off from
    // http://svn.apache.org/repos/asf/commons/proper/io/trunk/src/main/java/org/apache/commons/io/FileUtils.java
    if (srcDir == null) {
      throw new NullPointerException("Source must not be null");
    }
    if (destDir == null) {
      throw new NullPointerException("Destination must not be null");
    }
    LOGGER.debug("try to move folder " + srcDir.getPath() + " to " + destDir.getPath());
    if (!srcDir.exists()) {
      throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
    }
    if (!srcDir.isDirectory()) {
      throw new IOException("Source '" + srcDir + "' is not a directory");
    }
    if (destDir.exists()) {
      throw new FileExistsException("Destination '" + destDir + "' already exists");
    }
    if (!destDir.getParentFile().exists()) {
      // create parent folder structure, else renameTo does not work
      destDir.getParentFile().mkdirs();
    }

    // rename folder; try 5 times and wait a sec
    boolean rename = false;
    for (int i = 0; i < 5; i++) {
      rename = srcDir.renameTo(destDir);
      if (rename) {
        break; // ok it worked, step out
      }
      try {
        LOGGER.debug("rename did not work - sleep a while and try again...");
        Thread.sleep(1000);
      }
      catch (InterruptedException e) {
        LOGGER.warn("I'm so excited - could not sleep");
      }
    }

    // ok, we tried it 5 times - it still seems to be locked somehow. Continue
    // with copying as fallback
    // NOOO - we don't like to have some files copied and some not.

    if (!rename) {
      LOGGER.error("Failed to rename directory '" + srcDir + " to " + destDir.getPath());
      LOGGER.error("Movie renaming aborted.");
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, srcDir.getPath(), "message.renamer.failedrename"));
      return false;
    }
    else {
      LOGGER.info("Successfully moved folder " + srcDir.getPath() + " to " + destDir.getPath());
      return true;
    }
  }

  /**
   * modified version of commons-io FileUtils.moveFile();<br>
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
   * @since 1.4
   */
  public static boolean moveFileSafe(final File srcFile, final File destFile) throws IOException {
    if (srcFile == null) {
      throw new NullPointerException("Source must not be null");
    }
    if (destFile == null) {
      throw new NullPointerException("Destination must not be null");
    }
    if (!srcFile.equals(destFile)) {
      LOGGER.debug("try to move file " + srcFile.getPath() + " to " + destFile.getPath());
      if (!srcFile.exists()) {
        throw new FileNotFoundException("Source '" + srcFile + "' does not exist");
      }
      if (srcFile.isDirectory()) {
        throw new IOException("Source '" + srcFile + "' is a directory");
      }
      if (destFile.exists()) {
        throw new FileExistsException("Destination '" + destFile + "' already exists");
      }
      if (destFile.isDirectory()) {
        throw new IOException("Destination '" + destFile + "' is a directory");
      }

      // rename folder; try 5 times and wait a sec
      boolean rename = false;
      for (int i = 0; i < 5; i++) {
        rename = srcFile.renameTo(destFile);
        if (rename) {
          break; // ok it worked, step out
        }
        try {
          LOGGER.debug("rename did not work - sleep a while and try again...");
          Thread.sleep(1000);
        }
        catch (InterruptedException e) {
          LOGGER.warn("I'm so excited - could not sleep");
        }
      }

      // ok, we tried it 5 times - it still seems to be locked somehow. Continue
      // with copying as fallback
      // NOOO - we don't like to have some files copied and some not.

      if (!rename) {
        LOGGER.error("Failed to rename file '" + srcFile + " to " + destFile.getPath());
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, srcFile.getPath(), "message.renamer.failedrename"));
        return false;
      }
      else {
        LOGGER.info("Successfully moved file from " + srcFile.getPath() + " to " + destFile.getPath());
        return true;
      }
    }
    return true; // files are equal
  }

  /**
   * returns a list of all available GUI languages
   * 
   * @return List of Locales
   */
  public static List<Locale> getLanguages() {
    ArrayList<Locale> loc = new ArrayList<Locale>();
    loc.add(getLocaleFromLanguage(Locale.ENGLISH.getLanguage()));
    try {
      File[] props = new File("locale").listFiles();
      for (File file : props) {
        String l = file.getName().substring(9, 11); // messages_XX.properties
        Locale myloc = getLocaleFromLanguage(l);
        if (myloc != null && !loc.contains(myloc)) {
          loc.add(myloc);
        }
      }
    }
    catch (Exception e) {
      // do nothing
    }
    return loc;
  }

  /**
   * Gets a correct Locale (language + country) from given language.
   * 
   * @param language
   *          as 2char
   * @return Locale
   */
  public static Locale getLocaleFromLanguage(String language) {
    if (language == null || language.isEmpty()) {
      return null;
    }
    if (language.equalsIgnoreCase("en")) {
      return new Locale("en", "US"); // don't mess around; at least fixtate this
    }
    Locale l = null;
    List<Locale> countries = LocaleUtils.countriesByLanguage(language.toLowerCase());
    for (Locale locale : countries) {
      if (locale.getCountry().equalsIgnoreCase(language)) {
        // map to main countries; de->de_DE (and not de_CH)
        l = locale;
      }
    }
    if (l == null && countries != null && countries.size() > 0) {
      // well, take the first one
      l = countries.get(0);
    }

    return l;
  }

  /**
   * creates a backup of file in backup folder with yyyy-MM-dd timestamp<br>
   * <b>does not overwrite already existing file from today!</b>
   * 
   * @param f
   *          the file to backup
   */
  public static final void createBackupFile(File f) {
    createBackupFile(f, false);
  }

  /**
   * creates a backup of file in backup folder with yyyy-MM-dd timestamp
   * 
   * @param f
   *          the file to backup
   * @param overwrite
   *          if file is already there, ignore that and overwrite with new copy
   */
  public static final void createBackupFile(File f, boolean overwrite) {
    File backup = new File("backup");
    if (!backup.exists()) {
      backup.mkdir();
    }
    if (!f.exists()) {
      return;
    }
    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    String date = formatter.format(f.lastModified());
    backup = new File("backup", f.getName() + "." + date);
    if (!backup.exists() || overwrite == true) {
      try {
        FileUtils.copyFile(f, backup, true);
      }
      catch (IOException e) {
        LOGGER.error("Could not backup file " + backup);
      }
    }
  }

  /**
   * Deletes old backup files in backup folder; keep only last X files
   * 
   * @param f
   *          the file of backup to be deleted
   * @param keep
   *          keep last X versions
   */
  public static final void deleteOldBackupFile(File f, int keep) {
    File[] files = new File("backup").listFiles();
    if (files == null) {
      return;
    }
    ArrayList<File> al = new ArrayList<File>();
    for (File s : files) {
      if (s.getName().matches(f.getName() + "\\.\\d{4}\\-\\d{2}\\-\\d{2}")) { // name.yyyy-mm-dd
        al.add(s);
      }
    }
    for (int i = 0; i < al.size() - keep; i++) {
      // System.out.println("del " + al.get(i).getName());
      FileUtils.deleteQuietly(al.get(i));
    }
  }

  /**
   * Sends a wake-on-lan packet for specified MAC address across subnet
   * 
   * @param macAddr
   *          the mac address to 'wake up'
   */
  public static final void sendWakeOnLanPacket(String macAddr) {
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
      final DatagramSocket socket = new DatagramSocket();
      socket.send(packet);
      socket.close();

      LOGGER.info("Sent WOL packet to " + macAddr);
    }
    catch (final Exception e) {
      LOGGER.error("Error sending WOL packet to " + macAddr, e);
    }
  }

  /**
   * Converts milliseconds to HH:MM:SS
   * 
   * @param msec
   * @return formatted time
   */
  public static String MSECtoHHMMSS(long msec) {
    if (msec == 0) {
      return "";
    }
    long sec = msec / 1000; // sec
    int seconds = (int) sec % 60;
    int minutes = (int) (sec / 60) % 60;
    int hours = (int) (sec / (60 * 60)) % 24;
    return String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
  }

  /**
   * create a ProcessBuilder for restarting TMM
   * 
   * @return the process builder
   */
  public static ProcessBuilder getPBforTMMrestart() {
    File f = new File("tmm.jar");
    if (!f.exists()) {
      LOGGER.error("cannot restart TMM - tmm.jar not found.");
      return null; // when we are in SVN, return null = normal close
    }
    List<String> arguments = getJVMArguments();
    arguments.add(0, LaunchUtil.getJVMPath()); // java exe before JVM args
    arguments.add("-jar");
    arguments.add("tmm.jar");
    ProcessBuilder pb = new ProcessBuilder(arguments);
    pb.directory(new File("").getAbsoluteFile()); // set working directory (current TMM dir)
    return pb;
  }

  /**
   * create a ProcessBuilder for restarting TMM to the updater
   * 
   * @return the process builder
   */
  public static ProcessBuilder getPBforTMMupdate() {
    File f = new File("getdown.jar");
    if (!f.exists()) {
      LOGGER.error("cannot start updater - getdown.jar not found.");
      return null; // when we are in SVN, return null = normal close
    }
    List<String> arguments = getJVMArguments();
    arguments.add(0, LaunchUtil.getJVMPath()); // java exe before JVM args
    arguments.add("-jar");
    arguments.add("getdown.jar");
    arguments.add(".");
    ProcessBuilder pb = new ProcessBuilder(arguments);
    pb.directory(new File("").getAbsoluteFile()); // set working directory (current TMM dir)
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
    List<String> arguments = new ArrayList<String>(runtimeMxBean.getInputArguments());
    // fixtate some
    if (!arguments.contains("-Djava.net.preferIPv4Stack=true")) {
      arguments.add("-Djava.net.preferIPv4Stack=true");
    }
    if (!arguments.contains("-Dfile.encoding=UTF-8")) {
      arguments.add("-Dfile.encoding=UTF-8");
    }
    return arguments;
  }

}
