tinyMediaManager scraper API
============================

Author: Manuel Laggner

This API provides a connection between tinyMediaManager and its meta data (and trailer/artwork) scraper.

Usage
-----
To use this API, you must implement one of the following interfaces and mark your implementation with the annotation
_@PluginImplementation_.

Intefaces:
* IMovieMetadataProvider (to provide meta data for movies)
* IMovieArtworkProvider (to provide artwork for movies)
* IMovieTrailerProvider (to provide trailer for movies)
* ITvShowMetadataProvider (to provide meta data for TV shows)
* ITvShowArtworkProvider (to provide artwork for TV shows)

You should put your scraper into a sub package from org.tinymediamanager.scraper. Only packages/classes beneath this
package will be searched for implementations.

Whenever you need to build up a connection to the internet, use either the Url class (from the package
org.tinymediamanager.scraper.util) or the HttpClient from the class TmmHttpClient (org.tinymediamanager.scraper.util).

All needed libs, which are not part of the scraper-api, should be bundled within the scraper. You can use the
_maven-shade-plugin_ to achieve that.

Project Logging
---------------
This project uses SLF4J (http://www.slf4j.org) to abstract the logging in the project.

## Issues
All issues for tinyMediaManager and its components are managed at https://github.com/tinyMediaManager/tinyMediaManager/issues
