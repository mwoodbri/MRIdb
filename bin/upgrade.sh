#!/bin/bash -ex

prog="mridb"

service $prog stop

cd /tmp
curl -LO http://cisbic.bioinformatics.ic.ac.uk/static/$prog/$prog-latest.tar.bz2
tar xf /tmp/$prog-latest.tar.bz2 -C /opt
rm -f /tmp/$prog-latest.tar.bz2

service $prog start
