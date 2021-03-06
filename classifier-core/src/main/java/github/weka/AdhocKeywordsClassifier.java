package github.weka;

import au.com.bytecode.opencsv.CSVWriter;
import github.weka.schemas.Category;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class AdhocKeywordsClassifier {

  static Pattern pattern = Pattern.compile("Merge\\s+pull\\s+request\\s+#\\d+\\s+from\\s+\\w+/\\w+");

  static String[] classifications = Stream.of(Category.values())
          .map(Category::getText)
          .toArray(String[]::new);

  public static List<String> getKeywords(String name) throws Exception {
    List<String> result = new LinkedList<String>();
    try (InputStream is = AdhocKeywordsClassifier.class.getClassLoader()
            .getResourceAsStream("keywords/" + name + ".txt")) {
      try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
        String line = br.readLine();
        while (line != null) {
          result.add(line.trim());
          line = br.readLine();
        }
      }
    }

    return result;
  }

  public static class Classification {
    private String name;
    private CSVWriter writer;
    private List<String> keywords;
    int counter;

    public Classification(File directory, String name) throws Exception {
      this.name = name;
      writer = new CSVWriter(new FileWriter(new File(directory, name + ".csv")));
      keywords = getKeywords(name);
      counter = 0;
    }

    public Classification(String name) throws Exception {
      this.name = name;
      keywords = getKeywords(name);
      counter = 0;
    }

    public boolean matches(Set<String> wordSet) {
      return wordSet.stream().anyMatch((w) -> keywords.contains(w));
    }

    public void inc() {
      counter++;
    }

    public void write(String msg) {
      writer.writeNext(new String[]{msg.replaceAll("\\r|\\n|\\r|\\n", " ")});
    }

    public void close() throws IOException {
      writer.close();
    }
  }

  public static Set<String> getWords(String msg) {
    msg = msg.trim().replaceAll(":", "");
    if (msg.startsWith("Merge pull request")) {
      Matcher m = pattern.matcher(msg);
      msg = m.replaceFirst("");
    }

    String[] words = msg.toLowerCase().split("\\W+");
    Set<String> wordSet = new HashSet<String>();
    for (String word : words) {
      wordSet.add(word.trim());
    }
    return wordSet;
  }

  public static String classify(Map<String, Classification> classificationMap, String msg) throws Exception {

    Set<String> wordSet = getWords(msg);

    for (String classification : classifications) {
      if (classificationMap.get(classification).matches(wordSet)) {
        return classification;
      }
    }
    if (wordSet.size() == 1 && wordSet.iterator().next().contains(".")) {
      return Category.RELEASE.getText();
    } else {
      return Category.FEATURES.getText();
    }

  }

  public static Map<String, Classification> getClassifications() throws Exception {
    Map<String, Classification> classificationMap = new HashMap<>();
    for (String classification : classifications) {
      classificationMap.put(classification, new Classification(classification));
    }
    return classificationMap;
  }

  public static void main(String[] args) throws Exception {
  Map<String, Classification> classificationMap = new HashMap<>();

    File directory = new File(args[0]); //git repo directory
    try (Git git = Git.open(directory)) {

      for (String classification : classifications) {
        classificationMap.put(classification, new Classification(directory, classification));
      }

      LogCommand logCmd = git.log();
      Iterable<RevCommit> commits = logCmd.all().call();

      for (RevCommit commit : commits) {
        String msg = commit.getFullMessage();
        Set<String> wordSet = getWords(msg);

        boolean matched = false;
        for (String classification : classifications) {
          Classification classif = classificationMap.get(classification);

          if (!matched && classif.matches(wordSet)) {
            classif.inc();
            classif.write(msg);
            matched = true;
          }
        }

        if (!matched) {
          Classification classif = null;
          if (wordSet.size() == 1 && wordSet.iterator().next().contains(".")) {
            classif = classificationMap.get(Category.RELEASE.getText());
          } else {
            classif = classificationMap.get(Category.FEATURES.getText());
          }
          classif.inc();
          classif.write(msg);
        }
      }

      for (String classification : classifications) {
        Classification classif = classificationMap.get(classification);
        System.out.println(classif.name + " = " + classif.counter);
        classif.close();
      }
    }
  }

}
