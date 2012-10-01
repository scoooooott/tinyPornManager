package org.tinymediamanager.core.movie;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.tinymediamanager.core.movie.MovieToXbmcNfoConnector.Actor;

@XmlRootElement(name = "movie")
@XmlSeeAlso(Actor.class)
public class MovieToXbmcNfoConnector {

  private final static String NFO_NAME = "movie.nfo";

  private String              title;
  private String              originaltitle;
  private float               rating;
  private String              year;
  private String              outline;
  private String              plot;
  private String              tagline;
  private int                 runtime;
  private String              thumb;
  private String              id;
  private String              filenameandpath;
  private String              director;

  @XmlAnyElement(lax = true)
  private List<Actor>         actors;

  public MovieToXbmcNfoConnector() {
    actors = new ArrayList<MovieToXbmcNfoConnector.Actor>();
  }

  public static String setData(Movie movie) {
    MovieToXbmcNfoConnector xbmc = new MovieToXbmcNfoConnector();
    // set data
    xbmc.setTitle(movie.getName());
    xbmc.setOriginaltitle(movie.getOriginalName());
    xbmc.setRating(movie.getRating());
    xbmc.setYear(movie.getYear());
    xbmc.setPlot(movie.getOverview());

    // outline is only the first 200 characters of the plot
    int spaceIndex = 0;
    if (xbmc.getPlot().length() > 200) {
      spaceIndex = xbmc.getPlot().indexOf(" ", 200);
    }
    else {
      spaceIndex = xbmc.getPlot().length();
    }

    xbmc.setOutline(xbmc.getPlot().substring(0, spaceIndex));
    xbmc.setTagline(movie.getTagline());
    xbmc.setRuntime(movie.getRuntime());
    xbmc.setThumb(movie.getPosterUrl());
    xbmc.setId(movie.getImdbId());

    // filename and path
    if (movie.getMovieFiles().size() > 0) {
      xbmc.setFilenameandpath(movie.getPath() + File.separator + movie.getMovieFiles().get(0));
    }

    xbmc.setDirector(movie.getDirector());
    for (MovieCast cast : movie.getActors()) {
      xbmc.addActor(cast.getName(), cast.getCharacter());
    }

    // and marshall it
    String nfoFilename = movie.getPath() + File.separator + NFO_NAME;
    JAXBContext context;
    Writer w = null;
    try {
      context = JAXBContext.newInstance(MovieToXbmcNfoConnector.class, Actor.class);
      Marshaller m = context.createMarshaller();
      m.setProperty("jaxb.encoding", "Unicode");
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      w = new FileWriter(nfoFilename);
      m.marshal(xbmc, w);

    }
    catch (JAXBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    finally {
      try {
        w.close();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }

    return nfoFilename;

  }

  public static Movie getData(String nfoFilename) {
    // try to parse XML
    JAXBContext context;
    Movie movie = null;
    try {
      context = JAXBContext.newInstance(MovieToXbmcNfoConnector.class, Actor.class);
      Unmarshaller um = context.createUnmarshaller();
      try {
        MovieToXbmcNfoConnector xbmc = (MovieToXbmcNfoConnector) um.unmarshal(new FileReader(nfoFilename));
        movie = new Movie();
        movie.setName(xbmc.getTitle());
        movie.setOriginalName(xbmc.getOriginaltitle());
        movie.setRating(xbmc.getRating());
        movie.setYear(xbmc.getYear());
        movie.setOverview(xbmc.getPlot());
        movie.setTagline(xbmc.getTagline());
        movie.setRuntime(xbmc.getRuntime());
        movie.setPosterUrl(xbmc.getThumb());
        movie.setImdbId(xbmc.getId());
        movie.setDirector(xbmc.getDirector());

        for (Actor actor : xbmc.getActors()) {
          movie.addToCast(new MovieCast(actor.getName(), actor.getRole()));
        }

        movie.setNfoFilename(nfoFilename);

      }
      catch (FileNotFoundException e) {
        // e.printStackTrace();
        return null;
      }
      catch (IOException e) {
        // e.printStackTrace();
        return null;
      }
    }
    catch (JAXBException e) {
      return null;
    }

    return movie;
  }

  public void addActor(String name, String role) {
    Actor actor = new Actor(name, role);
    actors.add(actor);
  }

  public List<Actor> getActors() {
    return actors;
  }

  @XmlElement(name = "title")
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @XmlElement(name = "originaltitle")
  public String getOriginaltitle() {
    return originaltitle;
  }

  public void setOriginaltitle(String originaltitle) {
    this.originaltitle = originaltitle;
  }

  @XmlElement(name = "rating")
  public float getRating() {
    return rating;
  }

  public void setRating(float rating) {
    this.rating = rating;
  }

  @XmlElement(name = "year")
  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    this.year = year;
  }

  @XmlElement(name = "outline")
  public String getOutline() {
    return outline;
  }

  public void setOutline(String outline) {
    this.outline = outline;
  }

  @XmlElement(name = "plot")
  public String getPlot() {
    return plot;
  }

  public void setPlot(String plot) {
    this.plot = plot;
  }

  @XmlElement(name = "tagline")
  public String getTagline() {
    return tagline;
  }

  public void setTagline(String tagline) {
    this.tagline = tagline;
  }

  @XmlElement(name = "runtime")
  public int getRuntime() {
    return runtime;
  }

  public void setRuntime(int runtime) {
    this.runtime = runtime;
  }

  @XmlElement(name = "thumb")
  public String getThumb() {
    return thumb;
  }

  public void setThumb(String thumb) {
    this.thumb = thumb;
  }

  @XmlElement(name = "id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @XmlElement(name = "filenameandpath")
  public String getFilenameandpath() {
    return filenameandpath;
  }

  public void setFilenameandpath(String filenameandpath) {
    this.filenameandpath = filenameandpath;
  }

  @XmlElement(name = "director")
  public String getDirector() {
    return director;
  }

  public void setDirector(String director) {
    this.director = director;
  }

  // inner class actor to represent actors
  @XmlRootElement(name = "actor")
  public static class Actor {

    private String name;
    private String role;

    public Actor() {
    }

    public Actor(String name, String role) {
      this.name = name;
      this.role = role;
    }

    @XmlElement(name = "name")
    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    @XmlElement(name = "role")
    public String getRole() {
      return role;
    }

    public void setRole(String role) {
      this.role = role;
    }

  }
}
