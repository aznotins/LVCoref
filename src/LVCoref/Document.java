package LVCoref;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import lv.lumii.expressions.Expression;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.w3c.dom.NodeList;

import LVCoref.Dictionaries.MentionType;
import LVCoref.Mention.MentionSource;
import LVCoref.util.Log;
import edu.stanford.nlp.util.StringUtils;

/**
 * Document class contains document parse tree structure, 
 * mentions, coreferences and other information
 * @author arturs
 */
public class Document {
    
	private static final Logger log = Logger.getLogger( Log.class.getName() );
	//static { log.setLevel(Level.OFF); }
	
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
        properWords = new HashSet<String>();
        acronyms = new HashMap<String, Node>();
	}
    
    Document(Dictionaries d) {
        tree = new ArrayList<Node>();
		mentions = new ArrayList<Mention>();
        goldMentions = new ArrayList<Mention>();
        dict = d;
        refGraph = new RefGraph();
        corefClusters = new HashMap<Integer, CorefCluster>();
        goldCorefClusters = new HashMap<Integer, CorefCluster>();
        sentences = new ArrayList<Integer>();
        properWords = new HashSet<String>();
        acronyms = new HashMap<String, Node>();
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

    /**
     * Set conll coreference columns. Needs category to be set.
     */
    public void setConllCorefColumns() {
        for (Node n : tree) {
            if (n.mention != null) {
                n.conll_fields.add(Integer.toString(n.mention.corefClusterID));
                String category = getCluster(n.mention.corefClusterID).category;
//                if (n.mention.categories.size() > 0 && n.mention.category != null) {
//                	//n.conll_fields.add(Utils.implode(n.mention.categories, "|"));
//                	n.conll_fields.add(n.mention.category);
//                }
                if (category != null) {
                	n.conll_fields.add(category);
                }
                else n.conll_fields.add("_");
            } else {
                n.conll_fields.add("_");
                n.conll_fields.add("_");
            }
            if (Constants.EXTRA_CONLL_COLUMNS) {
                if (n.mention != null) n.conll_fields.add(n.mention.type.toString()); else n.conll_fields.add("_");
                n.conll_fields.add(n.getConllMentionColumn(this, true));
                n.conll_fields.add(n.ne_annotation);
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
    	log.fine("Read Conll " + filename);
		BufferedReader in = null;        
        in = new BufferedReader(new FileReader(filename));		
        readCONLL(in);
        in.close();
	}
    
    
    public void readCONLL(BufferedReader in) throws Exception {
        log.fine("Read conll stream");        
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
				String form = fields[1];
				String lemma = fields[2];
				String tag = fields[4];	
                String simpleTag = fields[3];
                String morphoFeatures = fields[5];
                if (simpleTag.length() == 0 && tag.length() > 0) simpleTag = tag.substring(0, 1); // fix empty tag
                int position = Integer.parseInt(fields[0]);
                int parent_in_sentence = Integer.parseInt(fields[6]);
                String ner_label = "O";
                if (fields.length >= 8) ner_label = fields[7];
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
				Node node = new Node(form, lemma, tag, parent, node_id, this);
                int columnCount = Math.min(fields.length, Constants.savedColumnCount);
                node.conll_fields.addAll(Arrays.asList(fields).subList(0, columnCount));
                node.simpleTag = simpleTag;
                node.parentIndex = parent_in_sentence;
                node.morphoFeatures = morphoFeatures;
                node.position = position;
                node.sentNum = sentence_id;
                node.ne_annotation = ner_label;
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
    
    public void readJSON(BufferedReader in) throws Exception {
    	log.fine("Read json stream");  
		int node_id = 0;
        int sentence_id = 0;
		int sentence_start_id = 0;
        
        StringBuilder builder = new StringBuilder();
        for (String line = null; (line = in.readLine()) != null;) {
            if (line.trim().length() == 0) break;
            builder.append(line).append("\n");
        }
        String jsonString = builder.toString();
        if (jsonString.length() == 0) return; //Empty document
        try {
            JSONObject jsonData = (JSONObject)JSONValue.parse(builder.toString());
            if (jsonData == null) throw new Exception("Empty document");

            JSONArray sentencesArr = (JSONArray) jsonData.get("sentences");
            if (sentencesArr == null) throw new Exception("Sentences key not set");
            for (int s_id = 0; s_id < sentencesArr.size(); s_id++) {
                JSONObject sentence = (JSONObject)sentencesArr.get(s_id);
                JSONArray tokens = (JSONArray) sentence.get("tokens");
                for (int n_id = 0 ; n_id < tokens.size(); n_id++) {
                    JSONObject token = (JSONObject)tokens.get(n_id);
                    String form = token.containsKey("form") 
                    		? token.get("form").toString() : "_";
                    String lemma = token.containsKey("lemma") 
                    		? token.get("lemma").toString() : "_";                    
                    String tag = token.containsKey("tag") 
                    		? token.get("tag").toString() : "_";
                    String simpleTag = token.containsKey("pos") 
                    		? token.get("pos").toString() : tag.substring(0, 1);
                    String morphoFeatures = token.containsKey("features") 
                    		? token.get("features").toString() : "_";
                    Integer position = token.containsKey("index") 
                    		? Integer.parseInt(token.get("index").toString()) : -1;
                    Integer parent_in_sentence = token.containsKey("parentIndex") 
                    		? Integer.parseInt(token.get("parentIndex").toString()) : -1;
                    String ner_label = "O";
                    if (token.containsKey("namedEntityType")) {
                    	ner_label = (String)token.get("namedEntityType");
                    }
                    Integer namedEntityID = token.containsKey("namedEntityID") 
                    		? Integer.parseInt(token.get("namedEntityID").toString()) : -1;
                    String idType = token.containsKey("idType") 
                    		? token.get("idType").toString() : "";
                    if (position == 1) {
                        if ((sentence_start_id != node_id)) {
                            sentences.add(sentence_start_id);
                            tree.get(node_id-1).sentEnd = true;
                            sentence_start_id = node_id;
                            sentence_id++;
                        }
                    }          
                    int parent = parent_in_sentence + sentence_start_id - 1;
                    Node node = new Node(form, lemma, tag, parent, node_id, this);
                    node.simpleTag = simpleTag;
                    node.morphoFeatures = morphoFeatures;
                    node.parentIndex = parent_in_sentence;
                    node.conll_fields.addAll(Arrays.asList(position.toString(), 
                    		form, lemma, simpleTag, tag, morphoFeatures, 
                    		parent_in_sentence.toString()));
                    node.position = position;
                    node.sentNum = sentence_id;
                    node.ne_annotation = ner_label;
                    node.namedEntityID = namedEntityID;
                    node.idType = idType;
                    if (position == 1) { node.sentStart = true; }
                    if (parent_in_sentence == 0) node.sentRoot = true;
                    tree.add(node);
                    node_id++;
                }
            }
            if (sentence_start_id != node_id) {
                sentences.add(sentence_start_id);
                tree.get(node_id-1).sentEnd = true;
            }
            initializeNodeTree();
            initializeBaseCoreference();
            
        } catch (Exception e) {
            System.err.println("Error parsing input json data");
            e.printStackTrace(System.err);
        }
	}

    
    /**
     * Initialize original mentions and coreferences provided from json document
     */
    public void initializeBaseCoreference() {
    	for (Node n : tree) {
    		if (n.namedEntityID > 0) {
    			int start = n.getSpanStart(this).id;
    			int end = n.getSpanEnd(this).id;
    			Mention m = setMention(start, end);
    			if (m != null) {
    				m.source = MentionSource.BASE;
    				if (corefClusters.containsKey(n.namedEntityID)) {
    					CorefCluster c = new CorefCluster(this, -1);
    					c.add(m);
    					m.corefClusterID = -1;
    					corefClusters.put(-1, c); // this cluster should be deleted in merging
    					mergeClusters(c, corefClusters.get(n.namedEntityID));
    				} else {
    					CorefCluster c = new CorefCluster(this, n.namedEntityID);
    					c.add(m);
    					m.corefClusterID = n.namedEntityID;
    					corefClusters.put(n.namedEntityID, c);
    				}
    				//System.err.println("New base mention " + m);
    			} else {
    				//System.err.println("Couldn't set mention: " + n);
    			}
    		}
    	}
    }
    
    public void useGoldClusters() {
        for (int ci: goldCorefClusters.keySet()) {
            CorefCluster cc = new CorefCluster(this, ci);
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
    
    /**
     * For each mention create new cluster
     */
    public void initializeEntities() {
        //initialize new cluster for each mention
    	int id = maxClusterID() + 1;
        for (Mention m: mentions) {
            if (m.corefClusterID == -1) {
                corefClusters.put(id, new CorefCluster(this, id));
                corefClusters.get(id).add(m);
                m.corefClusterID = id;
                id++;
            }            
        }
    }
    
    /**
     * Put all mentions from m cluster to n
     * @param m Mention
     * @param n Mention
     * @return true if cluster were changed
     */
    public boolean mergeClusters(Mention m, Mention n) {
    	if (m == null || n == null) {
    		System.err.println("Null mention");
    		return false;
    	}
    	if (!corefClusters.containsKey(m.corefClusterID) 
    			|| !corefClusters.containsKey(n.corefClusterID)) {
    		System.err.println("Clusters not set for merged mentions");
    		return false;
    	}
    	return mergeClusters(corefClusters.get(m.corefClusterID), 
    			corefClusters.get(n.corefClusterID));
    }
    
    public boolean mergeClusters(CorefCluster m, CorefCluster n) {
    	if (m == null || n == null) {
    		System.err.println("Null coref cluster");
    		return false;
    	}
        if (m != n) {
            int removeID = m.id;
        	//System.err.printf("Merge: %d > %d; remove %d\n", m.id, n.id, removeID);
            Set<Mention> cm = m.corefMentions;
            for (Mention mm : cm) {
                n.add(mm);
                mm.corefClusterID = n.id;
            }
            corefClusters.remove(removeID);
            return true;
        } else {
            return false;
        }
    }
    
    public int maxClusterID() {
        int k = 0;
        for (int i: corefClusters.keySet()) {
            if (i > k) k = i;
        }
        return k;
    }    
    
    @SuppressWarnings("unchecked")
	public void outputJSON(PrintStream out) {
        JSONArray sentencesArr = new JSONArray();
        for (int sentID = 0; sentID < sentences.size(); sentID++) {
            JSONObject sentenceObj = new JSONObject();
            int sentStart = sentences.get(sentID);
            JSONArray tokensArr = new JSONArray();
            for (int tokenID = sentStart; ; tokenID++) {
                Node node = tree.get(tokenID);
                JSONObject token = new JSONObject();
                token.put("index", node.position);
                token.put("form", node.word);
                token.put("lemma", node.lemma);
                token.put("pos", node.simpleTag);
                token.put("tag", node.tag);
                token.put("features", node.morphoFeatures);
                token.put("parentIndex", node.parentIndex);
                if (node.ne_annotation != null && node.ne_annotation != "O") token.put("namedEntityType", node.ne_annotation);
                if (node.mention != null) token.put("namedEntityID", node.mention.corefClusterID);
                tokensArr.add(token);
                if (node.sentEnd) break;
            }
            sentenceObj.put("tokens", tokensArr);
            sentenceObj.put("text", sentenceText(sentID));
            sentencesArr.add(sentenceObj);
        }
        
        JSONObject NEMap = new JSONObject();
        for (int cID : corefClusters.keySet()) {
            CorefCluster cluster = corefClusters.get(cID);
            JSONObject NEObj = new JSONObject();
            NEObj.put("id", cID);
            
            Set<String> aliases = new HashSet<String>();
            
            for (Mention m : cluster.corefMentions) {
            	if (m.type == MentionType.PRONOMINAL) continue; // Vietniekvārdus aliasos neliekam
            	Expression e;
				try {
					e = new Expression(m.nerString, m.category, false);
					String normalised = e.inflect("Nominatīvs");
	            	if (normalised != null) aliases.add(normalised);
	            	else aliases.add(m.nerString);   
				} catch (Exception e1) {
					e1.printStackTrace();
				}
                //System.err.printf("head:%s ner:%s\n", m.headString, m.nerString);
            }
            //System.err.println(aliases.toString());
            String category = cluster.category != null ? cluster.category : "null"; // labāk kā representative.category
            JSONArray aliasesArr = new JSONArray();
            aliasesArr.addAll(aliases);            
            NEObj.put("aliases", aliasesArr);
            NEObj.put("type", category);
            // if (category == null) System.err.println("Empty cluster category " + cluster.representative);
            if (cluster.representative.titleRepresentative()) NEObj.put("isTitle", 1);
            JSONObject oInflections = new JSONObject();
            String representative = cluster.representative.nerString;;
            try {
            	Expression e = new Expression(cluster.representative.nerString, category, false);
            	Map<String,String> inflections = e.getInflections();
            	//System.err.printf("Saucam getInflections vārdam '%s' ar kategoriju '%s'\n", cluster.representative.nerString, cluster.firstMention.category);
            	for (String i_case : inflections.keySet()) {
            		oInflections.put(i_case, inflections.get(i_case));
            	}
            	representative = e.inflect("Nominatīvs");
            	//System.err.printf("Locījām frāzi '%s' ar kategoriju '%s', dabūjām '%s'\n", cluster.representative.nerString, cluster.firstMention.category, representative);
			} catch (Exception e) {
				e.printStackTrace();
			}
            if (representative == null) representative = cluster.representative.nerString;
            NEObj.put("inflections", oInflections);
            NEObj.put("representative", representative);
            NEMap.put(cID, NEObj);
        }
        
        JSONObject doc = new JSONObject();
        doc.put("document", new JSONObject());
        doc.put("sentences", sentencesArr);
        doc.put("namedEntities", NEMap);
        
        out.println(doc.toString());
        out.flush();
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
                
                
                //if (category.equals("profession")) category = "person";
                //if (category.equals("event")) continue;
                //if (category.equals("product")) continue;
                //if (category.equals("media")) continue;
                //if (category.equals("time")) continue;
                //if (category.equals("sum")) continue;
                
                //if (head.tag.charAt(0) == 'p') continue;
                
                //currently supports only one mention per node as head
                if (head.goldMention == null) {
                    Mention goldMention = new Mention(this, i, head , start, end); // @FIXME
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
                        goldCorefClusters.put(cluster_id, new CorefCluster(this, cluster_id));
                    } else {
                        cluster_id = classToInt.get(cluster);
                        if (cluster_id == null) {
                            cluster_id = cluster_c++;
                            classToInt.put(cluster, cluster_id);
                            goldCorefClusters.put(cluster_id, new CorefCluster(this, cluster_id));
                        }
                    }
                    goldCorefClusters.get(cluster_id).add(goldMention);
                    goldMention.goldCorefClusterID = cluster_id;
                    

                    log.fine("goldCluster #" + cluster_id +"(size="+goldCorefClusters.get(cluster_id).corefMentions.size()+")" + " " + getSubString(start, end) +" : \""+ head.nodeProjection(this)+ "\"");
                } else {
                    log.fine("Could not add mention because of same head: " + getSubString(start, end));
                }
                
            }
            sortGoldMentions();   
        } catch (Exception e) {
            System.err.println("Error adding MMAX2 annotation:" + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
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
   
    
    public void setTmpMentions() {
        int mention_id = mentions.size();
        for (Node node : tree) {
            if (node.mention == null) {
                if (node.tag.charAt(0) == 'n' || node.tag.charAt(0) == 'p') {
                    if (!dict.excludeWords.contains(node.lemma)) {                        
                        node.isMention = true;
                        Mention m = new Mention(this, mention_id++, node, node.id, node.id);
                        mentions.add(m);
                        node.mention = m;
                        m.source  = MentionSource.TMP;
                        log.fine(Utils.getMentionComment(this, m, "Set tmp mention"));
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
                Mention m = new Mention(this, mention_id++, node, node.id, node.id);
                m.source = MentionSource.LIST;
                m.strict = true;
                mentions.add(m);
                node.mention = m;
                m.category = cat;
                m.categories.add(cat);
                log.fine(Utils.getMentionComment(this, m, "Set list mention"));
            }
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
                Mention m = new Mention(this, mention_id++, node, node.id, node.id);
                mentions.add(m);
                node.mention = m;
                node.mention.source = MentionSource.PROPERNODES;
                log.fine(Utils.getMentionComment(this, m, "Set proper node mention"));
            }
            else if (node.isProper() && node.mention != null && node.mention.type == MentionType.NOMINAL) {
                node.mention.type = MentionType.PROPER;
                log.fine(Utils.getMentionComment(this, node.mention, "Set proper node mention (update to proper)"));
            }
        }
    }
    
    public void setMentions() {
        int mention_id = mentions.size();
        for (Node node : tree) {
            if (node.mention == null && (node.isNoun() || node.isPronoun())) {
                node.isMention = true;
                Mention m = new Mention(this, mention_id++, node, node.id, node.id);
                m.source = MentionSource.ALLNODES;
                mentions.add(m);
                node.mention = m;
                log.fine(Utils.getMentionComment(this, m, "Set naive mention"));
            }
        }
    }
    
    
    public void setDetalizedNominalMentions() {
        int mention_id = mentions.size();
        for (Node node : tree) {
            if (node.isProper() && node.isNounGenitive() && node.parent != null && node.parent.mention == null && node.parent.isNoun()) {
                node.parent.isMention = true;
                Mention m = new Mention(this, mention_id++, node.parent, node.parent.id, node.parent.id);
                m.source = MentionSource.DETALIZEDNODES;
                mentions.add(m);
                node.parent.mention = m;
                log.fine(Utils.getMentionComment(this, m, "Set detalized nominal mention"));
            }
        }
    }
    
    public void setMentionsFromNEAnnotation() {
        int start = 0;
        String cur_cat = "O";
        Boolean isMention = false;
        String cat;
        Set<String> proper_cat = new HashSet<String>(Arrays.asList("person", "organization", "location", "product", "media"));
        Set<String> filter_cat = new HashSet<String>(Arrays.asList("person", "organization", "location", "product", "media", "time", "sum", "profession", "event", "O"));
        int id = 0;
        
        for (id = 0; id < tree.size(); id++) {
        	Node n = getNode(id);
            cat = n.ne_annotation;
            if (cur_cat.equals("O") && !cat.equals("O")) {
            	// Start reading new ner phrase
            	cur_cat = cat;
            	start = id;
            } else if (!cur_cat.equals("O") && (!cur_cat.equals(cat) || n.sentStart)) {
            	// Create new mention and possibly start reading new ner phrase
            	Mention m = null;
                int end = n.id-1;
                if (tree.get(start).isQuote()) {
                	start++; //System.err.println("Removed ner start quote :"  + getSubString(start, end));
                }                    
                if (tree.get(end).isQuote()) {
                	end--; //System.err.println("Removed ner end quote :"  + getSubString(start, end));
                }
                if (proper_cat.contains(cur_cat)) {
                	m = setMention(start, end, cur_cat, MentionType.PROPER);
                }
                else if (filter_cat.contains(cur_cat)) {
                	m = setMention(start, end, cur_cat, MentionType.NOMINAL);
                }
                else {
                    log.warning("NER Unsupported category @" + cur_cat);
                }
                if (m != null) {
                    m.strict = true;
                    m.source = MentionSource.NER;
                    log.fine("NER Mention " + m);
                    // System.err.println("NER Mention " + m);
                }
                
                cur_cat = cat;
                start = id;
            } else if (cur_cat.equals(cat) && !cur_cat.equals("O") && !n.sentStart) {
            	// Continue reading ner phrase
            }
        }
        if (!cur_cat.equals("O")) {
        	// Set document last mention
        	Mention m = null;
            int end = id-1;
            if (tree.get(start).isQuote()) {
            	start++; //System.err.println("Removed ner start quote :"  + getSubString(start, end));
            }                    
            if (tree.get(end).isQuote()) {
            	end--; //System.err.println("Removed ner end quote :"  + getSubString(start, end));
            }
            if (proper_cat.contains(cur_cat)) {
            	m = setMention(start, end, cur_cat, MentionType.PROPER);
            }
            else if (filter_cat.contains(cur_cat)) {
            	m = setMention(start, end, cur_cat, MentionType.NOMINAL);
            }
            else {
            	log.warning("NER Unsupported category @" + cur_cat);
            }
            if (m != null) {
                m.strict = true;
                m.source = MentionSource.NER;
                log.fine("NER Mention " + m);
               // System.err.println("NER Mention " + m);
            }
        }
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
                            	log.fine("Quote Mention :("+i+" " + j+") " + getSubString(i+1, j-1));
                                //assert(i+1 <= j-1);
                                Mention m = setMention(i+1, j-1, "", MentionType.PROPER);                                
                                if (m != null) {
                                    m.categories.addAll(dict.getCategories(m.node.lemma));
                                    log.fine(Utils.getMentionComment(this, m, ""));
                                    m.source = MentionSource.QUOTE;
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
    
    /**
     * Creates new mention and add to the mention list
     * @param from
     * @param to
     * @param cat can be null or empty
     * @param t
     * @return created Mention or null on error
     */
    public Mention setMention(int from, int to, String cat, MentionType t) {
        Mention x = getMention(from, to);
        if (x != null) {
        	if (cat != null && cat.trim().length() > 0) {
                x.categories.add(cat);
                x.category = cat;
            }
        	if (t != null && t != MentionType.UNKNOWN) x.type = t;
        	mentions.add(x);
        	x.node.mention = x;
        }
        return x;
    }
    public Mention setMention(int from, int to) {
    	return setMention(from, to, null, null);
    }
    
    /**
     * Try to create new mention
     * @param from
     * @param to
     * @param t
     * @return Mention on success, null on failure
     */
    public Mention getMention(int from, int to) {
        Node head = getHead(from, to);
        if (head.mention == null || head.mention.source == MentionSource.BASE) {
        	head.mention = null;
            //System.out.println("Mention \""+getSubString(from, to)+"\" head="+head.word);
        	log.fine("Mention \""+getSubString(from, to)+"\" head="+head.word);
            int id = mentions.size();
            Mention m = new Mention(this, id, head, from, to);
            m.start = from;
            m.end = to;
            m.root = head.id;
            return m;
        } else {
            //System.err.println("setMention() mention with this head already set: " + "old=" + head.mention.nerString + " new=" + getSubString(from, to));
        	log.fine("setMention() mention with this head already set: " + "old=" + head.mention.nerString + " new=" + getSubString(from, to));
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
                    Mention m = setMention(n.id, n.id, "organization", MentionType.PROPER); // TODO nav tas labākais, taču vispār NER jau vajadzētu šo nomarķēt
                    if (m != null) {
                        m.strict = true;                        
                        log.fine(Utils.getMentionComment(this, m, "Set abbreviation mention"));
                        m.source = MentionSource.ABBREVIATION;
                    } else {
                    	log.fine("Couldn't create abbreviation mention: " + n.word + "("+ n.id +")");
                    }
                }
            }
        }
    }
    
    /**
     * Remove nested mentions that are not strict.
     */
    public void removeNestedMentions() {
        int max_depth = 1;
        for (Mention m : mentions) {
            int l = 0;
            Node n = m.node.parent;
            Boolean nested = false;
            while (l++ < max_depth && n != null && m.node.sentNum == n.sentNum) {
                if (n.mention != null && n.id > m.node.id && (m.source != MentionSource.NER)) {
                    //remove m
                    n.mention.start = Math.min(n.mention.start, m.start);
                    nested = true;
                    break;
                }
                n = n.parent;
            }
            if (nested) {
                removeMention(m);
                log.fine(Utils.getMentionComment(this, m, "Removed nested mention"));
            }
        }
        normalizeMentions();
    }
    
    public void removeNestedQuoteMentions() {
        for (Mention m : mentions) {
            if (m.source == MentionSource.QUOTE) {
                for (int i = m.start; i <= m.end; i++) {
                    Node q = tree.get(i);
                    if (q.mention != null && q.mention != m) {
                    	removeMention(m);
                    	log.fine(Utils.getMentionComment(this, m, "Removed nested quote mention"));
                        //System.err.println("REMOVED QUOTE " + q.mention.nerString);
                    }
                }
            }
        }
        normalizeMentions();
    }
    
    
    public void removePluralMentions() {
        for (Mention m : mentions) {
            if (m.number == Dictionaries.Number.PLURAL) {
                removeMention(m);
                log.fine(Utils.getMentionComment(this, m, "Removed Plural mention"));
            }
        }
        normalizeMentions();
    }
    
    public void removeTmpMentions() {
        for (Mention m : mentions) {
            if (m.source == MentionSource.TMP) {
                removeMention(m);
                log.fine(Utils.getMentionComment(this, m, "Removed TMP mention"));
            }
        }
        normalizeMentions();
    }
    
    public void removePleonasticMentions() {
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
                log.fine(Utils.getMentionComment(this, m, "Removed Pleonastic mention"));
            }
        }
        normalizeMentions();
    }
    
    public void removeSingletonMentions() {
        for (Mention m : mentions) {
            if (corefClusters.get(m.corefClusterID) != null && corefClusters.get(m.corefClusterID).corefMentions.size() < 2) {
                removeMention(m);
                log.fine(Utils.getMentionComment(this, m, "Removed singleton mention"));
            }
        }
        normalizeMentions();
    }
    
    //FIXME
    //add unset attribules
    public void procesMentions() {
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
            	log.fine(Utils.getMentionComment(this, m, "Added mention modifiers "+"("+getSubString(left, m.node.id)+ ")"));
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
                    && (n.isNounGenitive() && (n.isProper() /*|| n.mention != null*/) || (n.isProperAdjective() || n.isDefiniteAdjective() || n.isNumber())
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
            	log.fine(Utils.getMentionComment(this, m, "Added mention modifiers "+"("+getSubString(left, m.node.id)+ ")"));
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
         if (m.source == MentionSource.QUOTE) {
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
                if (before != null && before.mention != null && before.mention.source == MentionSource.QUOTE) {
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
        
    
    /**
     * Remove references to mention (tree and clusters). But it should be
     * deleted from mention list.
     * @param m
     */
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
                log.fine(Utils.getMentionComment(this, m, "Removed pronoun singleton mention"));
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
                log.fine(Utils.getMentionComment(this, m, "Removed excluded mention"));
            } else {
                mm.add(m);
            }
        }
        mentions.clear();
        mentions = mm;
        normalizeMentions();
    }
    
    /**
     * Remove undefined mentions
     */
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
                log.fine(Utils.getMentionComment(this, m,"Removed undefinied mention"));
            } else {
                mm.add(m);
            }
        }
        mentions.clear();
        mentions = mm;
        normalizeMentions();
    }
    
    /**
     * Remove nested genetive mentions
     */
    public void removeGenitiveMentions() {
        for (Mention m : mentions) {
            //if (m.type == MentionType.PRONOMINAL) continue;
            //if (m.type == MentionType.PROPER) continue;
            if ((m.source != MentionSource.QUOTE && !m.strict) && m.mentionCase == Dictionaries.Case.GENITIVE && m.node.parent != null && m.node.parent.isNoun()) {
                removeMention(m);
                log.fine(Utils.getMentionComment(this, m,"Removed genitive mention"));
            }
        }
        normalizeMentions();
    }
    
    /**
     * Sort mentions in document order
     */
    public void sortMentions() {
        Collections.sort(mentions);
    }
    
    /**
     * Normalize mentions - remove deleted mentions (not referenced by nodes)
     */
    public void normalizeMentions() {
    	sortMentions(); // not always needed
    	int id = 0;
    	List <Mention> mm = new ArrayList<Mention>();
    	for (Mention m : mentions) {
    		if (m.node.mention != m) {
    			// ignore because already removed
    		} else {
    			mm.add(m);
    			m.id = id++;
    		}
    	}
    	mentions.clear();
    	mentions = mm;
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
        Node head;
        if (cand.size() == 0) {
        	// System.err.println("["+start+ ".." +end +"] head candidates: " + cand);
        	head = tree.get(end); // FIXME - teorētiski šeit nevajadzētu nonākt, bet nonāk; varbūt node.id lauki ir nepareizi? 
        } else head = cand.get(cand.size()-1);//FIXME
//        Node head = cand.get(0);
//        needed if markable contains multiple head candidates
//        lowest common ancestor is returned
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
            for (Integer i = 0; i < tree.size() ;i++) { 
                Node n = tree.get(i);
                if (n.sentStart) out.println("<div class='sentence'>"); 
                
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
                                        + " @acro="+n.mention.getAcronym()
                                        + " @source=" +n.mention.source
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
                                        + " @acro="+n.mention.getAcronym()
                                        + " @source=" +n.mention.source
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
                if (n.sentEnd)  {
                    if (root != null) { out.println(" <div class='parsetree' style='display:none;'>"+nodeSubTree(root)+"</div>"); }
                    out.println("</div> <!--sentence-->");
                }
            }

            out.print("</p>");
            
            for (Integer c_i : this.corefClusters.keySet()) {
                CorefCluster c = this.corefClusters.get(c_i);
                
                if (c.corefMentions.size() > 0) {
                    out.println(Utils.val("<div class='mentionCluster'>"));
                    if (c.representative.titleRepresentative()) {
                        out.println(Utils.bval(c.representative.nerString));
                        out.println(Utils.bval(c.representative.headString));
                    }
                    
                    out.println(Utils.val("========== " + c_i + " =========="));
                    out.println(Utils.keyValue("Representative", c.representative.id));
                    out.println(Utils.keyValue("Representative", c.representative.nerString));
                    out.println(Utils.keyValue("Heads", c.heads));
                    out.println(Utils.keyValue("Modifiers", c.modifiers));
                    out.println(Utils.keyValue("Words", c.words));
                    for (Mention m : c.corefMentions) {
                        //logger.finer("mention-> id:"+m.id+"\tspan: "+/*m.originalRef+*/"\t"+m.node.nodeProjection(m.document) +"\tsentNum: "+m.sentNum+"\tstartIndex: "+m.start);
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
    
    public CorefCluster getCluster(int i) {
        return corefClusters.get(i);
    }
    
    public Node getNode(int i) {
        if (i >= 0 && i < tree.size()) return tree.get(i);
        return null;
    }
    
    
    @SuppressWarnings("unused")
	public String sentenceText(int idx) {
        StringBuilder sb = new StringBuilder();
        Set<String> noGapBefore = new HashSet<String>(Arrays.asList(".", ",", ":", ";", "!", "?", ")", "]", "}", "%"));
        Set<String> noGapAfter =  new HashSet<String>(Arrays.asList("(", "[", "{"));
        Set<String> quoteSymbols =  new HashSet<String>(Arrays.asList("'", "\""));
        
        int s_start = sentences.get(idx);
        boolean gap = false;
        for (int t_id = s_start; ; t_id++) {
            Node node = getNode(t_id);
            sb.append(node.word);
            if (node.sentEnd) {
                break;
            } else {
                sb.append(" ");
            }
            // TODO uzlabot teksta veidošanu no sadalītiem tokeniem
        }
        return sb.toString();
    }   
    
    public void printNodes() {
        for (Node n: tree) {
            System.err.println(n);
        }
    }
    
    public void printMentions() {
    	Log.p("MENTIONS:");
    	for (Mention m : mentions) {
    		Log.p(m.source + "\t" + m + "\t" + m.categories);
    		//System.err.println("#" +m.id + "\t" + m.node.word + "\t" + m.type+ "\t" + m.category + " ^"+m.node.parent);
    	}
    }
    
    public void printClusterRepresentatives() {
    	Log.p("CLUSTERS - representatives:");
    	for (int i : corefClusters.keySet()) {
    		CorefCluster c = getCluster(i);
    		if (c.representative.titleRepresentative()) {
    			Log.p(String.format("[%s] [%s]", c.representative.nerString, c.representative.category));
    		}
    	}
    }
    
    public void printClusters() {
    	Log.p("CLUSTERS:");
        for (Integer i : corefClusters.keySet()) {
        	CorefCluster c = corefClusters.get(i);
            if ( c.corefMentions.size() > 1){
            	Log.p(String.format("---C"+i+"--- [%s/%s] [%s]", 
            			c.category,
            			c.representative.category,
            			c.representative.nerString));
                for (Mention m : c.corefMentions) {
                	Log.p(String.format(
                			"%s(%s) %s [%s] ", 
                			m.node.word, m.node.tag,
                			m.categories.toString(),
                			StringUtils.trim(m.nerString + " |", 40)));
                }
            }
        }
    }
    
    public void printSimpleText(PrintStream s) {
    	for (Node n : tree) {
    		for (int i = 0; i < n.mentionStartList.size(); i++) s.print("[");
    		s.print(n.word); s.print("#"+n.id);
    		s.print(" ");
    		for (int i = 0; i < n.mentionEndList.size(); i++) {
    			if (n.mention != null) {
    				s.print("@");
    				s.print(n.mention.category!=null? n.mention.category : "null");
    				s.print(n.mention.corefClusterID); 
    			}
    			s.print("] ");
    		}
    		if (n.sentEnd) s.println();
    	}
    }
    
    /**
     * Postprocessing after coreference resolution
     */
    public void postProcess() {
    	// set final mention categories
    	for (Mention m : mentions) {
    		if (m.categories.size() > 0) {
    			String[] cats = new String[m.categories.size()];
    			int i = 0;
    			for (String cat: m.categories) {
    				cats[i++] = cat;
    			}
    			m.category = cats[0]; // rarely more than one category, priorities?
    		} else {
    			m.category = null;
    		}
    	}
    	setConllCorefColumns();
    }
}
