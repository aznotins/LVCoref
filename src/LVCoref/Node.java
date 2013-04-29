package LVCoref;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class Node {
	public Node parent;
    public int parentID;
	public int id;
	public List<Node> children;
	
	public boolean isMention;
	public String tag;
	public String lemma;
	public String word;
    
    public int sentNum = -1;
    public int position;
	
	public String conll_string;
    
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
	
	
	Node(String word, String lemma, String tag, int parent_id, Integer id) {
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
	}
    
    
    public Node prev(Document d) {
        if (id > 0) return d.tree.get(id-1);
        return null;
    }
    
    public Node next(Document d) {
        if (id + 1 < d.tree.size()) return d.tree.get(id+1);
        return null;
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
            n = n.parent;
        }
    }
 
    
    
    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");

        result.append( this.getClass().getName() );
        result.append( " Object {" );

        //determine fields declared in this class only (no fields of superclass)
        Field[] fields = this.getClass().getDeclaredFields();

        result.append(" word: " + this.word);
        result.append(" tag: " + this.tag);
        result.append(" lemma: " + this.lemma);
        result.append("}");
        //result.append(newLine);

        return result.toString();
    }
}
