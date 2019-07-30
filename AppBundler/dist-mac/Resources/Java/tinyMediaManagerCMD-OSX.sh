#!/usr/bin/env bash
#
# tinyMediaManager v3 by Manuel Laggner
# https://www.tinymediamanager.org/
# SPDX-License-Identifier: Apache-2.0
#
#  search the right Java JVM and control the execution parameters for tmm via the command line

# find the path where to execute tmm
PRG=$0
while [ -h "$PRG" ]; do
	ls=$(ls -ld "$PRG")
	link=$(expr "$ls" : '^.*-> \(.*\)$' 2>/dev/null)
	if expr "$link" : '^/' 2>/dev/null >/dev/null; then
		PRG="$link"
	else
		PRG="$(dirname "$PRG")/$link"
	fi
done

progdir=$(dirname "$PRG")

# check if tmm has been executed in a read only environment
if [ ! -w "$progdir" ]; then
	osascript -e "tell application \"System Events\" to display dialog \"ERROR launching tinyMediaManager!\n\nYou need to execute tinyMediaManager from a writeable location (e.g. the Applications folder)\" with title \"tinyMediaManager\" buttons {\" OK \"} default button 1 with icon path to resource \"tmm.icns\" in bundle (path to me)"
	exit 1
fi

# By default Mac OS X LC_ALL is set to "C", which means files with special characters will not be found.
export LC_ALL="en_US.UTF-8"

# search for the right JVM - priority is java 8
if [ -x /usr/libexec/java_home ]; then
	JAVA_HOME="$(/usr/libexec/java_home -v 1.8+ -F)"
	export JAVA_HOME
fi

if [ ! -f "$JAVA_HOME/bin/java" ] && [ -x "/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java" ]; then
	JAVA_HOME="/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home"
	export JAVA_HOME
fi
JAVACMD="${JAVA_HOME}/bin/java"

if [ ! -f "$JAVACMD" ] || [ ! -x "$JAVACMD" ]; then
	# display error message with applescript
	osascript -e "tell application \"System Events\" to display dialog \"ERROR launching tinyMediaManager!\n\nYou need to have JAVA installed on your Mac!\nVisit http://java.com for more information...\" with title \"tinyMediaManager\" buttons {\" OK \"} default button 1 with icon path to resource \"tmm.icns\" in bundle (path to me)"

	# and open java.com
	open http://java.com

	# exit with error
	exit 1
fi

# add classpath
PARAMS=(
	"-classpath"
	"tmm.jar:lib/*"
	"-XX:+IgnoreUnrecognizedVMOptions"
    "-XX:+UseG1GC"
    "-XX:+UseStringDeduplication"
	"-Djna.nosys=true"
	"-Djava.net.preferIPv4Stack=true"
	"-Xms64m"
    "-Dfile.encoding=UTF-8"
    "-Dappbase=https://www.tinymediamanager.org/"
    "-Djava.awt.headless=true"
    "-Xmx512m"
    "-Xss512k"
    "-Dtmm.consoleloglevel=INFO"
)

# What are you waiting for? An introduction??
cd "$progdir" || return 1
java "${PARAMS[@]}" org.tinymediamanager.TinyMediaManager "$@"
