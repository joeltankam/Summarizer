package ma.ac.emi.summarizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Summarizer {
    public static String summarize(String title, String content){
        return content;
    }

    private static String readAll(String filePath) throws IOException {
        String content;
        content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
        return content;
    }

    public static void main(String args[]){
        if(args.length < 2){
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
}
