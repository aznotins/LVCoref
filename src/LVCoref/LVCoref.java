package LVCoref;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Main process class
 * @author arturs
 */
public class LVCoref {

	public static void main(String[] args) throws Exception {
        String input_file = "data/input.conll";
        String output_file = "data/output.conll";
        String output_html = "data/output.html";
        
        
        if (args.length > 2) {
            for (String s: args) {
                if (s.startsWith("--input_file=")) {
                    input_file = s.replaceAll("\"|--input_file=", "");
                } else if (s.startsWith("--output_file=")) {
                    output_file = s.replaceAll("\"|--output_file=", "");
                } else if (s.startsWith("--output_html=")) {
                    output_html = s.replaceAll("\"|--output_html=", "");
                }
            }
        } else {
            
        }
            Document d;
            d = new Document();

            //System.out.println(d.dict.firstNames);

            //d.readCONLL("data/Sofija.conll");
           // d.readCONLL("data/SofijasPasaule1996_11-28-dep-unlabeled.conll");
            //d.readCONLL("data/intervija-unlabeled.conll");
           //d.readCONLL("data/LETA_IzlaseFreimiem-dep-unlabeled.conll");
           d.readCONLL(input_file);

            d.setMentions();
            for (Mention m : d.mentions) {
                m.setCategories(d);
            }

            //d.visualizeParseTree();



    //        List<Node> tmp;
    //        tmp = d.traverse(d.tree.get(1), null, new ArrayList<Node>(Arrays.asList(d.tree.get(1))));
    //        d.printNodes(tmp);

            //d.printMentions();
            //d.printNodes(d.tree);


            d.initializeEntities();
            Resolve.go(d);


            for (Node n : d.tree) {
                n.markMentionBorders(d, false);
            }

            //RefsToEntities.go(d);

            //d.printCoreferences();

            d.addAnnotationMMAX("data/LVCoref_coref_level.xml");
            
            if (!output_file.isEmpty()) {
               d.outputCONLL(output_file);
               d.outputCONLLforDavis(output_file+".davis");
            }
            
            if (!output_html.isEmpty()) {
               d.htmlOutput(output_html);
            }
            
            


    //		for (Node n: d.tree) {
    //			System.out.print(" " + n.word);
    //			if (n.mention != null && d.corefClusters.get(n.mention.corefClusterID).corefMentions.size() > 1) {
    //                Mention ant = d.refGraph.getFinalResolutions().get(n.mention);
    //                System.out.print("["+n.mention.corefClusterID+"/"+n.mention.id+"/"+((ant == null)?null:ant.id)+"/"+n.mention.type+"/"+n.mention.categories+"@"+n.mention.comments+"]");
    //            }
    //			if (n.word.equals(".")) System.out.println();
    //		}
    //		System.out.println();

        };
        
	

}
