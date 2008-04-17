WELCOME to the maven-googlewebtoolkit2-sample Project
=====================================================

Also see GWT-Maven documentation:
http://gwt-maven.googlecode.com/svn/docs/maven-googlewebtoolkit2-plugin/index.html

About:
======
This sample project has some specific goals:
 + GWT-Maven 'Best Practice' & Usage
 + Java5+ integration on the GWT server side & Java1.4 on the GWT client side using a multi-module maven project.
 + GWT RPC structure via multi-module maven project - and inheriting a GWT module
 + The features offered by GWT-Maven (like building a WAR, running the GWTShell, mergewebxml, and GWT testing)
 
Overview:
=========
This sample package contains 4 different examples to show how GWT-Maven
should be used with your project. If you intend to use multiple modules, you will want to build 
more than one of the examples. If you wish to set up a simpler single module (with just one pom.xml), 
then choose the example that is closest to the function of your project (likely the war project).

This sample uses GWT-Maven with the "automatic" GWT setup mode enabled. This means
that you do *not* have to download and install GWT yourself, the plugin will 
setup and extract GWT for you (when using this method). 
(You can optionally use manual method with a local GWT install, see documentation.)

Which project to build:
=======================
 + parent (this is the root parent module of the project)
 + rpc (this is the GWT RPC *INTERFACE* definition) Java 1.4
 + server (this is the GWT RPC *IMPLEMENTATION*, it builds a GWT module that war inherits) Java 1.5+
 + war (this is the GWT client code) Java 1.4 

Prerequisites:
==============
 + Maven2 - http://maven.apache.org
 + Maven bin directory added to Path


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
This war can then be deployed to a servlet container, and or you can run this
project in the GWTShell using GWT-Maven.

This project demonstrates not only the usefulness of GWT-Maven in terms of 
building WAR files, and running the shell, but also the "merge web xml" aspect. 
Notice that this project has a local web.xml source file, that is used to create
a deployment time file that includes a standard servlet entry, as well as a GWT-RPC entry.
(GWT-Maven allows you to use the embedded Tomcat server with source web.xml and other
files [context.xml, if you use Tomcat outside of the shell], and it configures the
embedded Tomcat for you.)

This project also includes a testing sample using the GWT-Maven testGwt goal, and 
includes EMMA based code coverage and reports for GWTTestCase based tests. 
Code coverage with GWT is a bit tricky, but using the coverage patch JAR (as this project does)
and generating ONLY a report (not instrumentation data itself, which GWT does for you when patched)
along with AntRun to move things around, creates an automated build with testing and reports. 
(If you want to see the testing stuff, uncomment the "testGwt" goal in the POM, it is commented
out by default to keep things faster and simpler.)
(Note* - when running tests the Surefire plugin is NOT used for GwtTestCase based tests, rather
a special testGwt goal is included with GWT-Maven for GwtTestCase based tests, thus TWO test phases
will happen during "mvn test", one for Surefire standard tests, and one for testGwt GWT tests.)

1. Use a command prompt to navigate into the maven-googlewebtoolkit2-sample/war directory.
2. Execute "mvn package".
3. Your sample war will be created in the war/target directory.
   
   OR you can run the GWTShell locally by executing "mvn gwt:gwt".
   
  Optionally: Execute "mvn test site" (with testGwt goal enabled) to run test and get coverage. 
(See "target/site" directory for output.)


Running the RPC example
========================
The RPC example is meant to show defining GWT-RPC interfaces.
This project creates a JAR archive that includes source, so that it can be used by other
GWT projects (the WAR example here uses it). This project does not need GWT-Maven 
(no shell to run, no GWT compiler, etc) - but does require GWT dependencies.

1. Use a command prompt to navigate into the maven-googlewebtoolkit2-sample/rpc directory.
2. Execute "mvn install".
3. Your JAR library, with source included, will be in the rpc/target directory.
 

Running the SERVER sample
=========================
The Server example demonstrates implementing GWT-RPC interfaces (the one from the RPC example),
creating a GWT library module (that other projects can inherit), and including a standard 
HttpServlet example in the same project. This project creates
a JAR archive that is built for the server side of a GWT project. This JAR can then
be inherited using GWT inheritance to expose a GWT-RPC endpoint. 
(This demonstrates that it can make sense to break up your GWT projects into client and server portions, 
sometimes that helps with re-use [more than one client can import and use the RPC] and with team divisions.)

1. Use a command prompt to navigate into the maven-googlewebtoolkit2-sample/server directory.
2. Execute "mvn install".
3. Your JAR library, with source included, will be in the server/target directory.
 

More help:
==========
Docs: http://gwt-maven.googlecode.com/svn/docs/maven-googlewebtoolkit2-plugin/index.html
Also refer to the the gwt-maven message board: http://groups.google.com/group/gwt-maven

