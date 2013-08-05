#!/bin/bash -ex

prog="mridb"

rm -fr /tmp/$prog
mkdir /tmp/$prog
git archive master | tar -x -C /tmp/$prog
REV=$(git rev-parse --short HEAD)
echo $(date +%Y%m%d)r$REV >/tmp/$prog/VERSION
touch /tmp/$prog/conf/application-site.conf
play dependencies /tmp/$prog --forceCopy --forProd
play precompile /tmp/$prog
rm /tmp/$prog/conf/application-site.conf
tar cfj /tmp/$prog-r$REV.tar.bz2 --exclude repo --exclude test -C /tmp $prog

if [ "ld-woodbridge.bc.ic.ac.uk" == "$HOSTNAME" ]; then
	DEST=/data/www/80/cisbic.bioinformatics.ic.ac.uk/htdocs/files/$prog
	scp /tmp/$prog-r$REV.tar.bz2 bss-srv4:$DEST
	ssh bss-srv4 "sed -i 's/-r[0-9a-f]\+/-r$REV/' $DEST/.htaccess"
fi
