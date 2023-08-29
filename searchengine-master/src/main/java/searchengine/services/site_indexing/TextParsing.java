package searchengine.services.site_indexing;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.hibernate.engine.internal.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextParsing {
    public static String testText = "Повторное появление леопарда в Осетии позволяет предположить,\n" +
            "что леопард постоянно обитает в некоторых районах Северного\n" +
            "Кавказа.<> href <.////\n";
    public static String[] ServiceParts = new String[]{"СОЮЗ", "ПРЕДЛ", "МЕЖД"};
    ;
    private static final Logger logger = LoggerFactory.getLogger(TextParsing.class);

    public static void main(String[] args) throws IOException {
        List<String> sourceTextList = splitTextIntoWords(testText);
        System.out.println(sourceTextList);
        List<String> filterText = sourceTextList.stream().filter(TextParsing::isNotServicePart).toList();
        System.out.println(filterText);


    }

    private static List<String> splitTextIntoWords(String text) {
        String res = text.replaceAll("-", " ")
                .replaceAll("\\p{Punct}|[0-9]", "")
                .replaceAll("  ", " ")
                .replaceAll(" ", "\n")
                .toLowerCase();
        return Arrays.asList(res.split("\n"));
    }

    private static boolean isNotServicePart(String word) {
        try {
            LuceneMorphology luceneMorph = new RussianLuceneMorphology();
            List<String> wordBaseForms = luceneMorph.getMorphInfo(word);
            String[] wordBaseFormsSplit = wordBaseForms.get(0).split(" ");
            return (!Arrays.asList(ServiceParts).contains(wordBaseFormsSplit[1]));
        } catch (Exception ex) {
            logger.info("Error > fun isNotServicePart | " + ex);
            return false;
        }
    }



    private static List<String> normFormsWord(String word) {
        List<String> wordBaseForms = new ArrayList<>();
        try {
            LuceneMorphology luceneMorph = new RussianLuceneMorphology();
            wordBaseForms = luceneMorph.getNormalForms(word);
        } catch (Exception ex) {
            logger.info("Error > fun normFormsWord | " + ex);
        }
        return wordBaseForms;
    }
}
