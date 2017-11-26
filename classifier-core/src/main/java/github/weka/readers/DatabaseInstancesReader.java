package github.weka.readers;

import github.weka.schemas.Schema;
import github.weka.schemas.SchemaBuilder;
import org.postgresql.util.PSQLException;
import weka.core.Instances;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class DatabaseInstancesReader implements InstancesReader {

  private String host = "192.168.99.100";

  @Override
  public Instances read() throws Exception {
    Schema schema = SchemaBuilder.emptySchema().build();
    Instances dataSet = schema.toInstances();
    try(Connection db =
                DriverManager.getConnection(
                        "jdbc:postgresql://"+host+":5432/postgres",
                        "postgres", "mysecretpassword")){

      try(Statement statement = db.createStatement()) {

        try(ResultSet rs = statement.executeQuery("SELECT commit, s_label FROM commits;")){

          List<String> categories = schema.getCategories();

          while(rs.next()) {
            String commit = rs.getString(1);
            String category = rs.getString(2);
            schema.train(commit, category);
          }
        } catch (PSQLException e) {
          System.out.println("Nothing to read because "+e.getServerErrorMessage());
        }
      }
    }

    if (dataSet.classIndex() == -1) {
      dataSet.setClassIndex(dataSet.numAttributes() - 1);
    }

    return dataSet;
  }
}
