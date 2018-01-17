package ma.ac.emi.summarizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ma.ac.emi.summarizer.Summarizer.lemmatizer;
import static ma.ac.emi.summarizer.Summarizer.posTagger;

public class Lemmatizer {
    static String[] lemmatize(String[] tokens){
        String tags[] = posTagger.tag(tokens);
        String lemmas[] = lemmatizer.lemmatize(tokens, tags);

        List<String> lemmasList = new ArrayList<>();
        for (String s :
                lemmas) {
            if(!s.equals("O")) lemmasList.add(s);
        }

        return lemmasList.toArray(new String[]{});
    }
}
