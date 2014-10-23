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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package LVCoref;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Artūrs
 */
public class FeatureFactory {
    
    public static int op = 0;
    public static Document d;
    
    public static enum Type {INT, DOUBLE, STRING, BOOL};
    
    public static Boolean sameGender(Mention s, Mention t) {
        op++;
        if (s.sameGender(t)) return true;
        return false;
    }
    
    public static Boolean sameHead(Mention s, Mention t) {
        op++;
        if (s.headString.equals(t.headString)) return true;
        return false;
    }
    
    
    
    public boolean set_types = true;
    public List<Type> types = new ArrayList<Type>();
    public List<String> featNames = new ArrayList<String>();
    
    public Flags flags;
    
    
    public void init(Flags flags) {
        this.flags = flags;
        set_types = true;
    }
    
    
    public List<String> features(Mention s, Mention t) {
        
        List<String> feats = new ArrayList<String>();
        
        if (flags.useAppositive) {
            if (set_types) {
                types.add(Type.BOOL);
                featNames.add("appositive");
            } else {
                if (Filter.inApposition(s, t)) {
                    feats.add("true");
                } else {
                    feats.add("false");
                }
            }
        }
        
        if (flags.useSameGender) {
            if (set_types) {
                types.add(Type.BOOL);
                featNames.add("sameGneder");
            } else {
                if (Filter.sameGender(s, t)) {
                    feats.add("true");
                } else {
                    feats.add("false");
                }
            }            
        }
        
        if (flags.useSameNumber) {
            if (set_types) {
                types.add(Type.BOOL);
                featNames.add("sameNumber");
            } else {
                if (Filter.sameNumber(s, t)) {
                    feats.add("true");
                } else {
                    feats.add("false");
                }
            }
        }
        
        if (flags.useSameCategory) {
            if (set_types) {
                types.add(Type.BOOL);
                featNames.add("sameCategory");
            } else {
                if (Filter.sameCategory(s, t)) {
                    feats.add("true");
                } else {
                    feats.add("false");
                }
            }
        }
        
        if (flags.useHeadMatch) {
            if (set_types) {
                types.add(Type.BOOL);
                featNames.add("sameHead");
            } else {
                if (Filter.sameHead(s, t)) {
                    feats.add("true");
                } else {
                    feats.add("false");
                }
            }
        }
        
        if (flags.useInQuoteI) {
            if (set_types) {
                types.add(Type.BOOL);
                featNames.add("inQuoteI");
            } else {
                if (Filter.isQuoteMention(s)) {
                    feats.add("true");
                } else {
                    feats.add("false");
                }
            }
        }
                
        if (flags.useInQuoteJ) {
            if (set_types) {
                types.add(Type.BOOL);
                featNames.add("inQuoteJ");
            } else {
                if (Filter.isQuoteMention(t)) {
                    feats.add("true");
                } else {
                    feats.add("false");
                }
            }
            
        }
        
//        if (flags.useModifier) {
//            if (Filter.modifierConstraint(s, t)) {
//                feats.add("true");
//            } else {
//                feats.add("false");
//            }
//            if (set_types) {
//                types.add(Type.BOOL);
//                featNames.add("modifier");
//            }
//        }
        
        if (flags.useProperI) {
            if (set_types) {
                types.add(Type.BOOL);
                featNames.add("properI");
            } else {
                if (Filter.proper(s)) {
                    feats.add("true");
                } else {
                    feats.add("false");
                }
            }
        }
        
                
        if (flags.useProperJ) {
            if (set_types) {
                types.add(Type.BOOL);
                featNames.add("properJ");
            } else {
                if (Filter.proper(t)) {
                    feats.add("true");
                } else {
                    feats.add("false");
                }
            }            
        }
        
        
        if (flags.useSameSentence) {
            if (set_types) {
                types.add(Type.BOOL);
                featNames.add("sameSentece");
            } else {
                if (Filter.sameSentece(s, t)) {
                    feats.add("true");
                } else {
                    feats.add("false");
                }
            }
        }
        
        if (flags.useSentNum) {
            
        }
        
        
        if (flags.useSubjectI) {
            
        }
        
        if (flags.useSubjectJ) {
            
        }
        if (flags.useExactMatch) {
            if (set_types) {
                types.add(Type.BOOL);
                featNames.add("exactMatch");
            } else {
                if (Filter.exactMatch(s, t)) {
                    feats.add("true");
                } else {
                    feats.add("false");
                }
            }
        }
        

        set_types = false;
        return feats;
        
    }
}
