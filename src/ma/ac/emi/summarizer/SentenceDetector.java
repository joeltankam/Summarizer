package ma.ac.emi.summarizer;

import static ma.ac.emi.summarizer.Summarizer.sentenceDetect;

public class SentenceDetector {

    static String[] splitToSentences(String content) {
        String[] sent = sentenceDetect.sentDetect(content);
        return sent;
    }

    static String[] splitToParagraphs(String content) {
        String[] string = content.split("\r\n");
        return string;
    }

    static String formatSentence(String sentence) {
        return sentence;
    }

}
