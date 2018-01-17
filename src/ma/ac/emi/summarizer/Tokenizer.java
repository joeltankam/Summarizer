package ma.ac.emi.summarizer;

import static ma.ac.emi.summarizer.Summarizer.tokenizer;

public class Tokenizer {
    static String[] tokenize(String sentence){
        return tokenizer.tokenize(sentence);
    }
}
