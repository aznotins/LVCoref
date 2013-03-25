package LVCoref;


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
        firstMention = null;
        representative = null;
        
    }
}
