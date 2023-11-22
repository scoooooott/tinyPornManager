![tinyPornManager](https://socialify.git.ci/scoooooott/tinyPornManager/image?forks=1&issues=1&language=1&name=1&pattern=Circuit%20Board&stargazers=1&theme=Light)

# Scraper-Addon-Pornhub

Aka: **tinyPornManager**

This is a _Pornhub.com_ [scraper addon](https://www.tinymediamanager.org/blog/third-party-scraper-addons/) which can be
loaded in tinyMediaManager(v4.x).

## Main Features

- Scrape basic info, such as Title, Thumbnail, Directors, Actors, Genres, Tags, Year, Rating, Duration, etc.
- Important variables can be customized to avoid frequent source code maintenance.
- Support i18n, you can choose the language you want to scrape from (_Depends on the languages provided by Pornhub_).

## Roadmap

- [x] Reimplement old tinyPornManager features
- [x] Add support for i18n
- [ ] Better error handling
- [ ] Scrape better quality image
- [ ] Use the CV library to crop poster & generate other images such as banner, logo, etc.
- [ ] Scrape trailer
- [ ] Performance optimization
- [ ] Test cases

## 🚀 Quick Start

1. Download & install tinyMediaManager
2. [Install Pornhub scraper](#-installation)
3. Launch tinyMediaManager
4. Click `Settings -> Movies -> Scraper`, enable Pornhub scraper
5. Download your favorite videos and name the files using the correct format (
   eg: `644d924a9ccc8 | #Double Tactical NUKE on Shoot House.. (Modern Warfare 2 Shoot House Gameplay).mp4`)
6. Add the directory where the video is saved as a movie data source
7. Click `Update source(s)`
8. Choose video(s) you want to scraping
9. Click `Search & scrape`

## 🔧 Installation

There are two options, you can either download the jar from the latest release or build it from source.

### Download Jar

1. download `scraper-addon-pornhub-xxx.jar`
   from [Release Page](https://github.com/scoooooott/tinyPornManager/releases/)
2. move `scraper-addon-pornhub-xxx.jar` to the subfolder `addons` of the tinyMediaManager installation

### Build from source

1. `git clone https://github.com/scoooooott/tinyPornManager.git`
2. `cd tinyPornManager/`
3. `mvn clean package`
4. move `target/scraper-addon-pornhub-xxx.jar` to the subfolder `addons` of the tinyMediaManager installation

## 📖 Use guide

### Q: How should I name the video files that need to be scraped?

For Pornhub.com, there are currently two **_UNIQUE IDENTIFIERS_** for videos

- viewKey: Use everywhere in HTML/API, was _**CHOSEN TO BE**_ the unique identifier for Pornhub Scraper
- ~~video-id(data-video-id)~~: Use sometimes, currently _**ONLY DOING STORAGE**_

We need a rule to extract unique identifier from filename
here is the regular expression in old version tpm:

```^[P|p]h(\\S+)(?:\\s\\S.+)?```

This expression can match the following cases:

```
├── ph1234abcd.mp4
├── ph1234abcd-Super Hot Lily Potter.mp4
└── ph1234abcd Super Hot Lily Potter.mp4
```

But, let's check out this url:

`https://www.pornhub.com/view_video.php?viewkey=644d924a9ccc8`

It seems that the generation rules of viewKey have changed. We need to always update the regular
expression to match the new viewKey.

Fortunately, you can modify the regular expression
in `Settings -> Movies -> Scraper -> Choose 'Pornhub' -> Scraper Options`.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details