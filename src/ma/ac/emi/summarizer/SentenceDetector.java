package ma.ac.emi.summarizer;

import static ma.ac.emi.summarizer.Summarizer.sentenceDetect;

public class SentenceDetector {

    //Text into sentences
    static String[] splitToSentences(String content) {

        String[] sent = sentenceDetect.sentDetect(content);
        return sent;
    }

    //Text into paragraphs
    static String[] splitToParagraphs(String content) {
        String[] mystring = content.split("\n\r\n");

        return mystring;
    }

    static String formatSentence(String sentence) {
        return sentence;
    }

}
