package LVCoref;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import LVCoref.util.Log;

public class ScorerMUC extends CorefScorer {
	
	private static final Logger log = Logger.getLogger( Log.class.getName() );
	//static { log.setLevel(Level.OFF); }
	
  public ScorerMUC(){
    super();
    scoreType = ScoreType.MUC;
  }
  
  @Override
  protected void calculateRecall(Document doc) {
    int rDen = 0;
    int rNum = 0;
    
    for(CorefCluster g : doc.goldCorefClusters.values()){
      if(g.corefMentions.size()==0) {
    	  log.severe("NO MENTIONS for cluster " + g.getClusterID());
        continue;
      }
      rDen += g.corefMentions.size()-1;
      rNum += g.corefMentions.size();
      
      Set<CorefCluster> partitions = new HashSet<CorefCluster>();
      Log.inf("--GoldCluster #" + g.id );
      for (Mention goldMention : g.corefMentions){        
        if(goldMention.node.mention == null) {  // twinless goldmention
          rNum--;
          log.info("\t* ["+goldMention.nerString+"]"+ "\t"+goldMention.getContext(doc, 3)+ "\t@ "+ goldMention.node.id );
        } else {
          partitions.add(doc.corefClusters.get(goldMention.node.mention.corefClusterID));
          log.info("\t"+goldMention.node.mention.corefClusterID+" ["+goldMention.nerString+"]" + "\t"+goldMention.getContext(doc, 3)+"\t@ "+ goldMention.node.id);
        }
      }
      rNum -= partitions.size();
    }
    if (rDen != doc.goldMentions.size()-doc.goldCorefClusters.values().size()) {
      log.severe("rDen is " + rDen);
      log.severe("doc.allGoldMentions.size() is " + doc.goldMentions.size());
      log.severe("doc.goldCorefClusters.values().size() is " + doc.goldCorefClusters.values().size());
    }
    assert(rDen == (doc.goldMentions.size()-doc.goldCorefClusters.values().size()));
    
    recallNumSum += rNum;
    recallDenSum += rDen;
  }
  
  @Override
  protected void calculatePrecision(Document doc) {
    int pDen = 0;
    int pNum = 0;    

    for(CorefCluster c : doc.corefClusters.values()){
      if(c.corefMentions.size()==0) continue;
      pDen += c.corefMentions.size()-1;
      pNum += c.corefMentions.size();
      Set<CorefCluster> partitions = new HashSet<CorefCluster>();
      //LVCoref.logger.fine("--PredictedCluster #" + c.id );
      for (Mention predictedMention : c.corefMentions){
        if(predictedMention.node.goldMention == null) {  // twinless goldmention
          pNum--;
          //LVCoref.logger.fine("\t* ["+predictedMention.nerString+"]"+ "\t"+predictedMention.getContext(doc, 3)+ "\t@ "+ predictedMention.node.id);
        } else {
          partitions.add(doc.goldCorefClusters.get(predictedMention.node.goldMention.goldCorefClusterID));
          //LVCoref.logger.fine("\t"+predictedMention.node.goldMention.goldCorefClusterID+" ["+predictedMention.nerString+"]" + "\t"+predictedMention.node.mention.getContext(doc, 3) +"\t@ "+ predictedMention.node.id);
        }
      }
      pNum -= partitions.size();
    }
    assert(pDen == (doc.mentions.size()-doc.corefClusters.values().size()));
    
    precisionDenSum += pDen;
    precisionNumSum += pNum;
  }
  
  
}
