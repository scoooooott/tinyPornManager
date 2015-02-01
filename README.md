tinyMediaManager scraper API
============================

Author: Manuel Laggner

This API provides a connection between tinyMediaManager and its metadata (and trailer/artwork) scraper.

Usage
-----
To use this API, you must implement one of the following interfaces and mark your implementation with the annotation
_@PluginImplementation_.

Intefaces:
* IMovieMetadataProvider (to provide meta data for movies)
* IMovieTrailerProvider (to provide trailer for movies)
* ITvShowMetadataProvider (to provide meta data for TV shows)
* IMediaArtworkProvider (to provide artwork for movies and/or TV shows)

Whenever you need to build up a connection to the internet, use either the Url class (from the package
org.tinymediamanager.scraper.util) or the HttpClient from the class TmmHttpClient (org.tinymediamanager.scraper.util).

All needed libs, which are not part of the scraper-api, should be bundled within the scraper. You can use the
_maven-shade-plugin_ to achieve that.

Project Logging
---------------
This project uses SLF4J (http://www.slf4j.org) to abstract the logging in the project.


