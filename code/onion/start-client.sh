#!/bin/bash -eu
# Pulls the current sources, builds them and then starts the client app.

PROJ="onion-client"
MAINCLASS="com.github.aic2014.onion.client.OnionClientApp"
VMARGS="-DONION_CONF_DIR=conf.local -Dlogging.config=conf.local/logback.xml"

echo Starting the Onion Routing client.
echo Updating from `git remote -v | grep fetch`...
git pull
echo Building...
mvn -pl $PROJ clean install
mvn -pl $PROJ exec:java -Dexec.mainClass="$MAINCLASS" -Dexec.classpathScope=runtime $VMARGS
