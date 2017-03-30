package org.tinymediamanager.scraper.animated;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;
import org.tinymediamanager.scraper.animated.entities.Base;
import org.tinymediamanager.scraper.animated.entities.Movie;

import com.google.gson.Gson;

public class AnimatedTest {

  @Test
  public void testLocal() throws IOException {
    byte[] fileArray = Files.readAllBytes(Paths.get("src/test/resources/movies.json"));
    String jsonStr = new String(fileArray, StandardCharsets.UTF_8);

    Gson gson = new Gson();
    Base json = gson.fromJson(jsonStr, Base.class);

    Movie m = json.getMovieByImdbId("tt1074638");
    if (m != null) {
      System.out.println(m);
    }
    else {
      System.err.println("movie not found");
    }
  }

  @Test
  public void testRemote() {
    AnimatedMetadataProvider amp = new AnimatedMetadataProvider();

    Movie m = amp.getJson().getMovieByImdbId("tt1074638");
    if (m != null) {
      System.out.println(m);
    }
    else {
      System.err.println("movie not found");
    }
  }
}
