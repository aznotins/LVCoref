/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package LVCoref;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import LVCoref.util.Log;


/**
 *
 * @author Artūrs
 */
public class MentionScorer {
    int pc = 0;
    int rc = 0;
    int pn = 0;
    int rn = 0;
    double p=0;
    double r=0;
    double f=0;
    
    public void add(Document d) {
        for (Node n : d.tree) {
        	//Log.log.info(n + "\t"+  n.mention + "\t" + n.goldMention);
        	if (n.mention != null) {
        		if (n.goldMention != null) {
        			if (n.mention.start == n.goldMention.start && n.mention.end == n.goldMention.end)
        			pc++;
        		} else {
        			//Log.log.info(Utils.getMentionComment(d, n.mention, "Precision error"));
        		}
        		pn++;
        	}
        	if (n.goldMention != null) {
        		if (n.mention != null) {
        			if (n.mention.start == n.goldMention.start && n.mention.end == n.goldMention.end)
        			rc++;
        		} else {
        			//Log.log.info(Utils.getMentionComment(d, n.goldMention, "Recall error"));
        		}
        		rn++;
        	}
        }
    }
    
    public void calculate() {
        p = (pn>0)?((double)pc/pn):0;
        r=(rn>0)?((double)rc/rn):0;
        f = (p+r >0)?((double)2*p*r/(p+r)):0;
    }
    
    public String getScore() {
        StringBuilder sb = new StringBuilder();
        calculate();
        NumberFormat nf = new DecimalFormat("00.00");
        sb.append("MI");
        sb.append("\t").append(nf.format(f*100));
        sb.append("\t").append(nf.format(p*100));
        sb.append("\t").append(nf.format(r*100));
//        sb.append("-- Mentions score --" +nl);
//        sb.append("Precision: \t" + (double)p + "\t ("+pc+ "/"+pn+")" +nl);
//        sb.append("Recall: \t" + (double)r + "\t ("+rc+ "/"+rn+")" +nl);
//        sb.append("F1: \t" + f +nl);
//        sb.append("-----------------" +nl );
        return sb.toString();
    }
}
