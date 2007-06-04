In order to build commons-jci please use subversion to check it out via

 svn co http://svn.apache.org/repos/asf/jakarta/commons/proper/jci/trunk/

You need to have maven 2.0.x installed. All you need to do is to call

 mvn clean install

Releases are done with

 mvn release:prepare -Prelease
 mvn release:perform -Prelease

It is suggested to be using a ssh and gpg agent for the release process.
