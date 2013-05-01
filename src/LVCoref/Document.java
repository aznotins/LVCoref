package LVCoref;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
    public ArrayList<Mention> goldMentions;
    
    public RefGraph refGraph;
    
    public Map<Integer, CorefCluster> corefClusters;
    public Map<Integer, CorefCluster> goldCorefClusters;
    
    public Dictionaries dict;
    
	
    Document(){
		tree = new ArrayList<>();
		mentions = new ArrayList<>();
        goldMentions = new ArrayList<>();
        dict = new Dictionaries();
        refGraph = new RefGraph();
        corefClusters = new HashMap<>();
        goldCorefClusters = new HashMap<>();
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
    
    
    public void outputCONLL(String filename) throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(filename));
         
        for (Node n : tree) {
            out.print(n.conll_string);
            if (n.mention != null) {
                out.print("\t" + n.mention.id);
                if (n.mention.categories.size() > 0) out.print("\t" + Utils.implode(n.mention.categories, "|"));
                else out.print("\t_");
                if (corefClusters.get(n.mention.corefClusterID).corefMentions.size() > 1) out.print("\t" + n.mention.corefClusterID);
                else out.print("\t_");
            }
            else out.print("\t_\t_\t_");
            
            if (n.goldMention != null) {
                //System.out.println(n.goldMention);
                out.print("\t" + n.goldMention.id);
                out.print("\t" + Utils.implode(n.goldMention.categories, "|"));
                if (goldCorefClusters.get(n.goldMention.goldCorefClusterID).corefMentions.size() > 0) out.print("\t" + n.goldMention.goldCorefClusterID);
                else out.print("\t_");
            }
            else out.print("\t_\t_\t_");
            
            
            out.print('\n');
            
            if (n.sentEnd) out.print('\n');
        }     
    }
    
    
    public void outputCONLLforDavis(String filename) throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(filename));
         
        for (Node n : tree) {
            out.print(n.conll_string);
            if (n.mention != null) {
                out.print("\t" + n.mention.corefClusterID);
                if (n.mention.categories.size() > 0) out.print("\t" + Utils.implode(n.mention.categories, "|"));
                else out.print("\t_");
            }
            else out.print("\t_\t_");
            out.print('\n');
            if (n.sentEnd) out.print('\n');
        }     
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
                node.conll_string = s;
                node.position = Integer.parseInt(fields[0]);
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
    
    
    public Boolean addAnnotationMMAX(String filename) {
        try {
            File mmax_file = new File(filename);
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 
            org.w3c.dom.Document doc = dBuilder.parse(mmax_file);
            //optional, but recommended - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work	
            //doc.getDocumentElement().normalize();
            
            //org.w3c.dom.Element root = doc.getDocumentElement();
            //NodeList markables = root.getChildNodes();
            NodeList markables = doc.getElementsByTagName("markable");
            Map<String, Integer> classToInt = new HashMap<>();
            Integer cluster_c = 0;
            Integer cluster_id;
            
            for (int i = 0; i < markables.getLength(); i++) {
                org.w3c.dom.Node markable = markables.item(i);

                String span = markable.getAttributes().getNamedItem("span").getNodeValue();
                String cluster = markable.getAttributes().getNamedItem("coref_class").getNodeValue();
                String category = markable.getAttributes().getNamedItem("category").getNodeValue();
                
                String[] intervals = span.split(",");
                String[] interval = intervals[0].split("\\.\\.");
                //TODO: more intervals w1..w2,w3
                int start = Integer.parseInt(interval[0].substring(5)) - 1 ;
                int end = start;
                if (interval.length > 1) {
                    end = Integer.parseInt(interval[1].substring(5)) - 1;
                }
                
                Node head = getHead(start, end);
                
                //currently supports only one mention per node as head
                if (head.goldMention == null) {
                    
                    Mention goldMention = new Mention(this, i, head, getSubString(start, end));
                    goldMentions.add(goldMention);
                    head.goldMention = goldMention;
                    goldMention.categories = new HashSet<>();
                    goldMention.categories.add(category);
                                        
                    if (cluster.equals("empty")) {
                        cluster_id = cluster_c++;
                        goldCorefClusters.put(cluster_id, new CorefCluster(cluster_id));
                    } else {
                        cluster_id = classToInt.get(cluster);
                        if (cluster_id == null) {
                            cluster_id = cluster_c++;
                            classToInt.put(cluster, cluster_id);
                            goldCorefClusters.put(cluster_id, new CorefCluster(cluster_id));
                        }
                    }
                    goldCorefClusters.get(cluster_id).add(goldMention);
                    goldMention.goldCorefClusterID = cluster_id;
                    

                    LVCoref.logger.fine("goldCluster #" + cluster_id +"(size="+goldCorefClusters.get(cluster_id).corefMentions.size()+")" + " " + getSubString(start, end) +" : \""+ head.nodeProjection(this)+ "\"");
                } else {
                    System.err.println("Could not add mention because of same head: " + getSubString(start, end));
                    System.err.flush();
                }
                
            }
                
        } catch (Exception e) {
            System.err.println("Error adding MMAX2 annotation:" + e.getMessage());
            return false;
        }
        return true;
    }
    
    
    public boolean mergeClusters(Mention m, Mention n) {
        // put all mentions from m corefCluster to n corefCluster
        
        if (corefClusters.get(m.corefClusterID) != corefClusters.get(n.corefClusterID)) {
        
            int removeID = m.corefClusterID;

            Set<Mention> cm = corefClusters.get(m.corefClusterID).corefMentions;
            Set<Mention> cn = corefClusters.get(n.corefClusterID).corefMentions;

            for (Mention mm : cm) {
                cn.add(mm);
                mm.corefClusterID = n.corefClusterID;
            }

            LVCoref.logger.fine("Merge clusters from " +removeID +" to " + n.corefClusterID +" " + Utils.linearizeMentionSet(cn));

            corefClusters.remove(removeID);
            return true;
        } else {
            return false;
        }
    }
    
    public void setMentions() {
        int mention_id = 0;
        for (Node node : tree) {
            if (node.tag.charAt(0) == 'n' || node.tag.charAt(0) == 'p') {
                if (!dict.excludeWords.contains(node.lemma)) {
                    node.isMention = true;
                    Mention m = new Mention(this, mention_id++, node, node.word);
                    mentions.add(m);
                    node.mention = m;
                }
            }
        }
    }
    
   
    
    
    public void initializeEntities() {
        //initialize new cluster for each mention
        for (Mention m: mentions) {
            corefClusters.put(m.id, new CorefCluster(m.id));
            corefClusters.get(m.id).add(m);
            m.corefClusterID = m.id;
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
    
    
    public Node getCommonAncestor(Node n, Node m) {
        Set<Node> path = new HashSet<>(); //all path nodes traversed by going up
        path.add(n);
        path.add(m);
        Node nn = n, mm = m;
        while (nn != null && mm != null) {
            nn = nn.parent;
            mm = mm.parent;
            if (nn != null) {
                if (path.contains(nn)) {
                    return nn;
                } else {
                    path.add(nn);
                }
                if (path.contains(mm)) {
                    return mm;
                } else {
                    path.add(mm);
                }
            }
        }
        return null; //something went wrong
    }
    
    public Node getHead(int start, int end) {
        List<Node> cand = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            Node n = tree.get(i);
            if (n.parent != null && ( n.parent.id < start || n.parent.id > end)) {
                cand.add(n);
            }
        }
        Node head = cand.get(0);
        //needed if markable contains multiple head candidates
        //lowest common ancestor is returned
        for (int i = 1; i < cand.size(); i++) {
            head = getCommonAncestor(head, cand.get(i));
        }
        return head;
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
        float step = ((float) 1.0) / cn;
        for (int i = 0; i < cn; i++) {
            cols[i] = Integer.toHexString(Color.HSBtoRGB(step*i, 0.5f, 1f )).substring(2,8);//cols[i] = Integer.toHexString(Color.HSBtoRGB((float) i / cn, 1, 1)).substring(2,8);
            System.out.println(i + " "+ i*step+ " " + cols[i]);
        }
        Map<Integer, String> corefColor = new HashMap<>();
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
                    + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">"
                    +"<script src=\"jquery-1.8.3.min.js\" type=\"text/javascript\"></script>\n"
                    +"<link rel='stylesheet' type='text/css' href='style.css'>"
                    //+"<script src=\"http://code.jquery.com/jquery-latest.min.js\" type=\"text/javascript\"></script>\n"
                    +"<script src=\"scripts.js\" type=\"text/javascript\"></script>\n"
                    + "</head>" 
                    
                +"<body><p class=text>");
            Node root = null;
            for (Integer s: sentences) {
                
                out.println("<div class='sentence'>"); 
                
                
                for (Integer i = s; ;i++) {
                    Node n = tree.get(i);
                    
                    //if (n.mentionStartList.size()>0) out.print(n.mentionStartList);
                    int count = n.mentionStartList.size();
                    for (int mid = 0; mid < count; mid++) {
                        out.println("<span class='span' id='s"+n.mentionStartList.get(mid)+"'> [ ");
                    }
                    
                    if (n.mention != null && corefClusters.get(n.mention.corefClusterID).corefMentions.size() > 1) {
                        Mention ant = refGraph.getFinalResolutions().get(n.mention);
                        
                        
                        out.print(" <span "
                                    + "class='coref'"
                                    + "style='background-color:#"+corefColor.get(n.mention.corefClusterID)+";'"
                                    + "id='"+n.mention.id+"' "
                                    + "title='"
                                        + "@cID=" + n.mention.corefClusterID
                                        + " @mID=" + n.mention.id
                                        + " @POS=" + n.tag+":"+n.lemma                                       
                                        + " @antID="+((ant == null)?null:ant.id)
                                        + " @type="+n.mention.type
                                        + " @cat="+n.mention.categories
                                        + " @resoInfo="+n.mention.comments+"]"
                                        + " @span=["+n.nodeProjection(this) +"]"
                                        + " @startM="+n.mention.start
                                        + " @endM="+n.mention.end
                                + "'>"
                                + " <em class='c"+n.mention.corefClusterID+"'>"+" " + n.word+"</em>"
                                + "["+n.mention.corefClusterID+"]"
                            + "</span>"/*+n.mention.categories*/);
                        
                    } else if (n.mention != null) {
                        out.print(" <span "
                                    + "title='"
                                        + " @mID="+n.mention.id
                                        + " @POS=" + n.tag+":"+n.lemma
                                        + " @type="+n.mention.type
                                        + " @cat"+n.mention.categories
                                        + " @span=["+n.nodeProjection(this) +"]"
                                    +"'>"
                                + "<em>" + n.word+"</em>"
                            + "</span>");
                    } else {
                        out.print(" <span "
                                + "title='"
                                + " @POS=" + n.tag+":"+n.lemma
                                + " @span=["+n.nodeProjection(this) +"]"
                            +"'>"
                                + n.word 
                            + "</span>");
                    }
                    
                    //if (n.mentionEndList.size()>0) out.print(n.mentionEndList);
                    count = n.mentionEndList.size();
                    for (int mid = 0; mid < count; mid++) {
                        out.println(" ] </span>");
                    }
                    if (n.sentRoot) root = n;
                    
                    if (n.sentEnd) break;
                }
 
                if (root != null) { out.println(" <div class='parsetree' style='display:none;'>"+nodeSubTree(root)+"</div>"); }
                out.println("</div>");
                
            }
            
            out.print("</p>");
            
            for (Integer c_i : this.corefClusters.keySet()) {
                CorefCluster c = this.corefClusters.get(c_i);
                if (c.corefMentions.size() > 1) {
                    out.println("<div class='mentionCluster'>");
                    out.println("========== " + c_i + " ==========");
                    for (Mention m : c.corefMentions) {
                        String projection = m.node.nodeProjection(this);
                        projection = projection.replace(m.node.word, "<b>"+m.node.word+"</b>");
                        
                        out.println
                                ("<div>"
                                + "<a href='#"+m.id+"'>"
                                + projection
                                + "</a>"
                                + "</div>");
                    }
                    out.println("</div>");
                }                
            }
            
            out.print("</body></html>");
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