aic2014
=======

Lab assignment of Advanced Internet Computing VU 2014

Technical University of Vienna

Assignment 3: Onion Routing

1. BUILD INSTRUCTIONS
---------------------

1.1 Requirements
----------------
* Java 8
* maven 3.0.5 or higher

1.2 Build
---------
The following command builds the code
```
cd code
mvn install
````

2. RUNNING
----------

2.1 general note on configuration
-----------------
All programs read configuration files from a configuration folder that MUST BE 
SPECIFIED as the environment variable 'ONION_CONF_DIR'. To pass it to a java program,
use the vm argument -DONION_CONF_DIR=[the-folder-where-you-copied_code/onion/conf]

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
mvn exec:java -Dexec.mainClass="com.github.aic2014.onion.quoteserver.QuoteServerApp" -DONION_
CONF_DIR=../conf.local
```
Point your browser to `http://localhost:20140/quote` to see if it works.

Running the directory node:
```
mvn exec:java -Dexec.mainClass="com.github.aic2014.onion.directorynode.DirectoryNodeApp"
-DONION_CONF_DIR=../conf.local
```
Point your browser to `http://localhost:20141/getChain` to see if it works.

2.2.2 Running inside IntelliJ
-----------------------------

In IntelliJ, a "Run Configuration" encapsulates all the parameters and configuration for running code. 
In a nutshell, you have to create a Run Configuration for the class you want to run and specify 
the -DONION_CONF_DIR=conf.local VM argument.

That can quite easily be done by right-clicking the class name in the project view (or into the code) 
and chooing 'Run'. Watch it fail once (because the configuration folder is not found), then add the 
VM argument tothe automatically generated run configuration for the class. Run again.

The classes to run are:
* com.github.aic2014.onion.quoteserver.QuoteServerApp
  * check: `http://localhost:20140/quote`
* com.github.aic2014.onion.directorynode.DirectoryNodeApp
  * check: `http://localhost:20141/getChain`


2.3 Running the AWS demo
------------------------
TODO #8: explain how to run the AWS demo
