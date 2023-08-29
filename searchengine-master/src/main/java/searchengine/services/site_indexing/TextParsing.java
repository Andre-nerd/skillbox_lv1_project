package searchengine.services.site_indexing;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.hibernate.engine.internal.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class TextParsing {
    public static String testText = "Повторное района появление леопарда в Осетии позволяет предположить,\n" +
            "что леопард постоянно обитает в некоторых районах Северного\n" +
            "Кавказа.<> href ah kl;sd  iserfmd nxfjkj @ </// <.//// Обитать, кавказ район\n";
    public static String[] ServiceParts = new String[]{"СОЮЗ", "ПРЕДЛ", "МЕЖД"};
    ;
    private static final Logger logger = LoggerFactory.getLogger(TextParsing.class);

    public static void main(String[] args) throws IOException {
        TextParsing parsing = new TextParsing();
        List<String> sourceTextList = parsing.splitTextIntoWords(testText);
        System.out.println(sourceTextList);
//        List<String> filterText = sourceTextList.stream().filter(TextParsing::isNotServicePart).toList();
//        System.out.println(filterText);
//        HashMap<String, Integer> map = parsingText(filterText);
//        System.out.println(map);

    }

    public HashMap<String, Integer> parsingOnePageText(String pageText){
        List<String> sourceTextList = splitTextIntoWords(pageText);
        List<String> filterText = sourceTextList.stream().filter(TextParsing::isNotServicePart).toList();
        System.out.println(filterText);
        HashMap<String, Integer> map = parsingText(filterText);
        return map;
    }

    private HashMap<String, Integer> parsingText(List<String> text) {
        logger.info("Counting the number of occurrences of words");
        HashMap<String, Integer> map = new HashMap<>();
        text.forEach(word -> {
            System.out.print(".");
                    String normWord = normFormsWord(word);
                    if (normWord != null) {
                        if (map.containsKey(normWord)){
                            map.put(normWord,map.get(normWord) + 1);
                        } else{
                            map.put(normWord, 1);
                        }
                    }
                }
        );
        System.out.println();
        return map;
    }

    private List<String> splitTextIntoWords(String text) {
        String res = text.replaceAll("-", " ")
                .replaceAll("\\p{Punct}|[0-9]|[A-Za-zÀ-ÿ]", "")
                .replaceAll("  ", " ")
                .replaceAll(" ", "\n")
                .toLowerCase();
        return Arrays.asList(res.split("\n")).stream().filter(this::isEmptyWord).toList();
    }

    private boolean isEmptyWord(String word){
        return !(word.isEmpty() || word.equals(" ") || word.equals("  "));
    }

    private static boolean isNotServicePart(String word) {
        try {
            LuceneMorphology luceneMorph = new RussianLuceneMorphology();
            List<String> wordBaseForms = luceneMorph.getMorphInfo(word);
            String[] wordBaseFormsSplit = wordBaseForms.get(0).split(" ");
            System.out.print(".");
            return (!Arrays.asList(ServiceParts).contains(wordBaseFormsSplit[1]));
        } catch (Exception ex) {
            System.out.println();
            logger.info("Check service symbol | skipped " + word);
            return false;
        }
    }


    private String normFormsWord(String word) {
        List<String> wordBaseForms = new ArrayList<>();
        try {
            LuceneMorphology luceneMorph = new RussianLuceneMorphology();
            wordBaseForms = luceneMorph.getNormalForms(word);
            return wordBaseForms.get(0);
        } catch (Exception ex) {
            logger.info("Error > fun normFormsWord | " + ex);
            return null;
        }
    }
}
