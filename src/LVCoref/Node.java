package LVCoref;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import LVCoref.Dictionaries.MentionType;
import edu.stanford.nlp.trees.Tree;


public class Node {
    public Document document;
	public Node parent;
    public int parentID;
	public int id;
	public List<Node> children;
	
	public Tree tree;
	
	public boolean isMention;
	public String tag;
    public String simpleTag;
	public String lemma;
	public String word;
    public String morphoFeatures;
    public int parentIndex;
    
    public String ne_annotation = "";
    public int namedEntityID = -1;
    public String idType = "";
    
    public int sentNum = -1;
    public int position;
    public boolean isProper = false;
	
	//public String conll_string;
    public List<String> conll_fields;
    
    public Mention mention;
    public Mention goldMention = null;
    
    
	public String m_s;
	
	public String type;
	public String category;
	
	public ArrayList<Integer> var;
	
	public ArrayList<Integer> successors;
	public int ant;
	
	public Integer corefs_id;
    
    public Boolean sentStart = false; //new sentece
    public Boolean sentRoot = false; //sentece root node
    public Boolean sentEnd = false; //sentece end
    
    public List<Integer> mentionStartList;
    public List<Integer> mentionEndList;
	
	
	Node(String word, String lemma, String tag, int parent_id, Integer id, Document d) {
		this.word = word;
		this.lemma = lemma;
		this.tag = tag;
		children = new ArrayList<Node>();
		this.parentID = parent_id;
		this.id = id;
		this.var = new ArrayList<Integer>();
		this.isMention = false;
		this.ant = -1;
		this.category = category;
		this.type = type;
		this.successors = new ArrayList<Integer>();
        this.mention = null;
        
        this.mentionStartList = new ArrayList<Integer>();
        this.mentionEndList = new ArrayList<Integer>();
        
        this.conll_fields = new ArrayList<String>();
        document = d;
	}
    
    /**
     * Get mention type, default NOMINAL
     * @return
     */
    public  MentionType getType() {
    	if (isProper) return MentionType.PROPER; // flag for proper nodes
    	if (isProper()) return MentionType.PROPER;
		if (isPronoun()) return MentionType.PRONOMINAL;
		return MentionType.NOMINAL;
	}
    
    public Node prev(Document d) {
        if (id > 0) return d.tree.get(id-1);
        return null;
    }
    public Node prev() {
        return document.getNode(id-1);
    }
    
    public Node next(Document d) {
        if (id + 1 < d.tree.size()) return d.tree.get(id+1);
        return null;
    }
    public Node next() {
        return document.getNode(id+1);
    }
    
    
    public Node getSpanStart(Document d) {
        Node min = this;
        for (Node n: this.children) {
            if (n.id > min.id) continue;
            if (n.sentNum != this.sentNum) continue;
            Node x = n.getSpanStart(d);
            if (x.id < min.id) min = x;
        }
        return min; 
    }
    
    public Node getSpanEnd(Document d) {
        Node max = this;
        for (Node n: this.children) {
            if (n.id < max.id) continue;
            if (n.sentNum != this.sentNum) continue;
            Node x = n.getSpanEnd(d);
            if (x.id > max.id) max = x; 
        }
        return max;
    }
    
    
    public String nodeProjection(Document d) {
        String s = "";
        Node n = getSpanStart(d);
        Node spanEnd = getSpanEnd(d);
        s = n.word;
        while (n != spanEnd) {
            n = n.next(d);
            s += " " + n.word;
        }
        return s;
    }
    
    
    public Boolean isConjuction() {
        return word.equals("un") || word.equals(",");
    }
    
    public Boolean isAbbreviation(){
        if (tag.charAt(0) == 'z') return false;
        if (word.length() < 2) return false;
        for (int i = 0; i < word.length(); i++) {
            if (!Character.isLetter(word.charAt(i))) return false;
            if (!Character.isUpperCase(word.charAt(i))) return false;
        }
        return word.toUpperCase().equals(word);
    }
    
    public Boolean isNoun() {
        return tag.charAt(0) == 'n';
    }
    
    public Boolean isPronoun() {
        return tag.charAt(0) == 'p';
    }
    
    public Boolean isNumber() {
        return tag.charAt(0) == 'm' || tag.equals("xo") || tag.equals("xn");
    }
    
    public Boolean isQuote() {
        if (tag.equals("zq") || word.equals("\'")) return true;
        return false;
    }
    
    public Boolean isPlural() {
        if (tag.charAt(0) == 'n' && tag.charAt(3) == 'p') return true;
        if (tag.charAt(0) == 'p' && tag.charAt(4) == 'p') return true;
        return false;
    }
    
    public Boolean isDefiniteAdjective(){
        if (tag.charAt(0) == 'a' && tag.charAt(5) == 'y') {
            return true;
        }
        return false;
    }
    
    public Boolean isProperAdjective() {
        if (tag.charAt(0) == 'a' && isProper()) {
            return true;
        }
        return false;
    }
    
    public Dictionaries.Gender getGender(){
        if (tag.charAt(0) == 'n') {
            if (tag.charAt(2) == 'm') return Dictionaries.Gender.MALE;
            else if (tag.charAt(2) == 'f') return Dictionaries.Gender.FEMALE;
        } else if (tag.charAt(0) == 'p') {
            if (tag.charAt(3) == 'm') return Dictionaries.Gender.MALE;
            else if (tag.charAt(3) == 'f') return Dictionaries.Gender.FEMALE;
        } else if (tag.charAt(0) == 'a') {
            if (tag.charAt(2) == 'm') return Dictionaries.Gender.MALE;
            else if (tag.charAt(2) == 'f') return Dictionaries.Gender.FEMALE;
        }
        return Dictionaries.Gender.UNKNOWN;
    }
    public Dictionaries.Number getNumber() {
        if (tag.charAt(0) == 'n') {
            if (tag.charAt(3) == 's') return Dictionaries.Number.SINGULAR;
            else if (tag.charAt(3) == 'p') return Dictionaries.Number.PLURAL;
        } else if (tag.charAt(0) == 'p') {
            if (tag.charAt(4) == 's') return Dictionaries.Number.SINGULAR;
            else if (tag.charAt(4) == 'p') return Dictionaries.Number.PLURAL;
        } else if (tag.charAt(0) == 'a') {
            if (tag.charAt(3) == 's') return Dictionaries.Number.SINGULAR;
            else if (tag.charAt(3) == 'p') return Dictionaries.Number.PLURAL;
        }
        return Dictionaries.Number.UNKNOWN;
        
    }
    
    public Boolean isNounGenitive() {
        if (tag.charAt(0) == 'n' && tag.charAt(4) == 'g' /*&& Character.isUpperCase(word.charAt(0))*/) {
            return true;
        }
        return false;
    }
    
    /**
     * Check first letter, if it is sentence start check if lemma starts with uppercase or 
     * NER annotation is person, organization, location or media
     * @return
     */
    public Boolean isProper() {
        if (Character.isUpperCase(word.charAt(0))) {
        	if (sentStart) {
        		if (Character.isUpperCase(lemma.charAt(0))
        				|| ne_annotation.equals("person") 
        				|| ne_annotation.equals("organization")
        	    		|| ne_annotation.equals("location")
        	    		|| ne_annotation.equals("media")
        	    		|| ne_annotation.equals("product")) {
        			return true;
        		} else {
        			return false;
        		}
        	}
        	return true;
        }
        return false;
    }
    
    public Boolean isProperByFirstLetter() {
        if (Character.isUpperCase(word.charAt(0))) return true; //FIXME very sloppy heurestics
        return false;
    }
    
    public boolean isRelativeClaus() {
        if (tag.equals("zc")) {
            Node next = this.next();
            if (next == null) return false;
            Node nextnext = next.next();
            if (next != null && document.dict.relativeClauseW.contains(next.lemma) || next.tag.charAt(0)=='s' && nextnext != null && document.dict.relativeClauseW.contains(nextnext.lemma)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isRelativePronoun() {
        if (this != null && 
                this.isPronoun() &&
                this.prev()!= null &&
                (
                    this.prev().isRelativeClaus() 
                    || this.prev().tag.startsWith("s") 
                    && this.prev().prev()!=null 
                    && this.prev().prev().isRelativeClaus()
                )
        ) return true;
        return false;
    }
    
    
    public int getDepth() {
        int r = 0;
        Node q  = this;
        while (q.parent != null && q.parent.sentNum == sentNum) {
            r++;
            q = q.parent;
        }
        return r;
    }
    
    public int minDistance(Node n) {
        int r = 0;            
        if (sentNum != n.sentNum) {
            r = Math.abs(sentNum-n.sentNum) + this.getDepth() + n.getDepth();
        } else {
            Node q = document.getCommonAncestor(this, n);
            Node t = n;
            while (t != q) {
                r++;
                t = t.parent;
            }
            t = this;
            while (t != q) {
                r++;
                t = t.parent;
            }
        }
        return r;
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
    
    
    
    /**
     * Could be optimized
     * @param d 
     */
    public void markMentionBorders(Document d, Boolean allowSingletonMentions) {
        Node n = this;
        while (n != null && n.sentNum == this.sentNum) {
            if (n.mention != null && (allowSingletonMentions || !n.mention.isSingleton(d)) ) {
                if (this.id == n.mention.start) {
                    this.mentionStartList.add(n.mention.id);
                }
                if (this.id == n.mention.end) {
                    this.mentionEndList.add(n.mention.id);
                }
            }
            n = n.next(d);
        }
    }
    
    public String getConllMentionColumn(Document d, Boolean allowSingletonMentions) {
        String s = "";
        int i = 0;
        int j = 0;
        while (i < this.mentionStartList.size() || j < this.mentionEndList.size()) {

            if (i < this.mentionStartList.size()) {
                s += "(" + mentionStartList.get(i); 
                if (j < this.mentionEndList.size() && mentionStartList.get(i) == mentionEndList.get(j)) {
                    s += ")";
                    if (i+1 < this.mentionStartList.size()) s+="|";
                    j++;
                }
                i++;
            } else {
                s += ")";
                j++;
            }            
        }  
        //System.out.println(mentionStartList + " " + mentionEndList + "\t" + s);
        if (s.equals("")) s="_";
        return s;
    }
 
    public String toString() {
    	return String.format("Node: %s | %s | %s", word, tag, lemma);
    }
}
