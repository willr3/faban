#!/bin/sh

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# -----------------------------------------------------------------------------
# Start Script for the CATALINA Server
#
# $Id: startup.sh 1202062 2011-11-15 06:50:02Z mturk $
# -----------------------------------------------------------------------------

# Better OS/400 detection: see Bugzilla 31132
os400=false
case "`uname`" in
OS400*) os400=true;;
esac

# resolve links - $0 may be a softlink
PRG="$0"
PWDR=`pwd`
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

JAVA_VER_STRING=`${JAVA_HOME}/bin/java -version 2>&1`
JAVA_VERSION=`echo $JAVA_VER_STRING | \
               awk '{ print substr($3, 2, length($3) - 2)}'`
HOST=`hostname`
PRGDIR=`dirname "$PRG"`


# The IBM JVM does not want the contents of the endorsed dir, others do.
unendorse() {
    cd "$PRGDIR"/../endorsed
    FILECOUNT=`ls | wc -l`
    if [ "$FILECOUNT" -gt 0 ] ; then
        cd ..
        rm -rf unendorsed
        mv endorsed unendorsed
        mkdir endorsed
    fi
}

endorse() {
    cd "$PRGDIR"/../endorsed
    FILECOUNT=`ls | wc -l`
    if [ "$FILECOUNT" -eq 0 ] ; then
        cd ..
        if [ -d unendorsed ] ; then
            rmdir endorsed
            mv unendorsed endorsed
        else
            echo "WARNING: Cannot find endorsed jars!" >&2
        fi
    fi
}

case $JAVA_VER_STRING in
    *IBM*) unendorse;;
    *)     endorse;;
esac

JAVA_OPTS="-Xms64m -Xmx1024m -Djava.awt.headless=true"
export JAVA_OPTS

EXECUTABLE=catalina.sh

# Check that target executable exists
if $os400; then
  # -x will Only work on the os400 if the files are:
  # 1. owned by the user
  # 2. owned by the PRIMARY group of the user
  # this will not work if the user belongs in secondary groups
  eval
else
  if [ ! -x "$PRGDIR"/"$EXECUTABLE" ]; then
    echo "Cannot find $PRGDIR/$EXECUTABLE"
    echo "The file is absent or does not have execute permission"
    echo "This file is needed to run this program"
    exit 1
  fi
fi

echo "Starting Faban Server"

# cd to logs to place chiba.log in logs directory
# TODO set chiba.log location in configuration
#
cd "$PRGDIR"/../logs
echo "Please point your browser to host://$HOST:9980/"
exec ../bin/"$EXECUTABLE" start "$@"
