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

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Wrapper for a coreference resolution score: MUC, B cubed, Pairwise.
 */
public abstract class CorefScorer {

  enum SubScoreType {Recall, Precision, F1}
  enum ScoreType { MUC, BCubed, Pairwise}

  double precisionNumSum;
  double precisionDenSum;
  double recallNumSum;
  double recallDenSum;
  ScoreType scoreType;

  CorefScorer() {
    precisionNumSum = 0.0;
    precisionDenSum = 0.0;
    recallNumSum = 0.0;
    recallDenSum = 0.0;
  }

  public double getScore(SubScoreType subScoreType) {
    switch (subScoreType) {
      case Precision:
        return getPrecision();
      case Recall:
        return getRecall();
      case F1:
        return getF1();
      default:
        throw new IllegalArgumentException("Unsupported subScoreType: " + subScoreType);
    }
  }

  public double getPrecision(){
    return precisionNumSum/precisionDenSum;
  }
  public double getRecall(){
    return recallNumSum/recallDenSum;
  }
  public double getF1(){
    double p = getPrecision();
    double r = getRecall();
    return 2.0 * p * r / (p + r);
  }

  public void calculateScore(Document doc){
    calculatePrecision(doc);
    calculateRecall(doc);
  }
  protected abstract void calculatePrecision(Document doc);
  protected abstract void calculateRecall(Document doc);

  public String getF1String(boolean simple) {
    NumberFormat nf = new DecimalFormat("0.000000");

    double r = getRecall();
    double p = getPrecision();
    double f1 = getF1();

    String R = nf.format(r);
    String P = nf.format(p);
    String F1 = nf.format(f1);

    NumberFormat nf2 = new DecimalFormat("00.00");
    String RR = nf2.format(r*100);
    String PP = nf2.format(p*100);
    String F1F1 = nf2.format(f1*100);

    StringBuilder sb = new StringBuilder();
    if(scoreType == ScoreType.Pairwise){
        sb.append("Pairwise\t");
	  } else if(scoreType == ScoreType.BCubed){
		  sb.append("B cube\t");
	  } else if(scoreType == ScoreType.MUC) {
		  sb.append("MUC\t");
	  }
    sb.append(F1F1); sb.append("\t");
    sb.append(PP); sb.append("\t");
    sb.append(RR);
    if (!simple) sb.append("\tF1 = "+F1+", P = "+P+" ("+(int) precisionNumSum+"/"+(int) precisionDenSum+"), R = "+R+" ("+(int) recallNumSum+"/"+(int) recallDenSum+")");
    return sb.toString();
  }
}
