package github.weka.readers;

import weka.core.Instances;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileInstancesReader implements InstancesReader {

  private String path = "training.arff";

  @Override
  public Instances read() throws IOException {
    BufferedReader reader = new BufferedReader(
            new FileReader(new File(path)));
    Instances instances =  new Instances(reader);
    if (instances.classIndex() == -1) {
      instances.setClassIndex(instances.numAttributes() - 1);
    }
    return instances;
  }
}
