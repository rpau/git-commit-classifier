package github.weka.readers;

import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static github.weka.RegressionModelExecutor.getCategories;
import static github.weka.readers.TrainingSet.initializeEmptyDataset;

public class DatabaseTrainingSetReader implements TrainingSetReader {

  private String host = "192.168.99.100";

  @Override
  public TrainingSet readTrainingSet() throws Exception {
    Instances dataSet = initializeEmptyDataset();
    try(Connection db =
                DriverManager.getConnection(
                        "jdbc:postgresql://"+host+":5432/postgres",
                        "postgres", "mysecretpassword")){

      try(Statement statement = db.createStatement()) {

        try(ResultSet rs = statement.executeQuery("SELECT commit, s_label FROM commits;")){

          List<String> categories = getCategories();

          while(rs.next()) {
            String commit = rs.getString(1);
            String category = rs.getString(2);
            int categoryId = getCategories().indexOf(category);
            double[] values = new double[dataSet.numAttributes()];
            values[0] = dataSet.attribute(0).addStringValue(commit);
            values[1] = categoryId;
            Instance instance = new DenseInstance(1.0, values);
            dataSet.add(instance);
          }
        }
      }
    }
    return new TrainingSet(dataSet);
  }
}
