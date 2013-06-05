package LVCoref;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.logging.Logger;

public class Constants {
   
    /** if false, use mention prediction */
    public static final boolean USE_GOLD_MENTIONS = false;
    
    public static final int SENTENCE_WINDOW = 30;
    
    public static final boolean USE_SINTAX = true;
    
    public static final boolean EXTRA_CONLL_COLUMNS = false;
    
    public static final boolean MULTIPLE_DOCS_EVAL = false;
    
    public static final boolean VERBOSE = true;

    /** if true, use given mention boundaries */
    public static final boolean USE_GOLD_MENTION_BOUNDARIES = false;

    /** if true, remove singletons in post processing */
    public static final boolean REMOVE_SINGLETONS = true;

    /** if true, read *auto_conll, if false, read *gold_conll */
    public static final boolean USE_CONLL_AUTO = true;

    public static final int savedColumnCount = 7;

    /** Default path for conll scorer script */
    public static final String conllMentionEvalScript = "/scr/nlp/data/conll-2011/scorer/v4/scorer.pl";

    /** Default sieve passes */
    public static final String SIEVEPASSES = "MarkRole, DiscourseMatch, ExactStringMatch, RelaxedExactStringMatch, PreciseConstructs, StrictHeadMatch1, StrictHeadMatch2, StrictHeadMatch3, StrictHeadMatch4, RelaxedHeadMatch, PronounMatch";

    /** Share attributes between coreferent mentions **/
    public static final boolean SHARE_ATTRIBUTES = true;

    public static final String SIEVES_PROP = "lvcoref.sievePasses";
    public static final String SCORE_PROP = "lvcoref.score";
    public static final String LOG_PATH_PROP = "lvcoref.logFile";
    public static final String LOG_PROP = "lvcoref.log";
    public static final String MAXDIST_PROP = "lvcoref.maxdist";
    
    public static final String STDOUT_PROP = "lvcoref.stdout";
    public static final String MMAX_EXPORT_PROP = "lvcoref.mmax.export";
    public static final String MMAX_EXPORT_PATH_PROP = "lvcoref.mmax.export.path";
    public static final String MMAX_EXPORT_NAME_PROP = "lvcoref.mmax.export.name";
    public static final String CONLL_OUTPUT_PROP = "lvcoref.conll.output";
    public static final String CONLL_INPUT_PROP = "lvcoref.conll.input";
    public static final String HTML_PROP = "lvcoref.html.output";
    public static final String MMAX_GOLD_PROP = "lvcoref.mmax.gold";
    public static final String NER_PROP = "lvcoref.ner";
    
    public static final String CONLL_SCORER = "lvcoref.conll.scorer";
    
    public static final String GOLD_MENTIONS_PROP = "lvcoref.gold.mentions";
    
    

   
//    public static void updateConstants(Properties props) {
//        if (Boolean.parseBoolean(props.getProperty(Constants.GOLD_MENTIONS_PROP, "false"))) Constants.USE_GOLD_MENTIONS = true;
//    }
    
    
    /** print the values of variables in this class */
    public static void printConstants(Logger logger) {
        
//        Field[] fields = Constants.class.getDeclaredFields();
//        for ( Field field : fields  ) {
//            try {
//                logger.info(field.getName() + ":"  + field.get(LVCoref.class));
//            } catch ( IllegalAccessException ex ) {
//                System.out.println(ex);
//          }
//        }
        
        if (Constants.USE_GOLD_MENTIONS) logger.info("USE_GOLD_MENTIONS on");
        else logger.info("USE_GOLD_MENTIONS off");
        if (Constants.USE_GOLD_MENTION_BOUNDARIES) logger.info("USE_GOLD_MENTION_BOUNDARIES on");
        else logger.info("USE_GOLD_MENTION_BOUNDARIES off");
        if (Constants.USE_CONLL_AUTO) logger.info("use conll auto set -> if GOLD_NE, GOLD_PARSE, GOLD_POS, etc turned on, use auto");
        else logger.info("use conll gold set -> if GOLD_NE, GOLD_PARSE, GOLD_POS, etc turned on, use gold");
        if (Constants.REMOVE_SINGLETONS) logger.info("REMOVE_SINGLETONS on");
        else logger.info("REMOVE_SINGLETONS off");
        logger.info("=================================================================");
    }
}
