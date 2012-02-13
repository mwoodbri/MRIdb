#!/bin/bash -ex
rm -fr /tmp/MRIdb
REV=$(svn export https://cisbic.bioinformatics.ic.ac.uk/svn/MRIdb/mridb /tmp/MRIdb | grep "^Exported revision" | egrep -o "[0-9]+")
play dependencies /tmp/MRIdb --forceCopy --forProd
play precompile /tmp/MRIdb
tar cfz /tmp/MRIdb-r$REV.tar.gz -C /tmp MRIdb

DEST=/data/www/80/cisbic.bioinformatics.ic.ac.uk/htdocs/static/mridb
scp /tmp/MRIdb-r$REV.tar.gz bss-srv4:$DEST
ssh bss-srv4 "sed -i 's/r[0-9]\+/r$REV/' $DEST/.htaccess"