package LVCoref;


import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

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
    
    public Set<String> modifiers;
    public Set<String> properModifiers;
    
     private static final long serialVersionUID = 8655265337578515592L;

//    // Attributes for cluster - can include multiple attribute e.g., {singular, plural}
//    protected Set<Number> numbers;
//    protected Set<Gender> genders;
//    protected Set<Animacy> animacies;
    protected Set<String> nerStrings;

    /** All words in this cluster - for word inclusion feature  */
    public Set<String> words;


    public int getClusterID(){ return id; }
    public Set<Mention> getCorefMentions() { return corefMentions; }
    public Mention getFirstMention() { return firstMention; }
    public Mention getRepresentativeMention() { return representative; }

    public CorefCluster(int ID) {
        id = ID;
        corefMentions = new HashSet<Mention>();
        nerStrings = new HashSet<String>();
        heads = new HashSet<String>();
        words = new HashSet<String>();
        firstMention = null;
        representative = null;
        modifiers = new HashSet<String>();
        properModifiers = new HashSet<String>();
    }
   
    public void add(Mention m) {
        corefMentions.add(m);
        modifiers.addAll(m.modifiers);
        properModifiers.addAll(m.properModifiers);
        words.addAll(m.words);
    }
    
    public boolean includeModifiers(CorefCluster c) {
        Set<String> new_modifiers = new HashSet<String>(this.modifiers);
        //System.out.println(new_modifiers +"-"+c.modifiers);
        new_modifiers.removeAll(c.modifiers);
        //System.out.println(new_modifiers);
        if (new_modifiers.size() > 0) return false;
        return true;
    }
    
//    public static void mergeClusters(CorefCluster to, CorefCluster from) {        
//        int toID = to.id;
//        for (Mention m : from.corefMentions){
//          m.corefClusterID = toID;
//        }
//        to.corefMentions.addAll(from.corefMentions);
//        System.out.println("Merge clusters from " +from.id +" to " + to.id +" " + Utils.linearizeMentionSet(to.corefMentions));
//    }
    
    
      /** Print cluster information */
//  public void printCorefCluster(Logger logger){
//    logger.finer("Cluster ID: "+id);//+"\tNumbers: "+numbers+"\tGenders: "+genders+"\tanimacies: "+animacies);
//    logger.finer("NE: "+nerStrings+"\tfirst Mention's ID: "+firstMention.id+"\tHeads: "+heads+"\twords: "+words);
//    TreeMap<Integer, edu.stanford.nlp.dcoref.Mention> forSortedPrint = new TreeMap<Integer, edu.stanford.nlp.dcoref.Mention>();
//    for(Mention m : this.corefMentions){
//      if(m.goldCorefClusterID==-1){
//        logger.finer("mention-> id:"+m.id+"\toriginalRef: "+/*m.originalRef+*/"\t"+m.node.nodeProjection(d) +"\tsentNum: "+m.sentNum+"\tstartIndex: "+m.start);
//      } else{
//        logger.finer("mention-> id:"+m.mentionID+"\toriginalClusterID: "+m.goldCorefClusterID+"\t"+m.spanToString() +"\tsentNum: "+m.sentNum+"\tstartIndex: "+m.startIndex +"\toriginalRef: "+m.originalRef+"\tType: "+m.mentionType);
//      }
//    }
//  }
}
