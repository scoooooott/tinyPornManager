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
  ARGS="-Dsilent=noupdate"
else
  ARGS="-Xdock:name=`tinyMediaManager updater`"
fi

ARGS="$ARGS -Djava.net.preferIPv4Stack=true"

# execute it :)
exec "$JAVACMD" $ARGS -jar getdown.jar .      

