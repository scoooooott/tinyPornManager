package xyz.ifnotnull.tmm.scraper.pornhub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.core.entities.MediaRating;
import org.tinymediamanager.core.entities.MediaTrailer;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaCertification;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.http.ProxySettings;
import org.tinymediamanager.scraper.interfaces.IMovieMetadataProvider;
import org.tinymediamanager.scraper.util.MetadataUtil;
import xyz.ifnotnull.tmm.scraper.pornhub.dto.LdJson;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PornhubMovieMetadataProvider implements IMovieMetadataProvider {
  public static final  String            ID                      = "pornhub";
  public static final  String            API_HOST                = "pornhub.com";
  public static final  String            API_URL                 = "https://" + API_HOST;
  private static final Logger            logger                  = LoggerFactory.getLogger(PornhubMovieMetadataProvider.class);
  private static final Pattern           ADD_DATE_REGEX          = Pattern.compile("^(\\d+)\\s*(\\S+)\\s*(?:ago|前)$");
  private static final Pattern           THUMB_URL_INDEX_PATTERN = Pattern.compile("\\{(\\d+)}");
  private static final String            CONFIG_ID_MATCHER       = "ID Matcher";
  private static final String            CONFIG_ACCOUNT          = "Pornhub Account";
  private static final String            CONFIG_PASSWORD         = "Pornhub Password";
  private final        ObjectMapper      objectMapper            = new ObjectMapper();
  private final        MediaProviderInfo providerInfo;
  private final        Playwright        playwright;
  private final        Browser           browser;

  public PornhubMovieMetadataProvider() {
    providerInfo = createProviderInfo();
    playwright = Playwright.create();
    browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    // 注册关闭钩子
    Runtime.getRuntime().addShutdownHook(new Thread(this::close));
  }

  private MediaProviderInfo createProviderInfo() {
    MediaProviderInfo info = new MediaProviderInfo(ID, "movie", "Pornhub", "Scraper addon for Pornhub",
        PornhubMovieMetadataProvider.class.getResource("/xyz/ifnotnull/tmm/scraper/pornhub/pornhub_logo.svg"));
    // the ResourceBundle to offer i18n support for scraper options
    info.setResourceBundle(ResourceBundle.getBundle("xyz.ifnotnull.tmm.scraper.pornhub.messages"));

    // create configuration properties
    info.getConfig().addText(CONFIG_ID_MATCHER, "^(\\w+?)\\s*[|!@].*", false);
    info.getConfig().addText(CONFIG_ACCOUNT, "", false);
    info.getConfig().addText(CONFIG_PASSWORD, "", true);

    /*info.getConfig().addBoolean("boolean", true);
    info.getConfig().addInteger("integer", 10);
    info.getConfig().addSelect("select", new String[] { "A", "B", "C" }, "A");*/

    // load any existing values from the storage
    info.getConfig().load();

    return info;
  }

  private void close() {
    logger.info("PornhubMovieMetadataProvider closing...");
    // 关闭资源
    browser.close();
    playwright.close();
  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  @Override
  public boolean isActive() {
    return true;
  }

  @Override
  public SortedSet<MediaSearchResult> search(MovieSearchAndScrapeOptions options) throws ScrapeException {
    logger.debug("search(): {}", options);
    SortedSet<MediaSearchResult> results = new TreeSet<>();

    String phId = options.getIdAsString(getId());

    // we hope got an id from options but not
    if (StringUtils.isEmpty(phId)) {
      // try if filename contains an id
      String idMatcher = getProviderInfo().getConfig().getValue(CONFIG_ID_MATCHER);
      Matcher matcher = Pattern.compile(idMatcher).matcher(options.getSearchQuery());
      if (matcher.matches()) {
        phId = matcher.group(1);
      }
    }

    if (StringUtils.isNotEmpty(phId)) {
      options.setId(getId(), phId);
      MediaMetadata metadata = getMetadata(options);
      if (metadata != null) {
        MediaSearchResult result = metadata.toSearchResult(MediaType.MOVIE);
        result.setMetadata(metadata);
        for (MediaArtwork mediaArtwork : metadata.getMediaArt(MediaArtwork.MediaArtworkType.POSTER)) {
          result.setPosterUrl(mediaArtwork.getOriginalUrl());
          break;
        }
        result.setScore(1);
        results.add(result);
        return results;
      }
    }

    // no id yet, search via filename
    String searchString = MetadataUtil.removeNonSearchCharacters(options.getSearchQuery());
    if (StringUtils.isNotEmpty(searchString)) {
      synchronized (browser) {
        try (Page page = browser.newPage(new Browser.NewPageOptions().setLocale(options.getLanguage().getLanguage())
            .setProxy(ProxySettings.INSTANCE.getHost() + ":" + ProxySettings.INSTANCE.getPort()))) {
          page.navigate(API_URL);

          //          Locator searchBar = page.getByPlaceholder("Search Pornhub");
          Locator searchBar = page.locator("#searchInput");
          searchBar.fill(searchString);
          searchBar.press("Enter");

          page.waitForLoadState();

          // find result list element
          List<Locator> searchResults = page.locator("#videoSearchResult")
              .getByRole(AriaRole.LISTITEM)
              .filter(new Locator.FilterOptions().setHas(page.getByRole(AriaRole.IMG)))
              .all();

          for (Locator result : searchResults) {
            Locator img = result.getByRole(AriaRole.IMG);
            String title = img.getAttribute("data-title");

            MediaSearchResult sr = new MediaSearchResult(getId(), MediaType.MOVIE);
            sr.setId(getId(), result.getAttribute("data-video-vkey"));
            sr.setId(getId() + "_id", result.getAttribute("data-video-id"));
            sr.setTitle(title);
            sr.setOriginalTitle(title);

            String addedDate = result.locator(".added").textContent().trim();
            Matcher numRegex = ADD_DATE_REGEX.matcher(addedDate);
            LocalDateTime time = LocalDateTime.now();
            if (numRegex.find()) {
              switch (numRegex.group(2)) {
                case "年":
                case "year":
                case "years":
                  time = time.minusYears(Long.parseLong(numRegex.group(1)));
                  break;
                case "月":
                case "month":
                case "months":
                  time = time.minusMonths(Long.parseLong(numRegex.group(1)));
                  break;
                case "日":
                case "day":
                case "days":
                  time = time.minusDays(Long.parseLong(numRegex.group(1)));
                  break;
                case "小时":
                case "hour":
                case "hours":
                  time = time.minusHours(Long.parseLong(numRegex.group(1)));
                  break;
                case "分钟":
                case "minute":
                case "minutes":
                  time = time.minusMinutes(Long.parseLong(numRegex.group(1)));
                  break;
                default:
                  break;
              }
              sr.setYear(time.getYear());
            }

            // calculate score self
            sr.calculateScore(options);

            sr.setPosterUrl(img.getAttribute("src"));
            sr.setOverview("null for now");
            results.add(sr);
          }
        }
      }
    }

    return results;
  }

  @Override
  public MediaMetadata getMetadata(MovieSearchAndScrapeOptions options) throws ScrapeException {
    logger.debug("getMetadata(): {}", options);
    if (options.getSearchResult() != null && options.getSearchResult().getMediaMetadata() != null && getId().equals(
        options.getSearchResult().getMediaMetadata().getProviderId())) {
      return options.getSearchResult().getMediaMetadata();
    }

    MediaMetadata md = new MediaMetadata(getId());
    md.setScrapeOptions(options);

    String phId = options.getIdAsString(getId());
    if (StringUtils.isEmpty(phId)) {
      throw new MissingIdException(ID);
    }
    synchronized (browser) {
      try (Page page = browser.newPage(new Browser.NewPageOptions().setLocale(options.getLanguage().getLanguage())
          .setProxy(ProxySettings.INSTANCE.getHost() + ":" + ProxySettings.INSTANCE.getPort()))) {
        // 设置页面请求拦截器
        page.onResponse(response -> {
          if (response.status() > 400) {
            logger.warn("Response {}:{}, url: {}", response.status(), response.statusText(), response.url());
            throw new PlaywrightException("response code: " + response.status());
          }
        });
        String url = API_URL + "/" + "view_video.php?viewkey=" + phId;
        page.navigate(url);
        page.waitForLoadState();

        // todo: login logic
        /*page.locator("#headerLoginLink").click();
        page.locator("#topRightProfileMenu > div > a.logIn").click();
        page.locator("#usernameModal").fill(getProviderInfo().getConfig().getValue(CONFIG_ACCOUNT));
        page.locator("#passwordModal").fill(getProviderInfo().getConfig().getValue(CONFIG_PASSWORD));
        page.locator(".rememberMeText").click();
        page.locator("#signinSubmit").click();*/

        md.setId(getId(), options.getIdAsString(getId()));
        md.setId(getId() + "_id", page.locator("#player").getAttribute("data-video-id"));
        try {
          parseLdJson(md, page, options);

          parseFlashvars(md, page, options);

          parseVideoShow(md, page, options);

          parsePageElements(md, page);
        }
        catch (JsonProcessingException e) {
          logger.error("parse error", e);
          return null;
        }
      }
    }

    return md;
  }

  private void parseVideoShow(MediaMetadata md, Page page, MovieSearchAndScrapeOptions options) {
    Map<String, Object> videoShow = (Map<String, Object>) page.evaluate("VIDEO_SHOW");

    md.setTitle((String) videoShow.get("videoTitleTranslated"));
    md.setOriginalTitle((String) videoShow.get("videoTitleOriginal"));
    md.setOriginalLanguage(options.getLanguage().getLanguage());
  }

  private void parseLdJson(MediaMetadata md, Page page, MovieSearchAndScrapeOptions options) throws JsonProcessingException {
    LdJson ldJson = objectMapper.readValue(page.innerHTML("script[type=\"application/ld+json\"]"), LdJson.class);

    if (ldJson == null) {
      return;
    }
    md.setPlot(StringEscapeUtils.unescapeXml(ldJson.getDescription()).replace("&period;", ".").replace("&comma;", ","));

    DateTime uploadDate = DateTime.parse(ldJson.getUploadDate());
    md.setYear(uploadDate.getYear());
    md.setReleaseDate(uploadDate);

    // poster
    String imgUrl = ldJson.getThumbnailUrl();
    MediaArtwork poster = new MediaArtwork(getId(), MediaArtwork.MediaArtworkType.POSTER);
    poster.setLanguage(options.getLanguage().getLanguage());
    poster.setDefaultUrl(imgUrl);
    poster.setPreviewUrl(imgUrl);
    poster.setOriginalUrl(imgUrl);
    poster.setSizeOrder(8);
    md.addMediaArt(poster);
    md.addMediaArt(new MediaArtwork(poster, MediaArtwork.MediaArtworkType.THUMB));

    // extra thumbs
    //    Pattern pattern = Pattern.compile("^(.*)(\\d+)\\.jpg$");
    Pattern pattern = Pattern.compile("(?<=\\D)(\\d+)(?=\\.jpg)");
    // https://ei.phncdn.com/videos/202211/06/418983331/original/(m=qZ6-V2XbeWdTGgaaaa)(mh=ax-Hrw-9ZtGX9IIr)0.jpg 将末尾的0.jpg替换为i.jpg
    Matcher matcher = pattern.matcher(imgUrl);
    if (matcher.find()) {
      for (int i = 0; i <= 16; i++) {
        String newUrl = matcher.replaceAll(String.valueOf(i));
        MediaArtwork fanArt = new MediaArtwork(getId(), MediaArtwork.MediaArtworkType.BACKGROUND);
        fanArt.setLanguage(options.getLanguage().getLanguage());
        fanArt.setDefaultUrl(newUrl);
        fanArt.setPreviewUrl(newUrl);
        fanArt.setOriginalUrl(newUrl);
        fanArt.setSizeOrder(8);
        md.addMediaArt(fanArt);
      }
    }
  }

  private void parseFlashvars(MediaMetadata md, Page page, MovieSearchAndScrapeOptions options) {
    String videoId = md.getIdAsString(getId() + "_id");
    String urlPattern = (String) page.evaluate("flashvars_" + videoId + ".thumbs.urlPattern");

    Matcher matcher = THUMB_URL_INDEX_PATTERN.matcher(urlPattern);
    if (matcher.find()) {
      int thumbsCount = Integer.parseInt(matcher.group(1)) + 1;
      List<MediaArtwork> thumbs = IntStream.range(0, thumbsCount).mapToObj(idx -> {
        String url = matcher.replaceAll(String.valueOf(idx));
        MediaArtwork artwork = new MediaArtwork(getId(), MediaArtwork.MediaArtworkType.BACKGROUND);
        artwork.setLanguage(options.getLanguage().getLanguage());
        artwork.setDefaultUrl(url);
        artwork.setPreviewUrl(url);
        artwork.setOriginalUrl(url);
        artwork.setSizeOrder(8);
        return artwork;
      }).collect(Collectors.toList());
      md.addMediaArt(thumbs);
    }
  }

  private void parsePageElements(MediaMetadata md, Page page) {
    Locator videoLocator = page.locator("#hd-leftColVideoPage");

    // add certification
    md.addCertification(MediaCertification.US_NC17);

    // parse rating info
    int currentUp = Integer.parseInt(videoLocator.locator("[data-rating]").and(videoLocator.locator(".votesUp")).getAttribute("data-rating"));
    int currentDown = Integer.parseInt(videoLocator.locator("[data-rating]").and(videoLocator.locator(".votesDown")).getAttribute("data-rating"));
    int totalVotes = currentUp + currentDown;
    float rating = 10.0f * currentUp / totalVotes;
    md.addRating(new MediaRating(MediaRating.USER, rating, totalVotes, 10));

    // parse trailer url
    ElementHandle addToTabImg = page.querySelector("div.add-to-tab img");
    if (addToTabImg != null) {
      String trailerUrl = addToTabImg.getAttribute("data-mediabook");
      MediaTrailer trailer = new MediaTrailer();
      trailer.setProvider(getId());
      trailer.setName("mediabook");
      trailer.setUrl(trailerUrl);
      md.addTrailer(trailer);
    }

    // parse detail info
    Locator aboutTab = videoLocator.locator("div.about-tab");
    // author
    Locator userRow = aboutTab.locator("div.userRow");
    Person author = new Person(Person.Type.DIRECTOR, userRow.locator("span.usernameBadgesWrapper").textContent().trim(), null,
        userRow.getByRole(AriaRole.IMG).getAttribute("src"),
        API_URL + userRow.locator("span.usernameBadgesWrapper").getByRole(AriaRole.LINK).getAttribute("href"));
    md.addCastMember(author);

    Person writer = new Person(author);
    writer.setType(Person.Type.WRITER);
    md.addCastMember(writer);

    Person producer = new Person(author);
    producer.setType(Person.Type.PRODUCER);
    md.addCastMember(producer);
    // actors
    aboutTab.locator("[data-label='Pornstar']").all().forEach(actor -> {
      Locator img = actor.getByRole(AriaRole.IMG);
      md.addCastMember(new Person(Person.Type.ACTOR, actor.textContent().trim(), actor.textContent().trim(), img.getAttribute("src"),
          API_URL + actor.getAttribute("href")));
    });
    // if contains no actors, add the director as actor
    if (md.getCastMembers(Person.Type.ACTOR).isEmpty()) {
      Person person = new Person(author);
      person.setType(Person.Type.ACTOR);
      md.addCastMember(person);
    }

    aboutTab.locator("[data-label='Category']").all().stream().map(category -> MediaGenres.getGenre(category.textContent())).forEach(md::addGenre);
    md.addGenre(MediaGenres.EROTIC);
    aboutTab.locator("[data-label='Tag']").all().forEach(tag -> md.addTag(tag.textContent()));
  }

}

