package com.github.git;

import au.com.bytecode.opencsv.CSVWriter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdhocKeywordsClassifier {

    // ir commit a commit en el git log y ver cuantos se pueden clasificar segun el titulo (cleanup, fixup)/ (add, adds adding) / bug

    public static void main(String[] args) throws Exception {
        File directory = new File("/Users/raquel.pau/github/RxJava");
        Git git = Git.open(directory);

        CSVWriter featuresCSV = new CSVWriter(new FileWriter( new File(directory, "features.csv")));
        CSVWriter bugsCSV = new CSVWriter(new FileWriter( new File(directory, "bugs.csv")));
        CSVWriter cleanUpCSV = new CSVWriter(new FileWriter( new File(directory, "cleanup.csv")));
        CSVWriter releaseCSV = new CSVWriter(new FileWriter( new File(directory, "release.csv")));
        CSVWriter mergeCSV = new CSVWriter(new FileWriter( new File(directory, "merge.csv")));

        LogCommand logCmd = git.log();
        Iterable<RevCommit> commits = logCmd.all().call();
        int bugs = 0;
        int features = 0;
        int cleanup = 0;
        int releases = 0;
        int merges = 0;
        int other = 0;
        Pattern pattern = Pattern.compile("Merge\\s+pull\\s+request\\s+#\\d+\\s+from\\s+\\w+/\\w+");
        for(RevCommit commit : commits){
            int timeInSeconds = commit.getCommitTime();
            String msg = commit.getFullMessage().trim();
            msg.replaceAll(":", "");
            if(msg.startsWith("Merge pull request")) {

                Matcher m = pattern.matcher(msg);

                msg = m.replaceFirst("");

            }

            String[] words = msg.toLowerCase().split("\\W+");
            Set<String> wordSet = new HashSet<String>();
            for(String word: words){

                wordSet.add(word.trim());
            }
            if(wordSet.contains("cleanup")
                    || wordSet.contains("style")
                    || wordSet.contains("warnings")
                    || wordSet.contains("cleaning")
                    || wordSet.contains("findbugs")
                    || wordSet.contains("pmd")
                    || wordSet.contains("travis")
                    || wordSet.contains("indentation")
                    || wordSet.contains("docs")
                    || wordSet.contains("comments")
                    || wordSet.contains("naming")
                    || wordSet.contains("import")
                    || wordSet.contains("whitespace")
                    || wordSet.contains("whitespaces")
                    || wordSet.contains("unnecessary")
                    || wordSet.contains("cleanups")
                    || wordSet.contains("fixup")
                    || wordSet.contains("fixups")
                    || wordSet.contains("ignore")
                    || wordSet.contains("coverage")
                    || wordSet.contains("unused")
                    || wordSet.contains("remove")
                    || wordSet.contains("refactor")
                    || wordSet.contains("refactoring")
                    || wordSet.contains("refactored")
                    || wordSet.contains("extract")
                    || wordSet.contains("extracted")
                    || wordSet.contains("move")
                    || wordSet.contains("moves")
                    || wordSet.contains("moved")
                    || wordSet.contains("moving")
                    || wordSet.contains("rename")
                    || wordSet.contains("renames")
                    || wordSet.contains("renamed")
                    || wordSet.contains("renaming")
                    || wordSet.contains("renamings")
                    || wordSet.contains("removed")
                    || wordSet.contains("deleted")
                    || wordSet.contains("removes")
                    || wordSet.contains("removing")
                    || wordSet.contains("clean")
                    || wordSet.contains("update")
                    || wordSet.contains("updates")
                    || wordSet.contains("updated")
                    || wordSet.contains("updating")
                    || wordSet.contains("improve")
                    || wordSet.contains("improved")
                    || wordSet.contains("improves")
                    || wordSet.contains("improvement")
                    || wordSet.contains("improving")
                    || wordSet.contains("replace")
                    || wordSet.contains("replaces")
                    || wordSet.contains("replacement")
                    || wordSet.contains("replacing")
                    || wordSet.contains("replaced")
                    || wordSet.contains("amend")
                    || wordSet.contains("test")
                    || wordSet.contains("tests")
                    || wordSet.contains("tested")
                    || wordSet.contains("testing")
                    || wordSet.contains("correcting")
                    || wordSet.contains("simplifications")
                    || wordSet.contains("simplification")
                    || wordSet.contains("simplified")
                    || wordSet.contains("change")
                    || wordSet.contains("changes")
                    || wordSet.contains("changed")
                    || wordSet.contains("changing")
                    || wordSet.contains("switch")
                    || wordSet.contains("use")
                    || wordSet.contains("usage")
                    || wordSet.contains("setting")
                    || wordSet.contains("using")
                    || wordSet.contains("migrate")
                    || wordSet.contains("reuse")
                    || wordSet.contains("avoid")
                    || wordSet.contains("documenting")
                    || wordSet.contains("documented")
                    || wordSet.contains("documentation")
                    || wordSet.contains("header")
                    || wordSet.contains("headers")
                    || wordSet.contains("upgrade")
                    || wordSet.contains("restructure")
                    || wordSet.contains("cleaned")
                    || wordSet.contains("formatting")
                    || wordSet.contains("format")
                    || wordSet.contains("reimplemented")
                    || wordSet.contains("adapt")
                    || wordSet.contains("improvements")
                    || wordSet.contains("improves")
                    || wordSet.contains("improving")
                    || wordSet.contains("improve")
                    || wordSet.contains("optimize")
                    || wordSet.contains("optimization")
                    || wordSet.contains("typos")
                    || wordSet.contains("typo")
                    || wordSet.contains("javadoc")
                    || wordSet.contains("missing")
                    || wordSet.contains("reorganize")
                    || wordSet.contains("deprecate")
                    || wordSet.contains("deprecated")
                    || wordSet.contains("readme")
                    || wordSet.contains("clarify")
                    || wordSet.contains("lint")
                    || wordSet.contains("reformatted")
                    || wordSet.contains("doc")
                    || wordSet.contains("rectify")
                    || wordSet.contains("duplicate")
                    || wordSet.contains("simplify")
                    || wordSet.contains("checkstyle")
                    ){
                cleanup++;
                cleanUpCSV.writeNext(new String[] {msg.replaceAll("\\r|\\n|\\r|\\n", " ")});
            }
            else if(wordSet.contains("bug")
                    || wordSet.contains("bugfix")
                    || wordSet.contains("NPE")
                    || wordSet.contains("issue")
                    || wordSet.contains("fix")
                    || wordSet.contains("fixes")
                    || wordSet.contains("fixed")
                    || wordSet.contains("fixing")
                    || wordSet.contains("error")
                    || wordSet.contains("errors")
                    || wordSet.contains("correct")
                    || wordSet.contains("bugfixes")
                    || wordSet.contains("bugs")
                    || wordSet.contains("revert")
                    || wordSet.contains("rollback")
                    || wordSet.contains("disable")
                    || wordSet.contains("undo")
                    || wordSet.contains("corrected")
                    || wordSet.contains("corrects")
                    || wordSet.contains("correct")
                    || wordSet.contains("restored")
                    || wordSet.contains("restore")
                    || wordSet.contains("restoring")
                    || wordSet.contains("restore")
                    || wordSet.contains("restored")
                    || wordSet.contains("wrong")
                    || wordSet.contains("incorrectly")
                    || wordSet.contains("incorrect")
                    || wordSet.contains("workaround")
                    || wordSet.contains("backport")
                    || wordSet.contains("repair")){
                bugs ++;
                bugsCSV.writeNext(new String[] {msg.replaceAll("\\r|\\n|\\r|\\n", " ")});
            }
            else if (wordSet.contains("add")
                    || wordSet.contains("adds")
                    || wordSet.contains("addition")
                    || wordSet.contains("added")
                    || wordSet.contains("adding")
                    || wordSet.contains("new")
                    || wordSet.contains("implement")
                    || wordSet.contains("support")
                    || wordSet.contains("supports")
                    || wordSet.contains("supported")
                    || wordSet.contains("supporting")
                    || wordSet.contains("implemented")
                    || wordSet.contains("implementation")
                    || wordSet.contains("implementing")
                    || wordSet.contains("implements")
                    || wordSet.contains("create")
                    || wordSet.contains("creating")
                    || wordSet.contains("created")
                    || wordSet.contains("make")){
                features++;
                featuresCSV.writeNext(new String[] {msg.replaceAll("\\r|\\n|\\r|\\n", " ")});
            }
            else if (wordSet.contains("release") || wordSet.contains("version") || wordSet.contains("archive")
                    || wordSet.contains("tag") || wordSet.contains("snapshot") || (
                            wordSet.size() == 1 && wordSet.iterator().next().contains("."))){
                releases++;
                releaseCSV.writeNext(new String[] {msg.replaceAll("\\r|\\n|\\r|\\n", " ")});

            }
            else if (wordSet.contains("merge") || wordSet.contains("merges") || wordSet.contains("merging")
                    || wordSet.contains("merged")){
                merges++;
                mergeCSV.writeNext(new String[] {msg.replaceAll("\\r|\\n|\\r|\\n", " ")});
            }
            else{
                other++;
                featuresCSV.writeNext(new String[] {msg.replaceAll("\\r|\\n|\\r|\\n", " ")});
                //unclassifiedCommits.writeNext(new String[] {msg.replaceAll("\\r|\\n|\\r|\\n", " ")});
            }
        }
        System.out.println("result: bugs:"+bugs+", features: "+features+", cleanup: "+cleanup+", releases: "
                +releases+", merges :"+merges+" others: "+other);
        git.close();

        featuresCSV.close();
        releaseCSV.close();
        cleanUpCSV.close();
        bugsCSV.close();
        mergeCSV.close();
        //unclassifiedCommits.close();
    }

    //fichero => #bugs, #features, #cleanups

    //commits por dia / #contributors de ese mes
}
