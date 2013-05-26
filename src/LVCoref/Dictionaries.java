package LVCoref;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Dictionaries {

    //Used attributes for mentions
    public enum MentionType { PRONOMINAL, NOMINAL, PROPER }
    public enum Gender { MALE, FEMALE, NEUTRAL, UNKNOWN }
    public enum Number { SINGULAR, PLURAL, UNKNOWN }    
    public enum Animacy { ANIMATE, INANIMATE, UNKNOWN }    
    public enum Case { NOMINATIVE, GENITIVE, DATIVE, ACCUSATIVE, LOCATIVE, VOCATIVE, UNKNOWN }
	public enum PronounType { PERSONAL, REFLEXIVE, POSSESIVE, DEMONSTRATIVE, INDEFINITE, INTERROGATIVE, RELATIVE, DEFINITE, UNKNOWN };
    
    
    public static final Map<String, Set<String>> pronouns = new HashMap<>();
    //public static final Map<String, Set<String>> pronouns = new HashMap<String,HashMap<String> >();
	public static final Map<String, Set<String>> commonNouns = new HashMap<>();
	public static final Map<String, Set<String>> properNouns = new HashMap<>();
    
    public final Set<String> firstNames = new HashSet<>();
    public final Set<String> lastNames = new HashSet<>();
    public final Map<String, Set<Integer>> sinonyms = new HashMap<>();
    
    public final Set<String> excludeWords = new HashSet<>();
    public final Map<String, Set<Integer>> genetives = new HashMap<>();
    
    public final Set<String> relativeClauseW = new HashSet<>(Arrays.asList("jo", "ja", "kas", "ka", "lai", "vai", "kas", "kurš", "kura", "kurš", "kāds", "kāda", "cik", "kā", "kad", "kur", "tiklīdz", "līdz", "kopš"));
    
    public final Set<String> unclearGenderPronouns = new HashSet<>(Arrays.asList("savs", "sava"));
    

    public Dictionaries(
        ){
        try {
            getWordsFromFile("lists/firstnames.txt", firstNames, true);
            getWordsFromFile("lists/lastnames.txt", lastNames, true);
            getWordGroupsFromFile("lists/sinonyms.txt", sinonyms, true);
            getWordsFromFile("lists/exclude.txt", excludeWords, true);
            getWordGroupsFromFile("lists/genetives.txt", sinonyms, true);
            
            commonNouns.put("person",new HashSet<String>());
            properNouns.put("person",new HashSet<String>());
            pronouns.put("person",new HashSet<String>());
            properNouns.get("person").addAll(firstNames);
            properNouns.get("person").addAll(lastNames);           
            getWordsFromFile("lists/roles.txt", commonNouns.get("person"), true);
            pronouns.get("person").addAll(Arrays.asList("kurš","kura","es","tu","viņš","viņa","mēs", "jūs", "savs", "sava"));
                        
            commonNouns.put("organization",new HashSet<String>());
            properNouns.put("organization",new HashSet<String>());
            pronouns.put("organization",new HashSet<String>());
            getWordsFromFile("lists/org_proper.txt", properNouns.get("organization"), true);
            getWordsFromFile("lists/org_common.txt", commonNouns.get("organization"), true);
            pronouns.get("organization").addAll(Arrays.asList("kas", "kurš", "kura", "tas", "tā"));
            
            commonNouns.put("location",new HashSet<String>());
            properNouns.put("location",new HashSet<String>());
            pronouns.put("location",new HashSet<String>());
            getWordsFromFile("lists/locations_proper.txt", properNouns.get("location"), true);
            getWordsFromFile("lists/locations_common.txt", commonNouns.get("location"), true);
            pronouns.get("location").addAll(Arrays.asList("tas","tā","kas"));
            
//            commonNouns.put("time",new HashSet<String>());
//            properNouns.put("time",new HashSet<String>());
//            pronouns.put("time",new HashSet<String>());
//            pronouns.get("time").addAll(Arrays.asList("tas","tā","kas"));
//            commonNouns.get("time").addAll(Arrays.asList("tagad","šodien, pirmdiena, otrdiena, trešdiena, ceturtdiena, piektdiena, sestdiena, svētdiena"));
//            properNouns.get("time").addAll(Arrays.asList("Ziemsvētki","Meteņi","Lieldienas","Pēteri","Jāņi","Kumēdiņi"));
            
//            commonNouns.put("thing",new HashSet<String>());
//            properNouns.put("thing",new HashSet<String>());
//            pronouns.put("thing",new HashSet<String>());
//            pronouns.get("thing").addAll(Arrays.asList("kas", "kurš", "kura", "tas", "tā"));
//            commonNouns.get("thing").addAll(Arrays.asList("pagrieziens","aploksne","kastīte","zīmīte","māja","vieta","vēstule","vēstulīte"));
//            properNouns.get("thing").addAll(Arrays.asList("Bībele"));
            
        } catch (IOException ex) {
            Logger.getLogger(Dictionaries.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
	
	public static Boolean isPronoun(Node n) {
		return true;
	}
	
    
	public static Set<String> getPronounCategories(String p) {
		Set<String> c = new HashSet<String>();
		for(String cat: pronouns.keySet()) {
			if (Dictionaries.pronouns.get(cat).contains(p)) {
				c.add(cat);
			}
		}
		return c;
	}	
    
    
    private static void getWordsFromFile(String filename, Set<String> resultSet, boolean lowercase) throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
            while(reader.ready()) {
                if(lowercase) resultSet.add(reader.readLine().toLowerCase());
                else resultSet.add(reader.readLine());
            }
            reader.close();
        } catch (IOException e) {
			e.printStackTrace();
        }
    }
    
    
    public Boolean belongsToSameGroup(String s1, String s2, Map<String, Set<Integer>> groups) {
		Set<Integer> t1 = groups.get(s1);
		Set<Integer> t2 = groups.get(s2);
		if (t1 != null && t2 != null) {
			Set<Integer> intersection = new HashSet<Integer>(t1);
			intersection.retainAll(t2);
			if (intersection.size() > 0) return true;
		}
		return false;
    }
    
    
    private static void getWordGroupsFromFile(String filename, Map<String, Set<Integer>> resultSet, boolean lowercase) throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
            String s;
            int group_id = 0;
            while(reader.ready()) {
                if(lowercase) s = reader.readLine().toLowerCase();
                else s = reader.readLine();
                
                if (s.trim().length() > 0) {
                    String[] words = s.split(" ");
                    for (String word : words) {
                        if (resultSet.keySet().contains(word)) {
                            resultSet.get(word).add(group_id);
                        } else {
                            resultSet.put(word, new HashSet<Integer>(Arrays.asList(group_id)));
                        }
                    }
                    group_id++;
                }
            }
            reader.close();
        } catch (IOException e) {
			e.printStackTrace();
        }
    }
    
    public Set<String> getCategories(String s) {
        s = s.toLowerCase();
        Set<String> r = new HashSet<String>();
        for(String cat: properNouns.keySet()) {
            if (properNouns.get(cat).contains(s)) {
                r.add(cat);
                //break;
            }
        }
        for(String cat: commonNouns.keySet()) {        
            if (commonNouns.get(cat).contains(s)) {
                r.add(cat);
                //break;									
            }
        }
        for(String cat: pronouns.keySet()) {        
            if (pronouns.get(cat).contains(s)) {
                r.add(cat);
                //break;									
            }
        }
        return r;
    }
    
    public String getCategory(String s) {
        Set<String> categories = getCategories(s);
        
        if (categories.size() > 0) {
            if (categories.size() > 1) {
                LVCoref.logger.fine("Get category : more categories for \"" +s+"\"");
            }
            return categories.iterator().next();
        }
        return null;
    }
    
    public Set<String> categoryIntersection(Set<String> s1, Set<String> s2){
        Set<String> r = new HashSet(s1);
        r.retainAll(s2);
        return r;
    }
    
}