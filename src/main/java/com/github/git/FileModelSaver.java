package com.github.git;

import weka.core.Instances;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.IOException;

public class FileModelSaver implements ModelSaver {
  private ArffSaver saver = new ArffSaver();
  private String path = "training.arff";
  File directory = new File("/Users/raquel.pau/rocket/rkt-clads-api");

  @Override
  public void save(Instances dataSet) throws IOException {
    saver.setInstances(dataSet);
    saver.setFile(new File(directory, path));
    saver.writeBatch();
  }
}
