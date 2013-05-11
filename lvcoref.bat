@echo off
java -Xmx1G -Dfile.encoding=UTF-8 -cp dist\lvcoref.jar;lib\commons-lang3-3.1.jar LVCoref.LVCoref %*