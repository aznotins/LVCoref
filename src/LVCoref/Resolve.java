package LVCoref;

import LVCoref.Dictionaries.MentionType;
import LVCoref.Dictionaries.Case;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;


public class Resolve {
       
	
	public static void go(Document d, Logger logger){
        
      appositive(d);
      predicativeNominative(d);      
      
      headMatch(d);   
      
      relaxedSintaxPronounMatch(d);
      
      //CHEAT for interviews
      firstPerson(d);
      secondPerson(d);
      firstPluralPerson(d);
      
      
            
      
      //roleAppositive(d);
      //headMatchSintax(d);//not really better than headMatch
      //relaxedPronounMatch(d);
      //categoryPronounMatch(d);
      
    }
    
    public static void headMatch(Document d){
        //Head match
        for (Mention m : d.mentions) {
            //if (!m.needsReso()) continue;
            if (m.type == MentionType.NOMINAL || m.type == MentionType.PROPER) {

                //simple look at previous mentions (without using sintax tree)

                int sentenceWindow = 30; //need to be larger for proper heads

                Mention prev = m.prev(d);
                while ( prev != null && (m.sentNum - prev.sentNum <= sentenceWindow || m.type == MentionType.PROPER)) {
                     if (prev.type == MentionType.NOMINAL || prev.type == MentionType.PROPER) {
                         if (prev.headString.equals(m.headString) ||
                             d.dict.belongsToSameGroup(prev.headString, m.headString, d.dict.sinonyms)) {
                             if (   m.gender == prev.gender &&  m.number == prev.number ) {
                                 if (!genetiveBad(d, m) && !genetiveBad(d, prev)) {
                                    //d.refGraph.setRef(m, prev);
                                    if(includeWords(prev, m)) {
                                        d.mergeClusters(m, prev);
                                        LVCoref.logger.fine(Utils.getMentionPairString(d, m, prev, "Head match(include all words)"));
                                        
                                        m.addRefComm(prev, "headMatch(includeAll)");
                                        m.setAsResolved();
                                        break;
                                    }
//                                        d.mergeClusters(m, prev);
//                                        LVCoref.logger.fine("Head match :" + prev.headString +"("+prev.node.tag+")#"+prev.id+" <- " + m.headString+"("+m.node.tag+")#"+m.id);
//                                        m.addRefComm(prev, "headMatch");
//                                        m.setAsResolved();
//                                        break;

                                }
                             }
                         }
                     }
                     prev = prev.prev(d);
                }
            }
        }
        
    }
    
    //m1 contains all m1 words
    private static boolean includeWords (Mention m1, Mention m2) {
        if (m1.words.containsAll(m2.words)) return true;
        else return false;
    }
    
    
    public static void exactStringMatch(Document d){
        for (Mention m : d.mentions) {
            if (m.needsReso()) {
                if (m.type == MentionType.NOMINAL || m.type == MentionType.PROPER) {

                    //simple look at previous mentions (without using sintax tree)

                    int sentenceWindow = 30; //need to be larger for proper heads

                    Mention prev = m.prev(d);
                    while ( prev != null && (m.sentNum - prev.sentNum <= sentenceWindow || m.type == MentionType.PROPER)) {
                         if (prev.type == MentionType.NOMINAL || prev.type == MentionType.PROPER) {
                             if (prev.normString.equals(m.normString)) {
                                //d.refGraph.setRef(m, prev);
                                d.mergeClusters(m, prev);
                                LVCoref.logger.fine(Utils.getMentionPairString(d, m, prev, "Exact String match"));
                                m.addRefComm(prev, "exactMatch");
                                m.setAsResolved();
                                break;
                             }
                         }
                         prev = prev.prev(d);
                    }
                }
            }
        }
    }
    
    
    public static void firstPerson(Document d) {
        Mention m_es = null;
        for (Mention m : d.mentions) {
            if (m.type == MentionType.PRONOMINAL && m.node.lemma.equals("es"))  {
                if (m_es == null) m_es = m;
                else {
                    d.mergeClusters(m, m_es);
                    LVCoref.logger.fine(Utils.getMentionPairString(d, m, m_es, "First person pronoun match"));
                    m.addRefComm(m, "firstPerson");
                }
            }
        }
    }
    
    public static void firstPluralPerson(Document d) {
        Mention m_es = null;
        for (Mention m : d.mentions) {
            if (m.type == MentionType.PRONOMINAL && m.node.lemma.equals("mēs"))  {
                if (m_es == null) m_es = m;
                else {
                    d.mergeClusters(m, m_es);
                    LVCoref.logger.fine(Utils.getMentionPairString(d, m, m_es, "Second person plural pronoun match"));
                    m.addRefComm(m, "firstPersonPlural");
                }
            }
        }
    }
    
    public static void secondPerson(Document d) {
        Mention m_es = null;
        for (Mention m : d.mentions) {
            if (m.type == MentionType.PRONOMINAL && m.node.lemma.equals("jūs"))  {
                if (m_es == null) m_es = m;
                else {
                    d.mergeClusters(m, m_es);
                    LVCoref.logger.fine(Utils.getMentionPairString(d, m, m_es, "Second person pronoun match"));
                    m.addRefComm(m, "secondPerson");
                }
            }
        }
    }
    
//    public static void headMatchSintax(Document d){
//        //Head match
//        for (Mention m : d.mentions) {
//            if (d.refGraph.needsReso(m)) {
//                if (m.type == MentionType.NOMINAL || m.type == MentionType.PROPER) {
//
//                    int sentenceWindow = 30;
//                    List<Node> q = new LinkedList<Node>(Arrays.asList(m.node));
//                    q = d.traverse(m.node, q);
//                    Boolean found = true;
//                    Node h = m.node; //the highest node from tree while traversing
//                    int level = 0;
//                    while (found) {
//                        found = false;
//                        Iterator<Node> i = q.iterator();
//                        while (i.hasNext()) {
//                            Node n = i.next();
//                            if (m.node.sentNum - n.sentNum <= sentenceWindow || m.type == MentionType.PROPER) {
//                                found = true;
//                                if (n.id < m.node.id && n.mention != null) {
//                                    Mention prev = n.mention;
//                                    if (prev.type == MentionType.NOMINAL || prev.type == MentionType.PROPER) {
//                                         if (prev.headString.equals(m.headString) ||
//                                             d.dict.belongsToSameGroup(prev.headString, m.headString, d.dict.sinonyms)) {
//                                             if (!genetiveBad(d, m) && !genetiveBad(d, prev)) {
//                                                 //d.refGraph.setRef(m, prev);
//                                                 d.mergeClusters(m, prev);
//                                                 System.out.println("Head match (sintax l="+level+"):" + prev.headString +"("+prev.node.tag+")#"+prev.id+" <- " + m.headString+"("+m.node.tag+")#"+m.id);
//                                                 m.addRefComm(prev, "headMatch");
//                                                 found = false;
//                                                 break;
//                                             }
//                                         }
//                                     }
//                                }
//                            } else {
//                                i.remove(); //outside sentece window
//                            }
//                        }
//                        if (found) {
//                            q = d.traverse( (h!= null) ? h.parent : null, q);
//                            h = (h != null) ? h.parent : null;
//                            level ++;
//                        }
//                    }
//                }
//            }
//        }
//    }
    
    public static void appositive(Document d) {
        //Appositive construction
		for (Mention m : d.mentions) {
            //if (d.refGraph.needsReso(m)) {
            
                Node parent = m.node.parent;
//                while (parent != null && parent.isConjuction()) parent = parent.parent;
            
                if (parent != null && parent.mention != null) {
                    Mention n = parent.mention;
                    if ( n.type == MentionType.PROPER && m.type == MentionType.PROPER ||
                         n.type == MentionType.NOMINAL && m.type == MentionType.PROPER ||
                         n.type == MentionType.PROPER && m.type == MentionType.NOMINAL ){

                        if ( n.gender == m.gender &&
                             n.mentionCase == m.mentionCase &&
                             n.number == m.number ) {

                            if (genetiveTest(d, n, m)) {
                                //d.refGraph.setRef(m, n);
                                d.mergeClusters(m, n);
                                LVCoref.logger.fine(Utils.getMentionPairString(d, m, n, "Appositive"));
                                m.addRefComm(n, "Appositive");
                            }
                        }
                    }
                }
            //}
        }
    }
    
        public static void roleAppositive(Document d) {
        //Appositive construction
		for (Mention m : d.mentions) {
            //if (m.needsReso()) {
                for (Node node : m.node.children) {
                    if (node.mention != null) {
                        Mention n = node.mention;
                        if ( n.type == MentionType.PROPER && m.type == MentionType.PROPER ||
                             n.type == MentionType.NOMINAL && m.type == MentionType.PROPER ||
                             n.type == MentionType.PROPER && m.type == MentionType.NOMINAL ){

                            if ( n.gender == m.gender &&
                                 n.mentionCase == m.mentionCase &&
                                 n.number == m.number ) {

                                if (genetiveTest(d, n, m)) {
                                    //d.refGraph.setRef(m, n);
                                    d.mergeClusters(n, m);
                                    LVCoref.logger.fine(Utils.getMentionPairString(d, m, n, "RoleAppositive"));
                                    m.addRefComm(n, "RoleAppositive");
                                    
                                }
                            }
                        }
                    }
                }
            //}
        }
    }
    
    public static void relaxedPronounMatch(Document d) {
        //Relaxed pronound match
        for (Mention m : d.mentions) {
            if (m.needsReso()) {
                if (m.type == MentionType.PRONOMINAL) {

                    //simple look at previous mentions (without using sintax tree)

                    int sentenceWindow = 3;

                    Mention prev = m.prev(d);
                    while ( prev != null && m.sentNum - prev.sentNum <= sentenceWindow) {
                         if (prev.type == MentionType.NOMINAL || prev.type == MentionType.PROPER) {
                             if (prev.number == m.number && prev.gender == m.gender) {
                                //d.refGraph.setRef(m, prev);
                                d.mergeClusters(prev, m);
                                LVCoref.logger.fine(Utils.getMentionPairString(d, m, prev, "Relaxed pronoun match"));
                                m.addRefComm(prev, "RelPronMatch");
                                m.setAsResolved();
                                break;
                             }
                         }
                         prev = prev.prev(d);
                    }
                }
            }
        }
    }
    
    
    public static void categoryPronounMatch(Document d) {
        //Relaxed pronound match
        for (Mention m : d.mentions) {
            if (m.needsReso()) {
                if (m.type == MentionType.PRONOMINAL) {

                    //simple look at previous mentions (without using sintax tree)

                    int sentenceWindow = 3;

                    Mention prev = m.prev(d);
                    while ( prev != null && m.sentNum - prev.sentNum <= sentenceWindow) {
                        if (prev.type == MentionType.NOMINAL || prev.type == MentionType.PROPER) {
                            if (prev.number == m.number && prev.gender == m.gender) {
                                Set<String> cat = d.dict.categoryIntersection(prev.categories, m.categories);
                                if (cat.size() > 1) {
                                    m.categories = cat;
                                    prev.categories = cat;
                                    //d.refGraph.setRef(m, prev);
                                    d.mergeClusters(m, prev);
                                    LVCoref.logger.fine(Utils.getMentionPairString(d, m, prev, "Category pronoun match"));
                                    m.addRefComm(prev, "CatPronMatch");
                                    m.setAsResolved();
                                    break;
                                }
                            }
                        }
                        prev = prev.prev(d);
                    }
                }
            }
        }
    }
    
    
        public static void relaxedSintaxPronounMatch(Document d) {
        //Relaxed pronoun match
        for (Mention m : d.mentions) {
            if (m.needsReso()) {
                if (m.type == MentionType.PRONOMINAL) {

                    int sentenceWindow = 2;
                    int maxLevel = 10;
                    List<Node> q = new LinkedList<Node>(Arrays.asList(m.node));
                    q = d.traverse(m.node, q);
                    Boolean found = true;
                    Node h = m.node; //the highest node from tree while traversing
                    int level = 0;
                    while (found && level <= maxLevel) {
                        found = false;
                        Iterator<Node> i = q.iterator();
                        while (i.hasNext()) {
                            Node n = i.next();
                            if (m.node.sentNum - n.sentNum <= sentenceWindow) {
                                found = true;
                                if (n.id < m.node.id && n.mention != null) {
                                    Mention prev = n.mention;
                                    if (prev.type == MentionType.NOMINAL || prev.type == MentionType.PROPER) {
                                        if (prev.number == m.number && prev.gender == m.gender) {
                                            Set<String> cat = d.dict.categoryIntersection(m.categories, prev.categories);
                                            if (cat.size() >= 1) {
                                                LVCoref.logger.fine(Utils.getMentionPairString(d, m, prev, "Relaxed sintax category pronoun match"));
                                                //m.categories = cat;
                                                //prev.categories = cat;//TODO pārbaudīt, vai nerodas kļūdas norādot uz vienu un to pašu obj
                                            
                                                //d.refGraph.setRef(m, prev);
                                                d.mergeClusters(m, prev);
                                                m.addRefComm(prev, "RelSintaxPronMatch+Cat");
                                                m.setAsResolved();
                                                found = false;
                                            break;
                                            }
                                        }
                                    }
                                }
                            } else {
                                i.remove(); //outside sentece window
                            }
                        }
                        if (found) {
                            q = d.traverse( (h!= null) ? h.parent : null, q);
                            h = (h != null) ? h.parent : null;
                            level ++;
                        }
                    }
                }
            }
        }
    }
        
        
        
    public static void predicativeNominative(Document d) {
        //Predicative nominative construction
		for (Mention m : d.mentions) {
            //if (d.refGraph.needsReso(m)) {
            Node parent = m.node.parent;
            if (parent != null && m.node.parent.isConjuction()) parent = m.node.parent.parent;
                if (parent != null && parent.lemma.equals("būt")) {
                    for (Node node : parent.children) {
                        //System.out.println(m.nerString + "("+ m.mentionCase + ")"+ " ????? " + node.word + "("+ node.tag + ")");
                        if (    m.node.id > parent.id &&  parent.id > node.id &&
                                node != m.node && node.mention != null) {
                            Mention n = node.mention;
                            //System.out.println(m.nerString + "("+ m.mentionCase + ")"+ " : " + n.nerString + "("+ n.mentionCase + ")");
                            if (m.mentionCase == Case.NOMINATIVE && n.mentionCase == Case.NOMINATIVE &&
                                m.number == n.number) {
                                d.mergeClusters(m, n);
                                LVCoref.logger.fine(Utils.getMentionPairString(d, m, n, "PredicativeNominative"));
                                m.addRefComm(n, "PredicativeNominative");                                
                                break;
                            }
                        }
                    }
                }
            //}
        }
    }
        
	
	public static Boolean genetiveBad(Document d, Mention m) {
		if (m.type == MentionType.NOMINAL && m.mentionCase == Case.GENITIVE) {
			if (m.node.parent != null && m.node.parent.mention != null){
				return true;
			}
		}
		return false;
	}
    
    
    public static Boolean genetiveTest(Document d, Mention x, Mention y){
        if (x.mentionCase == Case.GENITIVE && y.mentionCase == Case.GENITIVE) {
            if (x.type == MentionType.PROPER && y.type == MentionType.PROPER) return true;
            if (d.dict.belongsToSameGroup(x.headString, y.headString, d.dict.genetives)){
                return true;
            }
            return false;
        }
        return true;
      }

}
