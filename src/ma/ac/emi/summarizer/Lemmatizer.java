package ma.ac.emi.summarizer;

import static ma.ac.emi.summarizer.Summarizer.lemmatizer;
import static ma.ac.emi.summarizer.Summarizer.posTagger;

public class Lemmatizer {
    static String[] lemmatize(String[] tokens) {
        String tags[] = posTagger.tag(tokens);
        String lemmas[] = lemmatizer.lemmatize(tokens, tags);

        for (int i = 0; i < lemmas.length; i++) {
            if (lemmas[i].equals("O"))
                lemmas[i] = tokens[i];
        }
        return lemmas;
    }
}
