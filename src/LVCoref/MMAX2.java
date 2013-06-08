/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package LVCoref;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author Artūrs
 */
public class MMAX2 {
    public static void exportMentions(Document d, String filename){
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename),"UTF-8"));
        }
        catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
        Utils.toWriter(writer, "<?xml version=\"1.0\" encoding=\""+"UTF-8"+"\"?>\n<!DOCTYPE markables SYSTEM \"markables.dtd\">\n<markables xmlns=\"www.eml.org/NameSpaces/"+"coref"+"\">\n");
                    //writer.write("<?xml version=\"1.0\" encoding=\""+"UTF-8"+"\"?>\n<!DOCTYPE words SYSTEM \"words.dtd\">\n");
        
        for (Mention m : d.mentions) {
            String span = "";
            if (m.start == m.end) span = "word_"+(m.start+1);
            else span = "word_"+(m.start+1)+".."+"word_"+(m.end+1);
            String coref_class = "";
            if (d.corefClusters.get(m.corefClusterID).corefMentions.size() > 1) coref_class = "set_"+m.corefClusterID;
            else coref_class = "empty";
            String category = "other";
            if (m.category != null) {
                if (m.category.equals("ORG")) category = "organization";
                else if (m.category.equals("LOCATION")) category = "location";
                else if (m.category.equals("PERSON")) category = "person";
            }
            
            Utils.toWriter(writer, "<markable id=\"markable_"+(m.id+1)+"\" span=\""+span+"\" coref_class=\""+coref_class+"\" category=\""+category+"\" mmax_level=\"coref\"  rule=\"none\"  type=\"none\"/>\n");
        }
        Utils.toWriter(writer, "</markables>");
        try {
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Document.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void exportWords(Document d, String filename){
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename),"UTF-8"));
        }
        catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
        Utils.toWriter(writer, "<?xml version=\"1.0\" encoding=\""+"UTF-8"+"\"?>\n<!DOCTYPE words SYSTEM \"words.dtd\">\n<words>\n");
        
        for (Node n : d.tree) {
            Utils.toWriter(writer, "<word id=\"word_"+(n.id+1)+"\">"+StringEscapeUtils.escapeXml(n.word) + "</word>\n");
        }
        Utils.toWriter(writer, "</words>");
        try {
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Document.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void exportSentences(Document d, String filename){
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename),"UTF-8"));
        }
        catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
        Utils.toWriter(writer, "<?xml version=\"1.0\" encoding=\""+"UTF-8"+"\"?>\n<!DOCTYPE markables SYSTEM \"markables.dtd\">\n<markables xmlns=\"www.eml.org/NameSpaces/"+"sentence"+"\">\n");
        
        int start = -1;
        int end;
        int sentence_id = 0;
        for (Node n : d.tree) {
            if (n.sentStart) start = n.id+1;
            if (n.sentEnd) {
                end = n.id+1;
                sentence_id++;
                
                String span = "word_"+start;
                if (end != start) span += "..word_"+end;
                
                Utils.toWriter(writer, "<markable mmax_level=\"sentence\" id=\"markable_"+sentence_id+"\" span=\""+span+"\" />\n");
            }
        }
        Utils.toWriter(writer, "</markables>");
        try {
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Document.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * 
     * @param filename
     * @param path with trailing "/"
     */
    public static void createProject(Document d, String project, String path){        
        String words = path+project+"_words.xml";
        String coref_level = path+project+"_coref_level.xml";
        String sentence_level = path+project+"_sentence_level.xml";
        String project_file = path+project+".mmax";
        
        exportWords(d, words);
        exportMentions(d, coref_level);
        exportSentences(d, sentence_level);
        
        //-----Create project file
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(project_file),"UTF-8"));
        }
        catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
        Utils.toWriter(writer, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<mmax_project>\n"
                + "<words>"+project+"_words.xml"+"</words>\n"
                + "<keyactions></keyactions>\n"
                + "<gestures></gestures>\n"
                + "</mmax_project>\n");
        try {
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Document.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
