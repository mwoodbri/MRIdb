#!/bin/bash -u

echo >>$1/import.log

if [ $(pgrep dcmsnd) ]
then
	echo "Import is already running" >>$1/import.log
	exit
fi

if [ ! -f "$1/import.txt" ]
then
	echo "Import list doesn't exist" >>$1/import.log
	exit
fi

echo "(Re)starting import at $(date) ($(wc -l < $1/import.txt) remaining)" >>$1/import.log

while :
do
	DIR=$(head -n1 $1/import.txt)
	echo -n "    Importing $DIR " >>$1/import.log
	if find $DIR -type f -exec file -ib {} + 2>/dev/null | grep dicom >/dev/null
	then
		/opt/dcm4che-2.0.26/bin/dcmsnd DCM4CHEE@localhost:11112 $DIR >>$1/import.stdout 2>>$1/import.stderr && echo done >>$1/import.log && sed -i -e'1d' $1/import.txt || echo failed >>$1/import.log
	else
		echo ignored >>$1/import.log && sed -i -e'1d' $1/import.txt
	fi
done

echo "Import complete" >>$1/import.log