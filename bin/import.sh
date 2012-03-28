#!/bin/sh

PATH=/opt/redis-2.4.9/bin:/opt/dcm4che-2.0.26/bin:$PATH

if [ $(pgrep redis-server) ]
then
	echo "Import is already running"
	exit
fi

redis-server - <<EOF
appendonly yes
daemonize yes
dir $1
EOF

echo "(Re)starting import ($(redis-cli llen dirs) remaining)"

while :
do
	DIR=$(redis-cli lindex dirs 0)
	if [ -z "$DIR" ]
	then
		break
	fi
	echo -n "    Importing $DIR "
	#dcmsnd DCM4CHEE@localhost:11112 $DIR >>$1/import.stdout 2>>$1/import.stderr
	echo $DIR >>$1/import.stdout 2>>$1/import.stderr
	echo "done"
	redis-cli lpop dirs >/dev/null
done

echo "Import complete"

redis-cli shutdown