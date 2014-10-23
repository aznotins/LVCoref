/*******************************************************************************
 * Copyright 2013,2014 Institute of Mathematics and Computer Science, University of Latvia
 * Author: Artūrs Znotiņš
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package LVCoref;

import java.io.BufferedWriter;
import java.util.Set;

import edu.stanford.nlp.util.StringUtils;

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
    
    public static String val(String key) {
        return "<span class='svalue' style='display:block;color:#666;'>"+key+"</span>";
    }
    public static String bval(String key) {
        return "<b>" + val(key) + "</b>";
    }
    public static String keyValue(String key, Object value) {
        String str = "<span class='key' style='display:inline-block;width:200px;color:#666; padding-left:10px;color:#666'>"+ key + "</span>" + "<span class='value' style='display:inline-block'>" + value.toString() + "</span><br />";
        return str;
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
        if (Constants.SCORE) {
            if (m.node.goldMention != null && n.node.goldMention != null && n.node.goldMention.goldCorefClusterID == n.node.goldMention.goldCorefClusterID){
                s+= "+ ";
            } else {
                s+= "- ";
            }
        }
        if (comment.length() > 0) s += comment + ": ";
        s += "["+m.nerString+"]" + " ["+n.nerString+"]" + "\t\t("+m.getContext(d,3) +")@"+ m.node.id + "\t("+n.getContext(d,3) +")@"+ n.node.id;
        return s;
    }
    
    public static String getMentionComment(Document d, Mention m, String comment) {
        String s = "@@@";
        if (comment.length() > 0) s = comment + ": ";
        s +=  "["+m.nerString+"]" + "\t\t("+m.getContext(d,3)+")@"+m.node.id;
        s += " \t@type="+m.type 
                +" \t@cat="+m.categories
                +" \t@id=" +m.id
                +" \t@head="+m.node.word+"|"+m.node.tag;
        return s;
    }
    
    public static String prettyJSON(String s) {
    	int indent = 0;
    	String tab = "  ";
    	StringBuilder sb = new StringBuilder();
    	for (int i = 0; i < s.length(); i++) {
    		Character c = s.charAt(i);
    		if (c == '[' || c == '{') {
    			sb.append(c);
    			sb.append('\n');
    			indent++;
    			sb.append(StringUtils.repeat(tab, indent));
    		} else if (c == ',') {
    			sb.append(c);
    			sb.append('\n');
    			sb.append(StringUtils.repeat(tab, indent));
    		} else if (c == ']' || c == '}') {
    			sb.append('\n');
    			indent--;
    			sb.append(StringUtils.repeat(tab, indent));
    			sb.append(c);
    		} else {
    			sb.append(c);
    		}
    	}
    	return sb.toString();
    }
    
}
