#!/bin/sh

cd $(dirname $0)
java -Xmx1G -Dfile.encoding=UTF-8 -cp dist/lvcoref.jar:dist/morphology.jar:dist/CRF.jar:lib/* LVCoref.LVCoref $*
