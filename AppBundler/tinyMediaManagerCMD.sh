#!/bin/bash

PARAMS=
 
if [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
  PARAMS=-Djna.nosys=true
fi

java -Djava.awt.headless=true -Xms64m -Xmx512m -Xss512k $PARAMS -jar tmm.jar $1 $2 $3 $4 $5