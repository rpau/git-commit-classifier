package github.weka.schemas;

import github.weka.AdhocKeywordsClassifier;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Schema {

  private final Instances instances;

  private final List<String> categories;

  private final SchemaBuilder builder;

  private static Map<String, AdhocKeywordsClassifier.Classification> classificationMap;

  private Classifier classifier;

  private int updates = 0;

  private static int LIMIT_WITHOUT_DECISION_TREE = 100;

  private static int LIMIT_UPDATES_WITHOUT_TRAINING = 50;

  private static int TRAINING_SET_LIMIT = 100000;

  Schema(SchemaBuilder builder) {
    this.builder = builder;
    this.instances = emptySchema();
    this.categories = Arrays.asList(builder.categories);
  }

  private Instances emptySchema() {
    ArrayList<Attribute> atts = new ArrayList<Attribute>();
    atts.add(new Attribute("commit", (ArrayList<String>)null));

    ArrayList<String> classVal = new ArrayList<String>();
    for(String category: builder.categories){
      classVal.add(category);
    }
    atts.add(new Attribute("s_label", classVal));

    Instances dataSet = new Instances("commits", atts, 1);
    dataSet.setClassIndex(1);
    return  dataSet;
  }

  Schema(Instances instances, SchemaBuilder builder) {
    this.instances = instances;
    this.builder = builder;
    this.categories = Arrays.asList(builder.categories);
  }

  public Instances toInstances() {
    return instances;
  }

  public void train(String text, String category) {
    train(instances, text, category);
  }

  private void train(Instances instances, String text, String category) {
    int categoryId = categories.indexOf(category);
    double[] values = new double[instances.numAttributes()];
    values[0] = instances.attribute(0).addStringValue(text);
    values[1] = categoryId;
    Instance instance = new DenseInstance(1.0, values);
    instances.add(instance);
    updates ++;
  }

  public void put(String text, String category) throws IOException {
    train(text, category);

    Instances instances = emptySchema();
    train(instances,text, category);
    if (builder.saver != null && this.instances.size() - 1 < TRAINING_SET_LIMIT) {
      builder.saver.write(instances);
    }
  }

  public void flush() throws IOException {
    if (builder.saver != null) {
      builder.saver.write(null);
    }
  }

  public String infer(String message) throws Exception {

    if (instances.size() < LIMIT_WITHOUT_DECISION_TREE) {
      if (classificationMap == null) {
        classificationMap = AdhocKeywordsClassifier.getClassifications();
      }
      return AdhocKeywordsClassifier.classify(classificationMap, message);
    }
    train();

    Instances newDataset = emptySchema();

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

  public void inferAll() throws Exception {

    train();

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

  private void train() throws Exception {
    if (classifier == null ||
            (updates % LIMIT_UPDATES_WITHOUT_TRAINING == 0 && updates > 0)) {
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
      updates = 0;
    }
  }
}
