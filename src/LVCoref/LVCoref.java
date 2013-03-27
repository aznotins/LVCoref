package LVCoref;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Main process class
 * @author arturs
 */
public class LVCoref {

	public static void main(String[] args) throws Exception {
		Document d;
		d = new Document();
		
        
        d.readCONLL();
        System.out.println(d.getSubString(0, 2));
        
        List<Node> tmp;
        tmp = d.traverse(d.tree.get(1), null, new ArrayList<Node>(Arrays.asList(d.tree.get(1))));
        d.printNodes(tmp);
        
        d.printMentions();
//        
//		for(Node n : d.tree) {
//			System.out.print("#" +n.id + "\t" + n.word + "\t" + n.type + "\t" + n.category + " ^"+n.parent+" "/*n.children.toString()*/); 
//			System.out.print("[" );for(int g : n.children) {System.out.print(" " + d.tree.get(g).word + "#" +g+",");} System.out.println("]" );
//		}
//		
//		
//		Resolve.go(d);
//		
//		RefsToEntities.go(d);
//		
//		
//		for (Set<Node> x : d.corefs) {
//			System.out.println("---------"+""+ "----------");
//			for (Node n: x) {
//				System.out.println("#" +n.id + "\t" + n.word + "\t" + n.type + "\t" + n.category); 
//			}
//		}
//		
//		
//		for (Node n: d.tree) {
//			System.out.print(" " + n.word);
//			if (n.corefs_id != null) System.out.print("["+n.corefs_id+"/"+n.type+"/"+n.category+"]");
//			if (n.word.equals(".")) System.out.println();
//		}
//		System.out.println();
		
		
	};
	

}
