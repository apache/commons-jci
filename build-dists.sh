#!/bin/sh

TMP="`pwd`/target"
DIST="$TMP/dist"
LIB="$DIST/lib"

find $TMP -name *.zip -delete
find $TMP -name *.tar.gz -delete
rm -R $DIST 2>/dev/null


# build source dist

tar czvf $TMP/commons-jci-1.0-src.tar.gz --exclude .svn --exclude target --exclude dist.sh .
zip -r $TMP/commons-jci-1.0-src.zip . -x "*.svn/*" -x "target/*"



# build binary dist

mkdir -p $LIB 2>/dev/null

JARS=`find target -type f -name "*-1.0.jar"`

for A in $JARS ; do
    cp $A $LIB
done

cp LICENSE.txt NOTICE.txt $DIST



cd $DIST

tar czvf $TMP/commons-jci-1.0-bin.tar.gz .
zip -r $TMP/commons-jci-1.0-bin.zip .





ARTIFACTS="$TMP/commons-jci-1.0-bin.tar.gz $TMP/commons-jci-1.0-bin.zip $TMP/commons-jci-1.0-src.tar.gz $TMP/commons-jci-1.0-src.zip"


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


for A in $TGZS $ZIPS ; do
  echo $A
  gpg --verify $A.asc $A
done
