GWT-Maven Archetype Info/Usage
===============================

Creates a bare bones GWT app with all the GWT-Maven 
setup and structure in place (a template). 


1. Installing the archetype
---------------------------

You have several choices on how to install the archetype, you 
can just add the GWT-Maven repository to an active profile
and then use the archetype (it's in the GWT-Maven repo), 
or you can download the file and install it in your local repository. 

Example add the GWT-Maven repository to a profile in your ~/.m2/settings.xml file:

<settings>
  ...
  <profiles>
    ...
    <profile>
      <id>gwt-maven-repo-profile</id>
      <repositories> 
        <repository>
            <id>gwt-maven</id>
            <url>http://gwt-maven.googlecode.com/svn/trunk/mavenrepo/</url>
        </repository>
      </repositories>
    </profile>
    ...
  </profiles>
  ...
  <activeProfiles>
    ...
    <activeProfile>gwt-maven-repo-profile</activeProfile>
    ...
  </activeProfiles>
  ...
</settings>


Example download the file and install it:

Download the file from: 
http://gwt-maven.googlecode.com/svn/trunk/mavenrepo/com/totsp/gwt/maven-googlewebtoolkit2-archetype/VERSION/maven-googlewebtoolkit2-archetype-VERSION.jar

 mvn install:install-file \
   -DgroupId=com.totsp.gwt \
   -DartifactId=maven-googlewebtoolkit2-archetype \
   -Dversion=VERSION \
   -Dpackaging=maven-archetype 
   -Dfile=PATH_TO_JAR_YOU_DOWNLOADED/maven-googlewebtoolkit2-archetype-VERSION.jar


2.  Using the archetype 
-------------------------

 mvn archetype:create \
   -DarchetypeGroupId=com.totsp.gwt \
   -DarchetypeArtifactId=maven-googlewebtoolkit2-archetype \
   -DarchetypeVersion=1.0 \
   -DgroupId=myGroupId \
   -DartifactId=myArtifactId
