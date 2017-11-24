package github.git.readers;

import weka.classifiers.Classifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.stemmers.SnowballStemmer;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.ArrayList;

public class TrainingSet {

  private Instances instances;

  private Classifier classifier;

  static String[] categories = new String[] {"bugs", "features", "cleanups", "release", "merge"};

  public TrainingSet(Instances instances) {
    this.instances = instances;

    if (instances.classIndex() == -1) {
      instances.setClassIndex(instances.numAttributes() - 1);
    }
  }

  public Instances toInstances() {
    return instances;
  }

  private Instances initializeEmptyDataset() {

    ArrayList<Attribute> atts = new ArrayList<Attribute>();
    atts.add(new Attribute("commit", (ArrayList<String>)null));

    ArrayList<String> classVal = new ArrayList<String>();
    for(String category: categories){
      classVal.add(category);
    }

    atts.add(new Attribute("s_label", classVal));
    Instances dataSet = new Instances("commits", atts, 1);
    dataSet.setClassIndex(1);

    return dataSet;
  }

  public String infer(String message) throws Exception {

    if (classifier == null) {
      train();
    }

    Instances newDataset = initializeEmptyDataset();

    double[] values = new double[newDataset.numAttributes()];
    values[0] = newDataset.attribute(0).addStringValue(message);
    values[1] = -1;

    Instance instance = new DenseInstance(1.0, values);
    newDataset.add(instance);
    newDataset.setClassIndex(newDataset.numAttributes() - 1);
    // evaluate classifier and print some statistics
    Instance firstInstance = newDataset.firstInstance();

    double prediction = classifier.classifyInstance(firstInstance);
    return firstInstance.classAttribute().value((int) prediction);
  }

  public void evaluate() throws Exception {

    if (classifier == null) {
      train();
    }

    int errors = 0;
    for (int i = 0; i < instances.numInstances(); i++) {
      double pred = classifier.classifyInstance(instances.instance(i));
      String actual = instances.classAttribute().value((int) instances.instance(i).classValue());
      String predicted =  instances.classAttribute().value((int) pred);

      if(!actual.equals(predicted)){
        errors++;
      }

      System.out.print("ID: " + instances.instance(i).value(0));
      System.out.print(", actual: " + actual);
      System.out.println(", predicted: " + predicted);
    }
    System.out.println("Error ratio :"+ Double.toString((double)errors/ (double)instances.numInstances()));
  }


  private void train() throws Exception{
    StringToWordVector filter = new StringToWordVector();
    filter.setInputFormat(instances);

    filter.setStemmer(new SnowballStemmer());
    filter.setLowerCaseTokens(true);

    //here we build the model
    J48 j48 = new J48();
    Remove rm = new Remove();
    rm.setAttributeIndices("1");
    j48.setUnpruned(true);
    // meta-classifier
    FilteredClassifier fc = new FilteredClassifier();
    fc.setFilter(rm);
    fc.setClassifier(j48);
    fc.setFilter(filter);
    // train and make predictions
    fc.buildClassifier(instances);
    classifier = fc;
  }

}
