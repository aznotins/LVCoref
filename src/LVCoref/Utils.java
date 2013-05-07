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
}
