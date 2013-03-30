package LVCoref;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Document class contains document parse tree structure, 
 * mentions, coreferences and other information
 * @author arturs
 */
public class Document {
    
    /** 
     * Document parse tree
     */
	public ArrayList<Node> tree;

	public ArrayList<Mention> mentions;
    
    public RefGraph refGraph;
    
    public Map<Integer, CorefCluster> corefClusters;
    public Map<Integer, CorefCluster> goldCorefClusters;
    
    public Dictionaries dict;
    
	
    Document(){
		tree = new ArrayList<Node>();
		mentions = new ArrayList<Mention>();
        dict = new Dictionaries();
        refGraph = new RefGraph();
        corefClusters = new HashMap<Integer, CorefCluster>();
	}
    
    public void printMentions(){
        System.out.println("------Mentions---------");
        for (Mention m : mentions) {
			System.out.println("#" +m.id + "\t" + m.node.word + "\t" + m.type+ "\t" + m.category + " ^"+m.node.parent+" "/*n.children.toString()*/);
            System.out.println(m.toString());
        }
        System.out.println("------/Mentions---------");
    }
    
    public void printNodes(Collection<Node> c) {
        for (Node n: c) {
            System.out.println(n.toString());
        }
//      for(Node n : d.tree) {
//			System.out.print("#" +n.id + "\t" + n.word + "\t" + n.type + "\t" + n.category + " ^"+n.parent+" "/*n.children.toString()*/); 
//			System.out.print("[" );for(int g : n.children) {System.out.print(" " + d.tree.get(g).word + "#" +g+",");} System.out.println("]" );
//		}
    }

    
    public String getSubString(int startID, int endID) {
        String s = "";
        for(int i = startID; i <= endID;  i++) {
            s+= tree.get(i).word + " ";
        }
        return s.trim();
            
    }
    
    public void readCONLL() throws Exception {
        String s;
		int node_id = 0;
        int sentence_id = 0;
		int sentence_start_id = 0;
		BufferedReader in = null;
		//in = new BufferedReader(new FileReader("data/Sofija.conll"));
		in = new BufferedReader(new FileReader("data/SofijasPasaule1996_11-28-dep-unlabeled.conll"));
		//in = new BufferedReader(new FileReader("data/intervija-unlabeled.conll"));
		//in = new BufferedReader(new FileReader("data/LETA_IzlaseFreimiem-dep-unlabeled.conll"));
		
//		Mention m;
//		String m_t = "", m_str="";
//		Integer m_s = -1, m_r = -1, m_e=-1, m_id = 0;
		
		while ((s = in.readLine()) != null) {
			if (s.trim().length() > 0) {
				String[] fields = s.split("\t");
				String token = fields[1];
				String lemma = fields[2];
				String tag = fields[4];		
				int parent = Integer.parseInt(fields[6]) + sentence_start_id - 1;
				String category = fields[7];
				
				Node node = new Node(token, lemma, tag, parent, node_id);
                node.sentNum = sentence_id;

                node_id++;
                tree.add(node);
               
				
			} else {
				for (int i = sentence_start_id; i < tree.size(); i++) {
					//System.out.println(i);
                    Node n = tree.get(i);
					int p_id = n.parentID;
                    Node p;
                    try {
                        p = tree.get(p_id);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        p = null;
                    }
     
                    n.parent = p;
					if (p != null) {
						p.children.add(n);}
				}
				sentence_start_id = node_id;
                sentence_id++;
			}
			
		}
		
	}
    
    public void setMentions() {
        int mention_id = 0;
        for (Node node : tree) {
            if (node.tag.charAt(0) == 'n' || node.tag.charAt(0) == 'p') {
                if (!dict.excludeWords.contains(node.lemma)) {
                    node.isMention = true;
                    Mention m = new Mention(this, mention_id++, node.id, node.id, node, getSubString(node.id, node.id));
                    mentions.add(m);
                    node.mention = m;
                }
            }
        }
    }
    
    
    /**
     * 
     * @param p no kurienes iet uz augšu
     * @param p_prev iepriekšejais mezlgs no kurienes gāja uz augšu
     * @param from mezgli no kuienes iet uz leju
     * @return 
     */
    public List<Node> traverse(Node p, Node p_prev, List<Node> from) {
        List <Node> res = new ArrayList();
        res.add(p.parent);
        for(Node n : from) {
            if (n != p) {
                res.addAll(n.children);
            } else {
                for(Node x : p.children) {
                    if (x != p_prev) res.add(x);
                }
            }
        }
        return res;
    }
    

    
//        /**
//     * Apceļo visus mezglus vienu līmeni dziļāk (var gadīties nonākt arī iepriekš
//     * apsktatītā mezglā
//     * @param p no kurienes iet uz augšu
//     * @param from mezgli no kuienes iet uz leju
//     * @return 
//     */
//    public List<Node> traverseAll(Node p, List<Node> from) {
//        List <Node> res = new ArrayList();
//        res.add(p.parent);
//        for(Node n : from) {
//            res.addAll(n.children);
//        }
//        return res;
//    }
    
    
    
    

}