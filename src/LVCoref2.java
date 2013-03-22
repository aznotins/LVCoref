import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

//import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
//import edu.stanford.nlp.ling.CoreLabel;

//import edu.stanford.corenlp.ling.CoreLabel;
//import edu.stanford.nlp.ling.CoreAnnotations.ConllSyntaxAnnotation;
//import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
//import edu.stanford.nlp.sequences.LVMorphologyReaderAndWriter;


public class LVCoref2 {
	
	
	
	public static Document readCONLL(BufferedReader in) throws IOException {
		
		Document d = new Document();
		String s;
		Sentence sentence = new Sentence();
		while ((s = in.readLine()) != null) {
			if (s.trim().length() > 0) {
				String[] fields = s.split("\t");
				String token = fields[1];
				String lemma = fields[2];
				String tag = fields[3];
				
				Word word = new Word(token, lemma, tag);
				sentence.words.add(word);
			} else {
				d.sentences.add(sentence);
				sentence = new Sentence();
			}
		}
		if (sentence.words.size() > 0) {
			d.sentences.add(sentence);
		}
		
		return d;
		
	}

//	public static List<List<CoreLabel>> readCONLLs(BufferedReader in) throws IOException {
//		String s;
//	    List<CoreLabel> sentence = new LinkedList<CoreLabel>();
//	    List<List<CoreLabel>> result = new LinkedList<List<CoreLabel>>();
//	    
//	    CoreLabel stag = new CoreLabel();
//		stag.set(TextAnnotation.class, "<s>");
//		sentence.add(stag);
//	    
//	    while ((s = in.readLine()) != null) {
//	    	if (s.trim().length() > 0) {
//	    		String[] fields = s.split("\t");
//	    		String token = fields[1];
//	    		String syntax = fields[6] + "\t" + fields[7] + "\t" + fields[8] + "\t" + fields[9];
//
//	    		CoreLabel word = new CoreLabel();
//				word.set(TextAnnotation.class, token);
//				word.set(ConllSyntaxAnnotation.class, syntax);
//	    		sentence.add(word);
//	    	} else {
//	    		stag = new CoreLabel();
//	    		stag.set(TextAnnotation.class, "<s>");
//	    		sentence.add(stag);
//	    		
//	    		result.add(LVMorphologyReaderAndWriter.analyzeLabels(sentence));
//	    		
//	    		sentence = new LinkedList<CoreLabel>();
//	    		stag = new CoreLabel();
//	    		stag.set(TextAnnotation.class, "<s>");
//	    		sentence.add(stag);
//	    	}
//	    }
//	    if (sentence.size() > 0) {
//	    	stag = new CoreLabel();
//			stag.set(TextAnnotation.class, "<s>");
//			sentence.add(stag);
//	    	result.add(LVMorphologyReaderAndWriter.analyzeLabels(sentence));
//	    }
//	    		
//		return result;
//	}
	
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("Hello");
		Document d;
		BufferedReader in = null;
		try {
			
			in = new BufferedReader(new FileReader("Sofija.conll"));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
		}
		d = readCONLL(in);
		
		for (Sentence s: d.sentences) {
			for (Word w: s.words) {
				System.out.println(w.word);
			}
			
		}
		
		

	}

}
