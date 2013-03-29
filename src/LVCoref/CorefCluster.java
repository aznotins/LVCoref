package LVCoref;


import java.util.HashSet;
import java.util.Set;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author arturs
 */
public class CorefCluster {
    
    public int id;
    
    public Set<Mention> corefMentions;
    
    
    /**
     * First Mention for cluster
     */
    public Mention firstMention;
    
    /**
     * Most representative mention in cluster
     */    
    public Mention representative;
    
    /**
     * Cluster attributes
     */
    public Set<String> heads;
    
    

    public CorefCluster(int ID) {
        this.id = ID;
        corefMentions = new HashSet<Mention>();
    }
    

    public void add(Mention m) {
        corefMentions.add(m);
    }
    
    public static void mergeClusters(CorefCluster to, CorefCluster from) {        
        int toID = to.id;
        for (Mention m : from.corefMentions){
          m.corefClusterID = toID;
        }
        to.corefMentions.addAll(from.corefMentions);
        System.out.println("Merge clusters from " +from.id +" to " + to.id +" " + Utils.linearizeMentionSet(to.corefMentions));
    }
}
