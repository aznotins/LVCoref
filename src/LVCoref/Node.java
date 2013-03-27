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
    
    
    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");

        result.append( this.getClass().getName() );
        result.append( " Object {" );
        result.append(newLine);

        //determine fields declared in this class only (no fields of superclass)
        Field[] fields = this.getClass().getDeclaredFields();

        //print field names paired with their values
        //      for ( Field field : fields  ) {
        //        result.append("  ");
        //        try {
        //          result.append( field.getName() );
        //          result.append(": ");
        //          //requires access to private field:
        //          result.append( field.get(this) );
        //        } catch ( IllegalAccessException ex ) {
        //          System.out.println(ex);
        //        }
        //        result.append(newLine);
        //      }
        result.append(" word: " + this.word + newLine);
        result.append(" tag: " + this.tag + newLine);
        result.append(" lemma: " + this.lemma + newLine);
        result.append("}");

        return result.toString();
    }
}
