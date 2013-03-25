package LVCoref;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;


public class Resolve {
	
	public static void go(Document d){
		initializeGroups();
		
		
		for (Integer n_i = 0; n_i < d.tree.size(); n_i++) {
			Node n = d.tree.get(n_i);
		//for (Node n : d.tree) {
			
			if (n.parent >= 0 && n.isMention) {
				Node parent = d.tree.get(n.parent);
				if (parent.isMention) {
					if (n.type == "PROPER" && parent.type == "PROPER" ||
						n.type == "COMMON" && parent.type =="PROPER" ||
						n.type == "PROPER" && parent.type == "COMMON" ) {
						
						
						if (n.tag.charAt(2) == parent.tag.charAt(2) &&
							n.tag.charAt(3) == parent.tag.charAt(3) &&
							n.tag.charAt(4) == parent.tag.charAt(4)) {
							
							if (genetiveTest(n, parent)) {
								
								
								n.var.add(parent.id);
								parent.successors.add(n.id);
								System.out.println("GenTest appositive cand :" + parent.word +"("+parent.tag+") <- " + n.word+"("+n.tag+")");
							}
							//System.out.println("Mach appositive cand :" + parent.word +"("+parent.tag+") <- " + n.word+"("+n.tag+")");
						}
						//System.out.println(" appositive cand :" + parent.word +"("+parent.tag+") <- " + n.word+"("+n.tag+")");
												
					}
				}
			}
			 
			
			
			if (n.isMention && 
				(n.type == "COMMON" || n.type == "PROPER") && 
				n_i > 0) {
				for (Integer n_j = n_i-1; (n_i-n_j < 10000) && (n_j >= 0); n_j--) {
					Node ant = d.tree.get(n_j);
					if (ant.isMention && 
						(ant.type == "COMMON" || ant.type == "PROPER")) {
						//System.out.println("Match ? :" + ant.word +"("+ant.lemma+") <- " + n.word+"("+n.lemma+")");
						
						if (n.lemma.equals(ant.lemma) || Data.areSinonyms(n.lemma, ant.lemma)) {
							if (!genetiveBad(n,d) && !genetiveBad(ant,d)) {
								n.var.add(ant.id);
								ant.successors.add(n.id);
								System.out.println("Strict match :" + ant.word +"("+ant.tag+") <- " + n.word+"("+n.tag+")");
							}
						}
							
					}
				}
			}
			
			
			
			
			
			Data.load_data();
			if (n.isMention && 
				(n.type == "PRONOUN") && 
				n.parent > 0) {
				
				Set<String> categories = Data.getPronounCategories(n.lemma);
				
				if (categories.size() > 0) {
					Set<Integer> visited = new HashSet<Integer>();
					visited.add(n.id);
					Queue<Node> q = new LinkedList<Node>(), q2 = new LinkedList<Node>();
					Node parent = d.tree.get(n.parent);
					q.add(parent);								
					for (Integer ch : n.children) {
						q.add(d.tree.get(ch));
					}
					int level = 0;
					
					
				
					Node ant;
					while (level < 20) {
						ant = q.poll();
						
						if (ant != null) {
							visited.add(ant.id);
							if (ant.isMention && 
							    (ant.type == "COMMON" || ant.type == "PROPER")) {
								
								if (ant.id < n.id && n.id - ant.id < 5000) {
								
									if (n.tag.charAt(3) == ant.tag.charAt(2) &&
										n.tag.charAt(4) == ant.tag.charAt(3)) {
										
										Boolean found = false;
										String cat2="";
										for(String cat: categories) {
											if (Data.proper.get(cat).contains(ant.lemma)) {
												found = true;
												cat2 = cat;
												break;
											}
											if (Data.common.get(cat).contains(ant.lemma)) {
												found = true;
												cat2 = cat;
												break;										
											}
										}
										if(found) {
											n.category = cat2;
											ant.category = cat2;
											n.var.add(ant.id);
											ant.successors.add(n.id);
											System.out.println("Pronoun match (level:"+level+" " +cat2+") :" + ant.word +"("+ant.tag+") <- " + n.word+"("+n.tag+")");
											break;
										}
										
										
									}
								}
							} 
							
							if (ant.parent > 0 && !visited.contains(ant.parent)) {
								q2.add(d.tree.get(ant.parent));
							}
							for (Integer ch : ant.children) {
								if(!visited.contains(ch)) q2.add(d.tree.get(ch));
							}
						
						} else {
							q = q2;
							q2 = new LinkedList<Node>();
							level++;
						}
						
					}
				}
			}
		}
	}
	
	
	
	
	public static Boolean genetiveBad(Node n, Document d) {
		if (n.type == "COMMON" && n.tag.charAt(4) == 'g') {
			if (n.parent > 0 && d.tree.get(n.parent).tag.charAt(0) == 'n'){
				return true;
			}
		}
		return false;
	}
	
	
	public static void isAppositive(Node x, Node y) {
		
		
	}
	
	public static Boolean genetiveTest(Node x, Node y){		
		if (x.tag.charAt(4) == 'g' &&  y.tag.charAt(4) == 'g') {
			Set<Integer> s1 = group.get(x.lemma);
			Set<Integer> s2 = group.get(y.lemma);
			if (s1 != null && s2 != null) {
				Set<Integer> intersection = new HashSet<Integer>(s1);
				intersection.retainAll(s2);
				if (intersection.size() > 0) return true;
			}
			
			if (x.type == "PROPER" && y.type =="PROPER") return true;
			return false;
		}
		return true;
	}
	
	public static Map<String, Set<Integer>> group;
	
	public static void initializeGroups(){
		group = new HashMap<String, Set<Integer> >();
		group.put("Latvija", new HashSet<Integer>(Arrays.asList(new Integer[] {1,2})));
		group.put("republika", new HashSet<Integer>(Arrays.asList(new Integer[] {1})));
		group.put("universitāte", new HashSet<Integer>(Arrays.asList(new Integer[] {2})));
	}
	
	
	
	
}
