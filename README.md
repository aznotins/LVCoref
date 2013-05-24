LVCOREF: Coreference resolution system for Latvian.

Arguments:
-props lvcoref.prop

Other arguments could be specified in prop file:
-lvcoref.sievePasses = ExactStringMatch,PreciseConstructs,RelaxedHeadMatch,PronounMatch
lvcoref.conll.input = data/test.conll 
lvcoref.mmax.gold = data/test_coref_level.xml
lvcoref.conll.output = data/test_out.conll
log = false
lvcoref.score = false
...
