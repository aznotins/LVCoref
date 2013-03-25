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


public class Data {
    
    
    public enum MentionType { PRONOMINAL, NOMINAL, PROPER }

    public enum Gender { MALE, FEMALE, NEUTRAL, UNKNOWN }

    public enum Number { SINGULAR, PLURAL, UNKNOWN }
    
    public enum Animacy { ANIMATE, INANIMATE, UNKNOWN }
    
    public enum Case {NOMINATIVE, GENITIVE, DATIVE, ACCUSATIVE, LOCATIVE, VOCATIVE, UNKNOWN}
	
    public enum PronounType {PERSONAL, REFLEXIVE, POSSESIVE, DEMONSTRATIVE, INDEFINITE, INTERROGATIVE, RELATIVE, DEFINITE, UNKNOWN};
    
	public static Map<String, Set<String>>pronoun;
	public static Map<String, Set<String>>common;
	public static Map<String, Set<String>>proper;
	
	public static Map<String, Set<Integer>> sinonyms;
	
	
	public static void load_data() {
		
		String
			person_proper[] = {
				"Andra","Neiburga","Anrī","Kartjē","Bresons","Dievs","Opis","Dieviņš","Teodors","Raimonds","Pauls",
				"Gulbīši","Robis","Kārta","Opītis","Sanī","Čingishans","Solvita","Mīlnieki","Mišels","Amoss","Ozs","Agnese",
				"Aīda","Miro","Zemesmāte","Didzis","Valdis",
				"Sprūdžs","Rāviņš","Edmunds","Andris","Irēna","Škutāne","Egita","Diure","Sofija","Amundsena","Jūruna",
				"Āboliņš","Amudsena","Šerekāns","Zeltīte","Sarkangalvīte","Pēteris","Smits","Smulē","Govinds","Anna",
				"Knutsena","Sinēve","Klods","Debisī","Marija","Hilda","Mellere","Knāga","Mellers","Knāgs","Andersens",
				"Nīlsens","Jepsens","Tomass","Balvis","Emīls","Lepters","Armands","Viktors",
				"Sezārija","Evora","Nora","Džonsa","Tēsejs","Ariadne","Oto","Čingizhans",
				"Viņķeļi","Francis","Ella","Aleksandra","Tristāns","Izolde","Andžs","Tīna","Zumpji","Dārta",
				"Augustīns","Terēze","Mia","Kopeloviča","Žukovs","Oļehnoviča","Kravale-Pauliņa","Praulīte",
				"Gedrovics","Rauckiene","Samuseviča","Sīle","Opincāns","Juta","Tiešis","Šaltenis",
				"Dzemida","Maija","Kārlis","Kabacis","Jānis","Čārlzs","Juris","Emīlija"
			},
			person_common[] = {
				"kapteinis","bērns","pētnieks","zinātnieks","deputāts","meitene","zēns","tētis", "tēvs","mamma", "māte"
			},
			person_pronoun[] = {
				"kurš","kura","es","tu","viņš","viņa"
			},
		
			location_proper[] ={
				"Visums","Zeme","Mēness","Latvija","Palestīna","Izraēla","Austrālija","Rīga","Vācija","Sibīrija",
				"Maskava","Holande","Krievija","Šmerlis","Holivuda","Rozes","Mežotne","Jelgava","Zemgale","Jēkbapils","Jēkabpils",
				"Ēdene","Slēpnis","Londona","Jūrmala","Eiropa","Lietuva","Meksika","Francija","Ungārija","Čehija","Florida","Somija"
			},
			location_common[] = {
				"iela", "upe","jūra","ceļš","skola"
			},
			location_pronoun[] = {
				"tas", "tā", "kas"
			},
			
			org_proper[] ={
				"Dunhill", "Rimi", "Adidas", "Pilsētsaimniecība", "Baltkonsultants"
			},
			org_common[] = {
				"kompānija", "uzņēmums"
			},
			org_pronoun[] = {
				"kas", "kurš", "kura", "tas", "tā"
			},
		
		
			time_proper[] ={
				"Ziemsvētki","Meteņi","Lieldienas","Pēteri","Jāņi","Kumēdiņi"
			},
			time_common[] = {
				"tagad","šodien"
			},
			time_pronoun[] = {
				"tad", "toreiz"
			},
			
			thing_proper[] ={
				"Bībele"
			},
			thing_common[] = {
				"pagrieziens","aploksne","kastīte","zīmīte","māja","vieta","vēstule","vēstulīte"
			},
			thing_pronoun[] = {
				"kas", "kurš", "kura", "tas", "tā"
			}
		;
		
		
		proper = new HashMap<String, Set<String>>();
		common = new HashMap<String, Set<String>>();
		pronoun = new HashMap<String, Set<String>>();
		
		proper.put("PERSON", new HashSet<String>(Arrays.asList(person_proper)));
		
		
		common.put("PERSON", new HashSet<String>(Arrays.asList(person_common)));
		pronoun.put("PERSON", new HashSet<String>(Arrays.asList(person_pronoun)));
		
		proper.put("ORG", new HashSet<String>(Arrays.asList(org_proper)));
		common.put("ORG", new HashSet<String>(Arrays.asList(org_common)));
		pronoun.put("ORG", new HashSet<String>(Arrays.asList(org_pronoun)));

		proper.put("LOCATION", new HashSet<String>(Arrays.asList(location_proper)));
		common.put("LOCATION", new HashSet<String>(Arrays.asList(location_common)));
		pronoun.put("LOCATION", new HashSet<String>(Arrays.asList(location_pronoun)));
		
		proper.put("TIME", new HashSet<String>(Arrays.asList(time_proper)));
		common.put("TIME", new HashSet<String>(Arrays.asList(time_common)));
		pronoun.put("TIME", new HashSet<String>(Arrays.asList(time_pronoun)));
		
		proper.put("THING", new HashSet<String>(Arrays.asList(thing_proper)));
		common.put("THING", new HashSet<String>(Arrays.asList(thing_common)));
		pronoun.put("THING", new HashSet<String>(Arrays.asList(thing_pronoun)));
		
		
		
		
		sinonyms = new HashMap<String, Set<Integer> >();
		
		
		sinonyms.put("tēvs", new HashSet<Integer>(Arrays.asList(new Integer[] {1})));
		sinonyms.put("tētis", new HashSet<Integer>(Arrays.asList(new Integer[] {1})));
		sinonyms.put("vēstule", new HashSet<Integer>(Arrays.asList(new Integer[] {2})));
		sinonyms.put("vēstulīte", new HashSet<Integer>(Arrays.asList(new Integer[] {2})));
		sinonyms.put("aploksne", new HashSet<Integer>(Arrays.asList(new Integer[] {2})));
		sinonyms.put("vēstuļkaste", new HashSet<Integer>(Arrays.asList(new Integer[] {3})));
		sinonyms.put("vēstuļkastīte", new HashSet<Integer>(Arrays.asList(new Integer[] {3})));
		sinonyms.put("runcis", new HashSet<Integer>(Arrays.asList(new Integer[] {4})));
		sinonyms.put("mincis", new HashSet<Integer>(Arrays.asList(new Integer[] {4})));
		sinonyms.put("māte", new HashSet<Integer>(Arrays.asList(new Integer[] {5})));
		sinonyms.put("māmiņa", new HashSet<Integer>(Arrays.asList(new Integer[] {5})));
	}
	
	
	public static Boolean areSinonyms(String n, String m) {
		
		Set<Integer> s1 = sinonyms.get(n);
		Set<Integer> s2 = sinonyms.get(m);
		if (s1 != null && s2 != null) {
			Set<Integer> intersection = new HashSet<Integer>(s1);
			intersection.retainAll(s2);
			if (intersection.size() > 0) return true;
		}
		return false;
		
	}
	
	public static Boolean isPronoun(Node n) {
		return true;
	}
	
	
	public static Set<String> getPronounCategories(String p) {
		Set<String> c = new HashSet<String>();
		for(String cat: pronoun.keySet()) {
			if (Data.pronoun.get(cat).contains(p)) {
				c.add(cat);
			}
		}
		return c;
	}
	
	
		
	private Map<String, Double> loadNameFrequencies(String path){
		Map<String, Double> res = new HashMap<String, Double>();
		
		String buf;
		String [] parts;
		String name;
		Double freq;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			while((buf=br.readLine()) != null){
				parts = buf.split("\\s+");
				name = parts[0].toLowerCase();
				freq = new Double(parts[1]);
				
				res.put(name, freq);
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return res;
	}
}
