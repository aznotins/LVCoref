package LVCoref;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
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
    
    public ArrayList<Integer> sentences; //node ids starting senteces

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
        sentences = new ArrayList<Integer>();
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
    
    public void readCONLL(String filename) throws Exception {
        String s;
		int node_id = 0;
        int sentence_id = 0;
		int sentence_start_id = 0;
		BufferedReader in = null;
        
        in = new BufferedReader(new FileReader(filename));
		
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
                if (Integer.parseInt(fields[6]) == 0) node.sentRoot = true;

                node_id++;
                tree.add(node);
				
			} else if ((sentence_start_id != node_id)) {
                tree.get(sentence_start_id).sentStart = true;
                sentences.add(sentence_start_id);
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
                tree.get(node_id-1).sentEnd = true;
				sentence_start_id = node_id;
                sentence_id++;
			}
			
		}
        if (sentence_start_id != node_id) {
            tree.get(sentence_start_id).sentStart = true;
            sentences.add(sentence_start_id);
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
            tree.get(node_id-1).sentEnd = true;
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
     * @param from mezgli no kuienes iet uz leju
     * @return 
     */
   public List<Node> traverse(Node p, List<Node> from) {
        List <Node> res = new LinkedList<Node>();
        Node p_prev = null;
        if (p != null && p.parent != null) res.add(p.parent);
        for(Node n : from) {
            if (n != p) {
                res.addAll(n.children);
                if (n.parent != null && n.parent == p) p_prev = n;
                
            } else if (p != null) {
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
    
    
    public String nodeSubTree(Node n) {
        String s = "";
        int sent = n.sentNum;
        Boolean singleton = true;
        for(int i = sentences.get(sent); (i < tree.size()) && (tree.get(i).sentNum == sent); i++) {
            if (tree.get(i).parent == n) {
                if (singleton) {
                    s = nodeSubTree(tree.get(i));
                    singleton = false;
                } else {
                    s += " " + nodeSubTree(tree.get(i));
                }
            }
        }
        if (!singleton) s = n.word + " ( "+s+ " ) "; 
        else s = n.word;
        return s;
    }
    
    public void visualizeParseTree() {
        for(Node n: tree) {
            if (n.sentRoot) {
                System.out.println(nodeSubTree(n));
            }
        }
    }
    
    
    public void htmlOutput(String filename){
        int cn = corefClusters.keySet().size();
        String[] cols = new String[cn];        
        float step = (float) 360/cn;
        for (int i = 0; i < cn; i++)
            cols[i] = Integer.toHexString(Color.HSBtoRGB(step*i, 1, 1)).substring(2,8);//cols[i] = Integer.toHexString(Color.HSBtoRGB((float) i / cn, 1, 1)).substring(2,8);
        
        Map<Integer, String> corefColor = new HashMap<Integer,String>();
        
        Collections.shuffle(Arrays.asList(cols));
        
        for (Integer id: corefClusters.keySet()) {
            corefColor.put(id, cols[--cn]);
        }
        
        try {
            
            PrintWriter out = new PrintWriter(new FileWriter(filename));
            out.print(
                "<!DOCTYPE html>\n"
                    +"<html>\n"
                    + "<head>"
                    + "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">"
               
                    +"<link rel='stylesheet' type='text/css' href='style.css'>"
                    +"<script src=\"http://code.jquery.com/jquery-latest.min.js\" type=\"text/javascript\"></script>\n"
                    +"<script src=\"scripts.js\" type=\"text/javascript\"></script>\n"
                    + "</head>" 
                    
                +"<body><p class=text>");
            Node root = null;
            for (Integer s: sentences) {
                
                out.println("<div class='sentence'>"); 
                
                for (Integer i = s; ;i++) {
                    Node n = tree.get(i);
                    
                    if (n.mention != null && corefClusters.get(n.mention.corefClusterID).corefMentions.size() > 1) {
                        Mention ant = refGraph.getFinalResolutions().get(n.mention);
                        out.print("<span style='color:#"+corefColor.get(n.mention.corefClusterID)+";'id='"+n.mention.id+"' title='"+n.mention.corefClusterID+"/"+n.mention.id+"("+n.tag+":"+n.lemma+")"+"/"+((ant == null)?null:ant.id)+"/"+n.mention.type+"/"+n.mention.categories+"@"+n.mention.comments+"]'><em class='c"+n.mention.corefClusterID+"'>"+" " + n.word+"</em>["+n.mention.corefClusterID+"]</span>"/*+n.mention.categories*/);
                    } else if (n.mention != null) {
                        out.print(" <em title='#"+n.mention.id+"("+n.tag+":"+n.lemma+")"+"/"+n.mention.type+"/"+n.mention.categories+"'><span>" + n.word+"</span></em>");
                    } else {
                        out.print(" <span title='"+n.tag+":"+n.lemma+"'>" + n.word + "</span>");
                    }
                    if (n.sentRoot) root = n;
                    
                    if (n.sentEnd) break;
                }
 
                if (root != null) { out.println(" <div class='parsetree' style='color:#bbb; display:none;' title='"+nodeSubTree(root) +"'>"+nodeSubTree(root)+"</div>"); }
                out.println("</div>");
                
            }
            
            out.print("</p></body></html>");
            out.close();
        } catch (IOException e) {
            
        }
    }
    
    public void printCoreferences() {
        for (Integer i : corefClusters.keySet()) {
            if ( corefClusters.get(i).corefMentions.size() > 1){
                System.out.println("---C"+i+"---");
                for (Mention m : corefClusters.get(i).corefMentions) {
                    System.out.println(m.node);
                }
            }
        }
    }
        
}