package LVCoref;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
    
    
    public static final Map<String, Set<String>> pronouns = new HashMap<String, Set<String>>();
	public static final Map<String, Set<String>> commonNouns = new HashMap<String, Set<String>>();
	public static final Map<String, Set<String>> properNouns = new HashMap<String, Set<String>>();
    
    public final Set<String> firstNames = new HashSet<String>();
    public final Set<String> lastNames = new HashSet<String>();
    public final Map<String, Set<Integer>> sinonyms = new HashMap<String, Set<Integer>>();
    
    public final Set<String> excludeWords = new HashSet<String>();
    public final Map<String, Set<Integer>> genetives = new HashMap<String, Set<Integer>>();
    

    public Dictionaries(
        ){
        try {
            getWordsFromFile("lists/firstnames.txt", firstNames, false);
            getWordsFromFile("lists/lastnames.txt", lastNames, false);
            getWordGroupsFromFile("lists/sinonyms.txt", sinonyms, false);
            getWordsFromFile("lists/exclude.txt", excludeWords, true);
            getWordGroupsFromFile("lists/genetives.txt", sinonyms, false);
            
            commonNouns.put("PERSON",new HashSet<String>());
            properNouns.put("PERSON",new HashSet<String>());
            pronouns.put("PERSON",new HashSet<String>());
            properNouns.get("PERSON").addAll(firstNames);
            properNouns.get("PERSON").addAll(lastNames);           
            getWordsFromFile("lists/roles.txt", commonNouns.get("PERSON"), true);
            pronouns.get("PERSON").addAll(Arrays.asList("kurš","kura","es","tu","viņš","viņa"));
                        
            commonNouns.put("ORG",new HashSet<String>());
            properNouns.put("ORG",new HashSet<String>());
            pronouns.put("ORG",new HashSet<String>());
            getWordsFromFile("lists/org_proper.txt", properNouns.get("ORG"), true);
            getWordsFromFile("lists/org_common.txt", commonNouns.get("ORG"), true);
            pronouns.get("ORG").addAll(Arrays.asList("kas", "kurš", "kura", "tas", "tā"));
            
            commonNouns.put("LOCATION",new HashSet<String>());
            properNouns.put("LOCATION",new HashSet<String>());
            pronouns.put("LOCATION",new HashSet<String>());
            getWordsFromFile("lists/locations_proper.txt", properNouns.get("LOCATION"), true);
            getWordsFromFile("lists/locations_common.txt", commonNouns.get("LOCATION"), true);
            pronouns.get("LOCATION").addAll(Arrays.asList("tas","tā","kas"));
            
            commonNouns.put("TIME",new HashSet<String>());
            properNouns.put("TIME",new HashSet<String>());
            pronouns.put("TIME",new HashSet<String>());
            pronouns.get("TIME").addAll(Arrays.asList("tas","tā","kas"));
            commonNouns.get("TIME").addAll(Arrays.asList("tagad","šodien"));
            properNouns.get("TIME").addAll(Arrays.asList("Ziemsvētki","Meteņi","Lieldienas","Pēteri","Jāņi","Kumēdiņi"));
            
            commonNouns.put("THING",new HashSet<String>());
            properNouns.put("THING",new HashSet<String>());
            pronouns.put("THING",new HashSet<String>());
            pronouns.get("THING").addAll(Arrays.asList("kas", "kurš", "kura", "tas", "tā"));
            commonNouns.get("THING").addAll(Arrays.asList("pagrieziens","aploksne","kastīte","zīmīte","māja","vieta","vēstule","vēstulīte"));
            properNouns.get("THING").addAll(Arrays.asList("Bībele"));
            
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
                System.err.println(s);
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
    
}