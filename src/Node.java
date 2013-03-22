import java.util.ArrayList;
import java.util.Set;


public class Node {
	public int parent;
	public int id;
	public ArrayList<Integer> children;
	
	public boolean isMention;
	public String tag;
	public String lemma;
	public String word;
	
	
	public String m_s;
	
	public String type;
	public String category;
	
	public ArrayList<Integer> var;
	
	public ArrayList<Integer> successors;
	public int ant;
	
	public Integer corefs_id;
	
	
	Node(String word, String lemma, String tag, Integer parent, Integer id, String category, String type) {
		this.word = word;
		this.lemma = lemma;
		this.tag = tag;
		children = new ArrayList<Integer>();
		this.parent = parent;
		this.id = id;
		this.var = new ArrayList<Integer>();
		this.isMention = false;
		this.ant = -1;
		this.category = category;
		this.type = type;
		this.successors = new ArrayList<Integer>();
		
		
		
	}
}
