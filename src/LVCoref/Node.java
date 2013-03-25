package LVCoref;

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
	
	
    public Mention mention;
    
    
	public String m_s;
	
	public String type;
	public String category;
	
	public ArrayList<Integer> var;
	
	public ArrayList<Integer> successors;
	public int ant;
	
	public Integer corefs_id;
	
	
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
	}
}
