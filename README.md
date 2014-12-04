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
All programs read configuration files from a configuration folder that MUST BE 
SPECIFIED as the environment variable 'ONION_CONF_DIR'. To pass it to a java program,
use the vm argument -DONION_CONF_DIR=[the-folder-where-you-copied_code/onion/conf]

Logging: the logging framework in use is slf4j, using the logback implementation.
It is configured through the logback.xml file in the conf folder. However, that file must 
be specified as an environment variable 'logging.config' for it to be found by the
logging system.

2.2 Running locally during development
--------------------------------------
2.2.1 Configuration
-------------------
Start by copying code/onion/conf to code/onion/conf.local
During development, only make changes you need locally in the conf.local folder.

2.2.1 Running from the command line
-----------------------------------
Java needs the classpath to be specified on the command line, which is a bit of a 
pain. Maven helps us here.

Running the quoteserver:
```
cd code/onion/onion-quoteserver
mvn exec:java -Dexec.mainClass="com.github.aic2014.onion.quoteserver.QuoteServerApp" -DONION_CONF_DIR=../conf.local -Dlogging.config=../conf.local/logback.xml
```
Point your browser to `http://localhost:20140/quote` to see if it works.

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
Currently, the client fetches the chain from the directory node and sends a request to the first chain node in the chain.


2.2.2 Running inside IntelliJ
-----------------------------

In IntelliJ, a "Run Configuration" encapsulates all the parameters and configuration for running code. 
In a nutshell, you have to create a Run Configuration for each class you want to run and specify 
the -DONION_CONF_DIR=conf.local VM argument.

That can quite easily be done by right-clicking the class name in the project view (or into the code) 
and chooing 'Run'. Watch it fail once (because the configuration folder is not found), then add the 
VM argument tothe automatically generated run configuration for the class. Run again.

The classes to run are:
* com.github.aic2014.onion.quoteserver.QuoteServerApp
  * check: `http://localhost:20140/quote`
* com.github.aic2014.onion.directorynode.DirectoryNodeApp
  * check: `http://localhost:20141/getChain`
* com.github.aic2014.onion.chainnode.ChainNodeApp
  * run this 3x, then check again `http://localhost:20141/getChain`

If you want to run an application directly (without Spring), you need to add an additional VM argument to your Run Configuration:
 -Dlogback.configurationFile=../conf.local/logback.xml

2.3 Running the AWS demo
------------------------
_Disclaimer: (I know, this is not a sufficiently designed How-to.)_
In order to run the application the onion "system", do the following:
* Login to AWS-EC2. Switch to Region **US West (N. California)**
* Start the instances: 
  * G6-T3-chainnode-0
  * G6-T3-chainnode-1
  * G6-T3-chainnode-2
  * G6-T3-directorynode
  * G6-T3-quoteserver
* If any of the chainnode-instances is already RUNNING. Stop the instance and start it again. (The directorynode is in charge of starting the onion-service)
* Connect to G6-T3-directorynode (user=onion), run the following command:
  ```
  nohup java -DONION_CONF_DIR=/home/onion/directorynode/conf.local -Dlogging.config=/home/onion/directorynode/conf.local/logback.xml -jar /home/onion/directorynode/onion-directorynode-1.0-SNAPSHOT-allinone.jar >/dev/null 2>&1 &
  ```
  Check http://54.67.23.238:20141/ after about 10-20 seconds (you may also want to check ~/onion.log). Three chain nodes should have registered with the directory node
* Connect to G6-T3-quoteserver (user=onion), run the following command:
  ```
  nohup java -DONION_CONF_DIR=/home/onion/quoteserver/conf.local -Dlogging.config=/home/onion/quoteserver/conf.local/logback.xml -jar /home/onion/quoteserver/onion-quoteserver-1.0-SNAPSHOT-allinone.jar >/dev/null 2>&1 &
  ```
  Check http://54.67.23.87:20140/quote.
* Connect to G6-T3-directorynode again and run the client (this can also be execute from another server within AWS):
  ```
  java -DONION_CONF_DIR=/home/onion/client/conf.local -Dlogging.config=/home/onion/client/conf.local/logback.xml -Dquoteserver.baseUri= -Dquoteserver.hostnamePort=54.67.23.87:20140 -jar /home/onion/client/onion-client-1.0-SNAPSHOT-allinone.jar
  ```
