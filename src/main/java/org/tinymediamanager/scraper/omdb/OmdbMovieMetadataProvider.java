package org.tinymediamanager.scraper.omdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.UnsupportedMediaTypeException;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaCastMember;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.omdb.entities.MovieEntity;
import org.tinymediamanager.scraper.omdb.entities.MovieSearch;
import org.tinymediamanager.scraper.omdb.service.Controller;
import org.tinymediamanager.scraper.util.ListUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OmdbMovieMetadataProvider {

	// Declaration
	private static final Logger LOGGER = LoggerFactory.getLogger(OmdbMovieMetadataProvider.class);
	private Controller controller;

	public OmdbMovieMetadataProvider() {
		this.controller = new Controller();
	}

	/**
	 * constructor for verbose testing
	 *
	 * @param verbose
	 *            verbose mode
	 **/
	OmdbMovieMetadataProvider(boolean verbose) {
		this.controller = new Controller(verbose);
	}
	
	
	/**
	 * Getting Media Infos
	 * @param query
	 * @return Results to one Movie
	 * @throws Exception
	 */
	public MediaMetadata getMetadata(MediaScrapeOptions query) throws Exception {

		// Declaration
		MediaMetadata metadata = new MediaMetadata(OmdbMetadataProvider.providerinfo.getId());
		MediaArtwork artwork = new MediaArtwork(OmdbMetadataProvider.providerinfo.getId(),
				MediaArtwork.MediaArtworkType.POSTER);
		DateFormat format = new SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH);
		Pattern p = Pattern.compile("\\d+");
		Matcher m;
		MovieEntity result = null;

		LOGGER.debug("scrape()" + query.toString());
		LOGGER.info("========= BEGIN OMDB Scraping");

		try {

			if (query.getType() == MediaType.MOVIE) {
				result = controller.getScrapeDataById(query.getId(OmdbMetadataProvider.providerinfo.getId()), "movie",
						true);
			} else if (query.getType() == MediaType.TV_EPISODE) {
				result = controller.getScrapeDataById(query.getId(OmdbMetadataProvider.providerinfo.getId()), "series",
						true);
			} else {
				throw new UnsupportedMediaTypeException(query.getType());
			}
		} catch (Exception e) {
			LOGGER.error("error searching: " + e.getMessage());
		}

		if (result == null) {
			LOGGER.warn("no result found");
			return metadata;
		}

		metadata.setTitle(result.title);

		m = p.matcher(result.year);
		while (m.find()) {
			try {
				metadata.setYear(Integer.parseInt(m.group()));
			} catch (NumberFormatException ignored) {
			}
		}

		metadata.addCertification(Certification.findCertification(result.rated));
		metadata.setReleaseDate(format.parse(result.released));

		m = p.matcher(result.runtime);
		while (m.find()) {
			try {
				metadata.setRuntime(Integer.parseInt(m.group()));
			} catch (NumberFormatException ignored) {
			}
		}

		String[] genres = result.genre.split(",");
		for (String genre : genres) {
			genre = genre.trim();
			if ("Sci-Fi".equals(genre)) {
				metadata.addGenre(MediaGenres.SCIENCE_FICTION);
			} else {
				MediaGenres mediaGenres = MediaGenres.getGenre(genre);
				metadata.addGenre(mediaGenres);
			}
		}

		metadata.setPlot(result.plot);

		String[] directors = result.director.split(",");
		for (String d : directors) {
			MediaCastMember director = new MediaCastMember(MediaCastMember.CastType.DIRECTOR);
			director.setName(d.trim());
			metadata.addCastMember(director);
		}

		String[] writers = result.writer.split(",");
		for (String w : writers) {
			MediaCastMember writer = new MediaCastMember(MediaCastMember.CastType.WRITER);
			writer.setName(w.trim());
			metadata.addCastMember(writer);
		}

		String[] actors = result.actors.split(",");
		for (String a : actors) {
			MediaCastMember actor = new MediaCastMember(MediaCastMember.CastType.ACTOR);
			actor.setName(a.trim());
			metadata.addCastMember(actor);
		}

		metadata.setSpokenLanguages(getResult(result.language, ","));
		metadata.setCountries(getResult(result.country, ","));

		try {
			metadata.setRating(Double.parseDouble(result.imdbRating));
			metadata.setVoteCount(Integer.parseInt(result.imdbVotes));
		} catch (NumberFormatException ignored) {
		}

		artwork.setDefaultUrl(result.poster);
		metadata.addMediaArt(artwork);

		return metadata;
	}

	/**
	 * Getting the search results
	 * 
	 * @param query
	 *            options
	 * @return List of results
	 * @throws Exception
	 */
	
	public List<MediaSearchResult> search(MediaSearchOptions query) throws Exception {

		// Declaration
		MovieSearch resultList = null;
		List<MediaSearchResult> mediaResult = new ArrayList<>();

		// Begin
		LOGGER.debug("search() " + query.toString());

		try {

			if (query.getMediaType() == MediaType.MOVIE) {

				// MediaType Movie
				LOGGER.info("========= BEGIN OMDB Scraper Search for Movie: " + query.getQuery());
				resultList = controller.getMovieSearchInfo(query.getQuery(), "movie", null);

			} else if (query.getMediaType() == MediaType.TV_EPISODE) {

				// MediaType Series
				LOGGER.info("========= BEGIN OMDB Scraper Search for Series: " + query.getQuery());
				resultList = controller.getMovieSearchInfo(query.getQuery(), "series", null);

			}

		} catch (Exception e) {

			LOGGER.error("error searching: " + e.getMessage());
			return mediaResult;

		}

		if (resultList == null) {
			LOGGER.warn("no result from omdbapi");
			return mediaResult;
		}

		for (MovieEntity entity : ListUtils.nullSafe(resultList.search)) {
			MediaSearchResult result = new MediaSearchResult(OmdbMetadataProvider.providerinfo.getId(),
					MediaType.MOVIE);

			result.setTitle(entity.title);
			result.setIMDBId(entity.imdbID);
			try {
				result.setYear(Integer.parseInt(entity.year));
			} catch (NumberFormatException ignored) {
			}
			result.setPosterUrl(entity.poster);

			mediaResult.add(result);

		}

		return mediaResult;
	}

	/**
	 *
	 * return a list of results that were separated by a delimiter
	 *
	 * @param input
	 *            result from API
	 * @param delimiter
	 *            used delimiter
	 * @return List of results
	 */
	private List<String> getResult(String input, String delimiter) {

		String[] result = input.split(delimiter);
		List<String> output = new ArrayList<>();

		for (String r : result) {
			output.add(r.trim());
		}

		return output;

	}

}
