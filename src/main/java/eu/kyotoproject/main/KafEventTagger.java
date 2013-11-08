package eu.kyotoproject.main;

import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafSense;
import eu.kyotoproject.kaf.KafTerm;
import eu.kyotoproject.util.Resources;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 1/29/13
 * Time: 3:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class KafEventTagger {

    static final String layer = "terms";
    static final String name = "vua-predicate-matrix-tagger";
    static final String version = "1.0";

    static public void main (String[] args) {
        Resources resources = new Resources();
        String pathToKafFile = "";
        String pathToMatrixFile = "";
        String pathToGrammaticalVerbsFile = "";
        String pmVersion = "";
        boolean ili = false;
        String pos = "";
        String key = "";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ((arg.equalsIgnoreCase("--kaf-file")) && (args.length>(i+1))) {
                pathToKafFile = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--pos")) && (args.length>(i+1))) {
                pos = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--predicate-matrix")) && (args.length>(i+1))) {
                pathToMatrixFile = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--grammatical-words")) && (args.length>(i+1))) {
                pathToGrammaticalVerbsFile = args[i+1];
            }

            else if ((arg.equalsIgnoreCase("--version")) && (args.length>(i+1))) {
                pmVersion = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--key")) && (args.length>(i+1))) {
                pmVersion = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--ili"))) {
                ili = true;
            }
        }
        if (ili) {
            resources.processMatrixFileWithWordnetILI(pathToMatrixFile);
        }
        else if (!key.isEmpty()) {
            resources.processMatrixFile(pathToMatrixFile, key);
        }
        else {
            resources.processMatrixFileWithWordnetLemma(pathToMatrixFile);
        }
        if (!pathToGrammaticalVerbsFile.isEmpty()) {
            resources.processGrammaticalWordsFile(pathToGrammaticalVerbsFile);
        }
        KafSaxParser kafSaxParser = new KafSaxParser();
        processKafFileWordnetNetSynsets(kafSaxParser, pathToKafFile, pmVersion, resources);
        kafSaxParser.writeNafToStream(System.out);
    }

    static public void processKafFileVerbNet (KafSaxParser kafSaxParser, String pathToKafFile, Resources resources, String pmVersion, String pos) {
        kafSaxParser.parseFile(pathToKafFile);
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(System.currentTimeMillis());
        String strdate = null;
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy-MM-dd HH:mm:ss");

        if (date != null) {
            strdate = sdf.format(date.getTime());
        }

        kafSaxParser.getKafMetaData().addLayer(layer, name, version, strdate);
        for (int i = 0; i < kafSaxParser.getKafTerms().size(); i++) {
            KafTerm kafTerm = kafSaxParser.getKafTerms().get(i);
            if ((pos.isEmpty() || (kafTerm.getPos().toLowerCase().startsWith(pos))) &&
                    !kafTerm.getLemma().isEmpty() &&
                    (resources.verbNetPredicateMap.containsKey(kafTerm.getLemma()))) {
                ArrayList<ArrayList<String>> mappings = resources.verbNetPredicateMap.get(kafTerm.getLemma());
                for (int j = 0; j < mappings.size(); j++) {
                    ArrayList<String> mapping = mappings.get(j);
                    KafSense kafSense = new KafSense();
                    kafSense.setResource(pmVersion);
                    kafSense.setSensecode(mapping.get(0));//// we assume that the first mapping represents the sensCode
                    for (int k = 0; k < mapping.size(); k++) {
                        String s = mapping.get(k);
                        String resource = s.substring(0, 2);
                        KafSense child = new KafSense();
                        child.setResource(resource);
                        child.setSensecode(s);
                        kafSense.addChildren(child);
                    }
                    kafTerm.addSenseTag(kafSense);
                }

            }
        }
    }

    static public void processKafFileWordnetNetSenseKeys (KafSaxParser kafSaxParser, String pathToKafFile, Resources resources, String pmVersion, String pos) {
        kafSaxParser.parseFile(pathToKafFile);
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(System.currentTimeMillis());
        String strdate = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (date != null) {
            strdate = sdf.format(date.getTime());
        }

        kafSaxParser.getKafMetaData().addLayer(layer, name, version, strdate);
        for (int i = 0; i < kafSaxParser.getKafTerms().size(); i++) {
            KafTerm kafTerm = kafSaxParser.getKafTerms().get(i);
            if ((pos.isEmpty() || (kafTerm.getPos().toLowerCase().startsWith(pos))) &&
                    !kafTerm.getLemma().isEmpty() &&
                    (resources.wordNetLemmaSenseMap.containsKey(kafTerm.getLemma()))) {
                ArrayList<String> senses = resources.wordNetLemmaSenseMap.get(kafTerm.getLemma());
                for (int j = 0; j < senses.size(); j++) {
                    String senseKey = senses.get(j);
                    KafSense kafSense = new KafSense();
                    kafSense.setResource(pmVersion);
                    kafSense.setSensecode(senseKey);
                    if (resources.wordNetPredicateMap.containsKey(senseKey)) {
                        ArrayList<ArrayList<String>> mappings = resources.wordNetPredicateMap.get(senseKey);
                        KafSense mChild = new KafSense ();
                        for (int m = 0; m < mappings.size(); m++) {
                            ArrayList<String> mapping =  mappings.get(m);
                            for (int k = 1; k < mapping.size(); k++) {
                                String s = mapping.get(k);
                                String resource = s.substring(0, 2);
                                KafSense child = new KafSense();
                                child.setResource(resource);
                                child.setSensecode(s);
                                mChild.addChildren(child);
                            }
                        }
                        kafSense.addChildren(mChild);

                    }
                    kafTerm.addSenseTag(kafSense);
                }

            }
        }
    }

    static public void processKafFileWordnetNetLemmas (KafSaxParser kafSaxParser, String pathToKafFile, Resources resources, String pmVersion, String pos) {
        kafSaxParser.parseFile(pathToKafFile);
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(System.currentTimeMillis());
        String strdate = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (date != null) {
            strdate = sdf.format(date.getTime());
        }

        kafSaxParser.getKafMetaData().addLayer(layer, name, version, strdate);
        for (int i = 0; i < kafSaxParser.getKafTerms().size(); i++) {
            KafTerm kafTerm = kafSaxParser.getKafTerms().get(i);
            if ((pos.isEmpty() || (kafTerm.getPos().toLowerCase().startsWith(pos))) &&
                    !kafTerm.getLemma().isEmpty() &&
                    (resources.wordNetLemmaSenseMap.containsKey(kafTerm.getLemma()))) {
                ArrayList<String> senses = resources.wordNetLemmaSenseMap.get(kafTerm.getLemma());
                for (int j = 0; j < senses.size(); j++) {
                    String synsetId = senses.get(j);
                    KafSense kafSense = new KafSense();
                    kafSense.setResource(pmVersion);
                    kafSense.setSensecode(synsetId);
                    boolean matchingSense = false;
                    for (int k = 0; k < kafTerm.getSenseTags().size(); k++) {
                        KafSense givenKafSense = kafTerm.getSenseTags().get(k);
                        if (givenKafSense.getSensecode().equals(synsetId)) {
                           kafSense = givenKafSense;
                           matchingSense = true;
                           break;
                        }
                    }
                    if (resources.wordNetPredicateMap.containsKey(synsetId)) {
                        ArrayList<ArrayList<String>> mappings = resources.wordNetPredicateMap.get(synsetId);
                        KafSense mChild = new KafSense ();
                        for (int m = 0; m < mappings.size(); m++) {
                            ArrayList<String> mapping =  mappings.get(m);
                            for (int k = 1; k < mapping.size(); k++) {
                                String s = mapping.get(k);
                                String resource = s.substring(0, 2);
                                KafSense child = new KafSense();
                                child.setResource(resource);
                                child.setSensecode(s);
                                mChild.addChildren(child);
                            }
                        }
                        kafSense.addChildren(mChild);
                    }
                    if (!matchingSense) {
                        kafTerm.addSenseTag(kafSense);
                    }
                }

            }
        }
    }

    static public void processKafFileWordnetNetSynsets (KafSaxParser kafSaxParser, String pathToKafFile, String pmVersion, Resources resources) {
        kafSaxParser.parseFile(pathToKafFile);
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(System.currentTimeMillis());
        String strdate = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (date != null) {
            strdate = sdf.format(date.getTime());
        }

        kafSaxParser.getKafMetaData().addLayer(layer, name, version, strdate);
        for (int i = 0; i < kafSaxParser.getKafTerms().size(); i++) {
            KafTerm kafTerm = kafSaxParser.getKafTerms().get(i);
            if (resources.grammaticalWords.contains(kafTerm.getLemma())) {
                KafSense child = new KafSense();
                child.setSensecode("grammatical");
                kafTerm.addSenseTag(child);
            }
            else {
                for (int j = 0; j < kafTerm.getSenseTags().size(); j++) {

                    KafSense givenKafSense = kafTerm.getSenseTags().get(j);
                    if (resources.wordNetPredicateMap.containsKey(givenKafSense.getSensecode())) {
/*                        if (kafTerm.getPos().toLowerCase().startsWith("n")) {
                            System.out.println("Info for givenKafSense.getSensecode() = " + givenKafSense.getSensecode());
                        }*/

                        ArrayList<ArrayList<String>> mappings = resources.wordNetPredicateMap.get(givenKafSense.getSensecode());
                        for (int m = 0; m < mappings.size(); m++) {
                            KafSense mChild = new KafSense ();
                            mChild.setResource("predicate-matrix");

                            ArrayList<String> mapping =  mappings.get(m);
                            for (int k = 1; k < mapping.size(); k++) {
                                String s = mapping.get(k);
                                String resource = s.substring(0, 2);
                                KafSense child = new KafSense();
                                child.setResource(resource);
                                child.setSensecode(s);
                                mChild.addChildren(child);
                            }
                            givenKafSense.addChildren(mChild);
                        }

                    }
                    else {
/*                        if (kafTerm.getPos().toLowerCase().startsWith("n")) {
                          System.out.println(givenKafSense.getSensecode());
                        }*/
                    }
                }
            }
        }
    }




}
