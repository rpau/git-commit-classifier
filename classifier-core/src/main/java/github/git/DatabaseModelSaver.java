package github.git;

import weka.core.Instances;
import weka.core.converters.DatabaseSaver;

import java.io.IOException;

public class DatabaseModelSaver implements ModelSaver{

  DatabaseSaver save;

  String host = "192.168.99.100";

  public DatabaseModelSaver() throws Exception {
    save = new DatabaseSaver();
  }

  @Override
  public void save(Instances dataSet) throws IOException {
    save.setUrl("jdbc:postgresql://"+host+":5432/postgres");
    save.setUser("postgres");
    save.setPassword("mysecretpassword");

   save.setStructure(dataSet);
    //save.setRelationForTableName(false);
    //save.setTableName("commits");
    int max = dataSet.size();
    save.connectToDatabase();
    for (int i = 0; i < max; i++) {
      save.writeIncremental(dataSet.get(i));
    }
  }

}
