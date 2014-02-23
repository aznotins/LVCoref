package LVCoref;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;


/**
 *
 * @author Artūrs
 */
public class WekaWrapper {
    
    public static Instances toArff(Document d) throws Exception {
        List<Document> docs = new LinkedList<Document>();
        docs.add(d);
        return toArff2(docs);
   }
    
    public static Instances toArff(Collection<Document> docs) throws Exception {
        FastVector      atts;
        Instances       data;  
        double[]        vals;
        FastVector      attVals;

        atts = new FastVector();

        FastVector true_false = new FastVector(); true_false.addElement("true");true_false.addElement("false");

        atts.addElement(new Attribute("coreference", true_false));
        atts.addElement(new Attribute("same_head", true_false));
        atts.addElement(new Attribute("distance"));
        atts.addElement(new Attribute("exact_match", true_false));
        atts.addElement(new Attribute("i_pronoun", true_false));
        atts.addElement(new Attribute("j_pronoun", true_false));
        atts.addElement(new Attribute("i_proper", true_false));

        data = new Instances("Coreferences", atts, 0);

        for (Document d: docs) {
           for (Mention m : d.mentions) {
               Mention ant = m.prev();
               while (ant != null) {
                   if (ant.corefClusterID == m.corefClusterID || 
                          m.sentNum - ant.sentNum < 10) {
                      vals = new double[data.numAttributes()];
                      vals[0] = true_false.indexOf(Boolean.toString(ant.node.goldMention.goldCorefClusterID == m.node.goldMention.goldCorefClusterID));
                      vals[1] = true_false.indexOf(Boolean.toString(Filter.sameHead(ant, m)));
                      vals[2] = m.node.id - ant.node.id;
                      vals[3] = true_false.indexOf(Boolean.toString(Filter.exactMatch(ant, m)));
                      vals[4] = true_false.indexOf(Boolean.toString(Filter.pronominal(m)));
                      vals[5] = true_false.indexOf(Boolean.toString(Filter.pronominal(ant)));
                      vals[6] = true_false.indexOf(Boolean.toString(Filter.proper(m)));
                      data.add(new Instance(1.0, vals));
                   }
                  ant = ant.prevGold();
               }
           }
        }
        return data;
   }
    
    
    public static Instances toArff2(Collection<Document> docs) throws Exception {
        FastVector      atts;
        Instances       data;  
        double[]        vals;
        FastVector      attVals;

        atts = new FastVector();
        
        FeatureFactory ff = new FeatureFactory();
        ff.init(new Flags());

        FastVector true_false = new FastVector(); true_false.addElement("true");true_false.addElement("false");
        
        
        ff.features(null, null);
        for (int i = 0; i < ff.types.size(); i++) {
            switch (ff.types.get(i)) {
                case BOOL: 
                    atts.addElement(new Attribute(ff.featNames.get(i), true_false));
                    break;
                default:
                    atts.addElement(new Attribute(ff.featNames.get(i)));
            }
        }
        atts.addElement(new Attribute("coreferent", true_false));
        
        
        //System.out.println(ff.types);
        //System.out.println(ff.featNames);

        data = new Instances("Coreferences", atts, 0);
int c= 0;
        for (Document d: docs) {
           d.useGoldClusters();
            
           for (Mention m : d.mentions) {
               Mention ant = m.prev();
               int cc = 0;
               while (ant != null) {
                   cc++;
                   if (ant.corefClusterID == m.corefClusterID || 
                          cc++ < 5 && m.sentNum - ant.sentNum < 10) {
                       
                       
                       
                       List<String> features = ff.features(m, ant);
                       //System.out.println(features);
                      vals = new double[features.size()+1];
                      int idx = 0;
                      for (String fs : features) {
                          vals[idx++] = true_false.indexOf(fs);
                          
                          //System.out.println(fs)
                      }
                      vals[vals.length-1] = true_false.indexOf(Boolean.toString(ant.node.goldMention.goldCorefClusterID == m.node.goldMention.goldCorefClusterID));
                      data.add(new Instance(1.0, vals));
                      c++;
                      //if (ant.corefClusterID == m.corefClusterID) break;
                   }
                  ant = ant.prevGold();
               }
           }
        }
        
        
        

        System.out.println(c);
        
        return data;
   }
    
    
    
    
    
    
    
    public static void main(String[] args) {
        try {
            List<Document> docs = new LinkedList<Document>();
            Document d = new Document();
            d.readCONLL("data/pipeline/interview_16.lvsem.conll");
            d.addAnnotationMMAX("data/interview_16_coref_level.xml");
            d.useGoldMentions();
            docs.add(d);
            d = new Document();
            d.readCONLL("data/pipeline/interview_23.lvsem.conll");
            d.addAnnotationMMAX("data/interview_23_coref_level.xml");
            d.useGoldMentions();
            docs.add(d);
            d = new Document();
            d.readCONLL("data/pipeline/interview_27.lvsem.conll");
            d.addAnnotationMMAX("data/interview_27_coref_level.xml");
            d.useGoldMentions();
            docs.add(d);
            d = new Document();
            d.readCONLL("data/pipeline/interview_38.lvsem.conll");
            d.addAnnotationMMAX("data/interview_38_coref_level.xml");
            d.useGoldMentions();
            docs.add(d);
            
            
            Instances train = toArff2(docs);
            train.setClassIndex(train.numAttributes()-1);
            String [] options  = {"-U"};//, "-C", "0.5"};
            Classifier cls = new J48();
            cls.setOptions(options);
            cls.buildClassifier(train);
            

            docs = new LinkedList<Document>();
            d = new Document();
            d.readCONLL("data/pipeline/interview_43.lvsem.conll");
            d.addAnnotationMMAX("data/interview_43_coref_level.xml");
            d.useGoldMentions();
            docs.add(d);
            d = new Document();
            d.readCONLL("data/pipeline/interview_46.lvsem.conll");
            d.addAnnotationMMAX("data/interview_46_coref_level.xml");
            d.useGoldMentions();
            docs.add(d);
            
            Evaluation eval = new Evaluation(train);
            
            Instances data = toArff2(docs);
            data.setClassIndex(data.numAttributes() - 1);
            for (int i = 0; i < data.numInstances(); i++) {
                double clsLabel = cls.classifyInstance(data.instance(i));
                //System.out.println(clsLabel);
                data.instance(i).setClassValue(clsLabel);
                System.out.println(data.instance(i).toString(data.classIndex()));
            }
            
            
            
//     eval.crossValidateModel(cls, train, 10, new Random(1));
//            // generate curve
//     ThresholdCurve tc = new ThresholdCurve();
//     //int classIndex = test.numAttributes()-1;
//     Instances result = tc.getCurve(eval.predictions());//, classIndex);
// 
//     // plot curve
//     ThresholdVisualizePanel vmc = new ThresholdVisualizePanel();
//     vmc.setROCString("(Area under ROC = " + 
//         weka.core.Utils.doubleToString(tc.getROCArea(result), 4) + ")");
//     vmc.setName(result.relationName());
//     PlotData2D tempd = new PlotData2D(result);
//     tempd.setPlotName(result.relationName());
//     tempd.addInstanceNumberAttribute();
//     // specify which points are connected
//     boolean[] cp = new boolean[result.numInstances()];
//     for (int n = 1; n < cp.length; n++)
//       cp[n] = true;
//     tempd.setConnectPoints(cp);
//     // add plot
//     vmc.addPlot(tempd);
// 
//     // display curve
//     String plotName = vmc.getName(); 
//     final javax.swing.JFrame jf = 
//       new javax.swing.JFrame("Weka Classifier Visualize: "+plotName);
//     jf.setSize(500,400);
//     jf.getContentPane().setLayout(new BorderLayout());
//     jf.getContentPane().add(vmc, BorderLayout.CENTER);
//     jf.addWindowListener(new java.awt.event.WindowAdapter() {
//       public void windowClosing(java.awt.event.WindowEvent e) {
//       jf.dispose();
//       }
//     });
//     jf.setVisible(true);
            
            
            
//            Instances test = toArff2(docs);
//            test.setClassIndex(test.numAttributes()-1);
//            
//            
//           Evaluation evals = new Evaluation(train); 
//
//            evals.evaluateModel(cls, test);
//            System.out.println(evals.toSummaryString("\nResults\n======\n", false));
//             System.out.println(evals.toMatrixString());
//              System.out.println(evals.toClassDetailsString());
//            
//            System.out.println(cls);
//            //System.out.println(toArff2(docs));
            
            
            
            
        } catch (Exception ex) {
            Logger.getLogger(WekaWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
    public static void main1(String[] args) throws Exception {
     FastVector      atts;
     FastVector      attsRel;
     FastVector      attVals;
     FastVector      attValsRel;
     Instances       data;
     Instances       dataRel;
     double[]        vals;
     double[]        valsRel;
     int             i;
 
     // 1. set up attributes
     atts = new FastVector();
     // - numeric
     atts.addElement(new Attribute("att1"));
     // - nominal
     attVals = new FastVector();
     for (i = 0; i < 5; i++)
       attVals.addElement("val" + (i+1));
     atts.addElement(new Attribute("att2", attVals));
     // - string
     atts.addElement(new Attribute("att3", (FastVector) null));
     // - date
     atts.addElement(new Attribute("att4", "yyyy-MM-dd"));
     // - relational
     attsRel = new FastVector();
     // -- numeric
     attsRel.addElement(new Attribute("att5.1"));
     // -- nominal
     attValsRel = new FastVector();
     for (i = 0; i < 5; i++)
       attValsRel.addElement("val5." + (i+1));
     attsRel.addElement(new Attribute("att5.2", attValsRel));
     dataRel = new Instances("att5", attsRel, 0);
     atts.addElement(new Attribute("att5", dataRel, 0));
 
     // 2. create Instances object
     data = new Instances("MyRelation", atts, 0);
 
     // 3. fill with data
     // first instance
     vals = new double[data.numAttributes()];
     // - numeric
     vals[0] = Math.PI;
     // - nominal
     vals[1] = attVals.indexOf("val3");
     // - string
     vals[2] = data.attribute(2).addStringValue("This is a string!");
     // - date
     vals[3] = data.attribute(3).parseDate("2001-11-09");
     // - relational
     dataRel = new Instances(data.attribute(4).relation(), 0);
     // -- first instance
     valsRel = new double[2];
     valsRel[0] = Math.PI + 1;
     valsRel[1] = attValsRel.indexOf("val5.3");
     dataRel.add(new Instance(1.0, valsRel));
     // -- second instance
     valsRel = new double[2];
     valsRel[0] = Math.PI + 2;
     valsRel[1] = attValsRel.indexOf("val5.2");
     dataRel.add(new Instance(1.0, valsRel));
     vals[4] = data.attribute(4).addRelation(dataRel);
     // add
     data.add(new Instance(1.0, vals));
 
     // second instance
     vals = new double[data.numAttributes()];  // important: needs NEW array!
     // - numeric
     vals[0] = Math.E;
     // - nominal
     vals[1] = attVals.indexOf("val1");
     // - string
     vals[2] = data.attribute(2).addStringValue("And another one!");
     // - date
     vals[3] = data.attribute(3).parseDate("2000-12-01");
     // - relational
     dataRel = new Instances(data.attribute(4).relation(), 0);
     // -- first instance
     valsRel = new double[2];
     valsRel[0] = Math.E + 1;
     valsRel[1] = attValsRel.indexOf("val5.4");
     dataRel.add(new Instance(1.0, valsRel));
     // -- second instance
     valsRel = new double[2];
     valsRel[0] = Math.E + 2;
     valsRel[1] = attValsRel.indexOf("val5.1");
     dataRel.add(new Instance(1.0, valsRel));
     vals[4] = data.attribute(4).addRelation(dataRel);
     // add
     data.add(new Instance(1.0, vals));
 
     // 4. output data
     System.out.println(data);
   }
}
