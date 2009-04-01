WELCOME to the maven-googlewebtoolkit2-sample Project
=====================================================

Also see GWT-Maven documentation:
http://gwt-maven.googlecode.com/svn/docs/maven-googlewebtoolkit2-plugin/index.html


About:
======
This sample project has some specific goals:
 + GWT-Maven Usage
 + Java5+ integration on the GWT server side & Java1.5 (as of GWT 1.5) on the GWT client side using a multi-module maven project.
 + GWT RPC structure via multi-module maven project - and inheriting a GWT module
 + The features offered by GWT-Maven (like building a WAR, running the GWTShell, mergewebxml, and GWT testing)
 
Overview:
=========
This sample package contains 4 different examples to show how GWT-Maven
can be used with your project. If you intend to use multiple modules, you will want to build 
more than one of the examples. If you wish to set up a simpler single module (with just one pom.xml), 
then choose the example that is closest to the function of your project (likely the WAR project).

This sample uses GWT-Maven with the "automatic" GWT setup mode enabled. This means
that you do *not* have to download and install GWT yourself, the plugin will 
do this for you once the correct dependencies (included the zipped native libs, which are in the central repo)
are included, and the dependency plugin is configured (as the sample WAR project demonstrates). 

Which project to build:
=======================
 + parent (this is the root parent module of the project)
 + rpc (this is the GWT RPC *INTERFACE* definition) Java 1.5
 + server (this is the GWT RPC *IMPLEMENTATION*, it builds a GWT module that war inherits) Java 1.5+
 + war (this is the GWT client code) Java 1.5

Prerequisites:
==============
 + Maven2 - http://maven.apache.org
 + Maven bin directory added to Path
 + This sample project checked out on your local filesystem


Running the parent example:
===========================
The parent example demonstrates a multi-module Maven build with a GWT project. 
When run it will build the rpc, server, and war sub projects. 

1. Use a command prompt to navigate into the maven-googlewebtoolkit2-sample directory.
2. Execute "mvn clean install".
3. Sub projects will have artifacts (see details for each below)


Running the WAR example:
========================
The WAR project builds a Web-Application-Archive file for a GWT project. 
This WAR can then be deployed to a servlet container, and or you can run this
project in the GWTShell using GWT-Maven.

This project demonstrates not only the usefulness of GWT-Maven in terms of 
building WAR files, and running the shell, but also the "merge web xml" aspect. 
Notice that this project has a local web.xml source file, that is used to create
a deployment time file that includes a standard servlet entry, as well as a GWT-RPC entry.
(GWT-Maven will "merge" and configure the embedded GWT shell Tomcat for you.)

This project also includes a testing sample using the GWT-Maven test goal. 
GWT testing is special because the Surefire plugin used for regular Maven testing
does not work with GWTTestCase tests (long story). Because of this GWT-Maven
has its own "test" goal which executes during the "test" phase. This goal 
builds a test script for each test in your project that starts with a specified name 
filter (default is "GwtTest"), and runs it. The tests results end up in "target/gwtTest".

To work with the WAR project, do the following:

1. Build the server sub-project, using the instructions in "Running the parent example" above.
2. Use a command prompt to navigate into the maven-googlewebtoolkit2-sample/war directory.
3. Execute "mvn package".
4. Your sample WAR will be created in the war/target directory.
   
  OR you can run the GWTShell locally by executing "mvn gwt-maven:gwt".
   
  Optionally: Execute "mvn test" on its own to run the tests separately 
  (both standard Surefire and GWT-Maven for GWTTestCase will execute).


Running the RPC example
========================
The RPC example is meant to show defining GWT-RPC interfaces.
This project creates a JAR archive that includes source, so that it can be used by other
GWT projects (the WAR example here uses it). This project does not need GWT-Maven 
(no shell to run, no GWT compiler, etc) - but does require GWT dependencies.

To work with the RPC project, do the following:

1. Use a command prompt to navigate into the maven-googlewebtoolkit2-sample/rpc directory.
2. Execute "mvn install".
3. Your JAR library, with source included, will be in the rpc/target directory.
 

Running the SERVER sample
=========================
The Server example demonstrates implementing GWT-RPC interfaces (the one from the RPC example),
creating a GWT library module (that other projects can inherit), and including a standard 
HttpServlet example in the same project. This project creates a JAR archive that is built for 
the server side of a GWT project. This JAR can then be inherited using GWT inheritance to expose 
a GWT-RPC endpoint. (This demonstrates that it can make sense to break up your GWT projects into 
client and server portions, and API and impl portions, sometimes that helps with re-use 
[more than one client can import and use the RPC] and with team divisions.)

To work with the SERVER project, do the following:

1. Use a command prompt to navigate into the maven-googlewebtoolkit2-sample/server directory.
2. Execute "mvn install".
3. Your JAR library, with source included, will be in the server/target directory.
 

More help:
==========
Docs: http://gwt-maven.googlecode.com/svn/docs/maven-googlewebtoolkit2-plugin/index.html
Also refer to the the GWT-Maven message board: http://groups.google.com/group/gwt-maven

