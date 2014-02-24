package LVCoref;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import LVCoref.ScorerBCubed.BCubedType;
import LVCoref.sievepasses.DeterministicCorefSieve;
import LVCoref.util.Log;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.StringUtils;

/**
 * Main process class
 * @author arturs
 */
public class LVCoref {
	private static final Logger log = Logger.getLogger( Log.class.getName() );
	//static { log.setLevel(Level.OFF); }
    
    private enum inputTypes {CONLL, STDIN_JSON, STDIN_CONLL};
    private enum outputTypes {CONLL, STDOUT_CONLL, STDOUT_JSON, NONE, TEXT};
    public static Properties props;
    public static Dictionaries dictionaries;

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
            + "\n\t" + Constants.MMAX_GOLD_PROP + ": file path to existing  mmax coref_level gold annotation, used for scoring"
            + "\n\t" + Constants.NER_PROP + ": file path to existing NE tagged file"
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
        maxSentDist = Integer.parseInt(props.getProperty(Constants.MAXDIST_PROP, "-1"));

        mmaxExport = Boolean.parseBoolean(props.getProperty(Constants.MMAX_EXPORT_PROP, "false"));
        mmaxExportPath = props.getProperty(Constants.MMAX_EXPORT_PATH_PROP, "");
        mmaxExportProjectName = props.getProperty(Constants.MMAX_EXPORT_NAME_PROP);
        
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
        
        log.severe(props.toString());
        Constants.printConstants(); //output constants to console
        
        dictionaries = new Dictionaries();
    }
    
    
	public static void main(String[] args) throws Exception {

		Properties properties = StringUtils.argsToProperties(args);
        //System.err.println(properties);
        
		Log.init();
        initializeProperties(properties);
        
        if (Constants.MULTIPLE_DOCS_EVAL) {
            inputConllList = new ArrayList<String>(Arrays.asList(conllInput.split(",")));
            String mmaxGold = props.getProperty(Constants.MMAX_GOLD_PROP, "");
            mmaxGoldList = new ArrayList<String>(Arrays.asList(mmaxGold.split(",")));
            nerList = new ArrayList<String>(Arrays.asList(nerAnnotation.split(",")));
            if (inputConllList.size() != mmaxGoldList.size()) System.err.println("Incorrect number of files for evaluation");    
            docID = 0;
        }
        
        if (Constants.MULTIPLE_DOCS_EVAL) {
            for (documentID=0; documentID < inputConllList.size(); documentID++) {
                System.err.println("NEW DOCUMENT: " + inputConllList.get(documentID));
                Document d = new Document(dictionaries);
                try {
                    d.readCONLL(inputConllList.get(documentID));
                } catch (Exception ex) {
                    System.err.println("Could not read conll file");
                    ex.printStackTrace();
                }
                if (d.tree.size() > 0) processDocument(d);                
            }
        } else {
        
        /*
         * Create document
         */
        BufferedReader in;
        switch(inputType) {
            case CONLL:
                Document d = new Document(dictionaries);
                try {
                    d.readCONLL(conllInput);
                } catch (Exception ex) {
                    System.err.println("Could not read conll file");
                    ex.printStackTrace();
                    break;
                }
                
                if (d.tree.size() > 0) processDocument(d);
                
                break;
            case STDIN_JSON:
                in = new BufferedReader(new InputStreamReader(System.in, "UTF8"));
                while (!stopProcess) {
                    Document doc = new Document(dictionaries);
                    try {
                        doc.readJSON(in);
                    } catch (Exception ex) {
                        System.err.println("Could not read json from stream");
                        ex.printStackTrace();
                        break;
                    }
                    if (doc.tree.size() > 0) processDocument(doc);  
                    else break;
                    documentID++;
                }
                break;
            default:
                in = new BufferedReader(new InputStreamReader(System.in, "UTF8"));
                while (!stopProcess) {
                    Document doc = new Document(dictionaries);
                    try {
                        doc.readCONLL(in);
                    } catch (Exception ex) {
                        System.err.println("Could not read conll from stream");
                        ex.printStackTrace();
                        break;
                    }
                    if (doc.tree.size() > 0) processDocument(doc);  
                    else break;
                    documentID++;
                }
            }
        }
    };
        
    
    public static void processDocument(Document d) { 
        String mmaxGold;
        if (props != null) {
	        if (Constants.MULTIPLE_DOCS_EVAL) {
	            mmaxGold = mmaxGoldList.get(documentID);
	//            if (nerList.size() > 0) {
	//                nerAnnotation = nerList.get(documentID);
	//            }
	        } else {
	            mmaxGold = props.getProperty(Constants.MMAX_GOLD_PROP, "");
	        }
	        if (mmaxGold.length() > 0) {d.addAnnotationMMAX(mmaxGold); }
        }
        
        d.updateProperWords();
        if (Constants.USE_GOLD_MENTIONS && mmaxGold.length() > 0) {
            d.useGoldMentions();
            d.setAbbreviationMentions(true);
            d.setProperNodeMentions(false);
            d.setMentionModifiers(false);
        } else {
            d.setMentionsFromNEAnnotation();
            d.setQuoteMentions();
            d.setAbbreviationMentions(false);
            d.setListMentions();
            d.setProperNodeMentions(true);
            d.setDetalizedNominalMentions();            
            
            // After creating all mentions
            d.normalizeMentions(); 	// sorts mentions (base and new mentions 
            						// could be out of order), removes ignored 
            						// base mentions
            d.removePleonasticMentions();
            d.removeNestedQuoteMentions();
            d.removeUndefiniedMentions();
            //d.removePluralMentions();
            d.removeNestedMentions();
            d.removeExcludedMentions();
            d.removeGenitiveMentions();
            d.setMentionModifiers_v2(true);
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
        for (Node n : d.tree) {
            n.markMentionBorders(d, true);
        }
        
//		d.printClusterRepresentatives();
//		d.printClusters();
//		d.printMentions();
//		d.printSimpleText(System.err);
        
        if(doScore()) {        
            log.info("Pairwise score for this doc: ");
            scoreSingleDoc.get(sieves.length-1).printF1(Log.log);
            log.info("Accumulated score: ");
            printF1(true);
            log.info("\n");

            singleDocMentionScorer.add(d);
            mentionScorer.add(d);
            stats.add(d, true);
            singleDocStats.add(d, true);

            log.fine("Document Statistics: ");
            log.fine(singleDocStats.corefStatistics(true));
            log.info("Accumulated Statistics: ");
            log.info(stats.corefStatistics(true));
            
            log.info("Mentions score: ");
            log.info(singleDocMentionScorer.getScore());
            log.info("Accumulated Mentions score: ");
            log.info(mentionScorer.getScore());  
        } else {
            stats.add(d, false);
            singleDocStats.add(d, false);
            log.fine("Document Statistics: ");
            log.fine(singleDocStats.corefStatistics(false));
            log.info("Accumulated Statistics: ");
            log.info(stats.corefStatistics(false));
        }

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
                    System.err.println("Unsupported output encoding");
                    ex.printStackTrace();
                }
                break;
            case TEXT :
                try {
                    ps = new PrintStream(System.out, true, "UTF8");
                    d.printSimpleText(ps);
                } catch (UnsupportedEncodingException ex) {
                    System.err.println("Unsupported output encoding");
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
                    System.err.println("Unsupported output encoding");
                    ex.printStackTrace();
                }
        }
        if (htmlOutput != null) {
            d.htmlOutput(htmlOutput);
        }
 
        if (Boolean.parseBoolean(props.getProperty(Constants.LOG_PROP, "false"))) {
            d.outputCONLL(logPath + timeStamp+ "_" + documentID + ".conll");
            
            d.htmlOutput(logPath + timeStamp+ "_" + documentID + ".html");
            d.htmlOutput(logPath + "log.html");
            d.outputCONLL(logPath + "log.conll");
        }

        if (mmaxExport) {
            String docId = (documentID == 0) ? "" : "_" + documentID;
            String projectName = (mmaxExportProjectName != null && mmaxExportProjectName.trim().length() > 0) ? mmaxExportProjectName+docId : timeStamp+ docId;
            MMAX2.createProject(d, projectName, mmaxExportPath);
        }
    }
	
    
    public static void coref(Document document, DeterministicCorefSieve sieve) {
        sieve.coreferent(document);
        additionalCorrectLinksCount = 0;
        additionalLinksCount = 0;
        //output scores
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

    public static boolean doScore() { 
        return doScore; 
    }
  
    public static void printF1(boolean printF1First) {
        scoreMUC.get(sieveClassNames.length - 1).printF1(Log.log, printF1First);
        scoreBcubed.get(sieveClassNames.length - 1).printF1(Log.log, printF1First);
        scorePairwise.get(sieveClassNames.length - 1).printF1(Log.log, printF1First);
    }
  
    
    private static void printSieveScore(Document document, DeterministicCorefSieve sieve) {
        log.severe("===========================================\n");
        log.severe("pass"+currentSieve+" " + sieve.getClass()+": \t"+ sieve.flagsToString());
        scoreMUC.get(currentSieve).printF1(Log.log);
        scoreBcubed.get(currentSieve).printF1(Log.log);
        scorePairwise.get(currentSieve).printF1(Log.log);
        log.severe("# of Clusters: "+document.corefClusters.size() + "\t,\t# of additional links: \t"+additionalLinksCount
            +"\t,\t# of additional correct links: \t"+additionalCorrectLinksCount
            +"\t,\tprecision of new links: \t"+1.0*additionalCorrectLinksCount/additionalLinksCount);
        log.severe("# of total additional links: \t"+linksCountInPass.get(currentSieve).second()
            +"\t,\t# of total additional correct links: \t"+linksCountInPass.get(currentSieve).first()
            +"\t,\taccumulated precision of this pass: \t"+1.0*linksCountInPass.get(currentSieve).first()/linksCountInPass.get(currentSieve).second());
        log.severe("--------------------------------------");
    }
}
