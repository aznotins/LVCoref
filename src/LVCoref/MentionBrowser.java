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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Artūrs
 */
public class MentionBrowser {    
    
    public static Mention getFirst(Mention s, Expression expression, JexlContext jexlContext) {
        Mention t = s.prev();
        while (t != null) {
            if (s.type == Dictionaries.MentionType.NOMINAL && Filter.sentenceDistance(s, t) > Constants.SENTENCE_WINDOW) {t = null; break;}
            if (s.type == Dictionaries.MentionType.PRONOMINAL && Filter.sentenceDistance(s, t) > 3) {t = null; break;}
            if (s != t && _filterAgree(s, t, expression, jexlContext)) {
                break;
            }
            t = t.prev();
        }
        return t;
    }
    
    public static Mention getFirstFromAll(Mention s, Expression expression, JexlContext jexlContext) {
        Mention t = null;
    	for (Mention tt: s.document.mentions) {
            if (s != tt && _filterAgree(s, tt, expression, jexlContext)) {
            	t = tt;
                break;
            }
        }
        return t;
    }
    
    public static List<Mention> getAllFromSameSentence(Mention s, Expression expression, JexlContext jexlContext) {
        List<Mention> found = new ArrayList<Mention>();
        for (Mention tt: s.document.mentions) {
            if (s != tt && tt.sentNum == s.sentNum && _filterAgree(s, tt, expression, jexlContext)) {
                found.add(tt);
            }
        }
        return found;
    }
    
    public static Mention getFirstFromSintax(Mention s, Expression expression, JexlContext jexlContext) {
        Mention t = null;
        int minDist = Integer.MAX_VALUE;
        Mention prev = s.prev();
        while (prev != null) {
            if (s.type == Dictionaries.MentionType.NOMINAL && Filter.sentenceDistance(s, prev) > Constants.SENTENCE_WINDOW) {break;}
            if (s.type == Dictionaries.MentionType.PRONOMINAL && Filter.sentenceDistance(s, prev) > 3) {break;}
                        
            if (s != prev && _filterAgree(s, prev, expression, jexlContext)) {
                int dist = s.node.minDistance(prev.node);
                if (t == null || dist < minDist) {
                    t = prev;
                    minDist = dist;
                }
            }
            prev = prev.prev();
        }
        return t;
    }
    
    public static boolean _filterAgree(Mention s, Mention t, Expression expression, JexlContext jexlContext) {
        Boolean agree = true;
        try {
	        jexlContext.set("t", t);
	        agree = (Boolean) expression.evaluate(jexlContext);
        } catch (Exception ex) {        	
        	System.err.println("Error evaluating jexl expression");
        	ex.printStackTrace(System.err);
        }
        return agree;
    }
    
    
}
