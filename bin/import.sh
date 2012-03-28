#!/bin/sh

PATH=/opt/redis-2.4.9/bin:$PATH

if [ $(pgrep redis-server) ]
then
	echo "Import is already running"
	exit
fi

redis-server - <<EOF
appendonly yes
daemonize yes
dir /tmp
EOF

while :
do
	DIR=$(redis-cli lindex dirs 0)
	if [ -z "$DIR" ]
	then
		break
	fi
	redis-cli lpop dirs
done

redis-cli shutdown
