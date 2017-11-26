package github.weka.savers;

import weka.core.Instances;
import weka.core.converters.DatabaseSaver;

import java.io.IOException;

public class DatabaseModelSaver implements ModelSaver{

  DatabaseSaver save;

  String host = "192.168.99.100";

  public boolean connected = false;

  public DatabaseModelSaver() throws Exception {
    save = new DatabaseSaver();
    save.setUrl("jdbc:postgresql://"+host+":5432/postgres");
    save.setUser("postgres");
    save.setPassword("mysecretpassword");

  }

  @Override
  public void save(Instances dataSet) throws IOException {
    if (!connected) {
      save.setStructure(dataSet);
      connected = true;
      save.connectToDatabase();
    }
    //save.setRelationForTableName(false);
    //save.setTableName("commits");
    if (dataSet != null) {
      int max = dataSet.size();
      for (int i = 0; i < max; i++) {
        save.writeIncremental(dataSet.get(i));
      }
    }
  }

}
