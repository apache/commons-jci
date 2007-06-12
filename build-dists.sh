#!/bin/sh

set -e

BASE=`pwd`

RC=`xml sel -N m=http://maven.apache.org/POM/4.0.0 -t -v '/m:project/m:properties/m:release.tag' $BASE/pom.xml`
STAGING=`xml sel -N m=http://maven.apache.org/POM/4.0.0 -t -v "/m:project/m:profiles/m:profile[m:id/text() = 'release']/m:distributionManagement/m:repository/m:url" $BASE/pom.xml | sed "s/\\${commons.deployment.protocol}:\/\//$USER@/" | sed "s/\\${release.tag}/$RC/" | sed 's#/#:/#'`
RELEASE=`echo $RC|sed s/-.*//`

echo "creating $RELEASE from $RC at $STAGING"

MAVEN_RELEASE="$BASE/target/maven-dist"
DIST="$BASE/target/dist"

rm -R $MAVEN_RELEASE 2>/dev/null || true
rm -R $DIST 2>/dev/null || true

scp -r $STAGING $MAVEN_RELEASE


# build binary dist

LIB="$DIST/bin/lib"
mkdir -p $LIB 2>/dev/null

JARS=`find $MAVEN_RELEASE -type f -name "*-$RELEASE.jar"`

for A in $JARS ; do
    cp $A $LIB
done

cp $BASE/LICENSE.txt $BASE/NOTICE.txt $DIST/bin

cd $DIST/bin

tar czvf $BASE/target/commons-jci-$RELEASE-bin.tar.gz .
zip -r $BASE/target/commons-jci-$RELEASE-bin.zip .

cd -




# build source dist

SOURCE=`xml sel -N m=http://maven.apache.org/POM/4.0.0 -t -v "/m:project/m:scm/m:developerConnection" $BASE/pom.xml | sed 's/scn:svn://' | sed "s/trunk/tags\/$RC/"`

mkdir -p $DIST/src 2>/dev/null
cd $DIST/src

svn co $SOURCE .


tar czvf $BASE/target/commons-jci-$RELEASE-src.tar.gz --exclude .svn --exclude target --exclude dist.sh .
zip -r $BASE/target/commons-jci-$RELEASE-src.zip . -x "*.svn/*" -x "target/*"


# sign

ARTIFACTS="`find $BASE/target -name *.zip -maxdepth 1` `find $BASE/target -name *.tar.gz -maxdepth 1`"

RUNNING=`ps -ax | grep gpg-agent | grep -v grep`
if [ -z "$RUNNING" ]; then
  echo "starting gpg-agent..."
  gpg-agent --daemon --use-standard-socket > $HOME/.gnupg/.gpg-agent
fi

export GPG_AGENT_INFO="$HOME/.gnupg/S.gpg-agent:4559:1"

for A in $ARTIFACTS ; do
  echo $A
  rm $A.asc* 2>/dev/null || true
  gpg --armor --output $A.asc --detach-sig $A
  openssl md5 < $A > $A.md5
  openssl sha1 < $A > $A.sha1
done

for A in $ARTIFACTS ; do
  echo $A
  gpg --verify $A.asc $A
done
