package ma.ac.emi.summarizer;

import static ma.ac.emi.summarizer.Summarizer.sentenceDetect;

class SentenceDetector {

    static String[] splitToSentences(String content) {
        return sentenceDetect.sentDetect(content);
    }

    static String[] splitToParagraphs(String content) {
        return content.split("\r\n");
    }

    static String formatSentence(String sentence) {
        return sentence;
    }

}
