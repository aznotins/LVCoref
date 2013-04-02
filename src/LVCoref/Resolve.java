package LVCoref;

import LVCoref.Dictionaries.MentionType;
import LVCoref.Dictionaries.Case;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public class Resolve {
	
	public static void go(Document d){
        
      headMatch(d);
      //headMatchSintax(d);//not really better than headMatch

      appositive(d);

      //relaxedPronounMatch(d);
      //categoryPronounMatch(d);
      relaxedSintaxPronounMatch(d);
    }
    
    public static void headMatch(Document d){
        //Head match
        for (Mention m : d.mentions) {
            if (d.refGraph.needsReso(m)) {
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
                                        d.refGraph.setRef(m, prev);
                                        System.out.println("Head match :" + prev.headString +"("+prev.node.tag+")#"+prev.id+" <- " + m.headString+"("+m.node.tag+")#"+m.id);
                                        m.addRefComm(prev, "headMatch");
                                        break;
                                    }
                                 }
                             }
                         }
                         prev = prev.prev(d);
                    }
                }
            }
        }
    }
    
    public static void headMatchSintax(Document d){
        //Head match
        for (Mention m : d.mentions) {
            if (d.refGraph.needsReso(m)) {
                if (m.type == MentionType.NOMINAL || m.type == MentionType.PROPER) {

                    int sentenceWindow = 30;
                    List<Node> q = new LinkedList<Node>(Arrays.asList(m.node));
                    q = d.traverse(m.node, q);
                    Boolean found = true;
                    Node h = m.node; //the highest node from tree while traversing
                    int level = 0;
                    while (found) {
                        found = false;
                        Iterator<Node> i = q.iterator();
                        while (i.hasNext()) {
                            Node n = i.next();
                            if (m.node.sentNum - n.sentNum <= sentenceWindow || m.type == MentionType.PROPER) {
                                found = true;
                                if (n.id < m.node.id && n.mention != null) {
                                    Mention prev = n.mention;
                                    if (prev.type == MentionType.NOMINAL || prev.type == MentionType.PROPER) {
                                         if (prev.headString.equals(m.headString) ||
                                             d.dict.belongsToSameGroup(prev.headString, m.headString, d.dict.sinonyms)) {
                                             if (!genetiveBad(d, m) && !genetiveBad(d, prev)) {
                                                 d.refGraph.setRef(m, prev);
                                                 System.out.println("Head match (sintax l="+level+"):" + prev.headString +"("+prev.node.tag+")#"+prev.id+" <- " + m.headString+"("+m.node.tag+")#"+m.id);
                                                 m.addRefComm(prev, "headMatch");
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
    
    public static void appositive(Document d) {
        //Appositive construction
		for (Mention m : d.mentions) {
            if (d.refGraph.needsReso(m)) {
                if (m.node.parent != null && m.node.parent.isMention) {
                    Mention n = m.node.parent.mention;
                    if ( n.type == MentionType.PROPER && m.type == MentionType.PROPER ||
                         n.type == MentionType.NOMINAL && m.type == MentionType.PROPER ||
                         n.type == MentionType.PROPER && m.type == MentionType.NOMINAL ){

                        if ( n.gender == m.gender &&
                             n.mentionCase == m.mentionCase &&
                             n.number == m.number ) {

                            if (genetiveTest(d, n, m)) {
                                d.refGraph.setRef(m, n);
                                System.out.println("Appositive :" + n.headString +"("+n.node.tag+") <- " + m.headString +"("+m.node.tag+")");
                                m.addRefComm(n, "Appositive");
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static void relaxedPronounMatch(Document d) {
        //Relaxed pronound match
        for (Mention m : d.mentions) {
            if (d.refGraph.needsReso(m)) {
                if (m.type == MentionType.PRONOMINAL) {

                    //simple look at previous mentions (without using sintax tree)

                    int sentenceWindow = 3;

                    Mention prev = m.prev(d);
                    while ( prev != null && m.sentNum - prev.sentNum <= sentenceWindow) {
                         if (prev.type == MentionType.NOMINAL || prev.type == MentionType.PROPER) {
                             if (prev.number == m.number && prev.gender == m.gender) {
                                 d.refGraph.setRef(m, prev);
                                System.out.println("Relaxed pronoun match :" + prev.headString +"("+prev.node.tag+") <- " + m.headString+"("+m.node.tag+")");
                                m.addRefComm(prev, "RelPronMatch");
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
            if (d.refGraph.needsReso(m)) {
                if (m.type == MentionType.PRONOMINAL) {

                    //simple look at previous mentions (without using sintax tree)

                    int sentenceWindow = 3;

                    Mention prev = m.prev(d);
                    while ( prev != null && m.sentNum - prev.sentNum <= sentenceWindow) {
                        if (prev.type == MentionType.NOMINAL || prev.type == MentionType.PROPER) {
                            if (prev.number == m.number && prev.gender == m.gender) {
                                Set<String> cat = d.dict.categoryIntersection(prev.categories, m.categories);
                                if (cat.size() > 0) {
                                    m.categories = cat;
                                    prev.categories = cat;
                                    d.refGraph.setRef(m, prev);
                                    System.out.println("Category pronoun match :" + prev.headString +"("+prev.node.tag+") <- " + m.headString+"("+m.node.tag+")");
                                    m.addRefComm(prev, "CatPronMatch");
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
        //Relaxed pronound match
        for (Mention m : d.mentions) {
            if (d.refGraph.needsReso(m)) {
                if (m.type == MentionType.PRONOMINAL) {

                    int sentenceWindow = 3;
                    List<Node> q = new LinkedList<Node>(Arrays.asList(m.node));
                    q = d.traverse(m.node, q);
                    Boolean found = true;
                    Node h = m.node; //the highest node from tree while traversing
                    int level = 0;
                    while (found) {
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
                                            if (cat.size() > 0) {
                                                System.out.println("Relaxed sintax +category pronoun match :"+"level:"+level+" :"  + prev.headString +"("+prev.node.tag+" "+prev.categories+") <- " + m.headString+"("+m.node.tag+" "+m.categories+")");
                                                //m.categories = cat;
                                                //prev.categories = cat;//TODO pārbaudīt, vai nerodas kļūdas norādot uz vienu un to pašu obj
                                            
                                                d.refGraph.setRef(m, prev);
                                                
                                                m.addRefComm(prev, "RelSintaxPronMatch+Cat");
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
        
	
	public static Boolean genetiveBad(Document d, Mention m) {
		if (m.type == MentionType.NOMINAL && m.mentionCase == Case.GENITIVE) {
			if (m.node.parent != null && m.node.parent.tag.charAt(0) == 'n'){
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
