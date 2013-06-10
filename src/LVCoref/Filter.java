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
        _updateOperationCount();
        if (s.sameGender(t)) return true;
        return false;
    }
    
    public static Boolean sameNumber(Mention s, Mention t) {
        _updateOperationCount();
        if (s.sameNumber(t)) return true;
        return false;
    }
    
    public static Boolean sameCase(Mention s, Mention t) {
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
        String a = s.getAcronym(d);
        if ( d.acronyms.containsKey(a)) {
            return true;
        }
        return false;
    }
    
    public static boolean sameCategory(Mention s, Mention t) {
        _updateOperationCount();
        Set<String> cat = s.document.dict.categoryIntersection(s.categories, t.categories);
        if (cat.size() >= 1) return true;
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
        _updateOperationCount();
        return s.categories.contains("person");
    }
    public static boolean isOrganization(Mention s) {
        _updateOperationCount();
        return s.categories.contains("organization");
    }
    public static boolean isLocation(Mention s) {
        _updateOperationCount();
        return s.categories.contains("location");
    }
    
    
    public static boolean firstPersonSingular(Mention s) {
        _updateOperationCount();
        return s.headString.equals("es");
    }
    public static boolean firstPersonPlural(Mention s) {
        _updateOperationCount();
        return s.headString.equals("mēs");
    }
    public static boolean secondPersonSingular(Mention s) {
        _updateOperationCount();
        return s.headString.equals("tu");
    }
    public static boolean secondPersonPlural(Mention s) {
        _updateOperationCount();
        return s.headString.equals("jūs");
    }
    
    public static boolean containsAllClusterModifiers(Mention s, Mention t) {
        _updateOperationCount();
        return s.document.getCluster(s.corefClusterID).includeModifiers(t.document.getCluster(t.corefClusterID));
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
        if (h == s.node || h == t.node) return true;
        return false;
    }
    public static boolean inPlainApposition(Mention s, Mention t) {
        _updateOperationCount();
        if (s.end + 1 == t.start || t.start +1 == s.start) {
            return true;
        }
        return false;
    }
    
    
    private static void _updateOperationCount() {
        op++;
    }
}

