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
              //LVCoref.logger.fine("Recall Not mention ("+m1.nerString+") @ "+ m1.node.id+" #" + ++not_mention);
          }
        for(Mention m2 : g.getCorefMentions()) {
          if(m1.id >= m2.id) continue;
          if(m1.node.mention != null && m2.node.mention != null
              && m1.node.mention.corefClusterID == m2.node.mention.corefClusterID){
            rNum++;
          } else if (m1.node.mention != null && m2.node.mention != null) {
              //System.out.println("Recall Incorrect coreference(referents)" + m1.nerString + "  :  " + m2.nerString);
              //LVCoref.logger.fine("Recall Incorrect coreference(referents)" + m1.nerString + "  :  " + m2.nerString);
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
              //LVCoref.logger.fine("Precision Not mention ("+m1.nerString+") @ "+ m1.node.id+" #" + ++not_mention);
          }
        for(Mention m2 : c.getCorefMentions()) {
          if(m1.id >= m2.id) continue;
          if(m1.node.goldMention != null && m2.node.goldMention != null
              && m1.node.goldMention.goldCorefClusterID == m2.node.goldMention.goldCorefClusterID){
            pNum++;
          } else if (m1.node.goldMention != null && m2.node.goldMention != null) {
              //System.out.println("Precision Incorrect coreference(referents)" + m1.nerString + "  :  " + m2.nerString);
              //LVCoref.logger.fine("Precision Incorrect coreference(referents)" + m1.nerString +" @"+ m1.node.id+ "  :  " + m2.nerString + "@ "+ m2.node.id);
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
