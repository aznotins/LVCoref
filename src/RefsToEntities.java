import java.util.HashSet;
import java.util.Set;


/** Do the transitive closure to make reference-referent pairs into entity partitions **/
public class RefsToEntities {
	public static void go(Document d) {

		Integer id = 0;
		for (Node n : d.tree) {
			if (n.corefs_id == null) {
				if (n.var.size() > 0 || n.successors.size() > 0) {
					d.corefs.add(new HashSet<Node>());
					closure(id++, n, d);
				}
			}
			
		}
	}
	
	
	public static void closure(Integer coref_id, Node n, Document d) {
		if (n.corefs_id == null) {
			d.corefs.get(coref_id).add(n);
			n.corefs_id = coref_id;
			
			for (Integer ant_id : n.var) {
				Node ant = d.tree.get(ant_id);
				closure(coref_id, ant, d);
			}
			for (Integer suc_id : n.successors) {
				Node suc = d.tree.get(suc_id);
				closure(coref_id, suc, d);
			}
		}
	}
}
