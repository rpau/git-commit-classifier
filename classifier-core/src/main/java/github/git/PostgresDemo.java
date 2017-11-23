package github.git;

public class PostgresDemo {

  public static void main(String[] args) throws Exception {
    DatabaseModelSaver saver = new DatabaseModelSaver();
    RegressionModelExecutor executor = new RegressionModelExecutor(saver);

    executor.store("tests", "features");
  }
}
