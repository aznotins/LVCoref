/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package LVCoref;

import LVCoref.Document;
import LVCoref.Mention;

/**
 *
 * @author Artūrs
 */
public class FilterFactory {
    
    public static int op = 0;
    public static Document d;
    
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
    
}
