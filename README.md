tinyMediaManager
========================

tinyMediaManager (http://www.tinymediamanager.org) is a media management tool written in Java/Swing. It is written to provide metadata for the XBOX Media Center (XBMC). Due to the fact that it is written in Java, tinyMediaManager will run on Windows, Linux and Mac OSX (and possible more OS).

tinyMediaManager is free and will stay free. If you appreciate all the effort that has gone into this application then consider a donation. While it's neither expected nor required it is highly appreciated!
[![Donate][1]][2]

[1]: https://www.paypal.com/en_US/i/btn/btn_donate_SM.gif
[2]: http://www.tinymediamanager.org/donate/

##Features##
[http://www.tinymediamanager.org/features/](http://www.tinymediamanager.org/features/)

## Release
you will find the latest release at [http://release.tinymediamanager.org](http://release.tinymediamanager.org)

## Changelog
[http://www.tinymediamanager.org/changelog](http://www.tinymediamanager.org/changelog)

##Screenshots##
[http://www.tinymediamanager.org/screenshots/](http://www.tinymediamanager.org/screenshots/)

## Developer info:
Please provide your pull requests against our **devel** branch.

To work with the SNAPSHOTs, you need to add following repository to your ~/.m2/settings.xml

```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <localRepository/>
  <interactiveMode/>
  <usePluginRegistry/>
  <offline/>
  <pluginGroups/>
  <servers/>
  <mirrors/>
  <proxies />
  <profiles>
    <profile>
	  <id>allow-snapshots</id>
	  <activation>
	    <activeByDefault>true</activeByDefault>
	  </activation>
	  <repositories>
	    <repository>
	      <id>snapshots-repo</id>
		  <url>https://oss.sonatype.org/content/repositories/snapshots</url>
		  <releases>
		    <enabled>false</enabled>
		  </releases>
		  <snapshots>
		    <enabled>true</enabled>
		  </snapshots>
		</repository>
	  </repositories>
	</profile>
  </profiles>
  <activeProfiles/>
</settings>
```

## How to build tinyMediaManager yourself
tinyMediaManager is being built with maven, so you need to have maven (and git of course) installed. If you are getting any errors that maven does not find SNAPSHOT artifcats, please add the maven settings from above.

1. get tinyMediaManager from GitHub

   `$ git clone https://github.com/tinyMediaManager/tinyMediaManager.git`

2. build tinyMediaManager using maven

   `$ mvn package`

After that you will find the packaged build in the folder `dist`