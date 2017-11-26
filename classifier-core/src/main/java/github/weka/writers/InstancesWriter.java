package github.weka.writers;

import weka.core.Instances;

import java.io.IOException;

public interface InstancesWriter {

   void write(Instances dataSet) throws IOException;
}
