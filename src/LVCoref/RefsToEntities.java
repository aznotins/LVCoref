package LVCoref;

import java.util.HashSet;
import java.util.Set;


/** Do the transitive closure to make reference-referent pairs into entity partitions **/
public class RefsToEntities {
	public static void go(Document d) {
        
        //initialize new cluster for each mention
        for (Mention m: d.mentions) {
            d.corefClusters.put(m.id, new CorefCluster(m.id));
            d.corefClusters.get(m.id).add(m);
            m.corefClusterID = m.id;
        }     
        
        for (Mention m : d.refGraph.getFinalResolutions().keySet()) {
            Mention n = d.refGraph.getFinalResolutions().get(m);
            if (n != null) {
                int removeID = n.corefClusterID;
                CorefCluster.mergeClusters(d.corefClusters.get(m.corefClusterID), d.corefClusters.get(n.corefClusterID));
                if (m.corefClusterID != removeID) d.corefClusters.remove(removeID); //trying to merge cluster with itself
            }
        }
    }
        
}
