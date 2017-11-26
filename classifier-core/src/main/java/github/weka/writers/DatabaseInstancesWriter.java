package github.weka.writers;

import weka.core.Instances;
import weka.core.converters.DatabaseSaver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseInstancesWriter implements InstancesWriter {

  private DatabaseSaver save;

  private boolean connected = false;

  public DatabaseInstancesWriter() throws Exception {
    save = new DatabaseSaver();
    Properties prop = new Properties();

    try(InputStream input = DatabaseInstancesWriter.class.getClassLoader()
            .getResourceAsStream("postgres.properties")) {
      prop.load(input);
      save.setUrl("jdbc:postgresql://" + prop.getProperty("host") + ":5432/postgres");
      save.setUser(prop.getProperty("user"));
      save.setPassword(prop.getProperty("pwd"));
    }

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
