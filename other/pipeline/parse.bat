@echo off

set tagger_dir="D:/Eclipse/LVTagger"
set parser_dir="D:/Work/maltparser"
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
		set output=%data_dir%/parsed.tmp
	) else (
		set output=%data_dir%/%2		
	)
	
	
:: Run morphotagger
:: Remove empty lines
if exist input.tmp (del input.tmp)
for /f "tokens=* delims= " %%a in (%input%) do echo %%a >> %data_dir%/input.tmp
cd %tagger_dir%
java -Xmx1G -Dfile.encoding=UTF8 -cp dist/CRF.jar;dist/morphology.jar;lib/json-simple-1.1.1.jar WordPipe -conll-x < %data_dir%/input.tmp > %data_dir%/tagged.tmp

:: Run parser
cd %parser_dir%
java -Xmx1G -jar maltparser-1.7.1.jar -c lazy-pos-new-all -i %data_dir%/tagged.tmp -a stacklazy -o %output% -F StackSwapLemmaLV.xml -m parse

cd %home_dir%
:END
pause

