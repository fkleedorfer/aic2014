aic2014
=======

Lab assignment of Advanced Internet Computing VU 2014

Technical University of Vienna

Group 6, Topic 3: Onion Routing

1. BUILD INSTRUCTIONS
---------------------

1.1 Requirements
----------------
* Java 8
* Maven 3.0.5 or higher

1.2 Build
---------
The following command builds the code
```
cd code/onion
mvn install
````

2. RUNNING
----------

2.1 general note on configuration
-----------------
All programs read configuration files from a configuration folder that **must be 
specified** as the environment variable `ONION_CONF_DIR`. To pass it to a Java program,
use the VM argument `-DONION_CONF_DIR=[the-folder-where-you-copied_code/onion/conf]`

Logging: the logging framework in use is slf4j, using the logback implementation.
It is configured through the logback.xml file in the conf folder. However, that file must 
be specified as an environment variable 'logging.config' for it to be found by the
logging system.

2.2 Running locally during development
--------------------------------------
2.2.1 Configuration
-------------------
Start by copying `code/onion/conf` to `code/onion/conf.local`
During development, only make changes you need locally in the `conf.local` folder.

2.2.2 Running from the command line
-----------------------------------
Java needs the Classpath to be specified on the command line, which is a bit of a 
pain. Maven helps us here.

Running the quote server:
```
cd code/onion/onion-quoteserver
mvn exec:java -Dexec.mainClass="com.github.aic2014.onion.quoteserver.QuoteServerApp" -DONION_CONF_DIR=../conf.local -Dlogging.config=../conf.local/logback.xml
```
Point your browser to `http://localhost:20140` to see if it works.

Running the directory node:
```
cd code/onion/onion-directorynode
mvn exec:java -Dexec.mainClass="com.github.aic2014.onion.directorynode.DirectoryNodeApp"
-DONION_CONF_DIR=../conf.local -Dlogging.config=../conf.local/logback.xml
```
Point your browser to `http://localhost:20141` to see if it works.

Running the chain node:
```
cd code/onion/onion-chainnode
mvn exec:java -Dexec.mainClass="com.github.aic2014.onion.chainnode.ChainNodeApp" -DONION_CONF_DIR=../conf.loca
l -Dlogging.config=../conf.local/logback.xml
``` 
The port the chain node uses is chosen at random. It is output as one of the last log messages.
Point your browser to `http://localhost:[port]` to see if it works.
Also, the during startup, the chain node registers with the directory node. Reload the index page of the 
directory node to see if it worked.

Running the client:
```
cd code/onion/onion-client
mvn exec:java -Dexec.mainClass="com.github.aic2014.onion.client.ClientApp" -DONION_CONF_DIR=../conf.loca
l -Dlogging.config=../conf.local/logback.xml
```

The client will present a list of commands you can enter.
Or you can point your browser to `http://localhost:20143` and click the nice buttons.


2.2.3 Running inside IntelliJ
-----------------------------

In IntelliJ, a "Run Configuration" encapsulates all the parameters and configuration for running code. 
In a nutshell, you have to create a Run Configuration for each class you want to run and specify 
the -DONION_CONF_DIR=conf.local VM argument.

That can quite easily be done by right-clicking the class name in the project view (or into the code) 
and choosing 'Run'. Watch it fail once (because the configuration folder is not found), then add the 
VM argument to the automatically generated run configuration for the class. Run again.

The classes to run are:
* com.github.aic2014.onion.quoteserver.QuoteServerApp
  * check: `http://localhost:20140`
* com.github.aic2014.onion.directorynode.DirectoryNodeApp
  * check: `http://localhost:20141` and `http://localhost:20141/getChain`
* com.github.aic2014.onion.chainnode.ChainNodeApp
  * run this 3x, then check again
* com.github.aic2014.onion.client.OnionClientApp
  * check: `http://localhost:20143`

If you want to run an application directly (without Spring), you need to add an additional VM argument to your Run Configuration:
 -Dlogback.configurationFile=../conf.local/logback.xml

3 DEPLOYMENT / STARTUP
----------------------
The following section will provide a step-by-step guide on how to build, deploy, and start the onion service and all its components. The guides can be applied to each component individually. 

**3.1 Preparation**

Make sure you have access to the following:
* Access to AWS EC2 Management Console (given by course administration)
* Access to the key file `G6-T3-id_rsa.pem`
* SSH client and `maven` installed on your local machine

**3.2 Build**

In order to build the all necessary binaries (JAR files), follow these instructions:
* Navigate into the `code/onion` folder within the code repository
* Execute the maven command `mvn clean install`
* Find the corresponding JAR files of each component here:
  * Directory Node: `onion-directorynode/target/onion-directorynode-1.0-SNAPSHOT-allinone.jar`
  * Chain Node: `onion-chainnode/target/onion-chainnode-1.0-SNAPSHOT-allinone.jar`
  * Quote Server: `onion-quoteserver/target/onion-quoteserver-1.0-SNAPSHOT-allinone.jar`
  * Client: `onion-client/target/onion-client-1.0-SNAPSHOT-allinone.jar`

**3.3 Configuration - localhost**

If you want to execute all onion components (directory node, chain nodes, quote server, client) within your local environment, use the following configuration setup.
* Create an empty folder `conf.local` within `code/onion`
* Copy all default property files from the repository `code/onion/conf/`
* Adapt the following values of `directorynode.properties`:
```
server.port=20141
aws.enableautosetup=false
aws.terminateExisting=false
```
* Adapt the following values of `chainnode.properties`:
```
directorynode.hostname=http://localhost
directorynode.baseUri=${directorynode.hostname}:20141
server.port=0
```
* Adapt the following values of `quoteserver.properties`:
```
server.port=20140
quotesFilename=quotes.txt
```
* Adapt the following values of `client.properties`:
```
directorynode.hostname=http://localhost
directorynode.baseUri=${directorynode.hostname}:20141

quoteserver.hostnamePort=localhost:20140
quoteserver.baseUri=http://${quoteserver.hostnamePort}

server.port=0
```

**3.4 Configuration - AWS**

If you want to execute all onion components (except client) within the AWS EC2 environment, use the following configuration setup:
* Create an empty folder "conf.local"
* Copy all default property files from the repository `code/onion/conf/`
* Adapt the following values of `directorynode.properties`:
```
server.port=20141
aws.enableautosetup=true
aws.terminateExisting=true

aws.accesskeyid=### use given credentials ###
aws.secretaccesskey=### use given credentials ###
aws.region=us-west-1
# ID of G6-T3-template-ami
aws.chainnode.defaultami=ami-0ad4cd4f
aws.chainnode.type=t2.micro
aws.chainnode.prefix=G6-T3-chainnode-
aws.chainnode.keyname=G6-T3-id
# ID of G6-T3-default-security-group
aws.chainnode.securitygroup=sg-6dbb6c08
aws.chainnode.subnet=subnet-7aa0631f
aws.chainnode.quantity=6
aws.chainnode.minQuantity=3

aws.chainnode.deploymentCommand=sh /home/onion/directorynode/deployment/chainnode-deployment.sh %s
aws.chainnode.port=20142
aws.chainnode.deploymentConfPath=/home/onion/directorynode/deployment/chainnode/conf.local/chainnode.properties
```
* Adapt the following values of `chainnode.properties`:
```
# keep the directory node's hostname "localhost". This will be replaced on startup
directorynode.hostname=http://localhost
directorynode.baseUri=${directorynode.hostname}:20141
server.port=20142
```
* Adapt the following values of `quoteserver.properties`:
```
server.port=20140
quotesFilename=quotes.txt
```
* Adapt the following values of `client.properties`:
```
### you may need to adapt the IP addresses, depending on the public IP of the instances
directorynode.hostname=http://54.67.84.173
directorynode.baseUri=${directorynode.hostname}:20141

quoteserver.hostnamePort=54.67.42.60:20140
quoteserver.baseUri=http://${quoteserver.hostnamePort}

server.port=20143
```
* Adapt the following values of `logback.xml`:
```
<root level="INFO">
	<appender-ref ref="FILE" />
	<!--<appender-ref ref="STDOUT" />-->
</root>
```

Next to the configuration folder containing all property files, an additional deployment folder is necessary.
* Create a new folder `deployment`
* Copy the file `code/onion/deployment/chainnode-deployment.sh` into this folder
* Adapt the following shell variables: 
```
IDFILE='/home/onion/directorynode/deployment/G6-T3-id_rsa.pem'
SOURCEDIR='deployment/chainnode/*'
```
* Copy key file `G6-T3-id_rsa.pem` into the folder `deployment`
* Create a subfolder called `chainnode` and `chainnode/conf.local`
* Copy `onion-chainnode-1.0-SNAPSHOT-allinone.jar` into `chainnode`
* Copy `conf.local/logback.xml` into `deployment/chainnode/conf.local/`
* Copy `conf.local/chainnode.properties` into `deployment/chainnode/conf.local/`

**3.5 Deployment - AWS**

Before the onion system can be executed within the AWS EC2 infrastructure, it has to be deployed first. For any remote connectivity (e.g. via PuTTY or WinSCP), use the correct IP address (if in doubt check the AWS web console) and connect with the user `onion` and the given key file `G6-T3-id_rsa.pem`.

We use the "US West (N. California)" region for our EC2 instances, and prepared the following two instances, both based on the "G6-T3-template-ami" AMI (AMI ID `ami-0ad4cd4f`).
* G6-T3-quoteserver (Instance ID `i-9b4b2358`)
* G6-T3-directorynode (Instance ID `i-8c40284f`)

* Deployment of the **Quote Server**:
  * Connect to the quote server (user `onion`)
  * Create a folder `~/quoteserver`
  * Create a subfolder `~/quoteserver/conf.local`
  * Copy `onion-quoteserver-1.0-SNAPSHOT-allinone.jar` (see Build) to `~/quoteserver`
  * Copy the `logback.xml` and `quotserver.properties` to `~/quoteserver/conf.local/`

* Deployment of the **Directory Server**:
  * Connect to the directory server (user `onion`)
  * Create a folder `~/directorynode`
  * Create the subfolders `~/directorynode/conf.local` and `~/directorynode/deployment`
  * Copy `onion-directorynode-1.0-SNAPSHOT-allinone.jar` (see Build) to `~/directorynode`
  * Copy the `logback.xml` and `quotserver.properties` to `~/directorynode/conf.local/` (see Configuration - AWS)
  * Copy the content of the folder `deployment` (see Configuration - AWS) to `~/directorynode/deployment`
  * Run command `chmod 600 ~/directorynode/deployment/*.pem`

**3.6 Startup - localhost**

(See 2.2.1 Running from the command line or 2.2.3 Running inside IntelliJ)

**3.7 Startup - AWS**

Perform the following tasks to start the directory server:
* Connect to directory server (via SSH / user `ec2-user`)
* Run the following command to start the directory server:
```
sudo service directory restart
```
Perform the following tasks to start the quote server: 
* Connect to quote server (via SSH / user `ec2-user`)
* Run the following command to start the quote server:
```
sudo service quote restart
```
Perform the following tasks to start the client (locally):
* (Either) Run client from within IntelliJ (see 2.2.3).
* (Or) Switch to the directory `code/onion/onion-client/target`
* Run the following command to start the client:
```
java -DONION_CONF_DIR=../../conf.local -Dlogging=../../conf.local/logback.xml -jar onion-client-1.0-SNAPSHOT-allinone.jar
```

**3.8 Use Cases/Testing - localhost**

Todo

**3.9 Use Cases/Testing - AWS**

**Check if remote services are ready:** After starting the directory node and quote server, it may take up to 1 minute until everything works fine. Check: 
* http://54.67.84.173:20141/ Shows the directory server page with a list of all registered chain nodes. 
* Click on one of the chain nodes. Shows a list of messages routed by this particular chain node (initially empty).
* http://54.67.42.60:20140/ Shows the quote server page with a button to request random quotes.

**Send a message:**
* (Either) Run to the client and enter the command `!send`
* (Or) Run the client, open http://localhost:20143/ and click the `Send Request` button
