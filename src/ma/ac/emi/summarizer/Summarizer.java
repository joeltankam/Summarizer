package ma.ac.emi.summarizer;

import opennlp.tools.lemmatizer.DictionaryLemmatizer;
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

public class Summarizer {
    public static TokenizerME tokenizer;
    public static SentenceDetectorME sentenceDetect;
    DictionaryLemmatizer lemmatizer;
    public static Summarizer Instance;

    public static String summarize(String title, String content) {
        Instance = new Summarizer();
        return content;
    }

    public Summarizer() {
        initialize();
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
        if (args.length < 2) {
            System.out.println("java Summarizer [title] [content-path]");
            System.exit(0);
        }

        String title = args[0];
        String content;
        try {
            content = readAll(args[1]);
            System.out.println(summarize(title, content));
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
