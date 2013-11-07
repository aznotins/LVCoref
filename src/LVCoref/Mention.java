package LVCoref;

import LVCoref.Dictionaries.Animacy;
import LVCoref.Dictionaries.Gender;
import LVCoref.Dictionaries.MentionType;
import LVCoref.Dictionaries.Number;
import LVCoref.Dictionaries.Case;
import LVCoref.Dictionaries.PronounType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Mention implements Comparable<Mention>{
    Document document;
	public Integer id;
	public String sel = "O"; // source comment
    
    public Boolean resolved = false;
    public String bucket = ""; //acronym quote etc
    
	public String headString;
    public String nerString = "";
    public String normString;
    
    public boolean tmp = false;
    public boolean strict = false; //listed mentions, this head is important for genetives
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
    
    public MentionType type;
    public Animacy animacy;
    public Number number;
    public Gender gender;
    
    public Case mentionCase;
    public PronounType pronounType;
    
    public int person;
    public String category;
    
    public String comments = "";
    public Set<String> words;
    public Set<String> modifiers;
    public Set<String> properModifiers;

//    public boolean isSubject;
//    public boolean isDirectObject;
//    public boolean isIndirectObject;
//    public boolean isPrepositionObject;
//    public IndexedWord dependingVerb;
//    public boolean twinless = true;
//    public boolean generic = false;
    
    /**
     * Coreference information
     */
    public int corefClusterID = -1;
    public int goldCorefClusterID = -1;    
    
    Set<String> categories;
    
    Mention(Mention m) {
        id = m.id;
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
  
    
    Mention(Document d, int id, Node node, MentionType type, int start, int end) {
        this.id = id;
		this.root = node.id;
        this.node = node;
        this.headString = node.lemma;
        //this.nerString = ner;
        //        this.start = node.getSpanStart(d).id;
//        this.end = node.getSpanEnd(d).id;
        this.start = start;
        this.end = end;
        //this.nerString = node.nodeProjection(d);
        this.nerString = d.getSubString(start, end);
                
        this.sentNum = node.sentNum;
        categories = new HashSet<String>();
        words = new HashSet<String>();
        modifiers = new HashSet<String>();
        properModifiers = new HashSet<String>();
        this.type = type;
        document = d;
        
        //this.category = d.dict.getCategory(node.lemma);
        //this.categories = d.dict.getCategories(node.lemma);
        
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
            
            //@TODO Anafora tags?
            
            person = node.tag.charAt(2)-'0';
            this.comments = "";
           
            
            

        } else {
            gender = Gender.UNKNOWN;
            number = Number.UNKNOWN;            
            mentionCase = Case.UNKNOWN;
            d.logger.fine("Unsuported tag: " + node.tag);
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
        if (node.isNounGenitive() && node.parent != null && node.parent.mention == m) return true;
        if (m.node.isNounGenitive() && m.node.parent != null && m.node.parent.mention == this) return true;
        else return false;
    }
    
    public boolean isPerson() {
        //return category.equals("person");
        return categories.contains("person");
    }
    
    public String toString() {
        return "["+nerString + "] " + "@head="+ headString + " "+" @id="+id+ " "+ " ("+getContext(document, 3) + ")";
        
//      StringBuilder result = new StringBuilder();
//      String newLine = System.getProperty("line.separator");
//
//      result.append( this.getClass().getName() );
//      result.append( " Object {" );
//      result.append(newLine);
//
//      //determine fields declared in this class only (no fields of superclass)
//      Field[] fields = this.getClass().getDeclaredFields();
//
//      //print field names paired with their values
//      for ( Field field : fields  ) {
//        result.append("  ");
//        try {
//          result.append( field.getName() );
//          result.append(": ");
//          //requires access to private field:
//          result.append( field.get(this) );
//        } catch ( IllegalAccessException ex ) {
//          System.out.println(ex);
//        }
//        result.append(newLine);
//      }
//      result.append("}");
//      result.append(newLine);
//
//      return result.toString();
    }
    
    public boolean categoryMatch(Mention m) {
        if (categories == null || m.categories == null || categories.size() == 0 ||  m.categories.size()==0) return true;
        if (categories.contains("other") || m.categories.contains("other")) return true;
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
    
    public Mention prevSyntactic(Document d) {
        for (Node n : node.children);
        	
        
        if (id > 0) return d.mentions.get(id-1);
        return null;
    }
    
    
    public Set<Node> traverse(Set<Node> from, Set<Node> exclude) {
        
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
        if (d.corefClusters.get(this.corefClusterID).corefMentions.size() < 2) return true;
        else return false;
    }
    
    public void setCategories(Document d) {
        this.categories = d.dict.getCategories(node.lemma);
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
    	if (bucket.equals("quote")) res = true;
    	else {
    		Node prev = document.getNode(start-1);
    		Node next = document.getNode(end+1);
    		if (prev != null && next != null && prev.isQuote() && next.isQuote()) res = true;
    	}
        return res;
    }
    
    public boolean moreRepresentative(Mention p) {
        if (p == null) return true;
        if (p.type!=Dictionaries.MentionType.PROPER && type==Dictionaries.MentionType.PROPER) return true;
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
