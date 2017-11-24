package github.git.readers;

import weka.core.Instances;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileTrainingSetReader implements TrainingSetReader {

  private String path = "training.arff";
  File directory = new File("/Users/raquel.pau/rocket/rkt-clads-api");

  @Override
  public TrainingSet readTrainingSet() throws IOException {
    BufferedReader reader = new BufferedReader(
            new FileReader(new File(directory,path)));
    Instances instances =  new Instances(reader);
    return new TrainingSet(instances);
  }
}
