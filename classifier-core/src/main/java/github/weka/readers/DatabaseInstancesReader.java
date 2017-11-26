package github.weka.readers;

import github.weka.schemas.Schema;
import github.weka.schemas.SchemaBuilder;
import org.postgresql.util.PSQLException;
import weka.core.Instances;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseInstancesReader implements InstancesReader {

  @Override
  public Instances read() throws Exception {
    Properties prop = new Properties();
    Schema schema = SchemaBuilder.emptySchema().build();
    Instances dataSet = schema.toInstances();

    try(InputStream input =DatabaseInstancesReader.class.getClassLoader()
            .getResourceAsStream("postgres.properties")) {

      prop.load(input);

      try (Connection db =
                   DriverManager.getConnection(
                           "jdbc:postgresql://" + prop.getProperty("host") + ":5432/postgres",
                           prop.getProperty("user"), prop.getProperty("pwd"))) {

        try (Statement statement = db.createStatement()) {

          try (ResultSet rs = statement.executeQuery("SELECT commit, s_label FROM commits;")) {

            while (rs.next()) {
              String commit = rs.getString(1);
              String category = rs.getString(2);
              schema.train(commit, category);
            }
          } catch (PSQLException e) {
            System.out.println("Nothing to read because " + e.getServerErrorMessage());
          }
        }
      }
    }

    if (dataSet.classIndex() == -1) {
      dataSet.setClassIndex(dataSet.numAttributes() - 1);
    }

    return dataSet;
  }
}
