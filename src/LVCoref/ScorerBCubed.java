/*******************************************************************************
 * Copyright 2013,2014 Institute of Mathematics and Computer Science, University of Latvia
 * Author: Artūrs Znotiņš
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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
        	//sameMention(doc, m.node) && sameMention(doc, m2.node) && // lai sakrīt ar conll scorer
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
        	//sameMention(doc, m.node) && sameMention(doc, m2.node) && // lai sakrīt ar conll scorer
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
  
  public boolean sameMention(Document doc, Node n) {
	  if (n.mention != null && n.goldMention != null 
			  && n.mention.start == n.goldMention.start 
			  && n.mention.end == n.goldMention.end) {
		  return true;
	  } else {
		  return false;
	  }
  }
}
