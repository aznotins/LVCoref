package LVCoref;

import LVCoref.util.Pair;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;



/**
 * Main process class
 * @author arturs
 */
public class LVCoref {
    
    
    public static final Logger logger = Logger.getLogger(LVCoref.class.getName());

  /**
   * If true, we score the output of the given test document
   * Assumes gold annotations are available
   */
  private boolean doScore;

  /**
   * If true, we do post processing.
   */
  private boolean doPostProcessing;

  /**
   * maximum sentence distance between two mentions for resolution (-1: no constraint on distance)
   */
  private int maxSentDist;

  /**
   * automatically set by looking at sieves
   */
  private boolean useSemantics;

  /** Final score to use for sieve optimization (default is pairwise.Precision) */
  private String optimizeScoreType;
  /** More useful break down of optimizeScoreType */
  private boolean optimizeConllScore;
  private String optimizeMetricType;
  private CorefScorer.SubScoreType optimizeSubScoreType;

  /**
   * Array of sieve passes to be used in the system
   * Ordered from highest precision to lowest!
   */
  //private /*final */DeterministicCorefSieve [] sieves;
  //private /*final*/ String [] sieveClassNames;

  /** Current sieve index */
//  public int currentSieve;

  /** counter for links in passes (Pair<correct links, total links>)  */
  public List<Pair<Integer, Integer>> linksCountInPass;


  /** Scores for each pass */
  public List<CorefScorer> scorePairwise;
  public List<CorefScorer> scoreBcubed;
  public List<CorefScorer> scoreMUC;

  private List<CorefScorer> scoreSingleDoc;

  /** Additional scoring stats */
  int additionalCorrectLinksCount;
  int additionalLinksCount;

  public static class LogFormatter extends Formatter {
    @Override
    public String format(LogRecord rec) {
      StringBuilder buf = new StringBuilder(1000);
      buf.append(formatMessage(rec));
      buf.append('\n');
      return buf.toString();
    }
  }

	public static void main(String[] args) throws Exception {
        String input_file = "data/input.conll";
        String output_file = "data/output.conll";
        String output_html = "data/output.html";
        
        input_file = "data/pipeline/parsed.tmp";
        output_file = "data/pipeline/output.conll";
        //output_html = "data/pipeline/output.html";
        
        String timeStamp = Calendar.getInstance().getTime().toString().replaceAll("\\s", "-").replaceAll(":", "-");
        
        try {
            String logFileName = "data/logs/log.txt";
            if(logFileName.endsWith(".txt")) {
                logFileName = logFileName.substring(0, logFileName.length()-4) +"_"+ timeStamp+".txt";
            } else {
                logFileName = logFileName + "_"+ timeStamp+".txt";
            }
            FileHandler fh = new FileHandler(logFileName, false);
            logger.addHandler(fh);
            logger.setLevel(Level.FINE);
            fh.setFormatter(new LogFormatter());
        } catch (SecurityException e) {
        System.err.println("ERROR: cannot initialize logger!");
        throw e;
        } catch (IOException e) {
            System.err.println("ERROR: cannot initialize logger!");
            throw e;
        }

        logger.fine(timeStamp);
        //logger.fine(props.toString());
        //Constants.printConstants(logger);
        
        
        if (args.length > 2) {
            for (String s: args) {
                if (s.startsWith("--input_file=")) {
                    input_file = s.replaceAll("\"|--input_file=", "");
                } else if (s.startsWith("--output_file=")) {
                    output_file = s.replaceAll("\"|--output_file=", "");
                } else if (s.startsWith("--output_html=")) {
                    output_html = s.replaceAll("\"|--output_html=", "");
                }
            }
        } else {
            
        }
            Document d;
            d = new Document();

            //System.out.println(d.dict.firstNames);

            //d.readCONLL("data/Sofija.conll");
           // d.readCONLL("data/SofijasPasaule1996_11-28-dep-unlabeled.conll");
            //d.readCONLL("data/intervija-unlabeled.conll");
           //d.readCONLL("data/LETA_IzlaseFreimiem-dep-unlabeled.conll");
           d.readCONLL(input_file);

            d.setMentionsNER("data/pipeline/ner.tmp");
            d.setQuoteMentions();
            d.setMentions();
            for (Mention m : d.mentions) {
                if (m.categories.size() == 0) m.setCategories(d);
            }
            
            
            //normalize mentions
            Collections.sort(d.mentions);
            for(int i = 0; i < d.mentions.size(); i++) {
                d.mentions.get(i).id = i;
            }

            
//            for(Mention m : d.mentions) {
//                System.out.println(m.nerString + " " + m.headString +  " node=" + m.node.word + " " + m.node.mention.node.word);
//            }
            
            //d.visualizeParseTree();



    //        List<Node> tmp;
    //        tmp = d.traverse(d.tree.get(1), null, new ArrayList<Node>(Arrays.asList(d.tree.get(1))));
    //        d.printNodes(tmp);

            //d.printMentions();
            //d.printNodes(d.tree);


            d.initializeEntities();
            Resolve.go(d, logger);


            for (Node n : d.tree) {
                n.markMentionBorders(d, false);
            }

            //RefsToEntities.go(d);

            //d.printCoreferences();

            //d.addAnnotationMMAX("data/sample_coref_level.xml");
            
            if (!output_file.isEmpty()) {
               d.outputCONLL(output_file);
               d.outputCONLLforDavis(output_file+".davis");
            }
            
            if (!output_html.isEmpty()) {
               d.htmlOutput(output_html);
            }
            
            
//            CorefScorer scorer = new ScorerPairwise();
//            scorer.calculateScore(d);
//            scorer.printF1(logger, true);
//            System.out.println(scorer.getF1String(true));
//            
//            CorefScorer scorerMUC = new ScorerMUC();
//            scorerMUC.calculateScore(d);
//            scorerMUC.printF1(logger, true);
//            System.out.println(scorerMUC.getF1String(true));
//            
//            CorefScorer scorerB3 = new ScorerBCubed(BCubedType.Bconll);
//            scorerB3.calculateScore(d);
//            scorerB3.printF1(logger, true);
//            System.out.println(scorerB3.getF1String(true));
            


    //		for (Node n: d.tree) {
    //			System.out.print(" " + n.word);
    //			if (n.mention != null && d.corefClusters.get(n.mention.corefClusterID).corefMentions.size() > 1) {
    //                Mention ant = d.refGraph.getFinalResolutions().get(n.mention);
    //                System.out.print("["+n.mention.corefClusterID+"/"+n.mention.id+"/"+((ant == null)?null:ant.id)+"/"+n.mention.type+"/"+n.mention.categories+"@"+n.mention.comments+"]");
    //            }
    //			if (n.word.equals(".")) System.out.println();
    //		}
    //		System.out.println();

        };
        
	

}
