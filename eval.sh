#!/bin/sh

JAR=target/WyEval-1.0-SNAPSHOT.jar

DIR=../wyc/tests/valid

mkdir -p breakdown

for FILE in `find "$DIR" -name '*.whiley' -type f` ; do
    WYIL=`echo "$FILE" | sed -e 's/whiley$/wyil/'`
    WYAL=`echo "$FILE" | sed -e 's/whiley$/wyal/'`
    TARGET=`echo "$FILE" | sed -e 's/^.*\//breakdown\//'` 
    NAME=`echo "$FILE" | sed -r 's/^.*\/(.*).whiley$/\1/'`

    echo
    echo '============================================================'
    echo "-> $FILE"
    echo

    if ! grep -q -E 'while' "$FILE" ; then
        echo "Skipping no loops"
        continue
    fi
    
    echo 'Checking base code'

    if ! `java -jar "$JAR" -check $FILE` ; then
        echo "$NAME,0,0,0,0,0,0,0,failed_check" >> results.csv
        echo "Failed check"
        continue
    fi

    echo "Cleaning up $WYIL and $WYAL"
    rm -f "$WYIL" "$WYAL"

    echo 'Running breakdown'
    echo "$FILE -> $TAGET"
    java -jar $JAR -breakdown $FILE -o $TARGET
    if ! [ $? ] ; then
        echo '0,0,0,0,0,0,0,failed_break' >> results.csv
        echo '!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!'
        continue
    fi

    echo "Running minimize on $TARGET"
    java -jar $JAR -minimize $TARGET >> results.csv
    if ! [ $? ] ; then
        echo '0,0,0,0,0,0,0,failed_min' >> results.csv
        echo 'FAILED'
    fi

    echo "Running minimize with generated loop invariants on $TARGET"
    java -jar $JAR -minimize $TARGET -loopinv >> results.csv
    if ! [ $? ] ; then
        echo '0,0,0,0,0,0,0,failed_gen' >> results.csv
        echo 'FAILED'
    fi


done
