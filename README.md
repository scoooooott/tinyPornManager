tinyMediaManager
========================

tinyMediaManager (http://www.tinymediamanager.org) is a media management tool written in Java/Swing. It is written to provide metadata for the XBOX Media Center (XBMC). Due to the fact that it is written in Java, tinyMediaManager will run on Windows, Linux and Mac OSX (and possible more OS).

tinyMediaManager is free and will stay free. If you appreciate all the effort that has gone into this application then consider a donation. While it's neither expected nor required it is highly appreciated!
[![Donate][1]][2]

[1]: https://www.paypal.com/en_US/i/btn/btn_donate_SM.gif
[2]: http://www.tinymediamanager.org/donate.php

##Features##
[http://www.tinymediamanager.org/index.php/features/](http://www.tinymediamanager.org/index.php/features/)

## Release
you will find the latest release at [http://release.tinymediamanager.org](http://release.tinymediamanager.org) 

## Changelog
[http://www.tinymediamanager.org/index.php/changelog/](http://www.tinymediamanager.org/index.php/changelog/)

##Screenshots##
more screenshots at [http://www.tinymediamanager.org/index.php/screenshots/](http://www.tinymediamanager.org/index.php/screenshots/)

###Main panel for movies

[![Movie panel](https://github.com/tinyMediaManager/tinyMediaManager/raw/master/screenshots/movie_panel_main_thumb.jpg)](https://github.com/tinyMediaManager/tinyMediaManager/raw/master/screenshots/movie_panel_main.jpg)

###Main panel for moviesets

[![Movie set panel](https://github.com/tinyMediaManager/tinyMediaManager/raw/master/screenshots/movieset_panel_main_thumb.jpg)](https://github.com/tinyMediaManager/tinyMediaManager/raw/master/screenshots/movieset_panel_main.jpg)

###Search for a movie

[![Movie search](https://github.com/tinyMediaManager/tinyMediaManager/raw/master/screenshots/moviechooser_dialog_thumb.jpg)](https://github.com/tinyMediaManager/tinyMediaManager/raw/master/screenshots/moviechooser_dialog.jpg)

###Search for a movieset

[![Movie set search](https://github.com/tinyMediaManager/tinyMediaManager/raw/master/screenshots/moviesetchooser_dialog_thumb.jpg)](https://github.com/tinyMediaManager/tinyMediaManager/raw/master/screenshots/moviesetchooser_dialog.jpg)

###Edit movie metadata

[![Edit movie](https://github.com/tinyMediaManager/tinyMediaManager/raw/master/screenshots/movieeditor_dialog_thumb.jpg)](https://github.com/tinyMediaManager/tinyMediaManager/raw/master/screenshots/movieeditor_dialog.jpg)

###Main panel for TV shows

[![TV show panel](https://github.com/tinyMediaManager/tinyMediaManager/raw/master/screenshots/tvshow_panel_main_thumb.jpg)](https://github.com/tinyMediaManager/tinyMediaManager/raw/master/screenshots/tvshow_panel_main.jpg)

###Change artwork

[![Image chooser](https://github.com/tinyMediaManager/tinyMediaManager/raw/master/screenshots/imagechooser_dialog_thumb.jpg)](https://github.com/tinyMediaManager/tinyMediaManager/raw/master/screenshots/imagechooser_dialog.jpg)



## Developer info:
To work with the SNAPSHOTs, you need to add following repository to your ~/.m2/settings.xml

				<repository>
					<id>ossrh-snapshot</id>
					<name>Sonatype Snapshot Repository</name>
					<url>https://oss.sonatype.org/content/repositories/snapshots/</url>
					<releases>
						<enabled>false</enabled>
					</releases>
					<snapshots>
						<enabled>true</enabled>
					</snapshots>
				</repository>
