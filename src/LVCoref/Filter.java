/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package LVCoref;

import LVCoref.Document;
import LVCoref.Mention;
import java.util.Set;

/**
 *
 * @author Artūrs
 */
public class Filter {
    public static Document d;
    public static int op = 0;
    
    public static Boolean sameGender(Mention s, Mention t) {
       //if (true) return true;
        _updateOperationCount();
        if (s.sameGender(t)) return true;
        return false;
    }
    
    public static Boolean sameNumber(Mention s, Mention t) {
        //if (true) return true;
        _updateOperationCount();
        if (s.sameNumber(t)) return true;
        return false;
    }
    
    public static Boolean sameCase(Mention s, Mention t) {
        //if (true) return true;
        _updateOperationCount();
        if (s.sameCase(t)) return true;
        return false;
    }
    
    public static Boolean sameHead(Mention s, Mention t) {
        _updateOperationCount();
        if (s.headString.equals(t.headString)) return true;
        return false;
    }
    
    public static int headDistance(Mention s, Mention t) {
        _updateOperationCount();
        return Math.abs(s.node.id - t.node.id);
    }
    public static int distance(Mention s, Mention t) {
        _updateOperationCount();
        return Math.min(Math.abs(s.start - t.end), Math.abs(s.end - t.start));
    }
    
    public static int sentenceDistance(Mention s, Mention t) {
        _updateOperationCount();
        //if (s == null || t == null) return Integer.MAX_VALUE;
        return Math.abs(s.node.sentNum - t.node.sentNum);
    }
    
    public static boolean nominal(Mention s) {
        _updateOperationCount();
        return s.type == Dictionaries.MentionType.NOMINAL;
    }
    
    public static boolean proper(Mention s) {
        //if (true) return true;
        _updateOperationCount();
        return s.type == Dictionaries.MentionType.PROPER;
    }
        
    public static boolean pronominal(Mention s) {
        _updateOperationCount();
        return s.type == Dictionaries.MentionType.PRONOMINAL;
    }
    public static boolean pronominalIndefinite(Mention s) {
        _updateOperationCount();
        return s.pronounType == Dictionaries.PronounType.INDEFINITE;
    }
    public static boolean pronominalReflexive(Mention s) {
        _updateOperationCount();
        return s.pronounType == Dictionaries.PronounType.REFLEXIVE;
    }
    public static boolean pronominalPossesive(Mention s) {
        _updateOperationCount();
        return s.pronounType == Dictionaries.PronounType.POSSESIVE;
    }
    public static boolean pronominalRelative(Mention s) {
        _updateOperationCount();
        return s.node.isRelativePronoun();
    }
    public static boolean pronominalModificator(Mention s) {
        _updateOperationCount();
        return s.node.parent != null && s.node.parent.isNoun() && s.mentionCase == Dictionaries.Case.GENITIVE;
    }
    
    public static boolean genitive(Mention s, Mention t) {
        _updateOperationCount();
        return s.isGenitive(t);
    }
    public static boolean genitive(Mention s) {
        _updateOperationCount();
        return s.mentionCase == Dictionaries.Case.GENITIVE;
    }
    public static boolean nominative(Mention s) {
        _updateOperationCount();
        return s.mentionCase == Dictionaries.Case.NOMINATIVE;
    }
    public static boolean locative(Mention s) {
        _updateOperationCount();
        return s.mentionCase == Dictionaries.Case.LOCATIVE;
    }
    public static boolean accusative(Mention s) {
        _updateOperationCount();
        return s.mentionCase == Dictionaries.Case.ACCUSATIVE;
    }
    public static boolean dative(Mention s) {
        _updateOperationCount();
        return s.mentionCase == Dictionaries.Case.DATIVE;
    }
    
    public static boolean exactMatch(Mention s, Mention t) {
        _updateOperationCount();
        return s.nerString.equals(t.nerString);
    }
    
    public static boolean inAcronymList(Mention s) {
        _updateOperationCount();
        String a = s.getAcronym();
        if ( s.document.acronyms.containsKey(a)) {
            return true;
        }
        return false;
    }
    
    public static boolean sameCategory(Mention s, Mention t) {
        //if (true) return true;
        _updateOperationCount();
        Set<String> cat = s.document.dict.categoryIntersection(s.categories, t.categories);
        if (cat.size() >= 1) return true;
        return false;
    }
    public static boolean sameCategoryConstraint(Mention s, Mention t) {
        //if (true) return true;
        _updateOperationCount();
        if (s.categoryMatch(t)) return true;
        return false;
    }
    
    public static boolean isResolved(Mention s) {
        _updateOperationCount();
        return s.needsReso();
    }
    
    public static boolean isQuoteMention(Mention s) {
        _updateOperationCount();
        return s.bucket.equals("quote");
    }
    
    public static boolean isPerson(Mention s) {
        //if (true) return true;
        _updateOperationCount();
        return s.categories.contains("person");
    }
    public static boolean isOrganization(Mention s) {
        //if (true) return true;
        _updateOperationCount();
        return s.categories.contains("organization");
    }
    public static boolean isLocation(Mention s) {
        //if (true) return true;
        _updateOperationCount();
        return s.categories.contains("location");
    }
    public static boolean isTime(Mention s) {
        return s.categories.contains("time");
    }
    
    
    public static boolean firstPersonSingular(Mention s) {
        _updateOperationCount();
        return s.node.lemma.equals("es");
    }
    public static boolean firstPersonPlural(Mention s) {
        _updateOperationCount();
        return s.node.lemma.equals("mēs");
    }
    public static boolean secondPersonSingular(Mention s) {
        _updateOperationCount();
        return s.node.lemma.equals("tu");
    }
    public static boolean secondPersonPlural(Mention s) {
        _updateOperationCount();
        return s.node.lemma.equals("jūs");
    }
    
    public static boolean containsAllClusterModifiers(Mention s, Mention t) {
        //if (true) return true;
        _updateOperationCount();
        return s.document.getCluster(s.corefClusterID).includeModifiers(t.document.getCluster(t.corefClusterID));
    }
    
    public static boolean modifierConstraint(Mention s, Mention t) {
        _updateOperationCount();
        //if (true) return true;
        
        Set<String> ss = s.document.getCluster(s.corefClusterID).properModifiers;
        Set<String> tt = t.document.getCluster(t.corefClusterID).properModifiers;
        Set<String> ssw = s.document.getCluster(s.corefClusterID).words;
        Set<String> ttw = t.document.getCluster(t.corefClusterID).words;
        
        boolean ok = true;
        for (String mod: ss) {
            if (!tt.contains(mod) && !tt.contains(mod)) {
                ok = false;
                break;
            }
        }
        if (!ok) {
            ok = true;
            for (String mod: tt) {
                if (!ss.contains(mod) && !ss.contains(mod)) {
                    ok = false;
                    break;
                }
            }
        }
        return ok;
    }
    
    public static boolean inPredicativeNominative(Mention s, Mention t) {   
        _updateOperationCount();
        Node h = s.document.getCommonAncestor(s.node, t.node);
        if (s.sentNum != t.sentNum) return false;
        if (h != null) {
            if (h.lemma.equals("būt")) return true;
        }
        return false;
    }    
    public static boolean inApposition(Mention s, Mention t) {
        _updateOperationCount();
        Node h = s.document.getCommonAncestor(s.node, t.node);
        int sint = s.node.minDistance(t.node);
        
        if (h == s.node || h == t.node)  {
            if (sint > 1 && s.type == Dictionaries.MentionType.PROPER && t.type == Dictionaries.MentionType.PROPER) return false;
            return true;
        }
        return false;
    }
    public static boolean inPlainApposition(Mention s, Mention t) {
        _updateOperationCount();
        if (s.end + 1 == t.start || t.start +1 == s.start) {
            return true;
        }
        return false;
    }
    
    public static boolean dominated(Mention s, Mention t) {
        //if (true) return false;
        if (s.start >= t.start && s.start <= t.end || t.start >= s.start && t.start <=s.end) return true;
        return false;
    }
    public static boolean iwithini(Mention s, Mention t) {
        //if (true) return false;
        if (s.node.parent == t.node.parent && !s.node.parent.lemma.equals("būt") && !Filter.pronominalReflexive(s) && !Filter.pronominalPossesive(s)) return true;
        int i = s.node.minDistance(t.node);
        Node q = s.node.getCommonAncestor(s.node, t.node);
        if (q != s.node && q!=t.node && i < 4) return true;
        return false;
    }
    
    //neņemt vērā
    public static boolean personPronounsSkewed(Mention s) {
        if (s.node.lemma.equals("es") || s.node.lemma.equals("mēs") || s.node.lemma.equals("jūs") || s.node.lemma.equals("tu")) return true;
       //if (s.node.lemma.equals("tas")) return true;
        return false;
    }
    
    public static boolean hasCategory(Mention s) {
        if (s.categories.size() == 0) return true;
        if (s.categories.contains("other")) return true;
        return false;
    }
    
    public static boolean sameSentece(Mention s, Mention t) {
        _updateOperationCount();
        return s.sentNum == t.sentNum;
    }
    
    private static void _updateOperationCount() {
        op++;
    }
}

