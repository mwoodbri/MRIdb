#!/bin/bash -ex

prog="mridb"

wget -O /tmp/$prog-latest.tar.bz2 http://cisbic.bioinformatics.ic.ac.uk/static/$prog/$prog-latest.tar.bz2
tar xf /tmp/$prog-latest.tar.bz2 -C /opt
service $prog restart
rm -f /tmp/$prog-latest.tar.bz2
