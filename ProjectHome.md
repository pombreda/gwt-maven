# Maven 2 and Maven 1 support for GWT #

NOTE/UPDATE: This project is deprecated/discontinued. New users should consider looking at the Codehaus gwt-maven-plugin (mojo.codehaus.org/gwt-maven-plugin/). The codehaus plugin merged in the code from this plugin (maven-googlewebtoolkit2-plugin) at one point and supports most of the same goals (and should be up to date). Fore more information see the mailing lists and various threads on the status and difference between the projects: http://groups.google.com/group/gwt-maven/browse_thread/thread/6ff5102700e6de3c.

[Maven](http://maven.apache.org/) is a build and "project management" tool developed by the Apache Software Foundation. GWT-Maven is a "plug-in" that extends Maven to add GWT related support and goals.

There are multiple versions of GWT-Maven, a Maven 2.x version and a Maven 1.1 version.

Both versions have the same set of basic goals (which are optional):

  * Run the GWTShell (Hosted mode, both with embedded Tomcat and -noserver)
  * Run the GWTShell and connect to it with a debugger
  * Run the GWTCompiler
  * Run GWTTestCase and GWTTestSuite based tests
  * Generate I18N Constants and Messages interfaces
  * Creation of Web Application Archive (WAR) deployable files for GWT projects
  * Creation of Java Application Archive (JAR) files for GWT module/library projects
  * Automatically download and extract GWT on build machines


---


## Documentation ##

**Maven2 docs**: **[docs/maven-googlewebtoolkit2-plugin](http://gwt-maven.googlecode.com/svn/docs/maven-googlewebtoolkit2-plugin/index.html)**

**Maven1 docs**: **[docs/maven-googlewebtoolkit-plugin](http://gwt-maven.googlecode.com/svn/docs/maven-googlewebtoolkit-plugin/index.html)**


---


## Source ##

**Maven2 source**: **[maven-googlewebtoolkit2-plugin](http://gwt-maven.googlecode.com/svn/trunk/maven-googlewebtoolkit2-plugin/)**

**Maven1 source**: **[maven-googlewebtoolkit-plugin](http://gwt-maven.googlecode.com/svn/trunk/maven-googlewebtoolkit-plugin/)**

**Support JAR** (used by both versions to perform common tasks): **[maven-googlewebtoolkit-support](http://gwt-maven.googlecode.com/svn/trunk/maven-googlewebtoolkit-support/)**


---


## Sample projects ##

**Maven2**
  * simple single project: **[simplesample](http://gwt-maven.googlecode.com/svn/trunk/maven-googlewebtoolkit2-plugin/simplesample)**

  * single project with GWT-RPC and a database: **[simpledatasample](http://gwt-maven.googlecode.com/svn/trunk/maven-googlewebtoolkit2-plugin/simpledatasample)**

  * multi module project with GWT-RPC: **[maven-googlewebtoolkit2-sample](http://gwt-maven.googlecode.com/svn/trunk/maven-googlewebtoolkit2-sample/)**

**Maven1**:
  * sample with GWT-RPC: **[maven-googlewebtoolkit-sample](http://gwt-maven.googlecode.com/svn/trunk/maven-googlewebtoolkit-sample/)**


---


## Wiki ##

The **[Wiki](http://code.google.com/p/gwt-maven/w/list)** also has additional information, including FAQs and upcoming plans.


---


## Maven Repository ##

Used for GWT-Maven and other related libraries.

**http://gwt-maven.googlecode.com/svn/trunk/mavenrepo/**

Repo has M1 and M2 style layout within it. In the future the repo may be moved and re-organized into separate M1/M2 repos.
