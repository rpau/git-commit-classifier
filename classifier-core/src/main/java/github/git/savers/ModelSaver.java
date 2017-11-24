package github.git.savers;

import weka.core.Instances;

import java.io.IOException;

public interface ModelSaver {

   void save(Instances dataSet) throws IOException;
}
