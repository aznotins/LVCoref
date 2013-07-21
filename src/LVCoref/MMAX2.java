/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package LVCoref;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.NodeList;

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
        Utils.toWriter(writer, "<?xml version=\"1.0\" encoding=\""+"UTF-8"+"\"?>\n"
                + "<!DOCTYPE markables SYSTEM \"markables.dtd\">\n"
                + "<markables xmlns=\"www.eml.org/NameSpaces/"+"coref"+"\">\n");        
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
        Utils.toWriter(writer, "<?xml version=\"1.0\" encoding=\""+"UTF-8"+"\"?>\n"
                + "<!DOCTYPE words SYSTEM \"words.dtd\">\n"
                + "<words>\n");
        
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
    
    
    
    public static Boolean addMmaxNeAnnotation(Document d, String annotation_filename) {
        try {
            File mmax_file = new File(annotation_filename);
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 
            org.w3c.dom.Document doc = dBuilder.parse(mmax_file);
            NodeList markables = doc.getElementsByTagName("markable");
            
            for (int i = 0; i < markables.getLength(); i++) {
                org.w3c.dom.Node markable = markables.item(i);

                String span = markable.getAttributes().getNamedItem("span").getNodeValue();
                String category = markable.getAttributes().getNamedItem("category").getNodeValue();
                
                String[] intervals = span.split(",");
                String[] interval = intervals[0].split("\\.\\.");
                int start = Integer.parseInt(interval[0].substring(5)) - 1 ;
                int end = start;
                if (interval.length > 1) {
                    end = Integer.parseInt(interval[1].substring(5)) - 1;
                }
                System.err.println(i+" :" + start+ "-"+end);
//                if (category.equals("profession")) category = "person";
//                if (category.equals("event")) continue;
//                //if (category.equals("product")) continue;
//                if (category.equals("media")) continue;
//                if (category.equals("time")) continue;
//                if (category.equals("sum")) continue;
                
                if (category.equals("other")) continue;
                if (d.getNode(start).ne_annotation.length() != 0 || d.getNode(start).ne_annotation.length() != 0) continue;
                
                //if (d.getNode(start).isProperByFirstLetter()) {
                    for (int j = start; j <= end; j++) {
                        //System.out.println(j);
                        Node q = d.getNode(j);
                        q.ne_annotation = category;
                    }
                //}
            }
        } catch (Exception e) {
            System.err.println("Error adding MMAX2 annotation:" + e.getMessage());
            return false;
        }
        return true;
    }
    
    
    public static void exportNeAnnotation(Document d, String export_filename) {
        PrintWriter out;
        try {
            String eol = System.getProperty("line.separator");
            out = new PrintWriter(new FileWriter(export_filename));
            StringBuilder s = new StringBuilder();
            for (Node n : d.tree) {
                s.append(n.conll_fields.get(1)); s.append("\t");
                s.append(n.conll_fields.get(3).charAt(0)); s.append("\t");
                s.append(n.conll_fields.get(2)); s.append("\t");
                s.append(n.conll_fields.get(4)); s.append("\t");
                if (n.ne_annotation.length() == 0) n.ne_annotation = "O";
                s.append( n.ne_annotation);
                s.append(eol);
                if (n.sentEnd) s.append(eol);
            }
            out.print(s.toString());
            out.flush();
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(Document.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("ERROR: couldn't create/open output conll file");
        }
    }
    
    
    public static void main(String[] args) throws Exception {
        Document d = new Document();
        
        String mmax = "data/interview_16_coref_level.xml";
        String conll = "data/pipeline/interview_16.lvsem.conll";
        String ne_export = "data/pipeline/interview_16.ne_export.tab";
        
//        String mmax = "data/interview_23_coref_level.xml";
//        String conll = "data/pipeline/interview_23.lvsem.conll";
//        String ne_export = "data/pipeline/interview_23.ne_export.tab";

//        String mmax = "data/interview_27_coref_level.xml";
//        String conll = "data/pipeline/interview_27.lvsem.conll";
//        String ne_export = "data/pipeline/interview_27.ne_export.tab";

//        String mmax = "data/interview_38_coref_level.xml";
//        String conll = "data/pipeline/interview_38.lvsem.conll";
//        String ne_export = "data/pipeline/interview_38.ne_export.tab";

//        String mmax = "data/interview_43_coref_level.xml";
//        String conll = "data/pipeline/interview_43.lvsem.conll";
//        String ne_export = "data/pipeline/interview_43.ne_export.tab";

//        String mmax = "data/interview_46_coref_level.xml";
//        String conll = "data/pipeline/interview_46.lvsem.conll";
//        String ne_export = "data/pipeline/interview_46.ne_export.tab";
        
        d.readCONLL(conll); 
        
        addMmaxNeAnnotation(d, mmax);
        exportNeAnnotation(d, ne_export);
        
    }
}
