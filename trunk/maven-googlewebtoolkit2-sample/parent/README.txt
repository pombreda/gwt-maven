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
1. If you have a linux or a mac machine, edit the <properties> at the top of parent/pom.xml to replace every occurrence of "windows" with "mac" or "linux"
2. Use a command prompt to navigate into the maven-googlewebtoolkit2-sample/war directory.
3. Execute "mvn package".
4. Your sample war will be created in the war/target directory.
   OR You can view the resulting GWT application in target/maven-googlewebtoolkit2-sample/com.totsp.mavenplugin.gwt.sample.Application by opening Application.html with a browser.

//@todo: instructions for running multi-module project
//@todo: add instructions for settings.xml for mac users

More help:
==========
Refer to the the gwt-maven message board at http://groups.google.com/group/gwt-maven





Project tasks:
==============
 + get profiles.xml working with poms so that ${gwt.dist} and ${gwt.version} get properly populated to eliminate instruction #1 above.
 + add sample tests that will run when "mvn test" is executed
 + get sample webapp in war running
 + replace various hard coded strings in poms using <properties> tag
 + make sure latest version of all libs are being used (e.g. gwt-user)
 + rename <gwt.dist> and <gwt.dev>
 + clean up generated classpath (lots of extraneous stuff currently in there)
 + gwt-dev dependency seems to be duplicated in parent pom (under artifact items) and war pom as a dependency.
 + use version 2 of gwt-maven plugin

