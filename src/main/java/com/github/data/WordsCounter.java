package com.github.data;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordsCounter {

    static class WordCount implements Comparable<WordCount>{
        int count;
        String word;

        @Override
        public int compareTo(WordCount other) {
            int result = Integer.compare(count, other.count);

            if(result == 0){
                result = word.compareTo(other.word);
            }
            return result*(-1);
        }

        public boolean equals(WordCount other) {
           return count == other.count && word == other.word;
        }

    }

    public void count(File file, int column) throws IOException{

        CSVReader reader = new CSVReader(new FileReader(file));

        String[] line = reader.readNext();
        TreeSet<WordCount> result = new TreeSet<>();
        HashMap<String, Integer> values = new HashMap<>();
        Pattern pattern = Pattern.compile("Merge\\s+pull\\s+request\\s+#\\d+\\s+from\\s+\\w+/\\w+");
        int lines = 0;
        while(line != null){

            if(line.length > column) {
                String text = line[column].trim();
                if(text.startsWith("Merge pull request")) {

                    Matcher m = pattern.matcher(text);

                    text = m.replaceFirst("");

                }

                String[] words = line[column].split("\\W+");
                for (String word : words) {
                    if(word.length() >= 3) {
                        Integer value = values.get(word.toLowerCase());
                        if (value != null) {
                            values.put(word.toLowerCase(), value + 1);
                        } else {
                            values.put(word.toLowerCase(), 1);
                        }
                    }
                }

            }
            line = reader.readNext();
            lines++;
        }

        Set<String> keys = values.keySet();

        for(String key: keys){
            WordCount wc = new WordCount();
            wc.word = key;
            wc.count = values.get(key);
            if(wc.count > 1) {
                result.add(wc);
            }
        }
        CSVWriter writer = new CSVWriter(new FileWriter( new File("words-counter-"+file.getName())));

        for (WordCount wc: result){
            writer.writeNext(new String[] {wc.word, String.valueOf(wc.count)});
        }
        writer.close();
        reader.close();
    }

    public static void main(String[] args) throws Exception {
        WordsCounter wc = new WordsCounter();
        wc.count(new File("commits-Bug.csv"), 7);
        wc.count(new File("commits-CleanUp.csv"), 7);
        wc.count(new File("commits-Enhancement.csv"), 7);
        wc.count(new File("commits-Feature-Request.csv"), 7);
    }

}
