/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package LVCoref;

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
        pn += d.mentions.size();
        rn += d.goldMentions.size();
        for (Node n : d.tree) {
            if (n.mention != null && n.goldMention != null) {
                pc++;
            } else if (n.mention != null) {
                //System.out.println(Utils.getMentionComment(d, n.mention, "Precision error"));
            }
            if (n.goldMention != null && n.mention != null) {
                rc++;
            } else if (n.goldMention != null) {
                //System.out.println(Utils.getMentionComment(d, n.goldMention, "Recall error"));
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
        String nl = "\n";
        calculate();
        sb.append("--Mentions eval--" +nl);
        sb.append("Precision: \t" + (double)p + "\t ("+pc+ "/"+pn+")" +nl);
        sb.append("Recall: \t" + (double)r + "\t ("+rc+ "/"+rn+")" +nl);
        sb.append("F1: \t" + f +nl);
        sb.append("-----------------" +nl );
        return sb.toString();
    }
}
