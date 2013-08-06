#!/bin/bash -eu

cd "$(dirname "$0")"/..

prog="mridb"
REV=$(git rev-parse --short HEAD)

rm -fr /tmp/$prog
mkdir /tmp/$prog
git archive master | tar -x -C /tmp/$prog
echo $(date +%Y%m%d)r$REV >/tmp/$prog/VERSION
touch /tmp/$prog/conf/application-site.conf
play dependencies /tmp/$prog --forceCopy --forProd
play precompile /tmp/$prog
rm /tmp/$prog/conf/application-site.conf
tar cfj /tmp/$prog-r$REV.tar.bz2 --exclude repo --exclude test -C /tmp $prog
