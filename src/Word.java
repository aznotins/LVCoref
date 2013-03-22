public class Word{
	String word;
	String lemma;
	String tag;
	Word(String word, String lemma, String tag, int id) {
		this.tag = tag;
		this.lemma = lemma;
		this.word = word;
	}
}