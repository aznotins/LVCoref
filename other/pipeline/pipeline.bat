@echo off

set tagger_dir="../LVTagger"
set parser_dir="../maltparser"
set ner_dir="../LVTagger"
set lvcoref_dir=".."
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
		set output=%data_dir%/output.txt
	) else (
		set output=%data_dir%/%2		
	)	
	set output_html=%data_dir%/output.html
	
	
:: Run morphotagger
:: Remove empty lines
if exist input.tmp (del input.tmp)
for /f "tokens=* delims= " %%a in (%input%) do echo %%a >> %data_dir%/input.tmp
cd %tagger_dir%
java -Xmx1G -Dfile.encoding=UTF8 -cp dist/CRF.jar;dist/morphology.jar;lib/json-simple-1.1.1.jar WordPipe -conll-x < %data_dir%/input.tmp > %data_dir%/tagged.tmp


:: Run parser
cd %parser_dir%
java -Xmx1G -jar maltparser-1.7.1.jar -c lazy-pos-new-all -i %data_dir%/tagged.tmp -a stacklazy -o %data_dir%/parsed.tmp -F StackSwapLemmaLV.xml -m parse


:: Run NER tagger
php %home_dir%/ner_prepare.php %data_dir%/parsed.tmp %data_dir%/ner_prepared.tmp
(
	echo testFile = %data_dir%/ner_prepared.tmp
	echo loadClassifier = lv-ner-model.ser.gz
	echo inputEncoding = utf-8
	echo outputEncoding = utf-8
	echo map = word=0,tag=1,lemma=2,answer=3
) > %data_dir%/ner.prop.tmp
cd %ner_dir%
java -mx1g -Dfile.encoding=utf-8 -cp dist/CRF.jar edu.stanford.nlp.ie.crf.CRFClassifier -prop %data_dir%/ner.prop.tmp > %data_dir%/ner.tmp

cd %lvcoref_dir%
java -Xmx1G -Dfile.encoding=UTF-8 -cp dist/lvcoref.jar;lib/commons-lang3-3.1.jar LVCoref.LVCoref -log --conllInput %data_dir%/parsed.tmp --htmlOutput %output_html% --conllOutput %output% --nerAnnotation %data_dir%/ner.tmp

cd %home_dir%
:END
pause

