package LVCoref;

import java.util.logging.Level;
import java.util.logging.Logger;

import LVCoref.util.Log;

public class Constants {
	private static final Logger log = Logger.getLogger( Log.class.getName() );
	//static { log.setLevel(Level.OFF); }
	
	/** if false, use mention prediction */
	public static boolean USE_GOLD_MENTIONS = false;
	public static boolean SCORE = false;
	public static String SCORE_PATH = "eval/all";
	public static String SCORE_OUT = "eval/processed/eval.conll";
	//public static String SCORE_EVAL_SCRIPT = null;
	public static String SCORE_EVAL_SCRIPT = null;//"scorer/scorer.bat";
	
	public static boolean PRINT_DECISIONS = false;
	
	public static boolean LOGGER_FILE = false;
	public static String LOGGER_FILE_PATH = "logs/";
	public static Level LOGGER_FILE_LEVEL = Level.FINE;
	public static Level LOGGER_ERR_LEVEL = Level.WARNING;
	public static Level LOGGER_LEVEL = Level.WARNING;
	
	public static boolean TAG_FOLDER = false;
	public static String TAG_FOLDER_PATH = "eval/all_new/";
	public static String TAG_FOLDER_OUT = "eval/processed/";
	
	public static boolean REPLICATE_CONLL = false;
	
	public static int SENTENCE_WINDOW = 30;
	
	public static boolean USE_SINTAX = true;
	
	public static boolean EXTRA_CONLL_COLUMNS = false;
	public static int SAVE_COLUMN_COUNT = 9;
	
	public static boolean VERBOSE = false;
	public static boolean DEBUG = false; // extra output (pretty json, ..)

	/** if true, use given mention boundaries */
	public static boolean USE_GOLD_MENTION_BOUNDARIES = false;

	/** if true, remove singletons in post processing */
	public static boolean REMOVE_SINGLETONS = false;

	/** if true, read *auto_conll, if false, read *gold_conll */
	public static boolean USE_CONLL_AUTO = true;

	public static int savedColumnCount = 7;

	/** Default sieve passes */
	public static String SIEVEPASSES = "ExactStringMatch,PreciseConstructs,RelaxedHeadMatch,PronounMatch";

	/** Share attributes between coreferent mentions **/
	public static boolean SHARE_ATTRIBUTES = true;

	public static String SIEVES_PROP = "lvcoref.sievePasses";
	public static String SCORE_PROP = "lvcoref.score";
	public static String LOG_PATH_PROP = "lvcoref.logFile";
	public static String LOG_PROP = "lvcoref.log";
	public static String MAXDIST_PROP = "lvcoref.maxdist";
	
	public static String INPUT_PROP = "lvcoref.input";
	public static String OUTPUT_PROP = "lvcoref.output";
	public static String MMAX_EXPORT_PROP = "lvcoref.mmax.export";
	public static String MMAX_EXPORT_PATH_PROP = "lvcoref.mmax.export.path";
	public static String MMAX_EXPORT_NAME_PROP = "lvcoref.mmax.export.name";
	public static String HTML_PROP = "lvcoref.html.output";
	public static String CONLL_INPUT_LIST = "lvcoref.input.conll";
	
	public static final String CONLL_SCORER = "lvcoref.conll.scorer";
	
	public static final String GOLD_MENTIONS_PROP = "lvcoref.gold.mentions";
	
	
	/** print the values of variables in this class */
	public static void printConstants() {
		if (Constants.USE_GOLD_MENTIONS) log.config("USE_GOLD_MENTIONS on");
		else log.config("USE_GOLD_MENTIONS off");
//		if (Constants.USE_GOLD_MENTION_BOUNDARIES) logger.info("USE_GOLD_MENTION_BOUNDARIES on");
//		else logger.info("USE_GOLD_MENTION_BOUNDARIES off");
		if (Constants.REMOVE_SINGLETONS) log.config("REMOVE_SINGLETONS on");
		else log.config("REMOVE_SINGLETONS off");
		log.config("=================================================================");
	}
}
