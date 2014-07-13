#!/bin/bash
#####################################################################################
# Launch tmm without the updater (or the updater if tmm.jar is missing)
#####################################################################################

# have a look if we need to launch the updater or tmm directly
if [ -f tmm.jar ]; then
  JAR="tmm.jar" 
  ARGS="-Xms64m -Xmx512m -Xss512k -splash:splashscreen.png"
else
  JAR="getdown.jar ."
fi

ARGS="$ARGS -Djna.nosys=true"
ARGS="$ARGS -Djava.net.preferIPv4Stack=true"
ARGS="$ARGS -XX:CompileCommand=exclude,ca/odell/glazedlists/impl/filter/TextMatchers,matches"
ARGS="$ARGS -XX:CompileCommand=exclude,ca/odell/glazedlists/impl/filter/BoyerMooreCaseInsensitiveTextSearchStrategy,indexOf"

# execute it :)
java $ARGS -jar $JAR   