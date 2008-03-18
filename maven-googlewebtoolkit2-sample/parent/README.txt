WELCOME to the maven-googlewebtoolkit2-sample Project
=====================================================

About:
======
This sample project has some specific goals:
 + GWT-Maven 'Best Practice' & Usage
 + Java5+ integration on the GWT server side & Java1.4 on the GWT client side using a multi-module maven project.
 + GWT RPC structure via multi-module maven project
 + The features offered by GWT-Maven (like mergexml)
 
Overview:
=========
This sample package contains 5 different examples to show how maven-gwt should be used with your project. If you intend to use multiple modules, you will want to build more than one of the examples. If you wish to set up a simpler single module (with just one pom.xml), then choose the example that is closest to the function of your project.
//@todo: expand this description

Which project to build:
=======================
 + parent (this is the root parent module of the project)
 + rpc (this is the GWT RPC *INTERFACE)   Java 1.4
 + server (this is the GWT RPC *IMPLEMENTATION) Java 1.5+
 + war (this is the GWT client code) Java 1.4 

Prerequisites:
==============
 + Maven2  - http://maven.apache.org
 + Maven bin directory added to Path.

Running the war example:
========================
1. Use a command prompt to navigate into the maven-googlewebtoolkit2-sample/war directory.
2. Execute "mvn package".
3. Your sample war will be created in the war/target directory.

//@todo: instructions for running multi-module project

More help:
==========
Refer to the the gwt-maven message board at http://groups.google.com/group/gwt-maven





Project tasks:
==============
 + add sample tests that will run when "mvn test" is executed

