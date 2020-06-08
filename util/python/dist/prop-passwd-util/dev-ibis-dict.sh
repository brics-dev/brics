#!/bin/bash

./propPasswdUtil.py --srcDir=${BRICS_HOME}/local/prop/dev/ibis-dict \
    --trgDir=/var/lib/brics/remote/dev/ibis-dict \
    --logDir=/var/lib/brics/log/propPasswdUtil/dev-ibis-dict \
    --secFile=/home/buildman/secure/all.pass
