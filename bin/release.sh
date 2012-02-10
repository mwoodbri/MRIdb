#!/bin/bash -ex
rm -fr /tmp/MRIdb
REV=$(svn export https://cisbic.bioinformatics.ic.ac.uk/svn/MRIdb/mridb /tmp/MRIdb | grep "^Exported revision" | egrep -o "[0-9]+")
play dependencies /tmp/MRIdb --forceCopy --forProd
play precompile /tmp/MRIdb
tar cfz /tmp/MRIdb-r$REV.tar.gz -C /tmp MRIdb