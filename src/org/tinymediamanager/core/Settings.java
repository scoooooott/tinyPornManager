package org.tinymediamanager.core;

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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jdesktop.observablecollections.ObservableCollections;

@XmlRootElement(name = "tinyMediaManager")
public class Settings extends AbstractModelObject {
	private static Settings instance;

	private final static String CONFIG_FILE = "config" + File.separator
			+ "config.xml";
	private final static String MOVIE_DATA_SOURCE = "movieDataSource";
	private final static String PATH = "path";
	private final static String VIDEO_FILE_TYPE = "videoFileTypes";
	private final static String FILETYPE = "filetype";
	private final static String PROXY_HOST = "proxyHost";
	private final static String PROXY_PORT = "proxyPort";
	private final static String PROXY_USERNAME = "proxyUsername";
	private final static String PROXY_PASSWORD = "proxyPassword";
	private final static String IMAGE_TMDB_LANGU = "imageTmdbLanguage";

	@XmlElementWrapper(name = VIDEO_FILE_TYPE)
	@XmlElement(name = FILETYPE)
	private final List<String> videoFileTypes = new ArrayList<String>();

	@XmlElementWrapper(name = MOVIE_DATA_SOURCE)
	@XmlElement(name = PATH)
	private final List<String> movieDataSources = ObservableCollections
			.observableList(new ArrayList<String>());

	private String proxyHost;
	private String proxyPort;
	private String proxyUsername;
	private String proxyPassword;

	private String imageTmdbLangugage;

	private Settings() {
	}

	public static Settings getInstance() {
		if (Settings.instance == null) {
			// try to parse XML
			JAXBContext context;
			try {
				context = JAXBContext.newInstance(Settings.class);
				Unmarshaller um = context.createUnmarshaller();
				try {
					Settings.instance = (Settings) um.unmarshal(new FileReader(
							CONFIG_FILE));
				} catch (FileNotFoundException e) {
					// e.printStackTrace();
					Settings.instance = new Settings();
					Settings.instance.writeDefaultSettings();
				} catch (IOException e) {
					// e.printStackTrace();
					Settings.instance = new Settings();
					Settings.instance.writeDefaultSettings();
				}
			} catch (JAXBException e) {
				e.printStackTrace();
			}

		}
		return Settings.instance;
	}

	public void addMovieDataSources(String path) {
		movieDataSources.add(path);
		firePropertyChange(MOVIE_DATA_SOURCE, null, movieDataSources);

	}

	public void removeMovieDataSources(String path) {
		movieDataSources.remove(path);
		firePropertyChange(MOVIE_DATA_SOURCE, null, movieDataSources);
	}

	public List<String> getMovieDataSource() {
		return movieDataSources;
	}

	public void addVideoFileTypes(String type) {
		videoFileTypes.add(type);
		firePropertyChange(VIDEO_FILE_TYPE, null, videoFileTypes);
	}

	public void removeVideoFileType(String type) {
		videoFileTypes.remove(type);
		firePropertyChange(VIDEO_FILE_TYPE, null, videoFileTypes);
	}

	public List<String> getVideoFileType() {
		return videoFileTypes;
	}

	public void saveSettings() {
		// create JAXB context and instantiate marshaller
		JAXBContext context;
		Writer w = null;
		try {
			context = JAXBContext.newInstance(Settings.class);
			Marshaller m = context.createMarshaller();
			m.setProperty("jaxb.encoding", "Unicode");
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			w = new FileWriter(CONFIG_FILE);
			m.marshal(this, w);

		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				w.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// set proxy information
		setProxy();
	}

	private void writeDefaultSettings() {
		// default video file types
		addVideoFileTypes(".mpg");
		addVideoFileTypes(".avi");
		addVideoFileTypes(".mp4");
		addVideoFileTypes(".mkv");

		setImageTmdbLangugage("de");

		saveSettings();
	}

	@XmlElement(name = PROXY_HOST)
	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String newValue) {
		String oldValue = this.proxyHost;
		this.proxyHost = newValue;
		firePropertyChange(PROXY_HOST, oldValue, newValue);
	}

	@XmlElement(name = PROXY_PORT)
	public String getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(String newValue) {
		String oldValue = this.proxyPort;
		this.proxyPort = newValue;
		firePropertyChange(PROXY_PORT, oldValue, newValue);
	}

	@XmlElement(name = PROXY_USERNAME)
	public String getProxyUsername() {
		return proxyUsername;
	}

	public void setProxyUsername(String newValue) {
		String oldValue = this.proxyUsername;
		this.proxyUsername = newValue;
		firePropertyChange(PROXY_USERNAME, oldValue, newValue);
	}

	@XmlElement(name = PROXY_PASSWORD)
	public String getProxyPassword() {
		return StringEscapeUtils.unescapeXml(proxyPassword);
	}

	public void setProxyPassword(String newValue) {
		newValue = StringEscapeUtils.escapeXml(newValue);
		String oldValue = this.proxyPassword;
		this.proxyPassword = newValue;
		firePropertyChange(PROXY_PASSWORD, oldValue, newValue);
	}

	@XmlElement(name = IMAGE_TMDB_LANGU)
	public String getImageTmdbLangugage() {
		return imageTmdbLangugage;
	}

	public void setImageTmdbLangugage(String newValue) {
		String oldValue = this.imageTmdbLangugage;
		this.imageTmdbLangugage = newValue;
		firePropertyChange(IMAGE_TMDB_LANGU, oldValue, newValue);
	}

	public void setProxy() {
		System.setProperty("proxyPort", getProxyPort());
		System.setProperty("proxyHost", getProxyHost());
		System.setProperty("http.proxyUser", getProxyUsername());
		System.setProperty("http.proxyPassword", getProxyPassword());
	}
}
