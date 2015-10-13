<?php
header('Content-Type: application/x-java-jnlp-file');
 
# without codebase we need a javascript to perform the launch - hence doing it in php
 
function getCurrentUrl() {
    $url  = isset( $_SERVER['HTTPS'] ) && 'on' === $_SERVER['HTTPS'] ? 'https' : 'http';
    $url .= '://' . $_SERVER['SERVER_NAME'];
    $url .= in_array( $_SERVER['SERVER_PORT'], array('80', '443') ) ? '' : ':' . $_SERVER['SERVER_PORT'];
    $url .= dirname($_SERVER['PHP_SELF']);
    return $url;
}
# do not confuse php parser with <?xml header
echo '<?xml version="1.0" encoding="utf-8"?>';
?>
<jnlp spec="6.0" codebase="<?php echo getCurrentUrl(); ?>" href="webstart.php">
    <information>
        <title>tinyMediaManager</title>
        <vendor>Manuel Laggner</vendor>
        <description>tinyMediaManager is a media management tool written in Java/Swing. It is written to provide metadata for Kodi (XBMC).
        </description>
        <description kind="short">TMM - THE MediaManager of your choice :)</description>
        <icon href="tmm.png" />
        <icon kind="splash" href="splashscreen.png" />
    </information>
    <security>
        <all-permissions />
    </security>
    <resources>
        <j2se version="1.7+" href="http://java.sun.com/products/autodl/j2se"
            java-vm-args="-Xmx250M -Djava.net.preferIPv4Stack=true" />
        <property name="MyApplicationProperty" value="myApplicationPropertyValue" />
        <jar href="tmm.jar" main="true" />
        <jar href="plugins/scraper-anidb.jar" />
        <jar href="plugins/scraper-fanarttv.jar" />
        <jar href="plugins/scraper-hdtrailers.jar" />
        <jar href="plugins/scraper-imdb.jar" />
        <jar href="plugins/scraper-kodi.jar" />
        <jar href="plugins/scraper-moviemeter.jar" />
        <jar href="plugins/scraper-ofdb.jar" />
        <jar href="plugins/scraper-rottentomatoes.jar" />
        <jar href="plugins/scraper-tmdb.jar" />
        <jar href="plugins/scraper-tvdb.jar" />
        <jar href="plugins/scraper-zelluloid.jar" />
%WEBSTARTLIBS%    </resources>
    <application-desc main-class="org.tinymediamanager.TinyMediaManager" />
</jnlp>