package github.weka.writers;

import weka.core.Instances;
import weka.core.converters.DatabaseSaver;

import java.io.IOException;

public class DatabaseInstancesWriter implements InstancesWriter {

  DatabaseSaver save;

  String host = "192.168.99.100";

  public boolean connected = false;

  public DatabaseInstancesWriter() throws Exception {
    save = new DatabaseSaver();
    save.setUrl("jdbc:postgresql://"+host+":5432/postgres");
    save.setUser("postgres");
    save.setPassword("mysecretpassword");

  }

  @Override
  public void write(Instances dataSet) throws IOException {
    if (!connected) {
      save.setStructure(dataSet);
      connected = true;
      save.connectToDatabase();
    }
    if (dataSet != null) {
      int max = dataSet.size();
      for (int i = 0; i < max; i++) {
        save.writeIncremental(dataSet.get(i));
      }
    }
  }

}
