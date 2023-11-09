# Scraper-Addon Sample
This project is a sample/template for a scraper addon which can be loaded in tinyMediaManager

## i18n
tinyMediaManager can read localized scraper option names from the custom scraper via an own `ResourceBundle` (v4.3.9+).

To use a custom `ResourceBundle` you need to

1. Create your own properties files containing the literals/translated contents (see `src/main/java/org/tinymediamanager/scraper/spisample/messages.properties`)
2. Register your `ResourceBundle` in the `MediaProviderInfo`
```java
// the ResourceBundle to offer i18n support for scraper options
providerInfo.setResourceBundle(ResourceBundle.getBundle("org.tinymediamanager.scraper.spisample.messages"));
```
3. Add a text for every option in the following form to the properties file
```
#scraper.<scraper id>.<option name>=<text>
scraper.spi-sample.text=Text field
```