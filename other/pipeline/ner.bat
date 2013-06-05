@echo off

set tagger_dir="D:/Eclipse/LVTagger"
set parser_dir="D:/Work/LVCoref/lib/maltparser"
set ner_dir="D:/Eclipse/LVTagger"
set lvcoref_dir="D:/Work/LVCoref/"
set home_dir=%CD%
set data_dir=%CD%

set home_dir=%home_dir:\=/%
set data_dir=%data_dir:\=/%

:BEGIN
    REM if "%1"=="-h" goto help
	if "%1"=="" (
		set input=%data_dir%/input.txt
	) else (
		set input=%data_dir%/%1
	)
	if "%2"=="" (
		set output=%data_dir%/ner.tmp
	) else (
		set output=%data_dir%/%2		
	)	
	
:: Run morphotagger
:: Remove empty lines
if exist input.tmp (del input.tmp)
for /f "tokens=* delims= " %%a in (%input%) do echo %%a >> %data_dir%/input.tmp
cd %tagger_dir%
java -Xmx1G -Dfile.encoding=UTF8 -cp dist/CRF.jar;dist/morphology.jar;lib/json-simple-1.1.1.jar WordPipe -conll-x < %data_dir%/input.tmp > %data_dir%/tagged.tmp


:: Run NER tagger
php %home_dir%/ner_prepare.php %data_dir%/tagged.tmp %data_dir%/ner_prepared.tmp
(
	echo testFile = %data_dir%/ner_prepared.tmp
	echo loadClassifier = lv-ner-model.ser.gz
	echo inputEncoding = utf-8
	echo outputEncoding = utf-8
	echo map = word=0,tag=1,lemma=2,answer=3
) > %data_dir%/ner.prop.tmp

cd %ner_dir%
java -mx1g -Dfile.encoding=utf-8 -cp dist/CRF.jar edu.stanford.nlp.ie.crf.CRFClassifier -prop %data_dir%/ner.prop.tmp > %output%

cd %home_dir%
:END
pause