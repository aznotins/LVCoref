package LVCoref;

import LVCoref.Dictionaries.Animacy;
import LVCoref.Dictionaries.Gender;
import LVCoref.Dictionaries.MentionType;
import LVCoref.Dictionaries.Number;
import LVCoref.Dictionaries.Case;
import LVCoref.Dictionaries.PronounType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;


public class Resolve {
	
	public static void go(Document d){
        
        
        //Appositive construction
		for (Mention m : d.mentions) {
            if (m.node.parent != null && m.node.parent.isMention) {
                Mention n = m.node.parent.mention;
                if ( n.type == MentionType.PROPER && m.type == MentionType.PROPER ||
                     n.type == MentionType.NOMINAL && m.type == MentionType.PROPER ||
                     n.type == MentionType.PROPER && m.type == MentionType.NOMINAL ){
                    
                    if ( n.gender == m.gender &&
                         n.mentionCase == m.mentionCase &&
                         n.number == m.number ) {
                        
                        if (genetiveTest(d, n, m)) {
                            System.out.println("Appositive cand :" + n.headString +"("+n.node.tag+") <- " + m.headString +"("+m.node.tag+")");
                        }
                    }
                }
                    
            }
        }
        
        //Head match
        for (Mention m : d.mentions) {
            if (m.type == MentionType.NOMINAL || m.type == MentionType.PROPER) {
                
                //simple look at previous mentions (without using sintax tree)
                
                int sentenceWindow = 30;
                
                Mention prev = m.prev(d);
                while ( prev != null && prev.sentNum - m.sentNum <= sentenceWindow) {
                     if (prev.type == MentionType.NOMINAL || prev.type == MentionType.PROPER) {
                         if (prev.headString.equals(m.headString) ||
                             d.dict.belongsToSameGroup(prev.headString, m.headString, d.dict.sinonyms)) {
                             if (!genetiveBad(d, m) && !genetiveBad(d, prev)) {
                                 System.out.println("Head match :" + prev.headString +"("+prev.node.tag+") <- " + prev.headString+"("+prev.node.tag+")");
                                 break;
                             }
                         }
                     }
                     prev = prev.prev(d);
                }
			}
        }
        
    }
        
  
//			
//			Data.load_data();
//			if (n.isMention && 
//				(n.type == "PRONOUN") && 
//				n.parent > 0) {
//				
//				Set<String> categories = Data.getPronounCategories(n.lemma);
//				
//				if (categories.size() > 0) {
//					Set<Integer> visited = new HashSet<Integer>();
//					visited.add(n.id);
//					Queue<Node> q = new LinkedList<Node>(), q2 = new LinkedList<Node>();
//					Node parent = d.tree.get(n.parent);
//					q.add(parent);								
//					for (Integer ch : n.children) {
//						q.add(d.tree.get(ch));
//					}
//					int level = 0;
//					
//					
//				
//					Node ant;
//					while (level < 20) {
//						ant = q.poll();
//						
//						if (ant != null) {
//							visited.add(ant.id);
//							if (ant.isMention && 
//							    (ant.type == "COMMON" || ant.type == "PROPER")) {
//								
//								if (ant.id < n.id && n.id - ant.id < 5000) {
//								
//									if (n.tag.charAt(3) == ant.tag.charAt(2) &&
//										n.tag.charAt(4) == ant.tag.charAt(3)) {
//										
//										Boolean found = false;
//										String cat2="";
//										for(String cat: categories) {
//											if (Data.proper.get(cat).contains(ant.lemma)) {
//												found = true;
//												cat2 = cat;
//												break;
//											}
//											if (Data.common.get(cat).contains(ant.lemma)) {
//												found = true;
//												cat2 = cat;
//												break;										
//											}
//										}
//										if(found) {
//											n.category = cat2;
//											ant.category = cat2;
//											n.var.add(ant.id);
//											ant.successors.add(n.id);
//											System.out.println("Pronoun match (level:"+level+" " +cat2+") :" + ant.word +"("+ant.tag+") <- " + n.word+"("+n.tag+")");
//											break;
//										}
//										
//										
//									}
//								}
//							} 
//							
//							if (ant.parent > 0 && !visited.contains(ant.parent)) {
//								q2.add(d.tree.get(ant.parent));
//							}
//							for (Integer ch : ant.children) {
//								if(!visited.contains(ch)) q2.add(d.tree.get(ch));
//							}
//						
//						} else {
//							q = q2;
//							q2 = new LinkedList<Node>();
//							level++;
//						}
//						
//					}
//				}
//			}
//		}
//	}
	
	
	
	
	public static Boolean genetiveBad(Document d, Mention m) {
		if (m.type == MentionType.NOMINAL && m.mentionCase == Case.GENITIVE) {
			if (m.node.parent != null && m.node.parent.tag.charAt(0) == 'n'){
				return true;
			}
		}
		return false;
	}
	
	
	public static void isAppositive(Node x, Node y) {
		
		
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
