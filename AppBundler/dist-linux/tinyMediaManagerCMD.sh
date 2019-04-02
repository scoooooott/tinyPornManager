#!/usr/bin/env bash
#
# tinyMediaManager v3 by Manuel Laggner
# https://www.tinymediamanager.org/
# SPDX-License-Identifier: Apache-2.0
#
# Control the execution parameters for tmm via the command line

# If invoked on Linux system, add 'jna.nosys' to the exec parameters
if [ "$OSTYPE" == "linux-gnu" ]; then
	PARAMS="-Djna.nosys=true"
fi

# And they wonder why Java gets a bad rap...
PARAMS+=(
    "-classpath tmm.jar:lib/*" "-Djava.net.preferIPv4Stack=true" "-Xms64m"
    "-Dfile.encoding=UTF-8" "-Dappbase=https://www.tinymediamanager.org/"
    "-Djava.awt.headless=true" "-XX:+IgnoreUnrecognizedVMOptions" "-Xmx512m"
    "-Djavax.accessibility.assistive_technologies=" "" "-Xss512k"
    "-Dtmm.consoleloglevel=INFO" "--add-modules=java.xml.bind"
    "--add-modules=java.xml.ws"
)

# Allow the script to be called from any directory and through symlinks
TMM_DIR="$(dirname "$(test -L "${BASH_SOURCE[0]}" && \
    readlink "${BASH_SOURCE[0]}" || echo "${BASH_SOURCE[0]}")")"

# What are you waiting for? An introduction??
cd "$TMM_DIR" || return 1
java "${PARAMS[@]}" org.tinymediamanager.TinyMediaManager "$@"
