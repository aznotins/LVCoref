import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class Document {
	public ArrayList<Node> tree;
	public ArrayList<Mention> mentions;
	public ArrayList<Set<Node>> corefs;
	Document(){
		tree = new ArrayList<Node>();
		corefs = new ArrayList<Set<Node>>();
		//mentions = new ArrayList<Mention>();
	}

	
//	public ArrayList<Sentence> sentences;
//	public ArrayList<Mention> mentions;
//	
//	Document(){
//		this.sentences = new ArrayList<Sentence>();
//		this.mentions = new ArrayList<Mention>();
//	}
}