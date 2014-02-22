package LVCoref;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import LVCoref.Dictionaries.Animacy;
import LVCoref.Dictionaries.Case;
import LVCoref.Dictionaries.Gender;
import LVCoref.Dictionaries.MentionType;
import LVCoref.Dictionaries.Number;
import LVCoref.Dictionaries.PronounType;
import LVCoref.util.Log;

public class Mention implements Comparable<Mention>{
    Document document;
	public Integer id;
	
	// Source that created mention
	public enum MentionSource { 
		DEFAULT, 
		NER, 
		QUOTE, 
		ABBREVIATION, 
		TMP, 
		LIST, 
		ALLNODES, 
		PROPERNODES,
		DETALIZEDNODES,
		BASE			// read from input data, can be overwritten
	}
	public MentionSource source = MentionSource.DEFAULT;
    public boolean strict = false; //listed mentions, this head is important for genetives
	
    public Boolean resolved = false;
    
	public String headString;
    public String nerString = "";
    public String normString;
    
    public boolean modifiersSet = false;
    /**
     * Info about parse tree
     */
    public Node node;    
	public int start;
    public int end;
    public int root;
    
    public int sentNum = -1;
    public int paragraph = -1;
    
    public MentionType type = MentionType.UNKNOWN;
    public Animacy animacy = Animacy.UNKNOWN;
    public Number number = Number.UNKNOWN;
    public Gender gender = Gender.UNKNOWN;
    
    public Case mentionCase;
    public PronounType pronounType;
    
    public int person;
    public String category; // main category - if know precisely should be set explicitly
    
    public String comments = "";
    public Set<String> words = new HashSet<>();
    public Set<String> modifiers = new HashSet<>();
    public Set<String> properModifiers = new HashSet<>();
    
    /**
     * Coreference information
     */
    public int corefClusterID = -1;
    public int goldCorefClusterID = -1;    
    
    Set<String> categories = new HashSet<>(); // all found categories
    
    Mention(Mention m) {
        id = m.id;
        source = m.source;
        resolved = m.resolved;
        headString = m.headString;
        nerString = m.nerString;
        normString = m.normString;
        node = m.node;
        start = m.start;
        end = m.end;
        root = m.root;
        sentNum = m.sentNum;
        type = m.type;
        animacy = m.animacy;
        number = m.number;
        gender = m.gender;
        mentionCase = m.mentionCase;
        pronounType = m.pronounType;
        person = m.person;
        category = m.category;
        comments = m.comments;
        words = m.words;
        corefClusterID = m.corefClusterID;
        goldCorefClusterID = m.goldCorefClusterID;
        categories = m.categories;       
        modifiers = m.modifiers;
        properModifiers = m.properModifiers;
        document = m.document;
    }
  
    /**
     * Create mention
     * @param d
     * @param id
     * @param node
     * @param type
     * @param start
     * @param end
     */
    Mention(Document d, int id, Node node, int start, int end) {
        this.id = id;
		this.root = node.id;
        this.node = node;
        this.headString = node.lemma;
        this.start = start;
        this.end = end;
        this.nerString = d.getSubString(start, end);
        this.sentNum = node.sentNum;
        document = d;
        
        // default values we can obtain
        type = node.getType();
        setCategories();
        
        normString = document.getNormSubString(start, end);
        for(int i = start; i <= end; i++) {
            words.add(document.tree.get(i).lemma);
        }
        nerString = document.getSubString(start, end);
        
        //Nomial word
        if (node.tag.charAt(0) == 'n') {
            if (node.tag.charAt(2) == 'm') gender = Gender.MALE;
            else if (node.tag.charAt(2) == 'f') gender = Gender.FEMALE;
            else gender = Gender.UNKNOWN;
            
            if (node.tag.charAt(3) == 's') number = Number.SINGULAR;
            else if (node.tag.charAt(3) == 'p') number = Number.PLURAL;
            else number = Number.UNKNOWN;
            
            switch(node.tag.charAt(4)) {
                case 'n':mentionCase = Case.NOMINATIVE; break;
                case 'g':mentionCase = Case.GENITIVE; break;
                case 'd':mentionCase = Case.DATIVE; break;
                case 'a':mentionCase = Case.ACCUSATIVE; break;
                case 'l':mentionCase = Case.LOCATIVE; break;
                case 'v':mentionCase = Case.VOCATIVE; break;
                //case 's':mentionCase = Case.NOMINATIVE; break; //ģenetīvenis
                default:mentionCase = Case.UNKNOWN;
            };
        } 
        //Pronoun
        else if (node.tag.charAt(0) == 'p') {
            if (node.tag.charAt(3) == 'm') gender = Gender.MALE;
            else if (node.tag.charAt(3) == 'f') gender = Gender.FEMALE;
            else gender = Gender.UNKNOWN;
            
            if (node.tag.charAt(4) == 's') number = Number.SINGULAR;
            else if (node.tag.charAt(4) == 'p') number = Number.PLURAL;
            else number = Number.UNKNOWN;
            
            switch(node.tag.charAt(5)) {
                case 'n':mentionCase = Case.NOMINATIVE; break;
                case 'g':mentionCase = Case.GENITIVE; break;
                case 'd':mentionCase = Case.DATIVE; break;
                case 'a':mentionCase = Case.ACCUSATIVE; break;
                case 'l':mentionCase = Case.LOCATIVE; break;
                //case 's':mentionCase = Case.NOMINATIVE; break; //ģenetīvenis
                default:mentionCase = Case.UNKNOWN;
            };
            
            switch(node.tag.charAt(1)) {
                case 'p':pronounType = PronounType.PERSONAL; break;
                case 'x':pronounType = PronounType.REFLEXIVE; break;
                case 's':pronounType = PronounType.POSSESIVE; break;
                case 'd':pronounType = PronounType.DEMONSTRATIVE; break;
                case 'i':pronounType = PronounType.INDEFINITE; break;
                case 'q':pronounType = PronounType.INTERROGATIVE; break; //ģenetīvenis
                case 'r':pronounType = PronounType.RELATIVE; break;
                case 'g':pronounType = PronounType.DEFINITE; break;
                default:pronounType = PronounType.UNKNOWN;
            };
            person = node.tag.charAt(2)-'0';
            this.comments = "";

        } else {
            gender = Gender.UNKNOWN;
            number = Number.UNKNOWN;            
            mentionCase = Case.UNKNOWN;
            Log.inf("Unsuported tag: " + node.tag);
        }        
    }
    
    public boolean sameGender(Mention m) {
        if (gender == Gender.UNKNOWN || m.gender == Gender.UNKNOWN) return true;
        if (gender == m.gender) return true;
        return false;
    }
    
    public boolean samePerson(Mention m) {
        if (person == m.person) return true;
        return false;
    }
    
    public boolean sameNumber(Mention m) {
        if (number == Number.UNKNOWN || m.number == Number.UNKNOWN) return true;
        if (number == m.number) return true;
        return false;
    }
    
    public boolean sameCase(Mention m) {
        if (mentionCase == Case.UNKNOWN || m.mentionCase == Case.UNKNOWN) return true;
        if (mentionCase == m.mentionCase) return true;
        return false;
    }
    
    /**
     * If m is m is dominated or dominates m
     * @param m
     * @return 
     */
    public boolean isGenitive(Mention m) {
        if (node.isNounGenitive() 
        		&& node.parent != null 
        		&& node.parent.mention == m) {
        	return true;
        }
        if (m.node.isNounGenitive() 
        		&& m.node.parent != null 
        		&& m.node.parent.mention == this) {
        	return true;
        }
        else return false;
    }
    
    public boolean isPerson() {
        //return category.equals("person");
        return categories.contains("person");
    }
    
    public String toString() {
        return String.format("[%s] | @source=%s | @head=%s | @id=%d | (%s)", 
        		nerString, source, headString, id, getContext(document, 3));
        
    }
    
    public boolean categoryMatch(Mention m) {
        if (categories == null 
        		|| m.categories == null 
        		|| categories.size() == 0 
        		|| m.categories.size()==0) {
        	return true;
        }
        if ((categories.contains("person") 
        		|| categories.contains("profession")) 
        		&& (m.categories.contains("person") 
        				|| m.categories.contains("profession"))) {
        	return true;
        }
        if (categories.contains("other") || m.categories.contains("other")) {
        	return true;
        }
        if (categories.containsAll(m.categories)) return true;
        return false;
    }
    
    public Mention prev(Document d) {
        if (id > 0) return d.mentions.get(id-1);
        return null;
    }
    public Mention prev() {
        if (id > 0) return document.mentions.get(id-1);
        return null;
    }
    
    public Mention prevGold() {
        if (id > 0) return document.goldMentions.get(id-1);
        return null;
    }
    
    public void addRefComm(Mention m, String s) {
        comments += ";"+m.node.word +"#"+m.id + "\""+s+"\"";
    }
    
    public Boolean needsReso() {
        return !this.resolved;
    }
    
    public void setAsResolved() {
        this.resolved = true;
    }
    
    public Boolean isSingleton(Document d) {
        if (d.corefClusters.get(this.corefClusterID).corefMentions.size() < 2) {
        	return true;
        }
        else return false;
    }
    
    /**
     * Set categories for mentions, try to fix mentions without categories
     * Used if categories not set by other tools
     */
    public void setCategories() {
    	// from annotation schema
        if (categories.contains("other")) {
            categories.remove("other");
        }
        if (categories.size() == 0) {
        	if (!node.idType.equals("")) categories.add(node.idType);
        	else if (node.ne_annotation != null
        			&& !node.ne_annotation.equals("")
        			&& !node.ne_annotation.equals("O")) {
        		categories.add(node.ne_annotation);
        	}
        }
        // from LVCoref lists
        categories.addAll(document.dict.getCategories(node.lemma)); 
        // FIXME can add unnecessary categories for proper NE
    }
    
    /**
     * Initialize mention type
     * Used if type not set by other tools
     */
    public void setType() {
    	if (type == MentionType.UNKNOWN) {
    		type = node.getType(); // based on root node
    	}
    }
    
    
    @Override 
    public int compareTo(Mention m) {
        return this.node.id - m.node.id;
    }  
    
    
    public String getContext(Document d, int size) {
        return d.getSubString(this.start-size, this.end+size);
    }
 
    
    public String getAcronym() {
        Document d = this.document;
        String r = "";
        Set<String> exclude = new HashSet<String>(Arrays.asList("un"));
        for(int i = start; i <= end; i++) {
            Node q = d.tree.get(i);
            if (exclude.contains(
                q.lemma)) continue;
            r += q.word.charAt(0);
        }
        return r.toUpperCase();
    }
    
    
    public boolean isQuote() {
    	boolean res = false;
    	if (source == MentionSource.QUOTE) res = true;
    	else {
    		Node prev = document.getNode(start-1);
    		Node next = document.getNode(end+1);
    		if (prev != null && next != null 
    				&& prev.isQuote() && next.isQuote()) res = true;
    	}
        return res;
    }
    
    // Vai šis mention ir vairāk reprezentatīvs par p
    public boolean moreRepresentative(Mention p) {
        if (p == null) return true;
        //System.err.println(nerString +  "("+(category!=null?category:"null")+")"+ " : " + p.nerString + "("+(p.category!=null?p.category:"null")+")");
        if (this.type == MentionType.PRONOMINAL) return false; // PP - lai nav vietniekvārdi kā reprezentatīvākie
        if (p.type == MentionType.PRONOMINAL) return true;
        if (p.category != null && this.category != null 
        		&& p.category.equals("profession") 
        		&& this.category.equals("person") 
        		&& type == MentionType.PROPER) return true;
        if (p.category != null && this.category != null 
        		&& p.category.equals("person") 
        		&& this.category.equals("profession")
        		&& p.type == MentionType.PROPER) return false;
        if (p.type != MentionType.PROPER && type == MentionType.PROPER){
        	return true;
        }
        if (getLength() > p.getLength()) {
            return true;
        }
        if (node.id < p.node.id) return true;
        return false;
    }
    
    public int getLength() {
        int len = 0;
        for (int i = start; i <= end; i++) {
            len += document.tree.get(i).lemma.length();
        }
        return len;
    }
    
    public boolean titleRepresentative(){
        if (type == Dictionaries.MentionType.PROPER) return true;
        return false;
    }
}
