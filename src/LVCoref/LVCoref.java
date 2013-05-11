package LVCoref;

import LVCoref.ScorerBCubed.BCubedType;
import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Main process class
 * @author arturs
 */
public class LVCoref {
    
    private enum inputTypes {CONLL, STDIN_CONLL};
    private enum outputTypes {CONLL, STDOUT_CONLL};
    public static final Logger logger = Logger.getLogger(LVCoref.class.getName());

    /**
    * If true, we score the output of the given test document
    * Assumes gold annotations are available
    */
    //private static int maxSentDist = 100;
    private static String mmaxExportPath = "data/mmax2/";
    private static String mmaxExportProjectName = "";
    private static boolean mmaxExport = false;
    private static String logPath = "data/logs/";
    private static boolean keepLogs = false;
    private static String conllInput = null;
    private static String conllOutput = null;
    private static String htmlOutput = null;
    private static String mmaxGold = null;
    private static String nerAnnotation = null;
    
    private static inputTypes inputType = inputTypes.STDIN_CONLL;
    private static outputTypes outputType = outputTypes.STDOUT_CONLL;
    
    public static String timeStamp = ""; //for logs
    public static int documentID = 0;
    public static boolean stopProcess = false;

    /** Scores for each pass */
    public List<CorefScorer> scorePairwise;
    public List<CorefScorer> scoreBcubed;
    public List<CorefScorer> scoreMUC;

    public List<CorefScorer> scoreSingleDoc;
    
    

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
        if (keepLogs) {
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
            logger.fine("----Parameters------"
                    + "\nmmaxExportPath: " + mmaxExportPath
                    + "\nmmaxExport: " + mmaxExport
                    + "\nlogPath: " + logPath
                    + "\nkeepLogs: " + keepLogs
                    + "\nconllInput: " + conllInput
                    + "\nconllOutput: " + conllOutput
                    + "\nhtmlOutput: " + htmlOutput
                    + "\nmmaxGold: " + mmaxGold
                    + "\nnerAnnotation: "+ nerAnnotation
                    + "\ninputType: " + inputType
                    + "\noutputType: " + outputType
                    + "\n----------\n"
                    );
        }
        //logger.fine(props.toString());
        //Constants.printConstants(logger);
    }
      
	public static void main(String[] args) throws Exception {
        /**
         * Parse arguments
         */
        try {
            int arg_i = 0;
            while (arg_i < args.length) {
                if (args[arg_i].equalsIgnoreCase("-log")) keepLogs = true;
                if (args[arg_i].equalsIgnoreCase("-mmaxExport")) mmaxExport = true;
                if (args[arg_i].equalsIgnoreCase("-stdout")) { outputType = outputTypes.STDOUT_CONLL; }
                if (args[arg_i].equalsIgnoreCase("--logPath")) logPath = args[arg_i+1];
                if (args[arg_i].equalsIgnoreCase("--conllInput")) { conllInput = args[arg_i+1]; inputType = inputTypes.CONLL; }
                if (args[arg_i].equalsIgnoreCase("--mmaxExportPath")) mmaxExportPath = args[arg_i+1];
                if (args[arg_i].equalsIgnoreCase("--mmaxExportProject")) mmaxExportProjectName = args[arg_i+1];
                if (args[arg_i].equalsIgnoreCase("--conllOutput")) { conllOutput = args[arg_i+1]; outputType = outputTypes.CONLL; }
                if (args[arg_i].equalsIgnoreCase("--htmlOutput")) htmlOutput = args[arg_i+1];
                if (args[arg_i].equalsIgnoreCase("--mmaxGold")) { mmaxGold = args[arg_i+1]; }
                if (args[arg_i].equalsIgnoreCase("--nerAnnotation")) nerAnnotation = args[arg_i+1];
                
                if (args[arg_i].equalsIgnoreCase("-h") || args[arg_i].equalsIgnoreCase("--help") || args[arg_i].equalsIgnoreCase("-?")) {
                    System.out.print(
                            "--------------------\n"
                            + "LVCoref: Latvian Coreference Resolver"
                            + "\nParameters:"
                            
                            + "\n\t-stdout: write conll format results to console (default)"                  
                            + "\n\t-log: keep logs, defaults to false"
                            + "\n\t--logPath: directory path with trailing /, (default = data/logs/)"
                            + "\n\t--conllInput: path to conll input file"
                            + "\n\t--mmaxExportPath: MMAX export path with trailing / (default = data/mmax2/"
                            + "\n\t--mmaxExportProject: project name (if no specified timestamp is used"
                            + "\n\t--htmlOutput: file path for html formatted coreference results, direcotory should include script.js, style.css"
                            + "\n\t--conllOutput: file path for conll output to file"
                            + "\n\t--mmaxGold: file path to existing  mmax coref_level gold annotation, used for scoring"
                            + "\n\t--nerAnnotation: file path to existing NE tagged file"
                            + "\n--------------------\n"
                    );
                    System.out.flush();
                    System.exit(0);
                }
                arg_i++;
            }
        } catch (Exception ex) {
            System.err.println("ERROR: cannot parse arguments");
        }
            
        /**
         * TMP default values
         */
//        inputType = inputTypes.CONLL;
//        conllInput = "data/pipeline/parsed.tmp";
//        conllOutput = "dainterview_46_nerta/output.conll";
//        htmlOutput = "data/output.html";
//        //nerAnnotation = "data/pipeline/ner.tmp";
//        conllInput = "data/pipeline/interview_46.conll";
//        nerAnnotation = "data/pipeline/interview_46_ner.txt"; 
//        mmaxGold = "data/interview_46_coref_level.xml";
//        
//        keepLogs = true;
//        
        timeStamp = Calendar.getInstance().getTime().toString().replaceAll("\\s", "-").replaceAll(":", "-");
        createLogger();
        
        /*
         * Create document
         */
        switch(inputType) {
            case CONLL:
                Document d = new Document(logger);
                try {
                    d.readCONLL(conllInput);
                } catch (Exception ex) {
                    System.err.println("Could not read conll file");
                    System.err.println(ex.getStackTrace());
                    break;
                }
                
                if (d.tree.size() > 0) processDocument(d);
                
                break;
            default:
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "UTF8"));
                while (!stopProcess) {
                    Document doc = new Document(logger);
                    try {
                        doc.readCONLL(in);
                    } catch (Exception ex) {
                        System.err.println("Could not read conll from stream");
                        System.err.println(ex.getStackTrace());
                        break;
                    }
                    if (doc.tree.size() > 0) processDocument(doc);  
                    else break;
                    documentID++;
                }
        }
    };
        
    
    public static void processDocument(Document d) {
        if (nerAnnotation != null) { d.setMentionsNER(nerAnnotation); } //pagaidām izskatās NER nesniedz nekādu lielu uzlabojumu
        
        d.setQuoteMentions();
        d.setAbbreviationMentions();
//             d.setMentions();
//             for (Mention m : d.mentions) {
//                 if (m.categories.size() == 0) m.setCategories(d);
//             }
        d.setListMentions();
        d.tweakPersonMentions();
        d.removeNestedMentions();
        d.sortMentions(); //needed for normalization (array index equals to id)

//        for(Mention m : d.mentions) {
//            System.out.println(m.nerString + " " + m.headString +  " node=" + m.node.word + " " + m.node.mention.node.word);
//        }
//        d.visualizeParseTree();
//        d.printMentions();
//        d.printNodes(d.tree);


        d.initializeEntities();
        Resolve.go(d, logger);

        d.removePronounSingletonMentions();
        //d.removeSingletonMentions();

        for (Node n : d.tree) {
            n.markMentionBorders(d, false);
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
                    d.outputCONLL(ps);
                } catch (UnsupportedEncodingException ex) {
                    System.err.println("Unsupported output encoding");
                    Logger.getLogger(LVCoref.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        if (htmlOutput != null) {
            d.htmlOutput(htmlOutput);
        }
 
        if (keepLogs) {
            d.outputCONLL(logPath + timeStamp+ "_" + documentID + ".conll");
            d.htmlOutput(logPath + timeStamp+ "_" + documentID + ".html");
        }

        if (mmaxExport) {
            String docId = (documentID == 0) ? "" : "_" + documentID;
            String projectName = (mmaxExportProjectName != null && mmaxExportProjectName.trim().length() > 0) ? mmaxExportProjectName+docId : timeStamp+ docId;
            MMAX2.createProject(d, projectName, mmaxExportPath);
        }

        if (mmaxGold != null) {
            d.addAnnotationMMAX(mmaxGold);
            
            CorefScorer scorerPairwise = new ScorerPairwise();
            scorerPairwise.calculateScore(d);
            scorerPairwise.printF1(logger, true);            

            CorefScorer scorerMUC = new ScorerMUC();
            scorerMUC.calculateScore(d);
            scorerMUC.printF1(logger, true);            

            CorefScorer scorerB3 = new ScorerBCubed(BCubedType.Bconll);
            scorerB3.calculateScore(d);
            scorerB3.printF1(logger, true);
            
            System.err.println(scorerPairwise.getF1String(true));
            System.err.println(scorerMUC.getF1String(true));
            System.err.println(scorerB3.getF1String(true));
        }
        

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
	

}
