
public class Mention {
//	public Integer id;
//	public String s;
//	public Integer start, end, root;
//	Mention(Integer id, String s, int start, int end, int root) {
//		this.id = id;
//		this.s = s;
//		this.start = start;
//		this.end = end;
//		this.root = root;
//	}
	
	
	
	public static String getType(String lemma, String tag) {
		if (tag.charAt(0) == 'n') {
			if (tag.charAt(1)=='p') {
				return "PROPER";
			} else {
				return "COMMON";
			}
		} else if (tag.charAt(0) == 'p') {
			return "PRONOUN";
		} else {
			return "NONE";
		}
	}
}
