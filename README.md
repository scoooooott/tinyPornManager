# [tinyMediaManager][1]

tinyMediaManager (https://www.tinymediamanager.org) full featured media manager to organize and clean up your media library. It is designed to allow you to create/view/edit the metadata, artwork and file structure for your media files used by Kodi (formerly XBMC), Plex, MediaPortal, Emby, Jellyfin and other compatible media center software. As a Java application it is truly cross-platform and will run on Windows, Linux and MacOS (and possibly more).

## [Features][4]

- Automatic updates
- GUI and command line interfaces
- Metadata scrapers for IMDb, TheMovieDb, TVDb, OFDb, Moviemeter, Trakt and more
- Artwork downloaders for TheMovieDb, TVDb and FanArt.tv
- Trailer downloads from TheMovieDb and HD-Trailers.net
- Subtitles downloaded from OpenSubtitles.org
- Manually edit any metadata fields with ease
- Automatic file renaming according to any user-defined format
- Powerful search features with custom filters and sorting
- Saves everything in .nfo files automatically recognized by Kodi and most other media centers
- Technical metadata like codecs, duration and resolution extracted from each media file
- Group movies into sets with special artwork common to all movies in it
- Import TV show collections no matter the file organization style used

## [Release][5]

You can always find the latest release [here][5]. [Pre-release builds][6] and [nightly builds][7] are also available, all of which automatically update within their release channel. You need at least Java Runtime Environment 8 to run tinyMediaManager, which you can get [here][8]. Linux users can download [OpenJDK][9] from the package manager (apt/dnf/yum/pacman) of their distribution too.

## [Changelog][10]

Each release's major improvements are announced on our [project blog][11] or you can view the full ChangeLog [here][12].

## [Screenshots][13]

[![movies01](https://www.tinymediamanager.org/images/screenshots/thumbs/v3/movies/movies01-thumb.png)](https://www.tinymediamanager.org/images/screenshots/v3/movies/movies01.png) [![movies04](https://www.tinymediamanager.org/images/screenshots/thumbs/v3/movies/movies04-thumb.png)](https://www.tinymediamanager.org/images/screenshots/v3/movies/movies04.png)

[![movies08](https://www.tinymediamanager.org/images/screenshots/thumbs/v3/movies/movies08-thumb.png)](https://www.tinymediamanager.org/images/screenshots/v3/movies/movies08.png) [![movies14](https://www.tinymediamanager.org/images/screenshots/thumbs/v3/movies/movies14-thumb.png)](https://www.tinymediamanager.org/images/screenshots/v3/movies/movies14.png)

[![tvshows01](https://www.tinymediamanager.org/images/screenshots/thumbs/v3/tvshows/tvshows01-thumb.png)](https://www.tinymediamanager.org/images/screenshots/v3/tvshows/tvshows01.png) [![tvshows02](https://www.tinymediamanager.org/images/screenshots/thumbs/v3/tvshows/tvshows02-thumb.png)](https://www.tinymediamanager.org/images/screenshots/v3/tvshows/tvshows02.png)

The complete gallery of screenshots can be viewed on our website [here][13].

## [Contributing][14]

Please read our [Contributors' Guide][14] and be sure to base your pull requests against our **devel** branch.

## Building from source

tinyMediaManager is compiled using Apache's build automation tool, [Maven][15]. Check that you have it installed (and git, of course) before attempting a build.

1. Clone this repository to your computer

   ```bash
   git clone https://gitlab.com/tinyMediaManager/tinyMediaManager.git
   ```

1. Build using maven

   ```bash
   mvn package
   ```

After that you will find the packaged build in the folder `dist`

[1]: https://www.tinymediamanager.org
[4]: https://www.tinymediamanager.org/features/
[5]: https://www.tinymediamanager.org/download/
[6]: https://www.tinymediamanager.org/download/prerelease
[7]: https://www.tinymediamanager.org/download/nightly-build
[8]: https://www.java.com/en/download/manual.jsp
[9]: https://openjdk.java.net/install/
[10]: /changelog.txt
[11]: https://www.tinymediamanager.org/blog/
[12]: https://www.tinymediamanager.org/changelog/
[13]: https://www.tinymediamanager.org/screenshots/
[14]: /CONTRIBUTING.md
[15]: https://maven.apache.org/
