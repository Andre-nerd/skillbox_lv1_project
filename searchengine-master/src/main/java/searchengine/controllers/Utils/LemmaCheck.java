package searchengine.controllers.Utils;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.List;

public class LemmaCheck {
    public static void main(String[] args) throws IOException {
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();
        List<String> wordBaseForms = luceneMorph.getNormalForms("леса");
        wordBaseForms.forEach(System.out::println);
        wordBaseForms = luceneMorph.getNormalForms("лесом");
        wordBaseForms.forEach(System.out::println);

        wordBaseForms = luceneMorph.getNormalForms("домой");
        wordBaseForms.forEach(System.out::println);

        wordBaseForms = luceneMorph.getNormalForms("дома");
        wordBaseForms.forEach(System.out::println);

    }
}
