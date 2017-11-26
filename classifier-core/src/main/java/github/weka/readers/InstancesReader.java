package github.weka.readers;

import weka.core.Instances;

public interface InstancesReader {

  Instances read() throws Exception;
}
