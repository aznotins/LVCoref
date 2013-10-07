package LVCoref;

/** 
 * B^3 scorer
 * @author heeyoung
 *
 */
public class ScorerBCubed extends CorefScorer {

  protected enum BCubedType {B0, Ball, Brahman, Bcai, Bconll};
  
  private BCubedType type;
  
  public ScorerBCubed(BCubedType _type){
    super();
    scoreType = ScoreType.BCubed;
    type = _type;
  }
  
  @Override
  protected void calculatePrecision(Document doc){
    switch(type){
      case Bcai: calculatePrecisionBcai(doc); break;
      case Ball: calculatePrecisionBall(doc); break;
      case Bconll: calculatePrecisionBconll(doc); break;  // same as Bcai
    }
  }


  @Override
  protected void calculateRecall(Document doc){
    switch(type){
      case Bcai: calculateRecallBcai(doc); break;
      case Ball: calculateRecallBall(doc); break;
      case Bconll: calculateRecallBconll(doc); break;
    }
  }

  private void calculatePrecisionBall(Document doc){
    int pDen = 0;
    double pNum = 0.0;

    for(Mention m : doc.mentions){
      double correct = 0.0;
      double total = 0.0;

      for(Mention m2 : doc.corefClusters.get(m.corefClusterID).getCorefMentions()){
        if(m==m2 || 
            (m.node.goldMention != null 
                && m2.node.goldMention != null 
                && m.node.goldMention.goldCorefClusterID == m2.node.goldMention.goldCorefClusterID)) {
          correct++;
        }
        total++;
      }
      pNum += correct/total;
      pDen++;
    }

    precisionDenSum += pDen;
    precisionNumSum += pNum;
  }
  private void calculateRecallBall(Document doc){
    int rDen = 0;
    double rNum = 0.0;

    for(Mention m : doc.goldMentions){
      double correct = 0.0;
      double total = 0.0;
      for(Mention m2 : doc.goldCorefClusters.get(m.goldCorefClusterID).getCorefMentions()){
        if(m==m2 ||
            (m.node.mention != null
                && m2.node.mention != null
                && m.node.mention.corefClusterID == m2.node.mention.corefClusterID)) {
          correct++;
        }
        total++;
      }
      rNum += correct/total;
      rDen++;
    }

    recallDenSum += rDen;
    recallNumSum += rNum;
    
  }
  private void calculatePrecisionBcai(Document doc) {
    int pDen = 0;
    double pNum = 0.0;
    
    for(Mention m : doc.mentions){
      if(m.node.goldMention == null && doc.corefClusters.get(m.corefClusterID).getCorefMentions().size()==1){
        continue;
      }
      double correct = 0.0;
      double total = 0.0;
      for(Mention m2 : doc.corefClusters.get(m.corefClusterID).getCorefMentions()){
        if(m==m2 || 
            (m.node.goldMention != null 
                && m2.node.goldMention != null 
                &&  m.node.goldMention.goldCorefClusterID == m2.node.goldMention.goldCorefClusterID)) {
          correct++;
        }
        total++;
      }
      pNum += correct/total;
      pDen++;
    }
    for(Mention m: doc.goldMentions) {
      if(m.node.mention == null) {
        pNum++;
        pDen++;
      }
    }
    precisionDenSum += pDen;
    precisionNumSum += pNum;
  }

  private void calculateRecallBcai(Document doc) {
    int rDen = 0;
    double rNum = 0.0;

    for(Mention m : doc.goldMentions){
      double correct = 0.0;
      double total = 0.0;
      for(Mention m2 : doc.goldCorefClusters.get(m.goldCorefClusterID).getCorefMentions()){
        if(m==m2 || 
            (m.node.mention != null
                && m2.node.mention != null
                && m.node.mention.corefClusterID == m2.node.mention.corefClusterID)) {
          correct++;
        }
        total++;
      }
      rNum += correct/total;
      rDen++;
    }

    recallDenSum += rDen;
    recallNumSum += rNum;
  }
  private void calculatePrecisionBconll(Document doc) {
    // same as Bcai
    calculatePrecisionBcai(doc);
  }
  private void calculateRecallBconll(Document doc) {
    int rDen = 0;
    double rNum = 0.0;

    for(Mention m : doc.goldMentions){
      double correct = 0.0;
      double total = 0.0;
      for(Mention m2 : doc.goldCorefClusters.get(m.goldCorefClusterID).getCorefMentions()){
        if(m==m2 || 
            (m.node.mention != null
                && m2.node.mention != null
                && m.node.mention.corefClusterID == m2.node.mention.corefClusterID)) {
          correct++;
        }
        total++;
      }
      rNum += correct/total;
      rDen++;
    }
    // this part is different from Bcai
    for(Mention m : doc.mentions) {
      if(m.node.goldMention == null && doc.corefClusters.get(m.corefClusterID).getCorefMentions().size()!=1) {
        rNum++;
        rDen++;
      }
    }

    recallDenSum += rDen;
    recallNumSum += rNum;    
  }
}
