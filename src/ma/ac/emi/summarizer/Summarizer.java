package ma.ac.emi.summarizer;

import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
                bestSent = getBestsentenceFromParagraph(title, p);
            else
                bestSent = getBestsentenceFromParagraph(p);
            if (bestSent != null && bestSent.length() > 0)
                summary.append(bestSent);
        }
        return summary;
    }

    public static float[] getSentenceScores(String[] sentences, float[][] scores) {
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

    public static String getBestsentenceFromParagraph(String title, String paragraph) {
        String[] sentences = splitToSentences(formatSentence(paragraph));
        if (sentences == null || sentences.length <= 2)
            return "";

        float[] sentenceScores = getSentenceIntersectionArray(title, sentences);

        return getBestSentence(sentences, sentenceScores);
    }

    public static String getBestsentenceFromParagraph(String paragraph) {
        String[] sentences = splitToSentences(formatSentence(paragraph));
        if (sentences == null || sentences.length <= 2)
            return "";

        float[][] intersectionMatrix = getSentenceIntersectionMatrix(sentences);

        float[] sentenceScores = getSentenceScores(sentences, intersectionMatrix);

        return getBestSentence(sentences, sentenceScores);
    }

    public static float[][] getSentenceIntersectionMatrix(String[] sentences) {
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

    public static float[] getSentenceIntersectionArray(String title, String[] sentences) {
        int n = sentences.length;

        float[] intersections = new float[n];

        for (int i = 0; i < n; i++) {
            intersections[i] = sentenceIntersection(title, sentences[i]);
        }
        return intersections;
    }

    public static String getBestSentence(String[] sentences, float[] scores) {
        return sentences[getMaxIndex(scores)];
    }

    public static int getMaxIndex(float[] array) {
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

    public static float sentenceIntersection(String sentence1, String sentence2) {
        String[] sent1 = lemmatize(tokenize(sentence1));
        String[] sent2 = lemmatize(tokenize(sentence2));

        if (sent1.length + sent2.length == 0)
            return 0;

        List<String> intersectArray = (List<String>) intersect(new ArrayList<>(Arrays.asList(sent1)), new ArrayList<>(Arrays.asList(sent2)));

        float result = ((float) intersectArray.size() / ((float) sent1.length + ((float) sent2.length) / 2));

        return result;
    }

    public static <T> Collection<T> intersect(Collection<? extends T> a, Collection<? extends T> b) {
        Collection<T> result = new ArrayList<T>();
        for (T t : a) {
            if (b.remove(t)) result.add(t);
        }

        return result;
    }

    public void initialize() {
        InputStream sentenceModelIS = null;
        try {
            sentenceModelIS = new FileInputStream("data/en-sent.bin");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        SentenceModel model;
        try {
            model = new SentenceModel(sentenceModelIS);
            sentenceDetect = new SentenceDetectorME(model);
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        InputStream posModelIn = null;
        try {
            posModelIn = new FileInputStream("data/en-pos-maxent.bin");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        POSModel posModel = null;
        try {
            posModel = new POSModel(posModelIn);
        } catch (IOException e) {
            e.printStackTrace();
        }

        posTagger = new POSTaggerME(posModel);
        InputStream dictLemmatizer = null;
        try {
            dictLemmatizer = new FileInputStream("data/en-lemmatizer.dict");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            lemmatizer = new DictionaryLemmatizer(dictLemmatizer);
        } catch (IOException e) {
            e.printStackTrace();
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
