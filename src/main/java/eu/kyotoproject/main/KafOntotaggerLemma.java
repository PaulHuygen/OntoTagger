package eu.kyotoproject.main;

import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafSense;
import eu.kyotoproject.kaf.KafTerm;
import eu.kyotoproject.util.Util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 1/29/13
 * Time: 3:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class KafOntotaggerLemma {
	private static String pathToLemmaClasses = "";

    static public void main (String[] args) {
    	String pathToKafFile = "";
        HashMap<String, ArrayList<String>> lemmaLabels = new HashMap<String, ArrayList<String>>();


        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ((arg.equalsIgnoreCase("--kaf-file")) && (args.length>(i+1))) {
                pathToKafFile = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--lemma-classes-file")) && (args.length>(i+1))) {
                pathToLemmaClasses = args[i+1];
                lemmaLabels = Util.ReadFileToStringHashMap("\t", pathToLemmaClasses);
            }
        }
        KafSaxParser kafSaxParser = new KafSaxParser();
        kafSaxParser.parseFile(pathToKafFile);
        for (int i = 0; i < kafSaxParser.getKafTerms().size(); i++) {
            KafTerm kafTerm = kafSaxParser.getKafTerms().get(i);
            String lemma = kafTerm.getLemma();
            if(lemmaLabels.containsKey(lemma)) {
            	ArrayList<String> labels = lemmaLabels.get(lemma);
                for (int j = 0; j < labels.size(); j++) {
                    String s = labels.get(j);
                    KafSense kafSense = new KafSense();
                    kafSense.setSensecode(s);
                    kafTerm.addSenseTag(kafSense);
                }
            }
        }
        kafSaxParser.writeKafToStream(System.out);
    }



}
