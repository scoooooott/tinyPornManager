#!/bin/bash
#####################################################################################
# Launch the updater of tmm
#####################################################################################

cd "$(dirname "$0")"
java -Djava.net.preferIPv4Stack=true -Dappbase=http://www.tinymediamanager.org/ -jar getdown.jar .
