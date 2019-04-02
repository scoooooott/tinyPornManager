#!/usr/bin/env bash
#
# tinyMediaManager v3 by Manuel Laggner
# https://www.tinymediamanager.org/
# SPDX-License-Identifier: Apache-2.0
#
# Launch tmm (or the updater if tmm.jar is missing)

# Allow the script to be called from any directory and through symlinks
TMM_DIR="$(dirname "$(test -L "${BASH_SOURCE[0]}" && \
    readlink "${BASH_SOURCE[0]}" || echo "${BASH_SOURCE[0]}")")"

# Cancel the updater if tmm.jar is present for execution
if [ -f "$TMM_DIR/tmm.jar" ]; then
    PARAMS="-Dsilent=noupdate"
fi

# Use IPv4 when possible and declare the appbase
PARAMS+=("-Djava.net.preferIPv4Stack=true"
        "-Dappbase=https://www.tinymediamanager.org/" "-jar"
)

# Ma! Start the car! :)
cd "$TMM_DIR" || return 1
java "${PARAMS[@]}" getdown.jar .
