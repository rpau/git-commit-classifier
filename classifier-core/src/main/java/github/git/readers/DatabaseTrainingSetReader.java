package github.git.readers;

import weka.experiment.InstanceQuery;

public class DatabaseTrainingSetReader implements TrainingSetReader {

  private String host = "192.168.99.100";

  @Override
  public TrainingSet readTrainingSet() throws Exception {
    InstanceQuery query = new InstanceQuery();

    query.setDatabaseURL("jdbc:postgresql://"+host+":5432/postgres");
    query.setUsername("postgres");
    query.setPassword("mysecretpassword");
    query.setQuery("SELECT commit, s_label FROM Commits");

    return new TrainingSet(query.retrieveInstances());
  }
}
