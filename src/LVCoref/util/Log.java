package LVCoref.util;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import LVCoref.Constants;

public class Log {
	
	public static final Logger log = Logger.getLogger(Log.class.getName());
	public static String id;
	public static String logPath = Constants.LOGGER_FILE_PATH;
	
	static {
		init();
	}
	
	public static class LogFormatter extends Formatter {
        @Override
        public String format(LogRecord rec) {
            StringBuilder buf = new StringBuilder(1000);
            if (rec.getLevel().intValue() > Level.INFO.intValue()) {
            	buf.append(String.format("%s [%s] %s.%s()\n",
            			rec.getLevel(), 
            			new Date(), 
            			rec.getSourceClassName(),
            			rec.getSourceMethodName()));
            }
            buf.append(formatMessage(rec));
            buf.append('\n');
            return buf.toString();            
        }
    }
	
	/**
	 * Output message to System.err
	 * @param msg
	 */
	public static void p(String msg) { System.err.println(msg); }
	public static void pt(String msg) {
		Date now = new Date();    	
//    	SimpleDateFormat sdf = new SimpleDateFormat("E, y-M-d 'at' h:m:s a z");
//    	System.out.println( sdf.format(now) );
		System.err.printf("[%s]\n%s\n", now, msg);
	}
	/**
	 * Output message to System.out
	 * @param msg
	 */
	public static void out(String msg) { System.out.println(msg); }
	
	public static void sev(String msg) { log.severe(msg); }
	public static void war(String msg) { log.warning(msg); }
	public static void inf(String msg) { log.info(msg); }
	public static void fin(String msg) { log.fine(msg); }
	
	public static void init() {
		id = Calendar.getInstance().getTime().toString().replaceAll("\\s", "-").replaceAll(":", "-");
		//remove all old log handlers		
		for(Handler fh : log.getHandlers()) { log.removeHandler(fh); }
		// remove default console logger
		log.setUseParentHandlers(false);
		
		// level fine messages to file logger
		if (Constants.LOGGER_FILE) {
			String logFileName = logPath + id + "_log.txt";
			try {
	            FileHandler fh = new FileHandler(logFileName, false);
	            log.addHandler(fh);
	            fh.setFormatter(new LogFormatter());
	            fh.setLevel(Constants.LOGGER_FILE_LEVEL);
	        } catch (IOException e) {
	            System.err.println("ERROR: cannot initialize file logger!");
	        }
		}
		
		// level info and higer to system.err
		StreamHandler errSh = new StreamHandler(System.err, new LogFormatter());
		errSh.setLevel(Constants.LOGGER_ERR_LEVEL);
		log.addHandler(errSh);
		log.setLevel(Constants.LOGGER_LEVEL);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		init();
		log.severe("severe");
		log.warning("warning");
		log.info("info");
		log.fine("fine");
	}
}
