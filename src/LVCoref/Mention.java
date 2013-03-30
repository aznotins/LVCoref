package LVCoref;

import LVCoref.Dictionaries.Animacy;
import LVCoref.Dictionaries.Gender;
import LVCoref.Dictionaries.MentionType;
import LVCoref.Dictionaries.Number;
import LVCoref.Dictionaries.Case;
import LVCoref.Dictionaries.PronounType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Mention{
    
	public Integer id;
    
	public String headString;
    public String nerString = "";
    
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
    
    public String comments;

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
  
    
    Mention(Document d, int id, int start, int end, Node node, String ner) {
        this.id = id;
		this.start = start;
		this.end = end;
		this.root = node.id;
        this.node = node;
        this.headString = node.lemma;
        this.nerString = ner;
                
        this.sentNum = node.sentNum;
        
        this.type = getType("", node.tag);
        
        //this.category = d.dict.getCategory(node.lemma);
        this.categories = d.dict.getCategories(node.lemma);
        
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
            
            switch(node.tag.charAt(4)) {
                case 'n':mentionCase = Case.NOMINATIVE; break;
                case 'g':mentionCase = Case.GENITIVE; break;
                case 'd':mentionCase = Case.DATIVE; break;
                case 'a':mentionCase = Case.ACCUSATIVE; break;
                case 'l':mentionCase = Case.LOCATIVE; break;
                //case 's':mentionCase = Case.NOMINATIVE; break; //ģenetīvenis
                default:mentionCase = Case.UNKNOWN;
            };
            
            switch(node.tag.charAt(2)) {
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

        }
        
    }

    
    public static MentionType getType(String lemma, String tag) {
		if (tag.charAt(0) == 'n') {
			if (tag.charAt(1)=='p') {
				return MentionType.PROPER;
            }
		} else if (tag.charAt(0) == 'p') {
			return MentionType.PRONOMINAL;
		}
        return MentionType.NOMINAL;
	}
    
    public String toString() {
      StringBuilder result = new StringBuilder();
      String newLine = System.getProperty("line.separator");

      result.append( this.getClass().getName() );
      result.append( " Object {" );
      result.append(newLine);

      //determine fields declared in this class only (no fields of superclass)
      Field[] fields = this.getClass().getDeclaredFields();

      //print field names paired with their values
      for ( Field field : fields  ) {
        result.append("  ");
        try {
          result.append( field.getName() );
          result.append(": ");
          //requires access to private field:
          result.append( field.get(this) );
        } catch ( IllegalAccessException ex ) {
          System.out.println(ex);
        }
        result.append(newLine);
      }
      result.append("}");
      result.append(newLine);

      return result.toString();
    }
    
    
    public Mention prev(Document d) {
        if (id > 0) return d.mentions.get(id-1);
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
 
}
