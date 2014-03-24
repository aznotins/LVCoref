package LVCoref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Triple;

public class Sentence implements Iterable<Node> {
	private int id = -1;
	private int start = -1;
	private int end = -1; //inclusive
	private List<Node> nodes = new ArrayList<>();
	private Node rootNode = null;
	private Tree rootTree = null;
	private Document document;
	
	public Sentence() {}
	public Sentence(int _id, Document doc) {
		id = _id;
	}
	
	@Override
	public Iterator<Node> iterator() {
		return nodes.iterator();
	}

	public class ChunkAnnotation implements Iterable<Entry<Pair<Integer,Integer>, String>> {
		Map<Pair<Integer,Integer>, String> annotation = new HashMap<>();
		public void add(int start, int end, String label) {
			annotation.put(Pair.makePair(start, end), label);
		}
		public void add(Triple<Integer,Integer,String> t) {
			annotation.put(Pair.makePair(t.first, t.second), t.third);
		}
		public void addAll(Collection<Triple<Integer,Integer,String>> c) {
			for (Triple<Integer,Integer,String> t : c) {
				annotation.put(Pair.makePair(t.first, t.second), t.third);
			}
		}
		public String get(int start, int end) {
			return annotation.get(Pair.makePair(start, end));
		}
		@Override
		public Iterator<Map.Entry<Pair<Integer, Integer>, String>> iterator() {
			return annotation.entrySet().iterator();
		}
	}
	public ChunkAnnotation corefSpans = new ChunkAnnotation();
	public ChunkAnnotation corefCatSpans = new ChunkAnnotation();
	public ChunkAnnotation corefTypeSpans = new ChunkAnnotation();
	
	public int getID() { return id; }
	public void setStart(int _start) { start = _start; }
	public void setEnd(int _end) { end = _end; }
	public int getStart() {	return start; }
	public int getEnd() { return end; }
	public void addNode(Node n) { nodes.add(n); }
	public void setRootTree(Tree t) { rootTree = t; }
	public Tree getRootTree() { return rootTree; }
	public void setRootNode(Node t) { rootNode = t; }
	public Node getRootNode() { return rootNode; }
	public int getSize() { return nodes.size(); }
	public List<Node> getNodes() { return nodes; }
	public Document getDocument() { return document; }
	
	public Node getNode(int index) {
		if (index < nodes.size()) {
			return nodes.get(index);
		} else {
			return null;
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("#"); sb.append(id);
		for (Node n : nodes) {
			sb.append(" "); sb.append(n.word);
		}
		return sb.toString();
	}
	
	public String formatted() {
		StringBuilder sb = new StringBuilder();
//		Set<String> noGapBefore = new HashSet<String>(Arrays.asList(".", ",", ":", ";", "!", "?", ")", "]", "}", "%"));
//		Set<String> noGapAfter =  new HashSet<String>(Arrays.asList("(", "[", "{"));
//		Set<String> quoteSymbols =  new HashSet<String>(Arrays.asList("'", "\""));
		for (Node n : nodes) {
			sb.append(" "); sb.append(n.word);
			// TODO uzlabot teksta veidošanu no sadalītiem tokeniem
		}
		return sb.toString();
	}  
	
	public static void main(String[] args) {
		
	}
}
