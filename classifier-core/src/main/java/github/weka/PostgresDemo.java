package github.weka;

import github.weka.readers.DatabaseTrainingSetReader;
import github.weka.savers.DatabaseModelSaver;

public class PostgresDemo {

  public static void main(String[] args) throws Exception {

    DatabaseModelSaver saver = new DatabaseModelSaver();
    DatabaseTrainingSetReader reader = new DatabaseTrainingSetReader();

    RegressionModelExecutor executor = new RegressionModelExecutor(saver);
    for (int i = 0; i < 1000; i++) {
      executor.store("features", "features");
      executor.store("bugs", "bugs");
      executor.store("cleanups", "cleanups");
      executor.store("release", "release");
      executor.store("merge", "merge");

    }
    executor.close();

    System.out.println(reader.readTrainingSet().infer("features"));
  }
}
