#!/bin/bash

# If directory-node is executed in a WINDOWS environment, use the following path-style
    #IDFILE='C:\\path\\to\\ID-File\\G6-T3-id_rsa.pem'
# For this property you cannot use absolute paths as this won't work with SCP (except with cygwin)
    #SOURCEDIR='deployment/chainnode/*'

# For deployment of the directory-node within the AWS environment, use the following path
    #IDFILE='/home/onion/directorynode/deployment/G6-T3-id_rsa.pem'
    #SOURCEDIR='/home/onion/directorynode/deployment/chainnode/*'

REMOTEHOST=$1

IDFILE='C:\\path\\to\\ID-File\\G6-T3-id_rsa.pem'
SOURCEDIR='deployment/chainnode/*'

REMOTEDIR='~/chainnode/'

# see loop-implementation: http://stackoverflow.com/a/21658290
echo "Check if available..."
`nc -w 5 -z ${REMOTEHOST} 22` 1>/dev/null 2>&1; RESULT=$?;
while [ ${RESULT} -eq 1 ]
    do
        echo "#"
        `nc -w 5 -z ${REMOTEHOST} 22` 1>/dev/null 2>&1; RESULT=$?;
        sleep 5
    done
echo "SSH is available..."

echo "Upload files to ${REMOTEHOST}... "
scp -oStrictHostKeyChecking=no -q -i "${IDFILE}" -r ${SOURCEDIR} onion@${REMOTEHOST}:${REMOTEDIR}

echo "----- " 1>&2

echo "Start Application..."
ssh -oStrictHostKeyChecking=no -i "${IDFILE}" onion@${REMOTEHOST} "nohup java \
     -DONION_CONF_DIR=/home/onion/chainnode/conf.local \
     -Dlogging.config=/home/onion/chainnode/conf.local/logback.xml \
     -jar /home/onion/chainnode/onion-chainnode-1.0-SNAPSHOT-allinone.jar >/dev/null 2>&1 &"

echo "Done!!!"