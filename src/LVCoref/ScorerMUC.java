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
  
  public boolean sameMention(Document doc, Node n) {
	  if (n.mention != null && n.goldMention != null 
			  && n.mention.start == n.goldMention.start 
			  && n.mention.end == n.goldMention.end) {
		  return true;
	  } else {
		  return false;
	  }
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
      log.fine("--GoldCluster #" + g.id );
      for (Mention goldMention : g.corefMentions){        
        if(goldMention.node.mention == null /*!sameMention(doc, goldMention.node)*/) {  // twinless goldmention
          rNum--;
          log.fine("\t* ["+goldMention.nerString+"]"+ "\t"+goldMention.getContext(doc, 3)+ "\t@ "+ goldMention.node.id );
        } else {
          partitions.add(doc.corefClusters.get(goldMention.node.mention.corefClusterID));
          log.fine("\t"+goldMention.node.mention.corefClusterID+" ["+goldMention.nerString+"]" + "\t"+goldMention.getContext(doc, 3)+"\t@ "+ goldMention.node.id);
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
        if(predictedMention.node.goldMention == null /*!sameMention(doc, predictedMention.node)*/) {  // twinless goldmention
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
