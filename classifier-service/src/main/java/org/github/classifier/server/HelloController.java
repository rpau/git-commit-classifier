package org.github.classifier.server;

import com.fasterxml.jackson.databind.JsonNode;
import github.git.DatabaseModelSaver;
import github.git.RegressionModelExecutor;
import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class HelloController {

  GitHubClient client;

  DatabaseModelSaver saver;
  RegressionModelExecutor executor;

  public HelloController() throws Exception {
    client = new GitHubClient("github.schibsted.io");
    client.setOAuth2Token("ac3ef4487110a53c8a48b4a236082fb7bb277c74");
    saver = new DatabaseModelSaver();
    executor = new RegressionModelExecutor(saver);
  }

  @RequestMapping(value = "/", method = RequestMethod.GET)
  public String index() {
    return "Greetings from Spring Boot!";
  }


  @RequestMapping(value = "/", method = RequestMethod.POST)
  public void consume(@RequestBody JsonNode node) throws IOException {
    System.out.println("RECEIVED!!! " + node.getNodeType());
    System.out.println(node.has("pull_request"));
    if (node.has("pull_request")) {
      onPullRequest(node);
    } else if (node.has("issue")) {
      onIssue(node);
    } else {
      System.out.println("Ignoring event, not a pull request: " + node);
    }
  }

  private void onPullRequest(JsonNode node) throws IOException {
    String action = node.get("action").textValue();
    if (action.equals("closed") || action.equals("merged") ) {
      int prNumber = node.get("number").asInt();
      String title = node.get("pull_request").get("title").asText();
      String body = node.get("pull_request").get("body").asText();
      PullRequestService srv = new PullRequestService(client);

      Repository repository = new RepositoryService(client).getRepository("xavi-leon", "mobile-app-android");
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
    } else {
      System.out.println("Ignoring event, not a merge action -> " + node);
    }
  }

  private void onIssue(JsonNode node) {
    String action = node.get("action").textValue();
    if (action.equals("labeled")) {

    }
  }
}