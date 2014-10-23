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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Artūrs
 */
public class Statistics {
    int words = 0;
    int sentences = 0;
    
    int mentions = 0;
    int clusters = 0;
    int pronomial_c = 0;
    int nominal_c = 0;
    int proper_c = 0;
    int singletons = 0;
    int max_len = 0;
    Map<String,Integer> cat = new HashMap<String, Integer>();
    
    int goldMentions = 0;
    int goldClusters = 0;
    int goldPronomial_c = 0;
    int goldNominal_c = 0;
    int goldProper_c = 0;
    int goldSingletons = 0;
    int goldMax_len = 0;
    Map<String,Integer> goldCat = new HashMap<String, Integer>();
    
    
    public void add(Document d, boolean gold) {
        words += d.tree.size();
        sentences += d.sentences.size();
        
        
        mentions += d.mentions.size();
        clusters += d.corefClusters.size();

        for (Mention m: d.mentions) {
            if (m.type == Dictionaries.MentionType.NOMINAL) {
                nominal_c++;
                if (m.categories.size() > 0) {
                    for (String category: m.categories) {
                        if (cat.containsKey(category)) {
                            cat.put(category, cat.get(category)+1);
                        } else {
                            cat.put(category, 0);                    
                        }
                    }
                }
            } else if (m.type == Dictionaries.MentionType.PROPER) {
                proper_c++;
                if (m.categories.size() > 0) {
                    for (String category: m.categories) {
                        if (cat.containsKey(category)) {
                            cat.put(category, cat.get(category)+1);
                        } else {
                            cat.put(category, 0);                    
                        }
                    }
                }
            } else if (m.type == Dictionaries.MentionType.PRONOMINAL) {
                pronomial_c++;
            }
        }
        
        for (int cl : d.corefClusters.keySet()) {
            if (d.corefClusters.get(cl).corefMentions.size() == 1) singletons++;
            if (d.corefClusters.get(cl).corefMentions.size() > max_len) max_len = d.corefClusters.get(cl).corefMentions.size();
        }
        
        
        if (gold) {
            goldMentions += d.goldMentions.size();
            goldClusters += d.goldCorefClusters.size();
            for (Mention m: d.goldMentions) {
                if (m.type == Dictionaries.MentionType.NOMINAL) {
                    goldNominal_c++;
                    if (m.categories.size() > 0) {
                        for (String category: m.categories) {
                            if (goldCat.containsKey(category)) {
                                goldCat.put(category, goldCat.get(category)+1);
                            } else {
                                goldCat.put(category, 0);                    
                            }
                        }
                    }
                } else if (m.type == Dictionaries.MentionType.PROPER) {
                    goldProper_c++;
                    if (m.categories.size() > 0) {
                        for (String category: m.categories) {
                            if (goldCat.containsKey(category)) {
                                goldCat.put(category, goldCat.get(category)+1);
                            } else {
                                goldCat.put(category, 0);                    
                            }
                        }
                    }
                } else if (m.type == Dictionaries.MentionType.PRONOMINAL) {
                    goldPronomial_c++;
                }
            }
            for (int cl : d.goldCorefClusters.keySet()) {
                if (d.goldCorefClusters.get(cl).corefMentions.size() == 1) goldSingletons++;
                if (d.goldCorefClusters.get(cl).corefMentions.size() > max_len) goldMax_len = d.goldCorefClusters.get(cl).corefMentions.size();
            }
        }        
    }
    
    
    public String corefStatistics(boolean gold) {
        StringBuilder sb = new StringBuilder();
        
        String nl = "\n";
        
        sb.append("--------" +nl);
        sb.append("Words: \t"  + words + nl);
        sb.append("Sentences: \t" + sentences +nl);
        
        {
            sb.append("-----------" + nl);
            sb.append("Mentions: \t" + mentions + nl);
            sb.append("Clusters: \t" + clusters+nl);

            sb.append("Pronomials: \t" + pronomial_c + nl);
            sb.append("Nominal: \t" + nominal_c + nl);
            sb.append("Proper: \t" + proper_c + nl);
            for (String category: cat.keySet()) {
                sb.append("Category " + category +": \t" + cat.get(category) + nl);
            }
            sb.append("Singletons: \t" + singletons + nl);
            sb.append("Max size: \t" + max_len + nl);
            sb.append("Avg size: \t" + ((clusters>0)?((double)mentions/clusters):0) + nl);
            sb.append("-----------" + nl);

        }
        
        if (gold) {
            sb.append("GoldMentions: \t" + goldMentions + nl);
            sb.append("GoldClusters: \t" + goldClusters+nl);
            sb.append("Pronomials: \t" + goldPronomial_c + nl);
            sb.append("Nominal: \t" + goldNominal_c + nl);
            sb.append("Proper: \t" + goldProper_c + nl);
            for (String category: goldCat.keySet()) {
                sb.append("Category " + category +": \t" + goldCat.get(category) + nl);
            }
            sb.append("Singletons: \t" + goldSingletons + nl);
            sb.append("Max size: \t" + goldMax_len + nl);
            sb.append("Avg size: \t" + ((goldClusters>0)?((double)goldMentions/goldClusters):0) + nl);
            sb.append("-----------" + nl);
        }
        return sb.toString();
    }
}
