package github.git;


import au.com.bytecode.opencsv.CSVReader;
import github.git.readers.FileTrainingSetReader;
import github.git.readers.TrainingSet;
import github.git.readers.TrainingSetReader;
import github.git.savers.FileModelSaver;
import github.git.savers.ModelSaver;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.revwalk.RevCommit;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class RegressionModelExecutor {

    private String path = "training.arff";

    File directory = new File("/Users/raquel.pau/rocket/rkt-clads-api");

    String[] categories = new String[] {"bugs", "features", "cleanups", "release", "merge"};

    private ModelSaver saver = new FileModelSaver();

    private TrainingSetReader reader = new FileTrainingSetReader();

    public RegressionModelExecutor() {
    }

    public RegressionModelExecutor(ModelSaver saver) {
        this.saver = saver;
    }

    public Instances initializeEmptyDataset() {

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

    public void store() throws IOException {

        Instances dataSet = initializeEmptyDataset();
        int categoryId = 0;

        for(String category: categories) {

            File commitsFile = new File(directory, category+".csv");

            try (FileReader freader = new FileReader(commitsFile)){
                CSVReader reader = new CSVReader(freader);

                String[] row = reader.readNext();

                while (row != null) {
                    double[] values = new double[dataSet.numAttributes()];

                    values[0] = dataSet.attribute(0).addStringValue(row[0]);
                    values[1] = categoryId;
                    Instance instance = new DenseInstance(1.0, values);
                    dataSet.add(instance);
                    row = reader.readNext();
                }
                categoryId++;
            }

        }
        dataSet.setClassIndex(dataSet.numAttributes() - 1);
        saver.save(dataSet);

    }

    public void store(String text, String category) throws IOException {
        int categoryId = Arrays.asList(categories).indexOf(category);
        Instances dataSet = initializeEmptyDataset();
        double[] values = new double[dataSet.numAttributes()];
        values[0] = dataSet.attribute(0).addStringValue(text);
        values[1] = categoryId;
        Instance instance = new DenseInstance(1.0, values);
        dataSet.add(instance);
        saver.save(dataSet);
    }

    public TrainingSet readTrainingSet() throws Exception{
        return reader.readTrainingSet();
    }


    public void infer() throws Exception {

        Git git = Git.open(directory);

        try {
            LogCommand logCmd = git.log();
            Iterable<RevCommit> commits = logCmd.all().call();
            store();
            TrainingSet trainingSet = readTrainingSet();

            for (RevCommit commit : commits) {
                String category =trainingSet.infer(commit.getFullMessage());
                System.out.println(commit.getName() + " " + category);
            }
        } finally {
            git.close();
        }

    }


    public void evaluate() throws Exception {
       store();
        TrainingSet trainingSet = readTrainingSet();
        trainingSet.evaluate();
    }

    public static void main(String[] args) throws Exception {
        RegressionModelExecutor exec = new RegressionModelExecutor();
        exec.infer();
    }
}
