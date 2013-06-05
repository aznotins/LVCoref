package LVCoref;

import LVCoref.Dictionaries.MentionType;
import LVCoref.ScorerBCubed.BCubedType;
import LVCoref.sievepasses.*;
import edu.stanford.nlp.util.Pair;
import java.io.IOException;

import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import edu.stanford.nlp.util.StringUtils;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Main process class
 * @author arturs
 */
public class LVCoref {
    
    private enum inputTypes {CONLL, STDIN_CONLL};
    private enum outputTypes {CONLL, STDOUT_CONLL};
    public static final Logger logger = Logger.getLogger(LVCoref.class.getName());
    public static Properties props;

    /**
    * If true, we score the output of the given test document
    * Assumes gold annotations are available
    */
    //private static int maxSentDist = 100;
    private static boolean doScore = false;
    private static int maxSentDist;
    private static String mmaxExportPath = "data/mmax2/";
    private static String mmaxExportProjectName = "";
    private static boolean mmaxExport = false;
    private static String logPath = "data/logs/";
    private static String conllInput = null;
    private static String conllOutput = null;
    private static String htmlOutput = null;
    private static String nerAnnotation = null;
    
    /**
    * Array of sieve passes to be used in the system
    * Ordered from highest precision to lowest!
    */
    private static /*final */DeterministicCorefSieve [] sieves;
    private static /*final*/ String [] sieveClassNames;

    /** Current sieve index */
    public static int currentSieve;
    
    //for evaluation
    public static int docID;
    public static List<String> inputConllList;
    public static List<String> mmaxGoldList;
    public static List<String> nerList;
    

    /** counter for links in passes (Pair<correct links, total links>)  */
    public static List<Pair<Integer, Integer>> linksCountInPass;
    
    private static inputTypes inputType = inputTypes.STDIN_CONLL;
    private static outputTypes outputType = outputTypes.STDOUT_CONLL;
    
    public static String timeStamp = ""; //for logs
    public static int documentID = 0;
    public static boolean stopProcess = false;

    /** Scores for each pass */
    public static List<CorefScorer> scorePairwise;
    public static List<CorefScorer> scoreBcubed;
    public static List<CorefScorer> scoreMUC;

    public static List<CorefScorer> scoreSingleDoc;
    
    public static MentionScorer singleDocMentionScorer = new MentionScorer();
    public static MentionScorer mentionScorer = new MentionScorer();
    public static Statistics singleDocStats = new Statistics();
    public static Statistics stats = new Statistics();
    
      /** Additional scoring stats */
    public static int additionalCorrectLinksCount;
    public static int additionalLinksCount;
    
    public static boolean useGoldMentions = false;

    public static class LogFormatter extends Formatter {
        @Override
        public String format(LogRecord rec) {
            StringBuilder buf = new StringBuilder(1000);
            buf.append(formatMessage(rec));
            buf.append('\n');
            return buf.toString();
        }
    }
    
    public static void createLogger() {
        timeStamp = Calendar.getInstance().getTime().toString().replaceAll("\\s", "-").replaceAll(":", "-");
        String logFileName = logPath + timeStamp + "_log.txt";
        for(Handler fh : logger.getHandlers()) { logger.removeHandler(fh); } //remove all old log handlers
        try {
            FileHandler fh = new FileHandler(logFileName, false);
            logger.addHandler(fh);
            logger.setLevel(Level.FINE);
            fh.setFormatter(new LogFormatter());
        } catch (IOException e) {
            System.err.println("ERROR: cannot initialize logger!");
        }
        logger.fine(timeStamp);            
//            logger.fine("----Parameters------"
//                    + "\nmmaxExportPath: " + mmaxExportPath
//                    + "\nmmaxExport: " + mmaxExport
//                    + "\nlogPath: " + logPath
//                    + "\nkeepLogs: " + keepLogs
//                    + "\nconllInput: " + conllInput
//                    + "\nconllOutput: " + conllOutput
//                    + "\nhtmlOutput: " + htmlOutput
//                    + "\nmmaxGold: " + mmaxGold
//                    + "\nnerAnnotation: "+ nerAnnotation
//                    + "\ninputType: " + inputType
//                    + "\noutputType: " + outputType
//                    + "\n----------\n"
//                    );

        //logger.fine(props.toString());
        //Constants.printConstants(logger);
    }
    
    public static void printHelp(){
        System.out.print(
            "--------------------\n"
            + "LVCoref: Latvian Coreference Resolver"
            + "\nParameters:"

            + "\n\t" + Constants.STDOUT_PROP + ": write conll format results to console (default)"                  
            + "\n\t" + Constants.LOG_PROP + ": keep logs, defaults to false"
            + "\n\t" + Constants.LOG_PATH_PROP + ": directory path with trailing /, (default = data/logs/)"
            + "\n\t" + Constants.CONLL_INPUT_PROP + ": path to conll input file"
            + "\n\t" + Constants.MMAX_EXPORT_PATH_PROP + ": MMAX export path with trailing / (default = data/mmax2/"
            + "\n\t" + Constants.MMAX_EXPORT_NAME_PROP + ": project name (if no specified timestamp is used"
            + "\n\t" + Constants.HTML_PROP + ": file path for html formatted coreference results, direcotory should include script.js, style.css"
            + "\n\t" + Constants.CONLL_OUTPUT_PROP + ": file path for conll output to file"
            + "\n\t" + Constants.MMAX_GOLD_PROP + ": file path to existing  mmax coref_level gold annotation, used for scoring"
            + "\n\t" + Constants.NER_PROP + ": file path to existing NE tagged file"
            + "\n--------------------\n"
        );
        System.out.flush();
    }
      
	public static void main(String[] args) throws Exception {
        /**
         * Parse arguments
         */
        //Properties props = StringUtils.argsToProperties(args);
        props = StringUtils.argsToProperties(args);
        //System.out.println(props);
                       
        if (Boolean.parseBoolean(props.getProperty(Constants.STDOUT_PROP, "false"))) outputType = outputTypes.STDOUT_CONLL;
        mmaxExport = Boolean.parseBoolean(props.getProperty(Constants.MMAX_EXPORT_PROP, "false"));
        maxSentDist = Integer.parseInt(props.getProperty(Constants.MAXDIST_PROP, "-1"));

        mmaxExport = Boolean.parseBoolean(props.getProperty(Constants.MMAX_EXPORT_PROP, "false"));
        mmaxExportPath = props.getProperty(Constants.MMAX_EXPORT_PATH_PROP, "");
        mmaxExportProjectName = props.getProperty(Constants.MMAX_EXPORT_NAME_PROP);

        conllOutput = props.getProperty(Constants.CONLL_OUTPUT_PROP, ""); if (conllOutput.length() > 0) outputType = outputTypes.CONLL;
        conllInput = props.getProperty(Constants.CONLL_INPUT_PROP, ""); if (conllInput.length() > 0) inputType = inputType.CONLL;
        
        htmlOutput = props.getProperty(Constants.HTML_PROP, "");
        nerAnnotation = props.getProperty(Constants.NER_PROP, "");

        if (Boolean.parseBoolean(props.getProperty("h", "false")) || Boolean.parseBoolean(props.getProperty("help",  "false"))) { printHelp(); System.exit(0); }
        

        currentSieve = -1;
        String sievePasses = props.getProperty(Constants.SIEVES_PROP, Constants.SIEVEPASSES);
        sieveClassNames = sievePasses.trim().split(",\\s*");
        sieves = new DeterministicCorefSieve[sieveClassNames.length];
        for(int i = 0; i < sieveClassNames.length; i ++){
            sieves[i] = (DeterministicCorefSieve) Class.forName("LVCoref.sievepasses."+sieveClassNames[i]).getConstructor().newInstance();
            sieves[i].init(props);
        }
        
        doScore = Boolean.parseBoolean(props.getProperty(Constants.SCORE_PROP, "false"));
        maxSentDist = Integer.parseInt(props.getProperty(Constants.MAXDIST_PROP, "-1"));
        if(doScore){
            initScorers();
        }
        
        timeStamp = Calendar.getInstance().getTime().toString().replaceAll("\\s", "-").replaceAll(":", "-");
        if (Boolean.parseBoolean(props.getProperty(Constants.LOG_PROP, "false"))) createLogger();
        
        logger.fine(props.toString());
        Constants.printConstants(logger);
        
        
        
        
        if (Constants.MULTIPLE_DOCS_EVAL) {
            inputConllList = new ArrayList(Arrays.asList(conllInput.split(",")));
            String mmaxGold = props.getProperty(Constants.MMAX_GOLD_PROP, "");
            mmaxGoldList = new ArrayList(Arrays.asList(mmaxGold.split(",")));
            nerList = new ArrayList(Arrays.asList(nerAnnotation.split(",")));
            if (inputConllList.size() != mmaxGoldList.size()) System.err.println("Incorrect number of files for evaluation");    
            docID = 0;
        }
        
        Dictionaries dictionaries = new Dictionaries();
        
        if (Constants.MULTIPLE_DOCS_EVAL) {
            for (documentID=0; documentID < inputConllList.size(); documentID++) {
                System.err.println("NEW DOCUMENT: " + inputConllList.get(documentID));
                Document d = new Document(logger, dictionaries);
                try {
                    d.readCONLL(inputConllList.get(documentID));
                } catch (Exception ex) {
                    System.err.println("Could not read conll file");
                    System.err.println(ex.getStackTrace());
                }
                if (d.tree.size() > 0) processDocument(d, props);                
            }
            
            
        } else {
        
        /*
         * Create document
         */
        switch(inputType) {
            case CONLL:
                Document d = new Document(logger, dictionaries);
                try {
                    d.readCONLL(conllInput);
                } catch (Exception ex) {
                    System.err.println("Could not read conll file");
                    System.err.println(ex.getStackTrace());
                    break;
                }
                
                if (d.tree.size() > 0) processDocument(d, props);
                
                break;
            default:
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "UTF8"));
                while (!stopProcess) {
                    Document doc = new Document(logger, dictionaries);
                    try {
                        doc.readCONLL(in);
                    } catch (Exception ex) {
                        System.err.println("Could not read conll from stream");
                        System.err.println(ex.getStackTrace());
                        break;
                    }
                    if (doc.tree.size() > 0) processDocument(doc, props);  
                    else break;
                    documentID++;
                }
        }
        }
    };
        
    
    public static void processDocument(Document d, Properties props) {
        
//        PrintWriter writerGold = null;
//        PrintWriter writerPredicted = null;
//        PrintWriter writerPredictedCoref = null;
//
//        String conllOutputMentionGoldFile = null;
//        String conllOutputMentionPredictedFile = null;
//        String conllOutputMentionCorefPredictedFile = null;
//        String conllMentionEvalFile = null;
//        String conllMentionEvalErrFile = null;
//        String conllMentionCorefEvalFile = null;
//        String conllMentionCorefEvalErrFile = null;
//        if(Constants.PRINT_CONLL_OUTPUT) {
//            String conllOutput = props.getProperty(Constants.CONLL_OUTPUT_PROP, "conlloutput");
//            conllOutputMentionGoldFile = conllOutput + "-"+timeStamp+".gold.txt";
//            conllOutputMentionPredictedFile = conllOutput +"-"+timeStamp+ ".predicted.txt";
//            conllOutputMentionCorefPredictedFile = conllOutput +"-"+timeStamp+ ".coref.predicted.txt";
//            conllMentionEvalFile = conllOutput +"-"+timeStamp+ ".eval.txt";
//            conllMentionEvalErrFile = conllOutput +"-"+timeStamp+ ".eval.err.txt";
//            conllMentionCorefEvalFile = conllOutput +"-"+timeStamp+ ".coref.eval.txt";
//            conllMentionCorefEvalErrFile = conllOutput +"-"+timeStamp+ ".coref.eval.err.txt";
//            logger.info("CONLL MENTION GOLD FILE: " + conllOutputMentionGoldFile);
//            logger.info("CONLL MENTION PREDICTED FILE: " + conllOutputMentionPredictedFile);
//            logger.info("CONLL MENTION EVAL FILE: " + conllMentionEvalFile);
//        }
        
                
        String mmaxGold;
        if (Constants.MULTIPLE_DOCS_EVAL) {
            mmaxGold = mmaxGoldList.get(documentID);
            if (nerList.size() > 0) {
                nerAnnotation = nerList.get(documentID);
            }
        } else {
            mmaxGold = props.getProperty(Constants.MMAX_GOLD_PROP, "");
        }
        if (mmaxGold.length() > 0) { d.addAnnotationMMAX(mmaxGold); }
        
        if (Constants.USE_GOLD_MENTIONS && mmaxGold.length() > 0) {
            d.useGoldMentions();
            d.setAbbreviationMentions(true);
            d.setProperNodeMentions(false);
            d.setMentionCategories();
            d.setMentionModifiers(false);
        } else {
            if (nerAnnotation.length() > 0) d.setMentionsNER(nerAnnotation);
            d.setListMentions();
            d.setQuoteMentions();
            d.setAbbreviationMentions(false);
                         //d.setMentions();
            d.setProperNodeMentions(true);
            d.setDetalizedNominalMentions();
            
            d.setMentionCategories();
//            d.setTmpMentions();
           
            d.tweakPersonMentions();d.tweakPersonMentions();//FIXME 
            
            
            d.removePleonasticMentions();
            
            d.removeUndefiniedMentions();
            //d.removeNestedMentions();
            d.removeExcludedMentions();
            d.removeGenitiveMentions();
            
            d.setMentionModifiers(true);
        }
        //d.removePluralMentions();
        d.updateMentions(); //FIXME move to constructor
        d.sortMentions(); //needed for normalization (array index equals to id)
               

        d.initializeEntities();
        //Resolve.go(d, logger);
               
        for(int i = 0; i < sieves.length; i++) {
            currentSieve = i;
            DeterministicCorefSieve sieve = sieves[i];
            // Do coreference resolution using this pass
            coref(d, sieve);
        }

        //d.removePronounSingletonMentions();
        //d.removeSingletonMentions();

        for (Node n : d.tree) {
            n.markMentionBorders(d, true);
        }
        
        
    if(doScore()){
        //Identifying possible coreferring mentions in the corpus along with any recall/precision errors with gold corpus
        //corefSystem.printTopK(logger, document, corefSystem.semantics);

        logger.fine("pairwise score for this doc: ");
        scoreSingleDoc.get(sieves.length-1).printF1(logger);
        logger.fine("accumulated score: ");
        printF1(true);
        logger.fine("\n");
        
        singleDocMentionScorer.add(d);
        mentionScorer.add(d);
        stats.add(d, true);
        singleDocStats.add(d, true);
        
        logger.fine("Document Statistics: ");
        logger.fine(singleDocStats.corefStatistics(true));
        logger.fine("Accumulated Statistics: ");
        logger.fine(stats.corefStatistics(true));
        System.err.println("Document Statistics: ");
        System.err.println(singleDocStats.corefStatistics(true));
        System.err.println("Accumulated Statistics: ");
        System.err.println(stats.corefStatistics(true));
        
        logger.fine("Mentions score: ");
        logger.fine(singleDocMentionScorer.getScore());
        logger.fine("Accumulated Mentions score: ");
        logger.fine(mentionScorer.getScore());
        System.err.println("Mentions score: ");
        System.err.println(singleDocMentionScorer.getScore());
        System.err.println("Accumulated Mentions score: ");
        System.err.println(mentionScorer.getScore());
        
      } else {
        stats.add(d, false);
        singleDocStats.add(d, false);
        logger.fine("Document Statistics: ");
        logger.fine(singleDocStats.corefStatistics(false));
        logger.fine("Accumulated Statistics: ");
        logger.fine(stats.corefStatistics(false));
        System.err.println("Document Statistics: ");
        System.err.println(singleDocStats.corefStatistics(false));
        System.err.println("Accumulated Statistics: ");
        System.err.println(stats.corefStatistics(false));
      }
        
        
        //d.printCoreferences();

        d.setConllCorefColumns();
        switch (outputType) {
            case CONLL:
                d.outputCONLL(conllOutput);                
                break;
            default:
                PrintStream ps;
                try {
                    ps = new PrintStream(System.out, true, "UTF8");
                    //d.outputCONLL(ps);
                } catch (UnsupportedEncodingException ex) {
                    System.err.println("Unsupported output encoding");
                    Logger.getLogger(LVCoref.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        if (htmlOutput != null) {
            d.htmlOutput(htmlOutput);
        }
 
        if (Boolean.parseBoolean(props.getProperty(Constants.LOG_PROP, "false"))) {
            d.outputCONLL(logPath + timeStamp+ "_" + documentID + ".conll");
            d.htmlOutput(logPath + timeStamp+ "_" + documentID + ".html");
        }

        if (mmaxExport) {
            String docId = (documentID == 0) ? "" : "_" + documentID;
            String projectName = (mmaxExportProjectName != null && mmaxExportProjectName.trim().length() > 0) ? mmaxExportProjectName+docId : timeStamp+ docId;
            MMAX2.createProject(d, projectName, mmaxExportPath);
        }

//        if (mmaxGold != null) {
//            d.addAnnotationMMAX(mmaxGold);
//            
//            CorefScorer scorerPairwise = new ScorerPairwise();
//            scorerPairwise.calculateScore(d);
//            scorerPairwise.printF1(logger, true);            
//
//            CorefScorer scorerMUC = new ScorerMUC();
//            scorerMUC.calculateScore(d);
//            scorerMUC.printF1(logger, true);            
//
//            CorefScorer scorerB3 = new ScorerBCubed(BCubedType.Bconll);
//            scorerB3.calculateScore(d);
//            scorerB3.printF1(logger, true);
//            
//            System.err.println(scorerPairwise.getF1String(true));
//            System.err.println(scorerMUC.getF1String(true));
//            System.err.println(scorerB3.getF1String(true));
//        }
        
        
//      }
        
        

//		for (Node n: d.tree) {
//			System.out.print(" " + n.word);
//			if (n.mention != null && d.corefClusters.get(n.mention.corefClusterID).corefMentions.size() > 1) {
//                Mention ant = d.refGraph.getFinalResolutions().get(n.mention);
//                System.out.print("["+n.mention.corefClusterID+"/"+n.mention.id+"/"+((ant == null)?null:ant.id)+"/"+n.mention.type+"/"+n.mention.categories+"@"+n.mention.comments+"]");
//            }
//			if (n.word.equals(".")) System.out.println();
//		}
//		System.out.println();
    }
    
    public static void db(String t) {
        System.out.println(t);
    }
	
    
    public static void coref(Document document, DeterministicCorefSieve sieve) {
        sieve.coreferent(document);
    additionalCorrectLinksCount = 0;
    additionalLinksCount = 0;
        // scoring
    if(doScore()){
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

      printSieveScore(document, sieve);
    }
    }
    
    
      public static String signature(Properties props) {
    StringBuilder os = new StringBuilder();
    os.append(Constants.SIEVES_PROP + ":" +
            props.getProperty(Constants.SIEVES_PROP,
                    Constants.SIEVEPASSES));
    os.append(Constants.SCORE_PROP + ":" +
            props.getProperty(Constants.SCORE_PROP,
                    "false"));
    os.append(Constants.CONLL_SCORER + ":" +
            props.getProperty(Constants.CONLL_SCORER,
                    Constants.conllMentionEvalScript));
    //os.append(Dictionaries.signature(props));
    return os.toString();
  }

  public static void initScorers() {
    linksCountInPass = new ArrayList<Pair<Integer, Integer>>();
    scorePairwise = new ArrayList<CorefScorer>();
    scoreBcubed = new ArrayList<CorefScorer>();
    scoreMUC = new ArrayList<CorefScorer>();
    for(int i = 0 ; i < sieveClassNames.length ; i++){
      scorePairwise.add(new ScorerPairwise());
      scoreBcubed.add(new ScorerBCubed(BCubedType.Bconll));
      scoreMUC.add(new ScorerMUC());
      linksCountInPass.add(new Pair<Integer, Integer>(0, 0));
    }  
  }

  public static boolean doScore() { return doScore; }

  
  public static void printF1(boolean printF1First) {
    scoreMUC.get(sieveClassNames.length - 1).printF1(logger, printF1First);
    scoreBcubed.get(sieveClassNames.length - 1).printF1(logger, printF1First);
    scorePairwise.get(sieveClassNames.length - 1).printF1(logger, printF1First);
  }
  
  private static void printSieveScore(Document document, DeterministicCorefSieve sieve) {
    logger.fine("===========================================\n");
    logger.fine("pass"+currentSieve+" " + sieve.getClass()+": \t"+ sieve.flagsToString());
    scoreMUC.get(currentSieve).printF1(logger);
    scoreBcubed.get(currentSieve).printF1(logger);
    scorePairwise.get(currentSieve).printF1(logger);
    logger.fine("# of Clusters: "+document.corefClusters.size() + "\t,\t# of additional links: \t"+additionalLinksCount
        +"\t,\t# of additional correct links: \t"+additionalCorrectLinksCount
        +"\t,\tprecision of new links: \t"+1.0*additionalCorrectLinksCount/additionalLinksCount);
    logger.fine("# of total additional links: \t"+linksCountInPass.get(currentSieve).second()
        +"\t,\t# of total additional correct links: \t"+linksCountInPass.get(currentSieve).first()
        +"\t,\taccumulated precision of this pass: \t"+1.0*linksCountInPass.get(currentSieve).first()/linksCountInPass.get(currentSieve).second());
    logger.fine("--------------------------------------");
    
    if (Constants.VERBOSE) {
        System.err.println("===========================================\n");
         System.err.println("pass"+currentSieve+" " + sieve.getClass()+": "+ sieve.flagsToString());
//        scoreMUC.get(currentSieve).printF1(logger);
//        scoreBcubed.get(currentSieve).printF1(logger);
//        scorePairwise.get(currentSieve).printF1(logger);
//        
        System.err.println(scoreMUC.get(currentSieve).getF1String(true));
        System.err.println(scoreBcubed.get(currentSieve).getF1String(true));
        System.err.println(scorePairwise.get(currentSieve).getF1String(true));
        
        System.err.println("# of Clusters: \t"+document.corefClusters.size() + "\t,\t# of additional links: \t"+additionalLinksCount
            +"\t,\t# of additional correct links: \t"+additionalCorrectLinksCount
            +"\t,\tprecision of new links: \t"+1.0*additionalCorrectLinksCount/additionalLinksCount);
        System.err.println("# of total additional links: \t"+linksCountInPass.get(currentSieve).second()
            +"\t,\t# of total additional correct links: \t"+linksCountInPass.get(currentSieve).first()
            +"\t,\taccumulated precision of this pass: \t"+1.0*linksCountInPass.get(currentSieve).first()/linksCountInPass.get(currentSieve).second());
        System.err.println("--------------------------------------");
        }
    
  }
//  /** Print coref link info */
//  private static void printLink(Logger logger, String header, IntTuple src, IntTuple dst, List<List<Mention>> orderedMentionsBySentence) {
//    Mention srcMention = orderedMentionsBySentence.get(src.get(0)).get(src.get(1));
//    Mention dstMention = orderedMentionsBySentence.get(dst.get(0)).get(dst.get(1));
//    if(src.get(0)==dst.get(0)) {
//      logger.fine(header + ": ["+srcMention.spanToString()+"](id="+srcMention.mentionID
//          +") in sent #"+src.get(0)+" => ["+dstMention.spanToString()+"](id="+dstMention.mentionID+") in sent #"+dst.get(0) + " Same Sentence");
//    } else {
//      logger.fine(header + ": ["+srcMention.spanToString()+"](id="+srcMention.mentionID
//          +") in sent #"+src.get(0)+" => ["+dstMention.spanToString()+"](id="+dstMention.mentionID+") in sent #"+dst.get(0));
//    }
//  }
  
  
  
}
