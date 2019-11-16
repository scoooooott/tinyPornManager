#!/usr/bin/env bash
#
# tinyMediaManager v3 by Manuel Laggner
# https://www.tinymediamanager.org/
# SPDX­License­Identifier: Apache-2.0
#
# Run the updater first whether we need it or not

# Set desired execution flags
PARAMS=( "-Dsilent" "-Djava.net.preferIPv4Stack=true"
         "-Dappbase=https://www.tinymediamanager.org/" "-jar"
)

# Allow the script to be called from any directory and through symlinks
TMM_DIR="$(dirname "$(test -L "${BASH_SOURCE[0]}" && \
    readlink "${BASH_SOURCE[0]}" || echo "${BASH_SOURCE[0]}")")"

# We're taking the long way around, so get a move-on already!!
cd "$TMM_DIR" || return 1
java "${PARAMS[@]}" getdown.jar .
