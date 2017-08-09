package org.tinymediamanager.scraper.omdb.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SeasonEntity {

	@SerializedName("Title")
	@Expose
	public String title;
	@SerializedName("Released")
	@Expose
	public String released;
	@SerializedName("Episode")
	@Expose
	public String episode;
	@SerializedName("imdbRating")
	@Expose
	public String imdbRating;
	@SerializedName("imdbID")
	@Expose
	public String imdbID;

}
