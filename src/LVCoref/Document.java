package LVCoref;

import LVCoref.Dictionaries.MentionType;
import edu.stanford.nlp.util.Pair;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
	public List<Node> tree;
    
    public List<Integer> sentences; //node ids starting senteces

	public List<Mention> mentions;
    public List<Mention> goldMentions;
    
    public RefGraph refGraph;
    
    public Map<Integer, CorefCluster> corefClusters;
    public Map<Integer, CorefCluster> goldCorefClusters;
    
    public Dictionaries dict;
    
    public Logger logger;
    
    public Set<String> properWords;
    
    public Map<String, Node> acronyms;
    
	
    Document(){
		tree = new ArrayList<Node>();
		mentions = new ArrayList<Mention>();
        goldMentions = new ArrayList<Mention>();
        dict = new Dictionaries();
        refGraph = new RefGraph();
        corefClusters = new HashMap<Integer, CorefCluster>();
        goldCorefClusters = new HashMap<Integer, CorefCluster>();
        sentences = new ArrayList<Integer>();
        properWords = new HashSet();
        acronyms = new HashMap<String, Node>();
        logger = Logger.getLogger(Document.class.getName());
	}
    
    Document(Logger _logger) {
        tree = new ArrayList<Node>();
		mentions = new ArrayList<Mention>();
        goldMentions = new ArrayList<Mention>();
        dict = new Dictionaries();
        refGraph = new RefGraph();
        corefClusters = new HashMap<Integer, CorefCluster>();
        goldCorefClusters = new HashMap<Integer, CorefCluster>();
        sentences = new ArrayList<Integer>();
        this.logger = _logger;
        properWords = new HashSet();
        acronyms = new HashMap<String, Node>();
    }
    
    Document(Logger _logger, Dictionaries d) {
        tree = new ArrayList<Node>();
		mentions = new ArrayList<Mention>();
        goldMentions = new ArrayList<Mention>();
        dict = d;
        refGraph = new RefGraph();
        corefClusters = new HashMap<Integer, CorefCluster>();
        goldCorefClusters = new HashMap<Integer, CorefCluster>();
        sentences = new ArrayList<Integer>();
        this.logger = _logger;
        properWords = new HashSet();
        acronyms = new HashMap<String, Node>();
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
        startID = Math.max(startID, 0);
        endID = Math.min(endID, tree.size()-1);
        for(int i = startID; i <= endID;  i++) {
            s+= tree.get(i).word + " ";
        }
        return s.trim();
            
    }
    
    public String getNormSubString(int startID, int endID) {
        String s = "";
        for(int i = startID; i <= endID;  i++) {
            s+= tree.get(i).lemma + " ";
        }
        return s.trim();
            
    }
    
    
//    public void outputCONLL(String filename) throws IOException {
//        PrintWriter out = new PrintWriter(new FileWriter(filename));
//         
//        for (Node n : tree) {
//            out.print(n.conll_string);
//            if (n.mention != null) {
//                out.print("\t" + n.mention.id);
//                if (n.mention.categories.size() > 0) out.print("\t" + Utils.implode(n.mention.categories, "|"));
//                else out.print("\t_");
//                if (corefClusters.get(n.mention.corefClusterID).corefMentions.size() > 1) out.print("\t" + n.mention.corefClusterID);
//                else out.print("\t_");
//            }
//            else out.print("\t_\t_\t_");
//            
//            if (n.goldMention != null) {
//                //System.out.println(n.goldMention);
//                out.print("\t" + n.goldMention.id);
//                out.print("\t" + Utils.implode(n.goldMention.categories, "|"));
//                if (goldCorefClusters.get(n.goldMention.goldCorefClusterID).corefMentions.size() > 0) out.print("\t" + n.goldMention.goldCorefClusterID);
//                else out.print("\t_");
//            }
//            else out.print("\t_\t_\t_");
//            
//            
//            out.print('\n');
//            
//            if (n.sentEnd) out.print('\n');
//        }     
//    }
    
    public void setConllCorefColumns() {
        for (Node n : tree) {
            if (n.mention != null) {
                n.conll_fields.add(Integer.toString(n.mention.corefClusterID));
                if (n.mention.categories.size() > 0) n.conll_fields.add(Utils.implode(n.mention.categories, "|"));
                else n.conll_fields.add("_");
            } else {
                n.conll_fields.add("_");
                n.conll_fields.add("_");
            }
            if (Constants.EXTRA_CONLL_COLUMNS) {
                if (n.mention != null) n.conll_fields.add(n.mention.type.toString()); else n.conll_fields.add("_");
                n.conll_fields.add(n.getConllMentionColumn(this, true));
            }
        }
    }
    
    public void outputCONLL(String filename){
        PrintWriter out;
        try {
            String eol = System.getProperty("line.separator");
            out = new PrintWriter(new FileWriter(filename));
            StringBuilder s = new StringBuilder();
            for (Node n : tree) {
                for (String field : n.conll_fields) s.append(field + "\t");
                s.deleteCharAt(s.length()-1); //delete last tab
                s.append(eol);
                if (n.sentEnd) s.append(eol);
            }
            out.print(s.toString());
            out.flush();
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(Document.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("ERROR: couldn't create/open output conll file");
        }
    }
    
    public void outputCONLL(PrintStream out){
        String eol = System.getProperty("line.separator");
        StringBuilder s = new StringBuilder();
        for (Node n : tree) {
            for (String field : n.conll_fields) s.append(field + "\t");
            s.deleteCharAt(s.length()-1); //delete last tab
            s.append(eol);
            if (n.sentEnd) s.append(eol);
        }
        s.append(eol); //document end
        out.print(s.toString());
        out.flush();
    }
    
    
    public void readCONLL(String filename) throws Exception {
        if (logger != null) logger.fine("Read Conll " + filename);
		BufferedReader in = null;        
        in = new BufferedReader(new FileReader(filename));		
        readCONLL(in);
        in.close();
	}
    
    
    public void readCONLL(BufferedReader in) throws Exception {
        if (logger != null) logger.fine("Read conll stream");        
        String s;
		int node_id = 0;
        int sentence_id = 0;
		int sentence_start_id = 0;
        int empty_lines = 0;
        
		while ((s = in.readLine()) != null) {
            if (s.equalsIgnoreCase("quit")) {
                LVCoref.stopProcess = true;
                break;
            }
            if (s.equalsIgnoreCase("stop")) {
                break;  //document end
            }
            
			if (s.trim().length() > 0) {
				String[] fields = s.split("\t");
                //for (int j = 0; j < fields.length; j++) System.out.println("<"+fields[j]++":"++">");
				String token = fields[1];
				String lemma = fields[2];
				String tag = fields[4];	
                int position = Integer.parseInt(fields[0]);
                int parent_in_sentence = Integer.parseInt(fields[6]);
                //parent_in_sentence = 0;
                if (position == 1) {
                    if ((sentence_start_id != node_id)) {
                        sentences.add(sentence_start_id);
                        tree.get(node_id-1).sentEnd = true;
                        sentence_start_id = node_id;
                        sentence_id++;
                    }
                }          
				int parent = parent_in_sentence + sentence_start_id - 1;
				//System.out.println(parent);
				Node node = new Node(token, lemma, tag, parent, node_id, this);
                
                int columnCount = Math.min(fields.length, Constants.savedColumnCount);
                node.conll_fields.addAll(Arrays.asList(fields).subList(0, columnCount));
                node.position = position;
                node.sentNum = sentence_id;
                if (position == 1) { node.sentStart = true; }
                if (parent_in_sentence == 0) node.sentRoot = true;
                tree.add(node);

                node_id++;
                empty_lines = 0;
			} else {
                empty_lines++;
                if (empty_lines >= 2) break;
            }		
		}
        if (sentence_start_id != node_id) {
            sentences.add(sentence_start_id);
            tree.get(node_id-1).sentEnd = true;
        }
        initializeNodeTree();
	}
    
    
    public void initializeNodeTree() {
        for (Node n : tree) {
            if (Constants.USE_SINTAX) {
                if (n.parentID > 0 && n.parentID < tree.size()) {
                    Node p = tree.get(n.parentID);
                    n.parent = p;
                    p.children.add(n);
                } else {
                    n.parent = null;
                    //log null pointers
                }
            } else {
                n.parentID = n.id + 1;
                if (n.parentID < tree.size()) {
                    n.parent = tree.get(n.parentID);
                } else {
                    n.parent = null;
                    n.parentID = -1;
                }
            }
            
        }
    }
    
    
    
    /**
     * Add MMAX Gold Annotation
     * @param filename
     * @return if annotation was ok
     */
    public Boolean addAnnotationMMAX(String filename) {
        try {
            File mmax_file = new File(filename);
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 
            org.w3c.dom.Document doc = dBuilder.parse(mmax_file);
            NodeList markables = doc.getElementsByTagName("markable");
            Map<String, Integer> classToInt = new HashMap<String, Integer>();
            Integer cluster_c = 0;
            Integer cluster_id;
            
            for (int i = 0; i < markables.getLength(); i++) {
                org.w3c.dom.Node markable = markables.item(i);

                String span = markable.getAttributes().getNamedItem("span").getNodeValue();
                String cluster = markable.getAttributes().getNamedItem("coref_class").getNodeValue();
                String category = markable.getAttributes().getNamedItem("category").getNodeValue();
                
                String[] intervals = span.split(",");
                String[] interval = intervals[0].split("\\.\\.");
                int start = Integer.parseInt(interval[0].substring(5)) - 1 ;
                int end = start;
                if (interval.length > 1) {
                    end = Integer.parseInt(interval[1].substring(5)) - 1;
                }
                
                Node head = getHead(start, end);
                
                
                if (category.equals("profession")) category = "person";
                if (category.equals("event")) continue;
                //if (category.equals("product")) continue;
                if (category.equals("media")) continue;
                if (category.equals("time")) continue;
                if (category.equals("sum")) continue;
                
                //if (head.tag.charAt(0) == 'p') continue;
                
                //currently supports only one mention per node as head
                if (head.goldMention == null) {
                    
                    MentionType type = head.getType();
                    Mention goldMention = new Mention(this, i, head, type , start, end); // @FIXME
                    goldMention.document = this;
                    goldMention.start = start;
                    goldMention.end = end;
                    goldMentions.add(goldMention);
                    head.goldMention = goldMention;
                    goldMention.categories = new HashSet<String>();
                    goldMention.categories.add(category);
                    goldMention.sentNum = head.sentNum;
                                        
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
                    logger.fine("Could not add mention because of same head: " + getSubString(start, end));
                }
                
            }
            sortGoldMentions();   
        } catch (Exception e) {
            System.err.println("Error adding MMAX2 annotation:" + e.getMessage());
            return false;
        }
        return true;
    }
    
      
    /**
     * Put all mentions from m cluster to n
     * @param m Mention
     * @param n Mention
     * @return true if cluster were changed
     */
    public boolean mergeClusters(Mention m, Mention n) {
        assert(m != null && n != null);
        assert(corefClusters.get(m.corefClusterID) != null && corefClusters.get(n.corefClusterID) != null);
        if (corefClusters.get(m.corefClusterID) != corefClusters.get(n.corefClusterID)) {
            int removeID = m.corefClusterID;
            Set<Mention> cm = corefClusters.get(m.corefClusterID).corefMentions;
            Set<Mention> cn = corefClusters.get(n.corefClusterID).corefMentions;
            for (Mention mm : cm) {
                getCluster(n.corefClusterID).add(mm);
                mm.corefClusterID = n.corefClusterID;
            }
            corefClusters.remove(removeID);
            return true;
        } else {
            return false;
        }
    }
    
    
    public void updateProperWords() {
        for(Node n : tree) {
            if (n.position > 1 && Character.isUpperCase(n.word.charAt(0))) {
                //properWords.add(n.lemma.toLowerCase());
                properWords.add(n.lemma.toLowerCase());
                //System.out.println(n.word + " " + n.id);
            }
        }
        for(Node n : tree) {
            if (n.position == 1 && Character.isUpperCase(n.word.charAt(0)) && properWords.contains(n.lemma.toLowerCase())) {
                n.isProper = true;
                //System.out.println(n.word + " " + n.id);
            }
        }
    }
    
    
    public int maxClusterID() {
        int k = 0;
        for (int i: corefClusters.keySet()) {
            if (i > k) k = i;
        }
        return k;
    }
    
    public void setTmpMentions() {
        int mention_id = mentions.size();
        for (Node node : tree) {
            if (node.mention == null) {
                if (node.tag.charAt(0) == 'n' || node.tag.charAt(0) == 'p') {
                    if (!dict.excludeWords.contains(node.lemma)) {                        
                        node.isMention = true;
                        Mention m = new Mention(this, mention_id++, node, node.getType(), node.id, node.id);
                        mentions.add(m);
                        node.mention = m;
                        m.tmp = true;
                        LVCoref.logger.fine(Utils.getMentionComment(this, m, "Set tmp mention"));
                    }
                }
            }
        }
    }
    
    public void setListMentions() {
        int mention_id = mentions.size();
        for (Node node : tree) {
            if (node.mention != null) continue;
            //if (node.tag.charAt(0) == 'p') continue;
            //if (node.tag.charAt(0) == 'n' || node.tag.charAt(0) == 'p') {
            if (node.tag.charAt(0) == 'r') continue;
            
                String cat = dict.getCategory(node.lemma);
                if (cat != null) {
                    node.isMention = true;
                    Mention m = new Mention(this, mention_id++, node, node.getType(), node.id, node.id);
                    m.strict = true;
                    mentions.add(m);
                    node.mention = m;
                    m.category = cat;
                    m.categories.add(cat);
                    LVCoref.logger.fine(Utils.getMentionComment(this, m, "Set list mention"));
                }
            //}
        }
    }
    
    
    public void setProperNodeMentions(Boolean create) {
        int mention_id = mentions.size();
        for (Node node : tree) {
            if (node.position > 1 && Character.isUpperCase(node.word.charAt(0))) {
                node.isProper = true;
            }
            if (node.mention == null && node.isNoun() && node.isProper() && create) {
                node.isMention = true;
                Mention m = new Mention(this, mention_id++, node, node.getType(), node.id, node.id);
                mentions.add(m);
                node.mention = m;
                LVCoref.logger.fine(Utils.getMentionComment(this, m, "Set proper node mention"));
            }
            else if (node.isProper() && node.mention != null && node.mention.type == MentionType.NOMINAL) {
                node.mention.type = MentionType.PROPER;
                LVCoref.logger.fine(Utils.getMentionComment(this, node.mention, "Set proper node mention (update to proper)"));
            }
        }
    }
    
    public void setMentions() {
        int mention_id = mentions.size();
        for (Node node : tree) {
            if (node.mention == null && (node.isNoun() || node.isPronoun())) {
                node.isMention = true;
                Mention m = new Mention(this, mention_id++, node, node.getType(), node.id, node.id);
                mentions.add(m);
                node.mention = m;
                LVCoref.logger.fine(Utils.getMentionComment(this, m, "Set naive mention"));
            }
        }
    }
    
    
    public void setDetalizedNominalMentions() {
        int mention_id = mentions.size();
        for (Node node : tree) {
            if (node.isProper() && node.isNounGenitive() && node.parent != null && node.parent.mention == null && node.parent.isNoun()) {
                node.parent.isMention = true;
                Mention m = new Mention(this, mention_id++, node.parent, node.parent.getType(), node.parent.id, node.parent.id);
                mentions.add(m);
                node.parent.mention = m;
                LVCoref.logger.fine(Utils.getMentionComment(this, m, "Set detalized nominal mention"));
            }
        }
    }
    
      
    
    public boolean setMentionsNER(String filename) {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Document.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Error opening NE annotation");
            return false;
        }
		
		String s;
        int id  = 0;
        int start = 0;
        String cur_cat = "O";
        Boolean isMention = false;
        try {
            while ((s = in.readLine()) != null) {
                if (s.trim().length() > 0) {
                    String[] fields = s.split("\t");
                    assert(fields.length == 3);
                    String word = fields[0];
                    String cat = fields[2];	
                    assert(word.substring(0,1).equals(tree.get(id).word.substring(0,1)));//assert(word.equals(tree.get(id).word)); ner dont suuport multiple words ?? FIXME
                    if (isMention && (cat.equals("O") || !cur_cat.equals(cat) || tree.get(id).sentStart)) {
                        LVCoref.logger.fine("NER Mention :("+start+" " + (id-1)+") " + getSubString(start, id-1));
                        Mention m = null;
                        if (cur_cat.equals("PERSONA")) m = setMention(start, id-1, "person", MentionType.PROPER);
                        else if (cur_cat.equals("LOKACIJA")) m = setMention(start, id-1, "location", MentionType.PROPER);
                        else if (cur_cat.equals("ORGANIZACIJA")) m = setMention(start, id-1, "organization", MentionType.PROPER);
                        else {
                            LVCoref.logger.fine("NER Unsupported category @" + cat);
                        }
                        if (m != null) {
                            m.strict = true;
                        }
                        isMention = !cat.equals("O");
                        if (isMention) {
                            start = id;
                            cur_cat = cat;
                        } else {
                            cur_cat = "O";
                        }
                    } else if (!cat.equals("O") && !cat.equals(cur_cat)) {
                        isMention = true;
                        start = id;
                        cur_cat = cat;
                    }
                    id++;
                }
            }
        } catch (IOException ex) {
            System.err.println("Error reading NE annotation");
            Logger.getLogger(Document.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    
    public void setQuoteMentions() {
        int max_l = 10;
        int i = -1;
        while (++i < tree.size()) {
            Node n =tree.get(i);
            if (n.isQuote()) {
                int j = i;
                while (++j - i <= max_l && j < tree.size()) {
                    if (tree.get(j).sentNum != n.sentNum) break;
                    if (tree.get(j).tag.charAt(0) == 'v' && !Character.isUpperCase(
                        tree.get(j).word.charAt(0))) break; //nesatur d.v.
                    if (tree.get(j).isQuote()) {
                        if (i + 1 <= j-1) {
                            String s = getSubString(i+1, j-1);
                            boolean add = false;
                            for (int k = 0; k < s.length(); k++) {
                                if (Character.isUpperCase(s.charAt(k)) ){
                                    add = true;
                                    break;
                                }
                            }
                            
                            if (add) {
                                LVCoref.logger.fine("Quote Mention :("+i+" " + j+") " + getSubString(i+1, j-1));
                                //assert(i+1 <= j-1);
                                Mention m = setMention(i+1, j-1, "", MentionType.PROPER);                                
                                if (m != null) {
                                    m.categories.addAll(dict.getCategories(m.node.lemma));
                                    LVCoref.logger.fine(Utils.getMentionComment(this, m, ""));
                                    m.bucket = "quote";
                                }
                            }                            
                            i = j-1;
                            break;
                        }                        
                    }
                }
            }
        }
    }
    
    public Mention setMention(int from, int to, String cat, MentionType t) {
        Mention x = getMention(from, to, cat, t);
        if (x != null) mentions.add(x);
        return x;
    }
    
    public Mention getMention(int from, int to, String cat, MentionType t) {
        
        if (tree.get(to).tag.equals("zs")) to--; //FIXME 

        Node head = getHead(from, to);
        if (head.mention == null) {
            //System.out.println("Mention \""+getSubString(from, to)+"\" head="+head.word);
            LVCoref.logger.fine("Mention \""+getSubString(from, to)+"\" head="+head.word);
            int id = mentions.size();
            Mention m = new Mention(this, id, head, t, from, to);
            head.mention = m;

            m.start = from;
            m.end = to;
            m.root = head.id;
            if (cat.trim().length() > 0) {
                m.categories.add(cat);
                m.category = cat;
            }
            return m;
        } else {
            System.err.println("setMention() mention with this head already set: " + "old=" + head.mention.nerString + " new=" + getSubString(from, to));
            LVCoref.logger.fine("setMention() mention with this head already set: " + "old=" + head.mention.nerString + " new=" + getSubString(from, to));
            return null;
        }
    }
   
    public void setAbbreviationMentions(Boolean gold){
        for (Node n : tree) {   
            if (gold && n.mention == null) continue;
            if (n.isAbbreviation()) {
                //if (gold) acronyms.put(n.word, n);
                acronyms.put(n.word, n);
                if(n.mention == null) {                
                    Mention m = setMention(n.id, n.id, "organization", MentionType.PROPER);
                    if (m != null) {
                        m.strict = true;                        
                        LVCoref.logger.fine(Utils.getMentionComment(this, m, "Set abbreviation mention"));
                        m.bucket = "acronym";
                    } else {
                        LVCoref.logger.fine("Couldn't create abbreviation mention: " + n.word + "("+ n.id +")");
                    }
                }
                
            }
        }
    }
    
    
    public void removeNestedMentions() {
        int max_depth = 1;
        List <Mention> mm = new ArrayList<Mention>();
        for (Mention m : mentions) {
            int l = 0;
            Node n = m.node.parent;
            Boolean nested = false;
            while (l++ < max_depth && n != null && m.node.sentNum == n.sentNum) {
                if (n.mention != null && n.id > m.node.id && (/*m.type == MentionType.NOMINAL || */m.strict == false || m.strict && n.mention.type == MentionType.NOMINAL && m.mentionCase == Dictionaries.Case.GENITIVE /*&&n.isProper()*/)) {
                    //remove m
                    n.mention.start = Math.min(n.mention.start, m.start);
                    nested = true;
                    break;
                }
                n = n.parent;
            }
            if (!nested) mm.add(m);
            else {
                removeMention(m);
                LVCoref.logger.fine(Utils.getMentionComment(this, m, "Removed nested mention"));
            }
        }
        mentions.clear();
        mentions = mm;
        normalizeMentions();
    }
    
    public void removeNestedQuoteMentions() {
        for (Mention m : mentions) {
            if (m.isQuote()) {
                for (int i = m.start; i <= m.end; i++) {
                    Node q = tree.get(i);
                    if (q.mention != null && !q.mention.isQuote()) {
                        q.mention.tmp = true;
                        LVCoref.logger.fine(Utils.getMentionComment(this, m, "Removed nested quote mention"));
                    }
                }
            }
        }
       removeTmpMentions();
    }
    
    
    public void removePluralMentions() {
        List <Mention> mm = new ArrayList<Mention>();
        for (Mention m : mentions) {
            if (m.number == Dictionaries.Number.PLURAL) {
                removeMention(m);
                LVCoref.logger.fine(Utils.getMentionComment(this, m, "Removed Plural mention"));
            } else {
                mm.add(m);
            }
        }
        mentions.clear();
        mentions = mm;
        normalizeMentions();
//        for (Mention m : mentions) {
//            if (m.number == Dictionaries.Number.PLURAL) {
//
//        }
    }
    
    public void removeTmpMentions() {
        List <Mention> mm = new ArrayList<Mention>();
        for (Mention m : mentions) {
            if (m.tmp) {
                removeMention(m);
                LVCoref.logger.fine(Utils.getMentionComment(this, m, "Removed TMP mention"));
            } else {
                mm.add(m);
            }
        }
        mentions.clear();
        mentions = mm;
        normalizeMentions();
//        for (Mention m : mentions) {
//            if (m.number == Dictionaries.Number.PLURAL) {
//
//        }
    }
    
    public void removePleonasticMentions() {
        List <Mention> mm = new ArrayList<Mention>();
        Set<String> plVerbs = new HashSet<String>(Arrays.asList("radīt","vedināt","liecināt", "nozīmēt"));
        Set<String> plVerbsAdv = new HashSet<String>(Arrays.asList("būt","nebūt","kļūt","izskatīties", "nozīmēt"));
        Set<String> mod = new HashSet<String>(Arrays.asList("viss"));
        
        for (Mention m : mentions) {
            boolean remove = false;
            
            
            if (m.node.lemma.equals("tas")) {
                //apstākļa vārds
                if(m.node.parent != null && plVerbsAdv.contains(m.node.parent.lemma)) {
                    for (Node n: m.node.parent.children) {
                        if ( n.tag.charAt(0) == 'r') {
                            remove = true;
                            break;
                        }
                    }
                }

                if (m.node.next(this) != null && m.node.next(this).isRelativeClaus()) {
                    remove = true;
                }
                
                if (m.node.next(this) != null && mod.contains(m.node.next(this).lemma)) {
                    remove = true;
                }
            }
            
            if (remove) {
                removeMention(m);
                LVCoref.logger.fine(Utils.getMentionComment(this, m, "Removed Pleonastic mention"));
            } else {
                mm.add(m);
            }
        }
        mentions.clear();
        mentions = mm;
        normalizeMentions();
    }
    
    public void removeSingletonMentions() {
        List <Mention> mm = new ArrayList<Mention>();
        for (Mention m : mentions) {
            if (corefClusters.get(m.corefClusterID) != null && corefClusters.get(m.corefClusterID).corefMentions.size() < 2) {
                removeMention(m);
                LVCoref.logger.fine(Utils.getMentionComment(this, m, "Removed singleton mention"));
            } else {
                mm.add(m);
            }
        }
        mentions.clear();
        mentions = mm;
        normalizeMentions();
    }
    
    //FIXME
    //add unset attribules
    public void updateMentions() {
        for (Mention m : mentions) {
            m.normString = getNormSubString(m.start, m.end);
            
            for(int i = m.start; i <= m.end; i++) {
                m.words.add(tree.get(i).lemma);
            }
            
            m.nerString = getSubString(m.start, m.end);
        }
    }
    
    public void setMentionModifiers(boolean updateMargins) {
        for (Mention m : mentions) {
            if (m.type != MentionType.NOMINAL && m.type != MentionType.PROPER) continue;
            Node n = m.node.prev(this);
            int left = m.node.id;
            while (
                    n != null 
                    && (n.isNounGenitive() || (n.isProperAdjective() || n.isDefiniteAdjective())
                    && (m.gender == Dictionaries.Gender.UNKNOWN || m.gender == n.getGender()) 
                    && (m.number == Dictionaries.Number.UNKNOWN || m.number == n.getNumber()) 
                    || m.isPerson() && n.isProper /*|| n.isQuote()*/)) {
//                if (n.isQuote()) {
//                    left--;
//                    n = n.prev(this);
//                    continue;
//                }
                m.modifiers.add(n.lemma.toLowerCase());
                if (n.isProper()) {
                    m.properModifiers.add(n.lemma.toLowerCase());
                    m.type = MentionType.PROPER;
                }
                left--;
                n = n.prev(this);
            }
            if (updateMargins) m.start = Math.min(left, m.start);
            
            if (left != m.node.id) {
                LVCoref.logger.fine(Utils.getMentionComment(this, m, "Added mention modifiers "+"("+getSubString(left, m.node.id)+ ")"));
            }
        }
    }
    public void setMentionModifiers_v2(boolean updateMargins) {
        for (Mention m : mentions) {
            if (m.type != MentionType.NOMINAL && m.type != MentionType.PROPER) continue;
            Node n = m.node.prev(this);
            int left = m.node.id;
            while (
                    n != null 
                    && (n.isNounGenitive() || (n.isProperAdjective() || n.isDefiniteAdjective() || n.isNumber())
                    && (m.gender == Dictionaries.Gender.UNKNOWN || m.gender == n.getGender()) 
                    && (m.number == Dictionaries.Number.UNKNOWN || m.number == n.getNumber()) 
                    || m.isPerson() && n.isProper /*|| n.isQuote()*/)) {
//                if (n.isQuote()) {
//                    left--;
//                    n = n.prev(this);
//                    continue;
//                }
                m.modifiers.add(n.lemma.toLowerCase());
                if (n.isProper()) {
                    m.properModifiers.add(n.lemma.toLowerCase());
                    m.type = MentionType.PROPER;
                }
                left--;
                n = n.prev(this);
            }
            if (updateMargins) m.start = Math.min(left, m.start);
            
            if (left != m.node.id) {
                LVCoref.logger.fine(Utils.getMentionComment(this, m, "Added mention modifiers "+"("+getSubString(left, m.node.id)+ ")"));
            }
        }
    }
    
    
    public void setMentionModifiers_XXXXXXXX(boolean updateMargins) {
        for (Mention m: mentions) {
            if (!m.modifiersSet) setMentionModifiers(m, updateMargins);
        }
    }
        
     public void setMentionModifiers(Mention m, Boolean updateMargins) {
         if (m.node.isPronoun()) { m.modifiersSet = true; return;}
         if (m.node.isAbbreviation()) { m.modifiersSet = true; return;}
         if (m.bucket.equals("quote")) {
             m.modifiersSet = true; 
             for (int i = m.start; i <= m.end; i++) {
                 m.modifiers.add(tree.get(i).lemma.toLowerCase());
                 if (tree.get(i).isProper) m.properModifiers.add(tree.get(i).lemma.toLowerCase());
             }
             return;
         }
            for (int i = m.start; i <= m.end; i++) {
               if (tree.get(i).isProper) m.properModifiers.add(tree.get(i).lemma.toLowerCase());
            }
         int starts = m.start;
         Node start = m.node.getSpanStart(m.document);
         Node q = m.node.prev();
         while (q != null && q.id >= start.id) {
             if (q == null) break;
             if (q.mention != null) {
                 if (!q.mention.modifiersSet) setMentionModifiers(q.mention, updateMargins);
                 int nesMentionStart = q.mention.start;
                 m.modifiers.addAll(q.mention.modifiers);
                 m.modifiers.add(q.lemma.toLowerCase());
                 m.words.addAll(q.mention.words);
                 m.properModifiers.addAll(q.mention.properModifiers);
                  if (updateMargins) m.start = Math.min(m.start, q.mention.start);
                  if (q.mention.type == Dictionaries.MentionType.PROPER) m.type = q.mention.type;
                 while (q != null && q.id > nesMentionStart) q = q.prev();                 
             } else if (q.isNounGenitive() || q.isNumber() || q.isProperAdjective()) {
                 m.modifiers.add(q.lemma.toLowerCase());
                 m.words.add(q.lemma.toLowerCase());
                  if (updateMargins) m.start = Math.min(m.start, q.id);
             } else if (q.tag.startsWith("a") || q.isNumber() || q.isPronoun() && q.mention != null && q.mention.mentionCase == Dictionaries.Case.GENITIVE) {
                 m.words.add(q.lemma.toLowerCase());
                  if (updateMargins) m.start = Math.min(m.start, q.id);
             //} else if (q.isQuote()) {
             } else {
                 break;
             }
             if (q.isProper) {
                 m.type = Dictionaries.MentionType.PROPER;
                 m.properModifiers.add(q.lemma.toLowerCase());
             }
             //if (q.tag.startsWith("v")) break;     
             q = q.prev();
         }
         Node before = tree.get(m.start).prev();
         if (before != null) {
            if (before.mention != null && before.isAbbreviation()) {
                m.modifiers.addAll(before.mention.modifiers);
                m.words.addAll(before.mention.words);
                m.modifiers.add(before.lemma.toLowerCase());
                if (updateMargins) m.start = Math.min(m.start, before.mention.start);
            } else if (before.isQuote() ){
                before = before.prev();
                if (before != null && before.mention != null && before.mention.bucket.equals("quote")) {
                    m.modifiers.addAll(before.mention.modifiers);
                    m.modifiers.add(before.lemma.toLowerCase());
                    m.words.addAll(before.mention.words);
                    if (updateMargins) m.start = Math.min(m.start, before.mention.start);
                }
            }
         }
         if (starts != m.start) System.out.println(getSubString(m.start, m.end));
         m.modifiersSet = true;
     }
        
    
    
    public void tweakPersonMentions(){
        List <Mention> mm = new ArrayList<Mention>();
        for (Mention m : mentions) {
            boolean remove = false;
            if (m.isPerson() && m.type == MentionType.PROPER) {
                //System.out.println(m.headString + "\t\t\t\t" + m.getContext(this, 3));
                int next = m.end + 1;
                int prev = m.start - 1;
                while(prev >= 0 && tree.get(prev).isProper && (tree.get(prev).mention == null || tree.get(prev).mention.isPerson() || tree.get(prev).mention.categories.size() == 0)) prev--;
                while(next < tree.size() && tree.get(next).isProper && (tree.get(next).mention == null || tree.get(next).mention.isPerson()|| tree.get(next).mention.categories.size() == 0)) next++;
                
                prev++; next--;
                if (m.start > prev || m.end < next) {
                    Node head = getHead(prev, next);
                    if (head.mention == null) {
                        m.node.mention = null;
                        m.node = head;
                        head.mention = m;
                        m.start = prev;
                        m.end = next;
                        LVCoref.logger.fine(Utils.getMentionComment(this, m,"Tweaked person mention"));
                    } else if (head.mention != m) {
                        if (head.mention.isPerson()) remove = true;
                        else if (head.mention.node.isProper && head.mention.categories.size() == 0) {
                            head.mention.categories.add("person");
                            remove = true;
                        }
                        head.mention.start = Math.min(head.mention.start, m.start);
                        head.mention.end= Math.max(head.mention.end, m.end);
                        LVCoref.logger.fine(Utils.getMentionComment(this, head.mention,"Couldnt tweak person mention cause of "));
                    }
                }
            }
            if (remove) {
                removeMention(m);
                LVCoref.logger.fine(Utils.getMentionComment(this, m,"Removed nested person mention"));
            } else {
                mm.add(m);
            }            
        }
        mentions.clear();
        mentions = mm;
        normalizeMentions();
    }
    
    
    public void removeMention(Mention m) {
        m.node.mention = null;
        CorefCluster cluster = corefClusters.get(m.corefClusterID);
        if (cluster != null) {
            if (cluster.corefMentions.size() < 2) corefClusters.remove(m.corefClusterID);
            else cluster.corefMentions.remove(m);
        }
    }
    
    public void removePronounSingletonMentions() {
        List <Mention> mm = new ArrayList<Mention>();
        for (Mention m : mentions) {
            if (m.type == MentionType.PRONOMINAL && corefClusters.get(m.corefClusterID) != null && corefClusters.get(m.corefClusterID).corefMentions.size() < 2) {
                removeMention(m);
                 LVCoref.logger.fine(Utils.getMentionComment(this, m, "Removed pronoun singleton mention"));
            } else {
                mm.add(m);
            }
        }
        mentions.clear();
        mentions = mm;
        normalizeMentions();
    }
    
    public void removeExcludedMentions() {
        List <Mention> mm = new ArrayList<Mention>();
        for (Mention m : mentions) {
            if (dict.excludeWords.contains(m.node.lemma.toLowerCase())) {
                removeMention(m);
                 LVCoref.logger.fine(Utils.getMentionComment(this, m, "Removed excluded mention"));
            } else {
                mm.add(m);
            }
        }
        mentions.clear();
        mentions = mm;
        normalizeMentions();
    }
    
    
    public void removeUndefiniedMentions() {        
        Set<String> exclude = new HashSet<String>(Arrays.asList("viss", "cits", "dažs", "daudz", "maz", "cits",  "cita"));
        List <Mention> mm = new ArrayList<Mention>();
        for (Mention m : mentions) {
            boolean needExclude = false;
            if (m.number != Dictionaries.Number.SINGULAR) {
                for (Node c : m.node.children) {
                    if (exclude.contains(c.lemma)) {
                        needExclude = true;
                        break;
                    }
                }
            }
            if (needExclude) {
                removeMention(m);
                 LVCoref.logger.fine(Utils.getMentionComment(this, m,"Removed undefinied mention"));
            } else {
                mm.add(m);
            }
        }
        mentions.clear();
        mentions = mm;
        normalizeMentions();
    }
    
    
    
    
    
    public void removeGenitiveMentions() {
        List <Mention> mm = new ArrayList<Mention>();
        for (Mention m : mentions) {
            //if (m.type == MentionType.PRONOMINAL) continue;
            //if (m.type == MentionType.PROPER) continue;
            if ((/*m.type == MentionType.NOMINAL && */!m.bucket.equals("quote")&&!m.strict/*|| m.type != MentionType.PROPER*/) && m.mentionCase == Dictionaries.Case.GENITIVE && m.node.parent != null && m.node.parent.isNoun()) {
                removeMention(m);
                LVCoref.logger.fine(Utils.getMentionComment(this, m,"Removed genitive mention"));
            } else {
                mm.add(m);
            }
        }
        mentions.clear();
        mentions = mm;
        normalizeMentions();
    }
    
    
    
    public void sortMentions() {
        Collections.sort(mentions);
        normalizeMentions();
    }
    
    public void normalizeMentions() {
        for(int i = 0; i < mentions.size(); i++) {
           mentions.get(i).id = i;
        }
    }
    
    public void sortGoldMentions() {
        Collections.sort(goldMentions);
        normalizeGoldMentions();
    }
    
    public void normalizeGoldMentions() {
        for(int i = 0; i < goldMentions.size(); i++) {
           goldMentions.get(i).id = i;
        }
    }
        
    public void useGoldMentions() {
        for (Mention m : goldMentions) {
            Mention mm = new Mention(m);
            m.node.mention = mm;
            mentions.add(mm);
        }
    }
    
    public void useGoldClusters() {
        for (int ci: goldCorefClusters.keySet()) {
            CorefCluster cc = new CorefCluster(ci);
            for (Mention m: goldCorefClusters.get(ci).corefMentions) {
                Mention n = m.node.mention;
                if (n != null) {
                    cc.add(n);
                    n.corefClusterID = ci;
                } else {
                }
            }
            corefClusters.put(ci, cc);
        }
    }
    
    public void setMentionCategories() {
        for (Mention m : mentions) {
            if (m.categories.size() == 0) m.setCategories(this);
            if (m.categories.contains("other")) {
                m.categories.remove("other");
                m.setCategories(this);
            }
        }
    }
    
    
    public void initializeEntities() {
        //initialize new cluster for each mention
        for (Mention m: mentions) {
            if (m.corefClusterID == -1) {
                corefClusters.put(m.id, new CorefCluster(m.id));
                corefClusters.get(m.id).add(m);
                m.corefClusterID = m.id;
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
    
    
    public Node getCommonAncestor(Node n, Node m) {
        Set<Node> path = new HashSet<Node>(); //all path nodes traversed by going up
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
        List<Node> cand = new ArrayList<Node>();
        assert(start <= end);
        for (int i = start; i <= end; i++) {
            Node n = tree.get(i);
            if ((n.parent != null && ( n.parent.id < start || n.parent.id > end)) || n.parent == null) {
                cand.add(n);
            }
        }
        //System.out.println("["+start+ ".." +end +"] head candidates: " + cand);
        assert(cand.size() > 0);
        Node head = cand.get(cand.size()-1);//FIXME
        //Node head = cand.get(0);
        //needed if markable contains multiple head candidates
        //lowest common ancestor is returned
//        for (int i = 1; i < cand.size(); i++) {
//            head = getCommonAncestor(head, cand.get(i));
//        }

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
        }
        Map<Integer, String> corefColor = new HashMap<Integer, String>();
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
                    //+"<link rel='stylesheet' type='text/css' href='bootstrap/css/bootstrap.css'>"
                    +"<link rel='stylesheet' type='text/css' href='style.css'>"
                    //+"<script src=\"http://code.jquery.com/jquery-latest.min.js\" type=\"text/javascript\"></script>\n"
                    //+"<script src=\"bootstrap/js/bootstrap.min.js\" type=\"text/javascript\"></script>\n"
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
                        out.println("<span class='label label-"+mentions.get(n.mentionStartList.get(mid)).category+"' id='s"+n.mentionStartList.get(mid)+"'> [ ");
                    }
                    
                    
                    
                    if (n.mention != null) { 
                        String notGoldError = "";
                        if (LVCoref.props.getProperty(Constants.GOLD_MENTIONS_PROP, "").length() > 0 && n.goldMention == null)
                                 notGoldError = " notGold";
                        if (n.mention != null && corefClusters.get(n.mention.corefClusterID).corefMentions.size() > 1) {
                            Mention ant = refGraph.getFinalResolutions().get(n.mention);
                            out.print(" <span "
                                        + "class='coref"+notGoldError+" '"
                                        + "style='background-color:#"+corefColor.get(n.mention.corefClusterID)+";'"
                                        + "id='"+n.mention.id+"' "
                                        + "title='"
                                            + "@cID=" + n.mention.corefClusterID
                                            + " @mID=" + n.mention.id
                                            + " @gender=" + n.mention.gender
                                            + " @number=" + n.mention.number
                                            + " @case=" + n.mention.mentionCase
                                            + " @POS=" + n.tag+":"+n.lemma    
                                            + " @properNode=" + n.isProper 
                                            + " @wID=" + n.id
                                            + " @antID="+((ant == null)?null:ant.id)
                                            + " @type="+n.mention.type
                                            + " @cat="+n.mention.categories + ":"+n.mention.category
                                            + " @resoInfo="+n.mention.comments+"]"
                                            + " @span=["+n.nodeProjection(this) +"]"
                                            + " @startM="+n.mention.start
                                            + " @endM="+n.mention.end
                                            + " @string="+n.mention.nerString
                                            + " @normalized="+n.mention.normString
                                            + " @words="+n.mention.words
                                            + " @modifiers="+n.mention.modifiers
                                            + " @ProperMod="+n.mention.properModifiers
                                            + " @acro="+n.mention.getAcronym(this)
                                            + " @type=" +n.mention.bucket
                                    + "'>"
                                    + " <em class='c"+n.mention.corefClusterID+"'>"+" " + n.word+"</em>"
                                    + "["+n.mention.corefClusterID+"]"
                                + "</span>"/*+n.mention.categories*/);

                        } else if (n.mention != null) {
                            out.print(" <span "
                                       + "class='singleton "+notGoldError+"'"
                                        + "title='"
                                            + " @mID="+n.mention.id+ " @gender=" + n.mention.gender
                                            + " @number=" + n.mention.number
                                            + " @case=" + n.mention.mentionCase
                                            + " @POS=" + n.tag+":"+n.lemma 
                                            + " @properNode=" + n.isProper 
                                            + " @wID=" + n.id
                                            + " @type="+n.mention.type
                                            + " @cat"+n.mention.categories + ":"+n.mention.category
                                            + " @span=["+n.nodeProjection(this) +"]"
                                            + " @string="+n.mention.nerString
                                            + " @normalized="+n.mention.normString
                                            + " @words="+n.mention.words
                                            + " @modifiers="+n.mention.modifiers
                                            + " @ProperMod="+n.mention.properModifiers
                                            + " @acro="+n.mention.getAcronym(this)
                                            + " @type=" +n.mention.bucket
                                        +"'>"
                                    + "<em>" + n.word+"</em>"
                                + "</span>");
                        }
                    } else {
                        
                        String notPredictedError = "";
                        if (LVCoref.props.getProperty(Constants.GOLD_MENTIONS_PROP, "").length() < 1 && n.goldMention != null)
                                 notPredictedError = " notPredicted";
                        
                        out.print(" <span "
                                + "class='"+notPredictedError+"'"
                                + "title='"
                                + " @POS=" + n.tag+":"+n.lemma    
                                + " @wID=" + n.id
                                + " @span=["+n.nodeProjection(this) +"]"
                                + " @properNode=" + n.isProper      
                                + " @relative=" + n.isRelativeClaus()
                            +"'>"
                                + n.word 
                            + "</span>");
                    }
                    
                    //if (n.mentionEndList.size()>0) out.print(n.mentionEndList);
                    count = n.mentionEndList.size();
                    for (int mid = 0; mid < count; mid++) {
                        out.println(" ] </span>");
                        //out.println(" <span class='badge'>"+n.mention.corefClusterID+"</span></span>");
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
                    out.println(c.modifiers);
                    for (Mention m : c.corefMentions) {
                        String projection = m.nerString;//= m.node.nodeProjection(this);
                        projection = projection.replace(m.node.word, "<b>"+m.node.word+"</b>");
                        
                        out.println
                                ("<div>"
                                + ((LVCoref.doScore()&&m.node.goldMention!=null)?"["+m.node.goldMention.goldCorefClusterID+"]":"")
                                + "<a href='#"+m.id+"'>"
                                + projection
                                + "</a>"
                                + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp"+m.categories+"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;("+m.getContext(this, 4)+")"
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
    
    
    public CorefCluster getCluster(int i) {
        return corefClusters.get(i);
    }
    
    public Node getNode(int i) {
        if (i >= 0 && i < tree.size()) return tree.get(i);
        return null;
    }
    
}