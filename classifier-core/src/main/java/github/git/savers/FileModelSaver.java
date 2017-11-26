package github.git.savers;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.Saver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileModelSaver implements ModelSaver {
  private ArffSaver saver = new ArffSaver();
  private String path = "training.arff";
  private boolean init = false;

  private FileOutputStream fos;

  public FileModelSaver() {
    try {
      fos = new FileOutputStream(new File(path).getAbsoluteFile(), true);

      saver.setCompressOutput(false);
      saver.setDestination(fos);
      saver.setRetrieval(Saver.INCREMENTAL);

    } catch (Exception e){
      e.fillInStackTrace();
    }
  }
  @Override
  public void save(Instances dataSet) throws IOException {
    if (!init) {
      saver.setStructure(dataSet);
      init = true;
    }
    if (dataSet == null) {
      saver.writeIncremental(null);
      fos.close();
    } else {
      int max = dataSet.size();
      for (int i = 0; i < max; i++) {
        saver.writeIncremental(dataSet.get(i));
      }
    }
  }
}
