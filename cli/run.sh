#!/bin/bash

TESTFILE="$HOME/test/HelloWorld.dex"

PREFIX="$HOME/android_tools"

CAFEDRAGON_SOURCE="$PREFIX/cafedragon"
CAFEDRAGON_DIRECTORY="$PREFIX/build"
SOOT_SOURCE="$PREFIX/soot"
CLEANSOOT_DIRECTORY="$SOOT_SOURCE/target"
DROIDCAFE_WORKDIR="$SOOT_SOURCE/cli"
ANDROID_ROOT="$HOME/Android/platforms"



while getopts "csdb" o; do
    case "${o}" in
        c)
            pushd $CAFEDRAGON_SOURCE
            JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64 ./sbrella_setup --target-directory $CAFEDRAGON_DIRECTORY --debug-build
            popd
            ;;
        s) 
            pushd $SOOT_SOURCE
            #mvn compile package -DskipTests
            mvn clean compile package -DskipTests
            cp $CLEANSOOT_DIRECTORY/sootclasses-trunk-jar-with-dependencies.jar $CAFEDRAGON_DIRECTORY/
            popd
            ;;
        d)
            pushd $CLEANSOOT_DIRECTORY
            # Output Under sootOutput/
            java -cp sootclasses-trunk-jar-with-dependencies.jar soot.Main -w -allow-phantom-refs -debug-resolver -debug -v -android-jars $ANDROID_ROOT -src-prec apk -f jimple -process-dir $TESTFILE
            popd
            ;;
        b)
            pushd $DROIDCAFE_WORKDIR 
            javac -cp "$CLEANSOOT_DIRECTORY/sootclasses-trunk-jar-with-dependencies.jar:$CAFEDRAGON_DIRECTORY/lib/cafedragon-compiler.jar:$CAFEDRAGON_DIRECTORY/lib/json-simple-1.1.1.jar" Main.java
            java -Djava.library.path=$CAFEDRAGON_DIRECTORY/lib/ -cp ".:$CLEANSOOT_DIRECTORY/sootclasses-trunk-jar-with-dependencies.jar:$CAFEDRAGON_DIRECTORY/lib/cafedragon-compiler.jar:$CAFEDRAGON_DIRECTORY/lib/json-simple-1.1.1.jar" Main -android-jars $ANDROID_ROOT -process-dir $TESTFILE 
            popd
            ;;
        *)
            printf "
            Usage: ./run.sh <-c:  Build CafeDragon>\n
                            <-s:  Build Clean Soot>\n
                            <-d:  Dump Test Dex File>\n
                            <-b:  Build DroidCafe Cli>\n\n"
            ;;
    esac

done
