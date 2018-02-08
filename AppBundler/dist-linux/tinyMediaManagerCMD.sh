#!/bin/bash
PARAMS=
 
if [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
  PARAMS=-Djna.nosys=true
fi

# change to the tmm directory
cd "${0%/*}"
java -classpath tmm.jar:lib/* -Djna.nosys=true -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8 -Dappbase=http://www.tinymediamanager.org/ -Djava.awt.headless=true -Dtmm.consoleloglevel=INFO -XX:+IgnoreUnrecognizedVMOptions --add-modules=java.xml.bind --add-modules=java.xml.ws -Xms64m -Xmx512m -Xss512k $PARAMS org.tinymediamanager.TinyMediaManager $1 $2 $3 $4 $5
