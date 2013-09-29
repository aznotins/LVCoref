# LVCoref

## Running
Run lvcoref.sh (.bat). 

### Arguments

Flag | Description
---|---
lvcoref.sievePasses | SievePasses used for coreference resolution
lvcoref.score = false | Score
lvcoref.logFile | Log file location
lvcoref.log = false | Keep log file
lvcoref.maxdist | Maximum sentence distance between two corefering mentions (used for optimization)
lvcoref.input = conll | Input format (conll, json)
locoref.ouput = conll | Output format (conll, json)
lvcoref.mmax.export = false | Export MMAX project
lvcoref.mmax.export.path | Path to output MMAX project
lvcoref.mmax.export.name | MMAX project name
lvcoref.html.output = "out.html" | Simple html coreference visualization
lvcoref.mmax.gold | Mmax format files with tagged coreferences for evaluation (seperated by commas)

## Input
LVCoref supports simple conll and json formats:
Conll format includes standart conll columns (ID, FORM, LEMMA, CPOSTAG, POSTAG, FEATS, HEAD, NER).

## Output
Output conll format changes NER column (leaving NER label only for NE head), and appends ENTITY column that contains local entity ID (number for mention head and underscore otherwise).
