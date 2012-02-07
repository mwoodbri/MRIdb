#!/bin/bash -ex
rm -fr /tmp/MRIdb
svn export https://cisbic.bioinformatics.ic.ac.uk/svn/MRIdb/mridb /tmp/MRIdb
play dependencies /tmp/MRIdb --forceCopy --forProd
play precompile /tmp/MRIdb
tar cfz /tmp/MRIdb-r$(svn info ~/workspace/MRIdb | grep "^Revision: " | cut -d' ' -f2).tar.gz -C /tmp MRIdb