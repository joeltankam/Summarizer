package ma.ac.emi.summarizer;

import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static ma.ac.emi.summarizer.Lemmatizer.lemmatize;
import static ma.ac.emi.summarizer.SentenceDetector.*;
import static ma.ac.emi.summarizer.Tokenizer.tokenize;

public class Summarizer {
    static TokenizerME tokenizer;
    static SentenceDetectorME sentenceDetect;
    static DictionaryLemmatizer lemmatizer;
    static POSTaggerME posTagger;
    static Summarizer Instance;

    public Summarizer() {
        initialize();
    }

    public static StringBuilder summarize(String content) {
        return summarize("", content, 1, false);
    }

    public static StringBuilder summarize(String content, int sentencesNumber) {
        return summarize("", content, sentencesNumber, false);
    }

    public static StringBuilder summarize(String title, String content) {
        return summarize(title, content, 1, true);
    }

    private static StringBuilder summarize(String title, String content, int sentencesNumber, Boolean byTitle) {
        Instance = new Summarizer();

        String[] paragraphs = splitToParagraphs(content);
        StringBuilder summary = new StringBuilder();

        for (String p : paragraphs) {
            String bestSent;
            if (byTitle)
                bestSent = getBestSentenceFromParagraph(title, p);
            else
                bestSent = getBestSentenceFromParagraph(p);
            if (bestSent != null && bestSent.length() > 0)
                summary.append(bestSent);
        }
        return summary;
    }

    static String getBestSentenceFromParagraph(String title, String paragraph) {
        String[] sentences = splitToSentences(formatSentence(paragraph));
        if (sentences == null || sentences.length <= 2)
            return "";

        float[] sentenceScores = getSentenceIntersectionArray(title, sentences);

        return getBestSentence(sentences, sentenceScores);
    }

    static String getBestSentenceFromParagraph(String paragraph) {
        String[] sentences = splitToSentences(formatSentence(paragraph));
        if (sentences == null || sentences.length <= 2)
            return "";

        float[][] intersectionMatrix = getSentenceIntersectionMatrix(sentences);

        float[] sentenceScores = getSentenceScores(sentences, intersectionMatrix);

        return getBestSentence(sentences, sentenceScores);
    }

    static float[][] getSentenceIntersectionMatrix(String[] sentences) {
        int n = sentences.length;

        float[][] intersectionMatrix = new float[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                try {
                    if (i == j)
                        continue;

                    intersectionMatrix[i][j] = sentenceIntersection(sentences[i], sentences[j]);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        return intersectionMatrix;
    }

    static float[] getSentenceIntersectionArray(String title, String[] sentences) {
        int n = sentences.length;

        float[] intersections = new float[n];

        for (int i = 0; i < n; i++) {
            intersections[i] = sentenceIntersection(title, sentences[i]);
        }
        return intersections;
    }

    static float[] getSentenceScores(String[] sentences, float[][] scores) {
        float[] scoresReturn = new float[sentences.length];

        for (int i = 0; i < sentences.length; i++) {
            int sentenceScore = 0;
            for (int j = 0; j < scores[i].length; j++) {
                sentenceScore += scores[i][j];
            }
            scoresReturn[i] = sentenceScore;
        }

        return scoresReturn;
    }

    static String getBestSentence(String[] sentences, float[] scores) {
        return sentences[getMaxIndex(scores)];
    }

    static int getMaxIndex(float[] array) {
        int maxIndex = 0;
        float max = -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    static float sentenceIntersection(String sentence1, String sentence2) {
        String[] sent1 = lemmatize(tokenize(sentence1));
        String[] sent2 = lemmatize(tokenize(sentence2));

        List<String> listSent2 = new ArrayList<>(Arrays.asList(sent2));

        if (sent1.length + sent2.length == 0)
            return 0;

        String[] intersectArray = intersection(sent1, sent2);

        int fakeSize = 0;
        for (String s :
                intersectArray) {
            fakeSize += Collections.frequency(listSent2, s);
        }

        float result = (float) fakeSize / (((float) sent1.length + (float) sent2.length) / 2);

        return result;
    }

    public static String[] intersection(String[] sent1, String[] sent2) {
        if (sent1 == null || sent1.length == 0 || sent2 == null || sent2.length == 0)
            return new String[0];

        List<String> sent1List = new ArrayList<String>(Arrays.asList(sent1));
        List<String> sent2List = new ArrayList<String>(Arrays.asList(sent2));

        sent1List.retainAll(sent2List);

        return sent1List.toArray(new String[0]);
    }

    void initialize() {
        InputStream sentenceModelIS = null;
        try {
            sentenceModelIS = new FileInputStream("data/en-sent.bin");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(0);
        }

        SentenceModel model;
        try {
            model = new SentenceModel(sentenceModelIS);
            sentenceDetect = new SentenceDetectorME(model);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        InputStream tokenizerModelIS = null;
        try {
            tokenizerModelIS = new FileInputStream("data/en-token.bin");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        TokenizerModel tokenModel;
        try {
            tokenModel = new TokenizerModel(tokenizerModelIS);
            tokenizer = new TokenizerME(tokenModel);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        InputStream posModelIn = null;
        try {
            posModelIn = new FileInputStream("data/en-pos-maxent.bin");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(0);
        }

        POSModel posModel = null;
        try {
            posModel = new POSModel(posModelIn);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        posTagger = new POSTaggerME(posModel);
        InputStream dictLemmatizer = null;
        try {
            dictLemmatizer = new FileInputStream("data/en-lemmatizer.dict");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(0);
        }
        try {
            lemmatizer = new DictionaryLemmatizer(dictLemmatizer);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void main(String args[]) {
        if (args.length < 1) {
            System.out.println("java Summarizer [content-path]");
            System.out.println("java Summarizer [content-path] [title]");
            System.exit(0);
        }


        String content;
        try {
            content = readAll(args[0]);
            if (args.length > 1) {
                String title = args[1];
                System.out.println(summarize(title, content));
            } else {
                System.out.println(summarize(content));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private static String readAll(String filePath) throws IOException {
        String content;
        content = new String(Files.readAllBytes(Paths.get(filePath)));
        return content;
    }
}
