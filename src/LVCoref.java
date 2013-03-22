import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class LVCoref {

	
	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException {
		
		Document d;
		d = new Document();
		
		
		String s;
		Integer id = 0;
		Integer id_s = 0;
		BufferedReader in = null;
		//in = new BufferedReader(new FileReader("Sofija.conll"));
		in = new BufferedReader(new FileReader("SofijasPasaule1996_11-28-dep-unlabeled.conll"));
		//in = new BufferedReader(new FileReader("intervija-unlabeled.conll"));
		//in = new BufferedReader(new FileReader("LETA_IzlaseFreimiem-dep-unlabeled.conll"));
		
//		Mention m;
//		String m_t = "", m_str="";
//		Integer m_s = -1, m_r = -1, m_e=-1, m_id = 0;
		
		
		while ((s = in.readLine()) != null) {
			//System.out.println(s);
			if (s.trim().length() > 0) {
				String[] fields = s.split("\t");
				String token = fields[1];
				String lemma = fields[2];
				String tag = fields[4];		
				int parent = Integer.parseInt(fields[6]) + id_s - 1;
				String category = fields[7];
				
				
				String type = Mention.getType(lemma, tag);
				
				Node node = new Node(token, lemma, tag, parent, id, category, type);
				
				
				
				
				String[] excl = {"skaits", "vārds", "gals", "laiks", "skaits", "interese", "gadījums", "reize", "sākums", "priekšā", "vieta"};
				Set<String> excluded = new HashSet<String>(Arrays.asList(excl));
				
				node.isMention = true;
				if (excluded.contains(node.lemma)) node.isMention = false;
				
				
				
				
				
				id++;
				d.tree.add(node);				
				
				//System.out.println("<"+type+">" +"  " +type.equals("_"));
				
//				if (type.length() > 1) { //type != "_") {
//					System.out.println("<"+type == m_t+">");
//					
//					if (type == m_t) {
//						m_e++;
//						m_str += " " + token;
//					} else {
//						if (m_t != "") {		
//							//get root node for mention
//							for (int i = m_s; i <= m_e; i++) {
//								if (d.tree.get(i).parent < m_s || d.tree.get(i).parent > m_e) {
//									m_r = i;
//									break;
//								}
//							}
//							m = new Mention (m_id++, m_str, m_s, m_e, m_r);
//							d.mentions.add(m);
//							
//							Node root = d.tree.get(m_r);
//							root.isMention = true;
//							root.mention = m;
//							
//							
//							System.out.println("+mention " + m_str + " root="+root.word + " type="+m_t  + " ["+m_str);
//						}
//						m_t = type;
//						m_str = token;
//						m_s = id;
//						m_e = id;
//						m_r = -1;
//					}
//				}
				
				

			} else {
				for (int i = id_s; i < d.tree.size(); i++) {
					//System.out.println(i);
					int p = d.tree.get(i).parent;
					if (p > 0) {
						d.tree.get(p).children.add(i);
						//System.out.println(" parent " +d.tree.get(p).word + " (" +p+") " );
						//System.out.print(" children ["); for(int g : d.tree.get(p).children) {System.out.print(" " + d.tree.get(g).word + " (" +g+") ");} System.out.println("]" );
					}
				}
				id_s = id;
			}
			
		}
		for(Node n : d.tree) {
			System.out.print("#" +n.id + "\t" + n.word + "\t" + n.type + "\t" + n.category + " ^"+n.parent+" "/*n.children.toString()*/); 
			System.out.print("[" );for(int g : n.children) {System.out.print(" " + d.tree.get(g).word + "#" +g+",");} System.out.println("]" );
		}
		
		
		Resolve.go(d);
		
		RefsToEntities.go(d);
		
		
		for (Set<Node> x : d.corefs) {
			System.out.println("---------"+""+ "----------");
			for (Node n: x) {
				System.out.println("#" +n.id + "\t" + n.word + "\t" + n.type + "\t" + n.category); 
			}
		}
		
		
		for (Node n: d.tree) {
			System.out.print(" " + n.word);
			if (n.corefs_id != null) System.out.print("["+n.corefs_id+"/"+n.type+"/"+n.category+"]");
			if (n.word.equals(".")) System.out.println();
		}
		System.out.println();
		
		
		
		
	};
	

	
	
}
