/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package LVCoref;

import java.io.BufferedWriter;
import java.util.Set;

/**
 *
 * @author arturs
 */
public class Utils {
    
    public static String linearizeMentionSet(Set<Mention> s) {
        String r;
        r = "[";
        for (Mention m : s) {
            r += m.node.word +"("+m.id+"),";
        }
        r+="]";
        return r;
    }
    
    
    
    public static String implode(Set<String> c, String glueString){
        StringBuilder s = new StringBuilder();
        for( String ss : c ) {
            s.append(ss);
            s.append('|');
        }
        if (c.size() > 0) {
            s.deleteCharAt(s.length()-1);
        }
        return s.toString();
    }
    
    public static void toWriter (BufferedWriter writer, String text) {
        try {
            writer.write(text);
        }
        catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }
    
    
    public static String getMentionPairString(Document d, Mention m, Mention n, String comment) {
        String s = ""; 
        if (LVCoref.props.getProperty(Constants.MMAX_GOLD_PROP, "").length() > 0) {
            if (m.node.goldMention != null && n.node.goldMention != null && m.node.goldMention.goldCorefClusterID == n.node.goldMention.goldCorefClusterID){
                s+= "+ ";
            } else {
                s+= "- ";
            }
        }
        if (comment.length() > 0) s += comment + ": ";
        s += "["+m.nerString+"]" + " ["+n.nerString+"]" + "\t\t("+m.getContext(d,3) +")" + "\t("+n.getContext(d,3) +")";
        return s;
    }
    
}
