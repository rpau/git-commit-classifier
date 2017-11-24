package org.github.classifier.server;

import com.fasterxml.jackson.databind.JsonNode;
import github.git.RegressionModelExecutor;
import github.git.readers.FileTrainingSetReader;
import github.git.savers.FileModelSaver;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;

@RestController
public class HelloController {

  GitHubClient client;

  FileModelSaver saver;
  RegressionModelExecutor executor;
  FileTrainingSetReader reader;
  HashMap<String, Label> categoryLabel;

  private final Label bug = new Label().setName("classifier:bug").setColor("ee0701");
  private final Label feature = new Label().setName("classifier:feature").setColor("84b6eb");
  private final Label cleanup = new Label().setName("classifier:cleanup").setColor("128A0C");
  private final Label release = new Label().setName("classifier:release").setColor("b42fa6");
  private final Label merge = new Label().setName("classifier:merge").setColor("f2f73c");

  public HelloController() throws Exception {
    client = new GitHubClient("github.schibsted.io");
    client.setOAuth2Token("");
    saver = new FileModelSaver();
    reader = new FileTrainingSetReader();
    executor = new RegressionModelExecutor(saver);
    categoryLabel = new HashMap<>();
    categoryLabel.put("bugs", bug);
    categoryLabel.put("features", feature);
    categoryLabel.put("cleanups", cleanup);
    categoryLabel.put("release", release);
    categoryLabel.put("merge", merge);

  }

  @RequestMapping(value = "/", method = RequestMethod.GET)
  public String index() {
    return "Greetings from Spring Boot!";
  }


  @RequestMapping(value = "/", method = RequestMethod.POST)
  public void consume(@RequestBody JsonNode node) throws Exception {
    System.out.println("RECEIVED!!! " + node.getNodeType());
    System.out.println(node.has("pull_request"));
    if (node.has("pull_request")) {
      onPullRequest(node);
    } else {
      System.out.println("Ignoring event, not a pull request: " + node);
    }
  }

  private void onPullRequest(JsonNode node) throws Exception {
    String action = node.get("action").textValue();
    int prNumber = node.get("number").asInt();
    String title = node.get("pull_request").get("title").asText();
    String body = node.get("pull_request").get("body").asText();
    Repository repository = new RepositoryService(client).getRepository("xavi-leon", "mobile-app-android");

    if (action.equals("closed") || action.equals("merged") ) {
      IssueService issueService = new IssueService(client);
      Issue issue = issueService.getIssue(repository, prNumber);
      if (issue.getLabels().size() > 0) {
        System.out.println("Labels for this issue: " + issue.getLabels());
        issue.getLabels().stream()
                .filter((Label l) -> l.getName().startsWith("classifier:"))
                .forEach((Label l) -> {
                  String category = l.getName().split(":")[1];
                  try {
                    executor.store(title + body, category);
                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                });
      } else {
        System.out.println("Ignoring event, no labels on the pull request -> " + node);
      }
    } else if (action.equals("opened")) {
      // It's an open pull request, let's try to infer what it is.
      String category = reader.readTrainingSet().infer(title);
      addCategoryLabel(repository, prNumber, category);
    } else {
      System.out.println("Ignoring event, not a merge action -> " + node);
    }
  }

  private void addCategoryLabel(Repository repo, int prNumber, String category) throws Exception {
    IssueService srv = new IssueService(client);
    Issue issue = srv.getIssue(repo, prNumber);
    issue.getLabels().add(categoryLabel.get(category));
    Issue edited = srv.editIssue(repo, issue);
    System.out.println("Label " + category + " added to the issue. " + edited);
  }

}