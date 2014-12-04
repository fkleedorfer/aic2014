#!/bin/bash

REMOTEHOST=$1
IDFILE="/home/onion/directorynode/deployment/G6-T3-id_rsa.pem"
SOURCEDIR='/home/onion/directorynode/deployment/chainnode/*'
REMOTEDIR='~/chainnode/'

echo "Upload files..."
scp -oStrictHostKeyChecking=no -i "${IDFILE}" -r ${SOURCEDIR} onion@${REMOTEHOST}:${REMOTEDIR}

echo "Start Application..."
ssh -oStrictHostKeyChecking=no -i "${IDFILE}" onion@${REMOTEHOST} "nohup java \
     -DONION_CONF_DIR=/home/onion/chainnode/conf.local \
     -Dlogging.config=/home/onion/chainnode/conf.local/logback.xml \
     -jar /home/onion/chainnode/onion-chainnode-1.0-SNAPSHOT-allinone.jar >/dev/null 2>&1 &"

echo "Done!"