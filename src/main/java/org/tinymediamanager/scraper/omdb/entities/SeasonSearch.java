package org.tinymediamanager.scraper.omdb.entities;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SeasonSearch {
	
	@SerializedName("Title")
	@Expose
	public String title;
	@SerializedName("Season")
	@Expose
	public String season;
	@SerializedName("totalSeasons")
	@Expose
	public String totalSeasons;
	@SerializedName("Episodes")
	@Expose
	public List<SeasonEntity> episodes = null;
	@SerializedName("Response")
	@Expose
	public String response;

}


