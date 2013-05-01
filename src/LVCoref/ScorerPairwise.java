package LVCoref;

import java.util.*;

public class ScorerPairwise extends CorefScorer {

  public ScorerPairwise(){
    super();
    scoreType = ScoreType.Pairwise;
  }

  @Override
  protected void calculateRecall(Document doc) {
    int rDen = 0;
    int rNum = 0;

    for(CorefCluster g : doc.goldCorefClusters.values()) {
        //if (g.corefMentions.size() < 2) continue;
        
      int clusterSize = g.getCorefMentions().size();
      
      rDen += clusterSize*(clusterSize-1)/2;
      for(Mention m1 : g.getCorefMentions()){
        for(Mention m2 : g.getCorefMentions()) {
          if(m1.id >= m2.id) continue;
          if(m1.node.mention != null && m2.node.mention != null
              && m1.node.mention.corefClusterID == m2.node.mention.corefClusterID){
            rNum++;
          }
        }
      }
    }
    recallDenSum += rDen;
    recallNumSum += rNum;
  }

  @Override
  protected void calculatePrecision(Document doc) {
    int pDen = 0;
    int pNum = 0;

    
    for(CorefCluster c : doc.corefClusters.values()){
    //if (c.corefMentions.size() < 2) continue;
      int clusterSize = c.getCorefMentions().size();
      pDen += clusterSize*(clusterSize-1)/2;
      for(Mention m1 : c.getCorefMentions()){
        for(Mention m2 : c.getCorefMentions()) {
          if(m1.id >= m2.id) continue;
          if(m1.node.goldMention != null && m2.node.goldMention != null
              && m1.node.goldMention.corefClusterID == m2.node.goldMention.corefClusterID){
            pNum++;
          }
        }
      }
    }
    precisionDenSum += pDen;
    precisionNumSum += pNum;
  }
}
