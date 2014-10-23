/*******************************************************************************
 * Copyright 2013,2014 Institute of Mathematics and Computer Science, University of Latvia
 * Author: Artūrs Znotiņš
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package LVCoref;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import LVCoref.ScorerBCubed.BCubedType;
import LVCoref.sievepasses.DeterministicCorefSieve;
import LVCoref.util.Log;
import edu.stanford.nlp.io.StringOutputStream;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.StringUtils;
import edu.stanford.nlp.util.SystemUtils;

/**
 * Main process class
 * @author arturs
 */
public class LVCoref {
	private static final Logger log = Logger.getLogger( Log.class.getName() );
	static { Log.init(); }
    
    private enum inputTypes {CONLL, STDIN_JSON, STDIN_CONLL};
    private enum outputTypes {CONLL, STDOUT_CONLL, STDOUT_JSON, NONE, TEXT};
    public static Properties props;
    public static Dictionaries dictionaries;
    private static inputTypes inputType = inputTypes.STDIN_CONLL;
    private static outputTypes outputType = outputTypes.STDOUT_CONLL;
    
    public static List<String> inputConllList;

    private static String mmaxExportPath = "data/mmax2/";
    private static String mmaxExportProjectName = "";
    private static boolean mmaxExport = false;
    private static String logPath = "data/logs/";
    private static String conllInput = null;
    private static String conllOutput = null;
    private static String htmlOutput = null;
    
    private static DeterministicCorefSieve [] sieves; // array of used sieves
    private static String [] sieveClassNames;
    public static int currentSieve; // current sieve index
    
    public static String timeStamp = "";

    /** Scores for each pass */
    public static List<CorefScorer> scorePairwise;
    public static List<CorefScorer> scoreBcubed;
    public static List<CorefScorer> scoreMUC;

    public static List<CorefScorer> scoreSingleDoc;
    
    public static MentionScorer singleDocMentionScorer = new MentionScorer();
    public static MentionScorer mentionScorer = new MentionScorer();
    public static Statistics singleDocStats = new Statistics();
    public static Statistics stats = new Statistics();
    /** counter for links in passes (Pair<correct links, total links>)  */
    public static List<Pair<Integer, Integer>> linksCountInPass;
    public static int additionalCorrectLinksCount;
    public static int additionalLinksCount;
    
    
    public static void printHelp(){
        System.out.print(
            "--------------------\n"
            + "LVCoref: Latvian Coreference Resolver"
            + "\nParameters:"

            + "\n\t" + Constants.INPUT_PROP + ": input format (json, conll - default)"   
            + "\n\t" + Constants.OUTPUT_PROP + ": output format (json, none, conll - default)"  
            + "\n\t" + Constants.LOG_PROP + ": keep logs, defaults to false"
            + "\n\t" + Constants.LOG_PATH_PROP + ": directory path with trailing /, (default = data/logs/)"
            + "\n\t" + Constants.MMAX_EXPORT_PATH_PROP + ": MMAX export path with trailing / (default = data/mmax2/"
            + "\n\t" + Constants.MMAX_EXPORT_NAME_PROP + ": project name (if no specified timestamp is used"
            + "\n\t" + Constants.HTML_PROP + ": file path for html formatted coreference results, direcotory should include script.js, style.css"
            + "\n--------------------\n"
        );
        System.out.flush();
    }
      
    
    public static void initializeProperties(Properties properties) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
    	props = properties;
    	
        String inputTypeString = props.getProperty(Constants.INPUT_PROP, "conll");
        if (inputTypeString.equalsIgnoreCase("conll")) inputType = inputTypes.STDIN_CONLL;
        if (inputTypeString.equalsIgnoreCase("json")) inputType = inputTypes.STDIN_JSON;
        
        String outputTypeString = props.getProperty(Constants.OUTPUT_PROP, "conll");
        if (outputTypeString.equalsIgnoreCase("conll")) outputType = outputTypes.STDOUT_CONLL;
        if (outputTypeString.equalsIgnoreCase("json")) outputType = outputTypes.STDOUT_JSON;
        if (outputTypeString.equalsIgnoreCase("text")) outputType = outputTypes.TEXT;
        if (outputTypeString.equalsIgnoreCase("none")) outputType = outputTypes.NONE;
        
        conllInput = props.getProperty(Constants.CONLL_INPUT_LIST, null);
        
        mmaxExport = Boolean.parseBoolean(props.getProperty(Constants.MMAX_EXPORT_PROP, "false"));

        mmaxExport = Boolean.parseBoolean(props.getProperty(Constants.MMAX_EXPORT_PROP, "false"));
        mmaxExportPath = props.getProperty(Constants.MMAX_EXPORT_PATH_PROP, "");
        mmaxExportProjectName = props.getProperty(Constants.MMAX_EXPORT_NAME_PROP);
        
        htmlOutput = props.getProperty(Constants.HTML_PROP, "");

        if (Boolean.parseBoolean(props.getProperty("h", "false")) || Boolean.parseBoolean(props.getProperty("help",  "false"))) { printHelp(); System.exit(0); }
        
        currentSieve = -1;
        String sievePasses = props.getProperty(Constants.SIEVES_PROP, Constants.SIEVEPASSES);
        sieveClassNames = sievePasses.trim().split(",\\s*");
        sieves = new DeterministicCorefSieve[sieveClassNames.length];
        for(int i = 0; i < sieveClassNames.length; i ++){
            sieves[i] = (DeterministicCorefSieve) Class.forName("LVCoref.sievepasses."+sieveClassNames[i]).getConstructor().newInstance();
            sieves[i].init(props);
        }
        
        timeStamp = Calendar.getInstance().getTime().toString().replaceAll("\\s", "-").replaceAll(":", "-");
        
        log.severe(props.toString());
        Constants.printConstants(); //output constants to console
        
        dictionaries = new Dictionaries();
    }
    
    public static void initScorers() {
        linksCountInPass = new ArrayList<Pair<Integer, Integer>>();
        scorePairwise = new ArrayList<CorefScorer>();
        scoreBcubed = new ArrayList<CorefScorer>();
        scoreMUC = new ArrayList<CorefScorer>();
        for(int i = 0 ; i < sieveClassNames.length ; i++){
            scorePairwise.add(new ScorerPairwise());
            scoreBcubed.add(new ScorerBCubed(BCubedType.Ball));
            scoreMUC.add(new ScorerMUC());
            linksCountInPass.add(new Pair<Integer, Integer>(0, 0));
        }  
    }
    
    public static void parserScoreCompare() {
    	StringBuilder summary = new StringBuilder();
    	String baseDir = "eval/lrec_coref_bestner";
    	//String baseDir = "eval/lrec_coref_ner";
    	File dir = new File(baseDir);
    	for (File f : dir.listFiles()) {
    		if (f.isDirectory()) {
    			scoreDirectory(f.getPath(), f.getPath() + ".conll");
    			summary.append(f.getName()).append("\t");
    			summary.append(getF1(true));
    		}
    	}
    	log.info("===== PARSER COMPARE SUMMARY =====");
    	log.info(summary.toString());
    }
    
    public static void scoreDirectory(String path) {
    	scoreDirectory(path, Constants.SCORE_OUT);
    }    
    public static void scoreDirectory(String path, String out) {
    	initScorers();
    	PrintStream ps = null;
    	if (Constants.SCORE_OUT.length() > 0) {
    		try {
				ps = new PrintStream(new File(out), "UTF8");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
    	}
    	try {
	    	File dir = new File(path);
	    	int docBase = 0;
	    	if (dir.isDirectory()) {
	    		for (File f : dir.listFiles()) {
	    			if (!f.isFile()) continue;
	    			if (dictionaries == null) dictionaries = new Dictionaries();
	    			Document d = new Document(dictionaries);
	    			d.id = f.getName();
	    			log.info("SCORE " + f.getPath());
	    			d.readCONLL(f.getPath());
	    			processDocument(d);
	    			d.updateMentionIds(docBase); docBase += 100000;
	    			d.setConllCorefColumns();
	    			if (ps != null) {
	    				d.outputCONLL(ps);
	    			}
	    		}
	    		if (ps != null) ps.close();
	    	}
	        if (Constants.SCORE_EVAL_SCRIPT != null) {
	              String summary = getConllEvalSummary(Constants.SCORE_EVAL_SCRIPT, Constants.SCORE_OUT, Constants.SCORE_OUT);
	              log.info("\nCONLL EVAL SUMMARY (After COREF)");
	              printScoreSummary(summary, true);
	              printFinalConllScore(summary);
	          }
    	} catch (Exception ex) {
    		log.severe("Could not read conll file for scoring");
    		ex.printStackTrace();
    	}
    }
    
	public static void printScoreSummary(String summary, boolean afterPostProcessing) {
		String[] lines = summary.split("\n");
		if(!afterPostProcessing) {
			for(String line : lines) {
				if(line.startsWith("Identification of Mentions")) {
					log.info(line);
					return;
				}
			}
		} else {
			StringBuilder sb = new StringBuilder();
			for(String line : lines) {
				if(line.startsWith("METRIC")) sb.append(line);
				if(!line.startsWith("Identification of Mentions") && line.contains("Recall")) {
					sb.append(line).append("\n");
				}
			}
			log.info(sb.toString());
		}
	}
	/** Print average F1 of MUC, B^3, CEAF_E */
	public static void printFinalConllScore(String summary) {
		Pattern f1 = Pattern.compile("Coreference:.*Recall: \\(.*\\) (.*)%\tPrecision: \\(.*\\) (.*)%\tF1: (.*)%");
		Matcher f1Matcher = f1.matcher(summary);
		int count = 4;
		Double[] F1s = new Double[count];
		Double[] Ps = new Double[count];
		Double[] Rs = new Double[count];
		String[] names = new String[count];
		int i = 0;
		while (f1Matcher.find()) {
			names[i] = f1Matcher.group(1);
			F1s[i] = Double.parseDouble(f1Matcher.group(3));
			Ps[i] = Double.parseDouble(f1Matcher.group(2));
			Rs[i] = Double.parseDouble(f1Matcher.group(1));
			i++;
		}
		Pattern pattern = Pattern.compile("METRIC\\s+(.*):");
		Matcher matcher = pattern.matcher(summary);
		i = 0;
		while (matcher.find()) {
			names[i] = matcher.group(1);
			i++;
		}
//		log.info(StringUtils.join(names, "\t"));
//		log.info(StringUtils.join(F1s, "\t"));
//		log.info(StringUtils.join(Rs, "\t"));
//		log.info(StringUtils.join(Ps, "\t"));
		for (int sc = 0 ; sc < count; sc++) {
			log.info(names[sc] + "\t" + F1s[sc] + "\t" + Ps[sc] + "\t" + Rs[sc]);
		}
		double finalScore = (F1s[0]+F1s[1]+F1s[3])/3;
		double finalP = (Ps[0]+Ps[1]+Ps[3])/3;
		double finalR = (Rs[0]+Rs[1]+Rs[3])/3;
		DecimalFormat df = new DecimalFormat("#.##");
		log.info("(muc+bcub+ceafe)/3\t" + df.format(finalScore) + "\t" + df.format(finalP) + "\t" + df.format(finalR));
	}
      
	public static String getConllEvalSummary(String conllMentionEvalScript,
			String goldFile, String predictFile) throws IOException	{
		if (conllMentionEvalScript == null) return "";
		ProcessBuilder process = new ProcessBuilder(conllMentionEvalScript, "all", goldFile, predictFile, "none");
		StringOutputStream errSos = new StringOutputStream();
		StringOutputStream outSos = new StringOutputStream();
		PrintWriter out = new PrintWriter(outSos);
		PrintWriter err = new PrintWriter(errSos);
		SystemUtils.run(process, out, err);
		out.close();
		err.close();
		String summary = outSos.toString();
		String errStr = errSos.toString();
		if (errStr.length() > 0) {
			summary += "\nERROR: " + errStr;
		}
		return summary;
	}
    
    public static void annotateFolder(String path, String outPath) {
    	try {
	    	File dir = new File(path);
	    	if (dir.isDirectory()) {
	    		for (File f : dir.listFiles()) {
	    			if (!f.isFile()) continue;
	    			if (dictionaries == null) dictionaries = new Dictionaries();
	    			Document d = new Document(dictionaries);
	    			d.id = f.getName();
	    			log.info("ANNOTATE " + f.getPath());
	    			d.readCONLL(f.getPath());
	    			processDocument(d);
	    			d.setConllCorefColumns();
	    			d.outputCONLL(outPath + f.getName());
	    		}
	    	}
    	} catch (Exception ex) {
    		log.severe("Could read conll file for folder annotation");
    		ex.printStackTrace();
    	}
    }
    
	public static void main(String[] args) throws Exception {
		Properties properties = StringUtils.argsToProperties(args);
        initializeProperties(properties);
//        if (true) {
//        	parserScoreCompare();
//        	return;
//        }
        if (Constants.SCORE) {
        	scoreDirectory(Constants.SCORE_PATH);
        	return;
        } 
        if (Constants.TAG_FOLDER) {
        	annotateFolder(Constants.TAG_FOLDER_PATH, Constants.TAG_FOLDER_OUT);
        	return;
        }
        
        int docID = 0; // document counter
        BufferedReader in;
        switch(inputType) {
            case CONLL:
                Document d = new Document(dictionaries);
                try {
                    d.readCONLL(conllInput);
                } catch (Exception ex) {
                	log.severe("Could not read conll file");
                    ex.printStackTrace();
                    break;
                }                
                if (d.tree.size() > 0) processDocument(d);
                break;
            case STDIN_JSON:
                in = new BufferedReader(new InputStreamReader(System.in, "UTF8"));
                while (true) {
                    Document doc = new Document(dictionaries);
                    doc.id = Integer.toString(docID++);
                    try {
                        doc.readJSON(in);
                    } catch (Exception ex) {
                        log.severe("Could not read json from stream");
                        ex.printStackTrace();
                        break;
                    }
                    if (doc.tree.size() > 0) processDocument(doc);  
                    else break;
                }
                break;
            default:
                in = new BufferedReader(new InputStreamReader(System.in, "UTF8"));
                while (true) {
                    Document doc = new Document(dictionaries);
                    doc.id = Integer.toString(docID++);
                    try {
                        doc.readCONLL(in);
                    } catch (Exception ex) {
                    	log.severe("Could not read conll from stream");
                        ex.printStackTrace();
                        break;
                    }
                    if (doc.tree.size() > 0) processDocument(doc);  
                    else break;
                }
        }
    }        
    
    public static void processDocument(Document d) { 
        d.updateProperWords();
        if (Constants.USE_GOLD_MENTIONS) {
            d.useGoldMentions();
            d.setAbbreviationMentions(true);
            d.setProperNodeMentions(false);
            d.setMentionModifiers(false);
        } else {
            d.setMentionsFromNEAnnotation();
            //d.setAllMentions();
            d.setQuoteMentions();
            d.setAbbreviationMentions(false);
            d.setListMentions();
            
//            d.setProperNodeMentions(true);
            d.setDetalizedNominalMentions();            
            
            // After creating all mentions
            d.normalizeMentions(); 	// sorts mentions (base and new mentions 
            						// could be out of order), removes ignored 
            						// base mentions
            d.removePleonasticMentions();
            
            d.removeNestedQuoteMentions();
            //d.removeUndefiniedMentions();
            
            d.removePluralMentions();
            
            d.removeNestedMentions();
            d.removeExcludedMentions();
            d.removeGenitiveMentions();
            d.setMentionModifiers_v2(true);
            d.removeDublicateMentions();
        }        
        d.normalizeMentions();	// just in case, removes unused mentions and sorts them        
        d.initializeEntities();	// set coreference cluster for each mention
        
        for(int i = 0; i < sieves.length; i++) {
            currentSieve = i;
            DeterministicCorefSieve sieve = sieves[i];
            coref(d, sieve);	// Do coreference resolution using this pass
        }
        
        /* Post processing step */
        d.postProcess(); // final category, conll columns
        if (Constants.REMOVE_SINGLETONS) d.removeSingletonMentions();

        // Set final mention borders for precise border conll output and html visualization
        d.markMentionBorders(true);
        
//		d.printClusterRepresentatives();
//		d.printClusters();
//		d.printMentions();
//		d.printSimpleText(System.err);
        
        if(Constants.SCORE) {        
            log.info("Pairwise score for this doc: ");
            log.info(scoreSingleDoc.get(sieves.length-1).getF1String(true));
            
            singleDocMentionScorer = new MentionScorer(); 
            singleDocMentionScorer.add(d);
            log.info(singleDocMentionScorer.getScore());
            
            mentionScorer.add(d);
            stats.add(d, true);
            singleDocStats.add(d, true);
            
            log.info("Accumulated score: ");
            log.info(getF1(true));
//            log.fine("Document Statistics: ");
//            log.fine(singleDocStats.corefStatistics(true));
//            log.info("Accumulated Statistics: ");
//            log.info(stats.corefStatistics(true));
        } else {
            stats.add(d, false);
            singleDocStats.add(d, false);
            log.fine("Document Statistics: ");
            log.fine(singleDocStats.corefStatistics(false));
            log.info("Accumulated Statistics: ");
            log.info(stats.corefStatistics(false));
        }
        outputDocument(d);
    }
    
    public static void outputDocument(Document d) {
        PrintStream ps;
        switch (outputType) {
            case CONLL:
                d.outputCONLL(conllOutput);                
                break;
                
            case STDOUT_JSON :
                try {
                    ps = new PrintStream(System.out, true, "UTF8");
                    d.outputJSON(ps);
                } catch (UnsupportedEncodingException ex) {
                    log.severe("Unsupported output encoding");
                    ex.printStackTrace();
                }
                break;
            case TEXT :
                try {
                    ps = new PrintStream(System.out, true, "UTF8");
                    d.printSimpleText(ps);
                } catch (UnsupportedEncodingException ex) {
                    log.severe("Unsupported output encoding");
                    ex.printStackTrace();
                }
                break;
            case NONE:
            	break;
            default:
                try {
                    ps = new PrintStream(System.out, true, "UTF8");
                    d.outputCONLL(ps);
                } catch (UnsupportedEncodingException ex) {
                    log.severe("Unsupported output encoding");
                    ex.printStackTrace();
                }
        }
        if (htmlOutput != null) {
            d.htmlOutput(htmlOutput);
        }
        if (Boolean.parseBoolean(props.getProperty(Constants.LOG_PROP, "false"))) {
            d.outputCONLL(logPath + timeStamp+ "_" + d.id + ".conll");
            d.htmlOutput(logPath + timeStamp+ "_" + d.id + ".html");
            d.htmlOutput(logPath + "log.html");
            d.outputCONLL(logPath + "log.conll");
        }
        if (mmaxExport) {
            String projectName = (mmaxExportProjectName != null && mmaxExportProjectName.trim().length() > 0) ? mmaxExportProjectName+d.id : timeStamp+ d.id;
            MMAX2.createProject(d, projectName, mmaxExportPath);
        }
    }
	
    
    public static void coref(Document document, DeterministicCorefSieve sieve) {
        sieve.coreferent(document);
        additionalCorrectLinksCount = 0;
        additionalLinksCount = 0;
        if(Constants.SCORE){
            scoreMUC.get(currentSieve).calculateScore(document);
            scoreBcubed.get(currentSieve).calculateScore(document);
            scorePairwise.get(currentSieve).calculateScore(document);
            if(currentSieve==0) {
                singleDocMentionScorer = new MentionScorer();
                singleDocStats = new Statistics();

                scoreSingleDoc = new ArrayList<CorefScorer>();
                scoreSingleDoc.add(new ScorerPairwise());
                scoreSingleDoc.get(currentSieve).calculateScore(document);
                additionalCorrectLinksCount = (int) scoreSingleDoc.get(currentSieve).precisionNumSum;
                additionalLinksCount = (int) scoreSingleDoc.get(currentSieve).precisionDenSum;
            } else {
                scoreSingleDoc.add(new ScorerPairwise());
                scoreSingleDoc.get(currentSieve).calculateScore(document);
                additionalCorrectLinksCount = (int) (scoreSingleDoc.get(currentSieve).precisionNumSum - scoreSingleDoc.get(currentSieve-1).precisionNumSum);
                additionalLinksCount = (int) (scoreSingleDoc.get(currentSieve).precisionDenSum - scoreSingleDoc.get(currentSieve-1).precisionDenSum);
            }
            linksCountInPass.get(currentSieve).setFirst(linksCountInPass.get(currentSieve).first() + additionalCorrectLinksCount);
            linksCountInPass.get(currentSieve).setSecond(linksCountInPass.get(currentSieve).second() + additionalLinksCount);

            //printSieveScore(document, sieve);
        }
    }
  
    public static String getF1(boolean simple) {
    	StringBuilder sb = new StringBuilder();
    	sb.append(scoreMUC.get(sieveClassNames.length - 1).getF1String(simple)).append("\n");
    	sb.append(scoreBcubed.get(sieveClassNames.length - 1).getF1String(simple)).append("\n");
    	sb.append(scorePairwise.get(sieveClassNames.length - 1).getF1String(simple)).append("\n");
    	double F1s = (scoreMUC.get(sieveClassNames.length - 1).getF1() 
    			+ scoreBcubed.get(sieveClassNames.length - 1).getF1()
    			+ scorePairwise.get(sieveClassNames.length - 1).getF1()) * 100 / 3;
    	double Ps = (scoreMUC.get(sieveClassNames.length - 1).getPrecision() 
    			+ scoreBcubed.get(sieveClassNames.length - 1).getPrecision()
    			+ scorePairwise.get(sieveClassNames.length - 1).getPrecision()) * 100 / 3;
    	double Rs = (scorePairwise.get(sieveClassNames.length - 1).getRecall() 
    			+ scoreBcubed.get(sieveClassNames.length - 1).getRecall()
    			+ scorePairwise.get(sieveClassNames.length - 1).getRecall()) * 100 / 3;
		NumberFormat nf = new DecimalFormat("00.00");
		sb.append(nf.format(F1s) + "\t" + nf.format(Ps) + "\t" + nf.format(Rs)).append("\n");
		sb.append(mentionScorer.getScore()).append("\n");
		return sb.toString();
    }
    
    private static void printSieveScore(Document document, DeterministicCorefSieve sieve) {
        log.info("===== PASS"+currentSieve+" " + sieve.getClass().getSimpleName()+": "+ sieve.flagsToString()+ "======");
        log.info(scoreMUC.get(currentSieve).getF1String(false));
        log.info(scoreBcubed.get(currentSieve).getF1String(false));
        log.info(scorePairwise.get(currentSieve).getF1String(false));
        double F1s = (scoreMUC.get(currentSieve).getF1() 
    			+ scoreBcubed.get(currentSieve).getF1()
    			+ scorePairwise.get(currentSieve).getF1()) * 100 / 3;
    	double Ps = (scoreMUC.get(currentSieve).getPrecision() 
    			+ scoreBcubed.get(currentSieve).getPrecision()
    			+ scorePairwise.get(currentSieve).getPrecision()) * 100 / 3;
    	double Rs = (scorePairwise.get(currentSieve).getRecall() 
    			+ scoreBcubed.get(currentSieve).getRecall()
    			+ scorePairwise.get(currentSieve).getRecall()) * 100 / 3;
		NumberFormat nf = new DecimalFormat("00.00");
		log.info("Total\t" + nf.format(F1s) + "\t" + nf.format(Ps) + "\t" + nf.format(Rs));
        
        
        log.info("# of Clusters: "+document.corefClusters.size() + "\t,\t# of additional links: \t"+additionalLinksCount
            +"\t,\t# of additional correct links: \t"+additionalCorrectLinksCount
            +"\t,\tprecision of new links: \t"+1.0*additionalCorrectLinksCount/additionalLinksCount);
        log.info("# of total additional links: \t"+linksCountInPass.get(currentSieve).second()
            +"\t,\t# of total additional correct links: \t"+linksCountInPass.get(currentSieve).first()
            +"\t,\taccumulated precision of this pass: \t"+1.0*linksCountInPass.get(currentSieve).first()/linksCountInPass.get(currentSieve).second());
        log.info("--------------------------------------");
    }
}
