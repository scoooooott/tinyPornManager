#!/bin/sh
#####################################################################################
# This is a "kickstarter" for OSX; we need to do some logic here, because in OSX
# there is no way to provide an updater and the app itself inside one app.
# There is exactly one "entry point" per .app which is defined in the info.plist.
# In our case this is JavaApplicationStub which is a simple shellscript that launches
# this shellscript. Here we do differend checks
#
# a) search the right Java JVM
# b) decide whether we need to launch the updater or launch tmm
#
#####################################################################################

# By default Mac OS X LC_ALL is set to "C", which means files with special characters will not be found.
export LC_ALL="en_US.UTF-8"

# search for the right JVM
if [ -n "$JAVA_HOME" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
elif [ -x /usr/libexec/java_home ]; then
  JAVACMD="`/usr/libexec/java_home`/bin/java"
else
  JAVACMD="/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java"
fi

# have a look if we need to launch the updater or tmm directly
if [ -f tmm.jar ]; then
  JAR="tmm.jar"
  ARGS="-Xdock:name=tinyMediaManager" 
  ARGS="$ARGS -Xms64m -Xmx512m -Xss512k -splash:splashscreen.png"
else
  JAR="getdown.jar ."
  ARGS="-Xdock:name=`tinyMediaManager updater`"
fi

ARGS="$ARGS -Dapple.laf.useScreenMenuBar=true"
ARGS="$ARGS -Dapple.awt.graphics.UseQuartz=true"
ARGS="$ARGS -Djava.net.preferIPv4Stack=true"
ARGS="$ARGS -Dfile.encoding=UTF-8"
ARGS="$ARGS -Xdock:icon=../tmm.icns"
ARGS="$ARGS -XX:CompileCommand=exclude,ca/odell/glazedlists/impl/filter/TextMatchers,matches"
ARGS="$ARGS -XX:CompileCommand=exclude,ca/odell/glazedlists/impl/filter/BoyerMooreCaseInsensitiveTextSearchStrategy,indexOf"

# execute it :)
exec $JAVACMD $ARGS -jar $JAR      

