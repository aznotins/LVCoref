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

import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;

import LVCoref.Dictionaries.MentionType;
import LVCoref.util.Log;


public class Resolve {
	private static final Logger log = Logger.getLogger( Log.class.getName() );
	//static { log.setLevel(Level.OFF); }
	
    public static final boolean JEXL_SILENT = true;
    public static final boolean JEXL_LENIENT = false;
    public static final boolean JEXL_DBG = false;
    
    public static void go(Document d) {
        //naiveHeadMatch(d);
        
        exactStringMatch(d);
        appositive(d);
        plainAppositive(d);
        acronymMatch(d);
        predicativeNominative(d);
        firstPersonPlural(d);
        firstPersonSingular(d);
        secondPersonSingular(d);
       
        relaxedHeadMatch(d);
        d.setTmpMentions();
        d.initializeEntities();
        relativePronounMatch(d);
        sintaxPronounMatch(d);
        d.removeTmpMentions();
    }
    
    private static void _resolveFirst(Document d, String filter, String comment) {        
        JexlEngine jexl = new JexlEngine();
        jexl.setSilent(JEXL_SILENT);
        jexl.setLenient(JEXL_LENIENT);
        jexl.setDebug(JEXL_DBG);
        Expression expression = jexl.createExpression(filter);
        JexlContext jexlContext = new MapContext();
        jexlContext.set("Filter", Filter.class);
        Mention t;
        for (Mention s: d.mentions) {
            jexlContext.set("s", s);
            t = MentionBrowser.getFirst(s, expression, jexlContext);            
            if (t != null) _resolve(d, s, t, comment);
        }
    }
    private static void _resolveFirstFromSintax(Document d, String filter, String comment) {        
        JexlEngine jexl = new JexlEngine();
        jexl.setSilent(JEXL_SILENT);
        jexl.setLenient(JEXL_LENIENT);
        jexl.setDebug(JEXL_DBG);
        Expression expression = jexl.createExpression(filter);
        JexlContext jexlContext = new MapContext();
        jexlContext.set("Filter", Filter.class);
        Mention t;
        for (Mention s: d.mentions) {
            jexlContext.set("s", s);
            t = MentionBrowser.getFirstFromSintax(s, expression, jexlContext); 
            if (t != null) _resolve(d, s, t, comment);
        }
    }
    private static void _resolveFirstFromAll(Document d, String filter, String comment) {        
        JexlEngine jexl = new JexlEngine();
        jexl.setSilent(JEXL_SILENT);
        jexl.setLenient(JEXL_LENIENT);
        jexl.setDebug(JEXL_DBG);
        Expression expression = jexl.createExpression(filter);
        JexlContext jexlContext = new MapContext();
        jexlContext.set("Filter", Filter.class);
        Mention t;
        for (Mention s: d.mentions) {
            jexlContext.set("s", s);
            t = MentionBrowser.getFirstFromAll(s, expression, jexlContext);            
            if (t != null) _resolve(d, s, t, comment);
        }
    }
    private static void _resolveAllFromSameSentence(Document d, String filter, String comment) {        
        JexlEngine jexl = new JexlEngine();
        jexl.setSilent(JEXL_SILENT);
        jexl.setLenient(JEXL_LENIENT);
        jexl.setDebug(JEXL_DBG);
        Expression expression = jexl.createExpression(filter);
        JexlContext jexlContext = new MapContext();
        jexlContext.set("Filter", Filter.class);
        for (Mention s: d.mentions) {
            jexlContext.set("s", s);
            List<Mention> tt = MentionBrowser.getAllFromSameSentence(s, expression, jexlContext);      
            for (Mention t : tt) {
                _resolve(d, s, t, comment);
            }
            
        }
    }
    private static void _resolve(Document d, Mention s, Mention t, String comment) {
        if (s.corefClusterID == t.corefClusterID) return;   
        d.mergeClusters(s, t);
        s.addRefComm(t, comment);
        if (Constants.SCORE) {
            if ((s.node.goldMention == null || t.node.goldMention == null || s.node.goldMention.corefClusterID != t.node.goldMention.corefClusterID)) {
                if (Constants.PRINT_DECISIONS) log.info("-" + Utils.getMentionPairString(d, s, t, comment));
            } else {
                if (s.node.goldMention != null && t.node.goldMention != null && s.node.goldMention.corefClusterID == t.node.goldMention.corefClusterID) {
                    if (Constants.PRINT_DECISIONS) log.info("+" + Utils.getMentionPairString(d, s, t, comment));
                }
            }
        } else {
            if (Constants.PRINT_DECISIONS) log.fine(Utils.getMentionPairString(d, s, t, comment));        	
        }
        s.setAsResolved();
    }
    
    public static void naiveHeadMatch(Document d) {
        //String filter = "!Filter.pronominal(s) && Filter.sameHead(s,t)";
        String filter = "Filter.sameHead(s,t) && !Filter.pronominal(s) && Filter.sameGender(s,t) && Filter.sameNumber(s,t)";
        _resolveFirst(d, filter, "naiveHead");
        if (Constants.VERBOSE) System.err.println("Operation count = " + Filter.op);
    }
   
    public static void exactStringMatch(Document d) {
        String filter = "Filter.proper(s) && Filter.proper(t) && !Filter.pronominal(s) && Filter.exactMatch(s,t)"; //if (prev.normString.equals(m.normString) &&( prev.number == m.number /*&& prev.type == m.type*/|| m.bucket.equals("acronym") || prev.bucket.equals("acronym"))) {
        _resolveFirst(d, filter, "exactStringMatch");
        if (Constants.VERBOSE) System.err.println("Operation count = " + Filter.op);
    }    
    public static void relaxedHeadMatch(Document d) {
        String filter = "!Filter.isTime(s) && !Filter.isSum(s)"
                + "&& !Filter.pronominal(s) "
                + "&& Filter.sameHead(s,t) "
                + "&& Filter.sameGender(s,t) "
                + "&& Filter.sameNumber(s,t) "
                //+ "&& Filter.containsAllClusterModifiers(s,t)"
                + "&& Filter.modifierConstraint(s,t)"
                + "";
        _resolveFirst(d, filter, "relaxedHeadMath-allClusterModifiers");
        if (Constants.VERBOSE) System.err.println("Operation count = " + Filter.op);
    }    

    public static void acronymMatch(Document d){
        String filter = "Filter.isAcronym(s) && Filter.proper(t) && Filter.isAcronymOf(s,t)";// 
        _resolveFirstFromAll(d, filter, "acronym");
    }

    public static void appositive(Document d) {
        // "gazprom" prezidents //quotas
        String filter = "(Filter.nominal(s) && Filter.proper(t) && !Filter.after(s,t) "
                    + "|| Filter.proper(s) && Filter.nominal(t) && Filter.after(s,t) "
                    + "|| Filter.proper(s) && Filter.proper(t) )"
                + "&& Filter.sameGender(s,t) "
                + "&& Filter.sameNumber(s,t) "
                + "&& Filter.sameCase(s,t) "
                + "&& Filter.sameCategoryConstraint(s,t) "
                + "&& !Filter.dominated(s,t) "
                + "&& Filter.distance(s,t) < 3 "
                + "&& (!Filter.genitive(s,t) || Filter.isPerson(s) || Filter.isPerson(t)) "
                + "&& !((Filter.isLocation(s) || Filter.isLocation(t)) && (Filter.locative(s) || Filter.locative(t)))"
                + "&& Filter.sentenceDistance(s,t) < 1 "
                + "&& Filter.inApposition(s,t) ";
//                + "||"
//                + "Filter.isQuoteMention(s)";
        _resolveAllFromSameSentence(d, filter, "appositive");
        if (Constants.VERBOSE) System.err.println("Operation count = " + Filter.op);
    } 
    public static void plainAppositive(Document d) {
        // "gazprom" prezidents //quotas
        String filter = "(Filter.nominal(s) && Filter.proper(t) && !Filter.after(s,t) "
                    + "|| Filter.proper(s) && Filter.nominal(t) && Filter.after(s,t)"
                    + "|| Filter.proper(s) && Filter.proper(t) )"
                + "&& Filter.sameGender(s,t) "
                + "&& Filter.sameNumber(s,t) "
                + "&& Filter.sameCase(s,t) "
                + "&& Filter.sameCategoryConstraint(s,t) "
                + "&& (!Filter.genitive(s,t) || Filter.isPerson(s) || Filter.isPerson(t)) "
                //+ "&& !((Filter.isLocation(s) || Filter.isLocation(t)) && (Filter.locative(s) || Filter.locative(t)))"
                + "&& Filter.inPlainApposition(s,t) ";
//                + "||"
//                + "Filter.isQuoteMention(s)";
        _resolveAllFromSameSentence(d, filter, "plain_appositive");
        if (Constants.VERBOSE) System.err.println("Operation count = " + Filter.op);
    } 
    
    public static void sintaxPronounMatch(Document d) {
        String filter = "Filter.pronominal(s) "
                + "&& !Filter.personPronounsSkewed(s)"
                + "&& !Filter.pronominalIndefinite(s)"
                + "&& !Filter.pronominalModificator(s)"
                + "&& !Filter.pronominalRelative(s)"
                + "&& Filter.sameCategoryConstraint(s,t) "
                //+ "&& !Filter.isResolved(s) "
                + "&& !Filter.pronominal(t) "
                + "&& Filter.sentenceDistance(s,t) < 2 "
                + "&& ( Filter.sameGender(s,t) && Filter.sameNumber(s,t)"
                    + "|| Filter.pronominalReflexive(s)"
                    + "|| Filter.pronominalPossesive(s) )"
                + "";
        _resolveFirstFromSintax(d, filter, "pronounMatch");
        if (Constants.VERBOSE) System.err.println("Operation count = " + Filter.op);
    }
    
    public static void relativePronounMatch(Document d) {
        //pielikt "tas" <-
        String filter = "Filter.pronominalRelative(s) "
                //+ "&& !Filter.pronominal(t) "
                + "&& Filter.sentenceDistance(s,t) < 1 "
                + "&& (Filter.distance(s,t) < 3 || Filter.isQuoteMention(t) && Filter.distance(s,t) < 5)"
                //+ "&& Filter.sameGender(s,t)"
                //+ "&& Filter.sameNumber(s,t)"
                + "";
        _resolveFirst(d, filter, "relativePronounMatch");
        if (Constants.VERBOSE) System.err.println("Operation count = " + Filter.op);
    }

    public static void predicativeNominative(Document d) {
        String filter = "Filter.nominative(s) "
        		+ "&& !Filter.pronominalRelative(s) && !Filter.pronominalRelative(t)"
                + "&& Filter.nominative(t) "
                + "&& Filter.sameSentece(s,t)"
                + "&& Filter.distance(s,t) < 15"
                + "&& Filter.sameNumber(s,t)"
                + "&& Filter.sameGender(s,t)"                
                + "&& Filter.sameCategoryConstraint(s,t) "
                + "&& Filter.inPredicativeNominative(s,t)";
        _resolveAllFromSameSentence(d, filter, "predicativeNominative");
        if (Constants.VERBOSE) System.err.println("Operation count = " + Filter.op);
    }
        
        
	
//	public static Boolean genetiveBad(Document d, Mention m) {
//		if (m.type == MentionType.NOMINAL && m.mentionCase == Case.GENITIVE) {
//			if (m.node.parent != null && m.node.parent.mention != null){
//				return true;
//			}
//		}
//		return false;
//	}
//    
//    public static Boolean genetiveTest(Document d, Mention x, Mention y){
//        if (x.mentionCase == Case.GENITIVE && y.mentionCase == Case.GENITIVE) {
//            if (x.type == MentionType.PROPER && y.type == MentionType.PROPER) return true;
//            if (d.dict.belongsToSameGroup(x.headString, y.headString, d.dict.genetives)){
//                return true;
//            }
//            return false;
//        }
//        return true;
//      }

    
//        public static void firstPersonSingular(Document d) {
//        String filter = "Filter.firstPersonSingular(s) && Filter.fistPersonSingular(t)";
//        _resolveFirstFromAll(d, filter, "firstPersonSingular");
//        if (Constants.VERBOSE) System.err.println("Operation count = " + Filter.op);
//    }
//    public static void firstPersonPlural(Document d) {
//        String filter = "Filter.fistPersonPlural(s) && Filter.fistPersonPlural(t)";
//        _resolveFirstFromAll(d, filter, "firstPersonPlural");
//        if (Constants.VERBOSE) System.err.println("Operation count = " + Filter.op);
//    }
//    public static void secondPersonSingular(Document d) {
//        String filter = "Filter.secondPersonSingular(s) && Filter.secondPersonSingular(t)";
//        _resolveFirstFromAll(d, filter, "secondPersonSingular");
//        if (Constants.VERBOSE) System.err.println("Operation count = " + Filter.op);
//    }
//    public static void secondPersonPlural(Document d) {
//        String filter = "Filter.secondPersonPlural(s) && Filter.secondPersonPlural(t)";
//        _resolveFirstFromAll(d, filter, "secondPersonPlural");
//        if (Constants.VERBOSE) System.err.println("Operation count = " + Filter.op);
//    }
    
    
    
    public static void firstPersonSingular(Document d) {
        Mention m_es = null;
        for (Mention m : d.mentions) {
            if (m.type == MentionType.PRONOMINAL && m.node.lemma.equals("es"))  {
                if (m_es == null) m_es = m;
                else {
                    d.mergeClusters(m, m_es);
                    _resolve(d, m, m_es, "firstPerson");
                }
            }
        }
    }
    
    public static void firstPersonPlural(Document d) {
        Mention m_es = null;
        for (Mention m : d.mentions) {
            if (m.type == MentionType.PRONOMINAL && m.node.lemma.equals("mēs"))  {
                if (m_es == null) m_es = m;
                else {
                    d.mergeClusters(m, m_es);
                    _resolve(d, m, m_es, "firstPersonPlural");
                }
            }
        }
    }
    
    public static void secondPersonPlural(Document d) {
        Mention m_es = null;
        for (Mention m : d.mentions) {
            if (m.type == MentionType.PRONOMINAL && m.node.lemma.equals("jūs"))  {
                if (m_es == null) m_es = m;
                else {d.mergeClusters(m, m_es);
                    _resolve(d, m, m_es, "secondPersonPlural");
                }
            }
        }
    }
        public static void secondPersonSingular(Document d) {
        Mention m_es = null;
        for (Mention m : d.mentions) {
            if (m.type == MentionType.PRONOMINAL && m.node.lemma.equals("tu"))  {
                if (m_es == null) m_es = m;
                else {d.mergeClusters(m, m_es);
                    _resolve(d, m, m_es, "secondPersonSingluar");
                }
            }
        }
    }
}
