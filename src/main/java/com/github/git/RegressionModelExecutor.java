package com.github.git;


import au.com.bytecode.opencsv.CSVReader;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.stemmers.SnowballStemmer;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class RegressionModelExecutor {

    private String path = "training.arff";

    File directory = new File("/Users/raquel.pau/github/RxJava");

    public void store() throws IOException {

        String[] categories = new String[] {"bugs", "features", "cleanup", "release", "merge"};
        ArrayList<Attribute> atts = new ArrayList<Attribute>();

        atts.add(new Attribute("commit", (ArrayList<String>)null));


        ArrayList<String> classVal = new ArrayList<String>();
        for(String category: categories){
            classVal.add(category);
        }
        atts.add(new Attribute("category", classVal));

        Instances dataSet = new Instances("Commits", atts, 1);
        dataSet.setClassIndex(1);


        int categoryId = 0;

        for(String category: categories) {

            File commitsFile = new File(directory, category+".csv");

            CSVReader reader = new CSVReader(new FileReader(commitsFile));

            String[] row = reader.readNext();

            while(row != null) {
                double[] values = new double[dataSet.numAttributes()];

                values[0] = dataSet.attribute(0).addStringValue(row[0]);
                values[1] = categoryId;
                Instance instance = new DenseInstance(1.0, values);
                dataSet.add(instance);
                row = reader.readNext();
            }
            categoryId++;

        }
        dataSet.setClassIndex(dataSet.numAttributes() - 1);

        ArffSaver saver = new ArffSaver();
        saver.setInstances(dataSet);

        saver.setFile(new File(directory, path));
        saver.writeBatch();
    }

    public Instances readTrainingSet() throws Exception{
        BufferedReader reader = new BufferedReader(
                new FileReader(new File(directory,path)));
        Instances instances =  new Instances(reader);
        instances.setClassIndex(instances.numAttributes() - 1);
        return instances;
    }

    public Classifier train(Instances trainingSet) throws Exception{

        StringToWordVector filter = new StringToWordVector();
        filter.setInputFormat(trainingSet);

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
        fc.buildClassifier(trainingSet);
        return fc;
    }


    public String infer(Classifier classifier, Instances trainingSet, String message) throws Exception {


        Instance instance = new DenseInstance(2);
        instance.attribute(0).addStringValue(message);
        // evaluate classifier and print some statistics
        Evaluation eval = new Evaluation(trainingSet);
        double prediction = eval.evaluateModelOnce(classifier, instance);

        return instance.classAttribute().value((int) prediction);
    }

    public static void main(String[] args) throws Exception {
        RegressionModelExecutor exec = new RegressionModelExecutor();
        exec.store();
        Instances trainingSet = exec.readTrainingSet();
        Classifier fc = exec.train(trainingSet);
        int errors = 0;
        for (int i = 0; i < trainingSet.numInstances(); i++) {
            double pred = fc.classifyInstance(trainingSet.instance(i));
            String actual = trainingSet.classAttribute().value((int) trainingSet.instance(i).classValue());
            String predicted =  trainingSet.classAttribute().value((int) pred);

            if(!actual.equals(predicted)){
                errors++;
            }

            System.out.print("ID: " + trainingSet.instance(i).value(0));
            System.out.print(", actual: " + actual);
            System.out.println(", predicted: " + predicted);
        }
        System.out.println("Error ratio :"+ Double.toString((double)errors/ (double)trainingSet.numInstances()));

        //exec.infer(classifier, trainingSet, "new commit");
    }
}
