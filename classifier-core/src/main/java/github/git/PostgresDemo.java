package github.git;

import github.git.readers.DatabaseTrainingSetReader;
import github.git.savers.DatabaseModelSaver;

public class PostgresDemo {

  public static void main(String[] args) throws Exception {

    DatabaseModelSaver saver = new DatabaseModelSaver();
    DatabaseTrainingSetReader reader = new DatabaseTrainingSetReader();

    RegressionModelExecutor executor = new RegressionModelExecutor(saver);
    /*for (int i = 0; i < 100; i++) {
      executor.store("tests", "features");
      executor.store("fix", "bugs");
      executor.store("cleanups", "cleanups");
      executor.store("release", "release");
      executor.store("merge", "merge");
    }*/
    System.out.println(reader.readTrainingSet().infer("style fix"));
  }
}
