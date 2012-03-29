#!/bin/bash -eu

if [ $(pgrep dcmsnd) ]
then
	echo "Import is already running" >>$1/import.log
	exit
fi

echo "(Re)starting import at $(date) ($(wc -l < $1/import.txt) remaining)" >>$1/import.log

while :
do
	DIR=$(head -n1 $1/import.txt)
	if [ -z "$DIR" ]
	then
		break
	fi
	echo -n "    Importing $DIR "  >>$1/import.log
	/opt/dcm4che-2.0.26/bin/dcmsnd DCM4CHEE@localhost:11112 $DIR >>$1/import.stdout 2>>$1/import.stderr
	echo "done" >>$1/import.log
	sed -i -e'1d' $1/import.txt
done

echo "Import complete" >>$1/import.log