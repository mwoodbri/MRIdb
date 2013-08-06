#!/bin/bash -eu

cd "$(dirname "$0")"/..

prog="mridb"
VERSION=$(date +%Y%m%d)r$(git rev-parse --short HEAD)

rm -fr /tmp/$prog
mkdir /tmp/$prog
git archive master | tar -x -C /tmp/$prog
echo $VERSION >/tmp/$prog/VERSION
touch /tmp/$prog/conf/application-site.conf
play dependencies /tmp/$prog --forceCopy --forProd
play precompile /tmp/$prog
rm /tmp/$prog/conf/application-site.conf
tar cfj /tmp/$prog-$VERSION.tar.bz2 --exclude repo --exclude test -C /tmp $prog
