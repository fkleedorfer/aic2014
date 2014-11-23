#!/bin/bash

# execute as root

USERNAME=onion
JAVA_VERSION=1.8.0
JAVA_PACKAGES=java-$VERSION-openjdk.x86_64 java-$VERSION-openjdk-devel
MAVEN_VERSION=3.2.2
MAVEN_REMOTE=http://mirror.olnevhost.net/pub/apache/maven/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz

cd ~

# Create a new user which will be used to run the application
adduser --create-home $USERNAME

# Install Java 
yum --assumeyes install JAVA_PACKAGES
yum --assumeyes remove java-1.7.0-openjdk.x86_64
export JAVA_HOME="/usr/lib/jvm/jre"

# Install Maven
mkdir /opt/apache-maven
cd /opt/apache-maven
wget MAVEN_REMOTE
tar xvf apache-maven-$MAVEN_VERSION-bin.tar.gz
rm apache-maven-$MAVEN_VERSION-bin.tar.gz
export M2_HOME=/opt/apache-maven/apache-maven-$MAVEN_VERSION
export M2=$M2_HOME/bin
export PATH=$M2:$PATH
chmod -R +rx *
cd ~