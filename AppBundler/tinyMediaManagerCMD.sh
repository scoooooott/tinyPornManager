#!/bin/bash
if [ "$(uname)" == "Darwin" ]; then
  java -jar tmm.jar -Xms64m -Xmx512m -Xss512k $1     
elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
  java -jar tmm.jar -Xms64m -Xmx512m -Xss512k -Djna.nosys=true $1
else 
  echo "not supported OS"
fi