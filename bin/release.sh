#!/bin/bash -ex

prog="mridb"

rm -fr /tmp/$prog

REV=$(svn export https://cisbic.bioinformatics.ic.ac.uk/svn/MRIdb/$prog /tmp/$prog | grep "^Exported revision" | egrep -o "[0-9]+")
DEST=/data/www/80/cisbic.bioinformatics.ic.ac.uk/htdocs/files/$prog

#tar cfj /tmp/$prog-r$REV-src.tar.bz2 -C /tmp $prog
#scp /tmp/$prog-r$REV-src.tar.bz2 bss-srv4:$DEST

echo $(date +%Y%m%d)r$REV >/tmp/$prog/VERSION
touch /tmp/$prog/conf/application-site.conf
play dependencies /tmp/$prog --forceCopy --forProd
play precompile /tmp/$prog
#find /tmp/$prog/app -mindepth 1 -maxdepth 1 ! -iname views -exec rm -r {} \;
rm /tmp/$prog/conf/application-site.conf
tar cfj /tmp/$prog-r$REV.tar.bz2 --exclude repo --exclude test -C /tmp $prog
scp /tmp/$prog-r$REV.tar.bz2 bss-srv4:$DEST

ssh bss-srv4 "sed -i 's/-r[0-9]\+/-r$REV/' $DEST/.htaccess"
