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
    int not_mention = 0;
    for(CorefCluster g : doc.goldCorefClusters.values()) {
        
        //if (g.corefMentions.size() < 2) continue;
        
      int clusterSize = g.getCorefMentions().size();
      
      rDen += clusterSize*(clusterSize-1)/2;
      for(Mention m1 : g.getCorefMentions()){
          if (m1.node.mention == null) {
              //System.out.println("Recall Not mention ("+m1.nerString+") @ "+ m1.node.id+" #" + ++not_mention);
              LVCoref.logger.fine("Recall Not mention ("+m1.nerString+") @ "+ m1.node.id+" #" + ++not_mention);
          }
        for(Mention m2 : g.getCorefMentions()) {
          if(m1.id >= m2.id) continue;
          if(m1.node.mention != null && m2.node.mention != null
              && m1.node.mention.corefClusterID == m2.node.mention.corefClusterID){
            rNum++;
          } else if (m1.node.mention != null && m2.node.mention != null) {
              //System.out.println("Recall Incorrect coreference(referents)" + m1.nerString + "  :  " + m2.nerString);
              LVCoref.logger.fine("Recall Incorrect coreference(referents)" + m1.nerString + "  :  " + m2.nerString);
          } else {
//              if (m1.node.mention == null) System.out.println("Recall Not mention ("+m1.nerString+") #" + ++not_mention);
//              if (m2.node.mention == null) System.out.println("Recall Not mention ("+m2.nerString+") #" + ++not_mention);
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

    int not_mention = 0;
    for(CorefCluster c : doc.corefClusters.values()){
    //if (c.corefMentions.size() < 2) continue;
      int clusterSize = c.getCorefMentions().size();
      pDen += clusterSize*(clusterSize-1)/2;
      for(Mention m1 : c.getCorefMentions()){
          if (m1.node.goldMention == null) {
              //System.out.println("Precision Not mention ("+m1.nerString+") @ "+ m1.node.id+" #" + ++not_mention);
              LVCoref.logger.fine("Precision Not mention ("+m1.nerString+") @ "+ m1.node.id+" #" + ++not_mention);
          }
        for(Mention m2 : c.getCorefMentions()) {
          if(m1.id >= m2.id) continue;
          if(m1.node.goldMention != null && m2.node.goldMention != null
              && m1.node.goldMention.corefClusterID == m2.node.goldMention.corefClusterID){
            pNum++;
          } else if (m1.node.goldMention != null && m2.node.goldMention != null) {
              //System.out.println("Precision Incorrect coreference(referents)" + m1.nerString + "  :  " + m2.nerString);
              LVCoref.logger.fine("Precision Incorrect coreference(referents)" + m1.nerString + "  :  " + m2.nerString);
          } else {
//              if (m1.node.goldMention == null) System.out.println("Precision Not mention ("+m1.nerString+") #" + ++not_mention);
//              if (m2.node.goldMention == null) System.out.println("Precision Not mention ("+m2.nerString+") #" + ++not_mention);
          }
        }
      }
    }
    precisionDenSum += pDen;
    precisionNumSum += pNum;
  }
}
