package org.github.classifier.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import github.weka.readers.DatabaseInstancesReader;
import github.weka.schemas.Schema;
import github.weka.schemas.SchemaBuilder;
import github.weka.writers.DatabaseInstancesWriter;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.event.PullRequestPayload;
import org.eclipse.egit.github.core.service.IssueService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.logging.Logger;

@RestController
public class ClassifierController {

  private final GitHubClient client;

  private final Schema schema;

  private final ObjectMapper mapper = new ObjectMapper();

  private static String GITHUB_HOST = "github.schibsted.io";

  private final String LABEL_PREFIX = "classifier";

  private final String LABEL_SEPARATOR = ":";

  private static final Logger LOG = Logger.getLogger(ClassifierController.class.getName());

  public ClassifierController() throws Exception {
    client = new GitHubClient(GITHUB_HOST);
    client.setOAuth2Token("");
    schema = SchemaBuilder.from(new DatabaseInstancesReader())
            .withSaver(new DatabaseInstancesWriter())
            .build();
  }

  @RequestMapping(value = "/", method = RequestMethod.GET)
  public String index() {
    return "Greetings from Spring Boot!";
  }


  @RequestMapping(value = "/", method = RequestMethod.POST)
  public void consume(@RequestBody JsonNode node) throws Exception {
    LOG.fine("GH Event received " + node.getNodeType());
    if (node.has("pull_request")) {
      PullRequestPayload payload = mapper.treeToValue(node, PullRequestPayload.class);
      onPullRequest(payload);
    } else {
      LOG.fine("Ignoring event, not a pull request: " + node);
    }
  }

  private void onPullRequest(PullRequestPayload payload) throws Exception {

    String action = payload.getAction();
    String title = payload.getPullRequest().getTitle();
    String body = payload.getPullRequest().getBody();

    if (action.equals("closed") || action.equals("merged") ) {
      IssueService issueService = new IssueService(client);
      Issue issue = issueService.getIssue(
              payload.getPullRequest().getBase().getRepo(),
              payload.getNumber());

      issue.getLabels().stream()
              .filter((Label l) -> l.getName().startsWith(LABEL_PREFIX + LABEL_SEPARATOR))
              .map(l -> l.getName().split(LABEL_SEPARATOR)[1])
              .forEach(category -> updateModel(title + body, category));

    } else if (action.equals("opened")) {
      // It's an open pull request, let's try to infer what it is.
      String category = schema.infer(title);
      addCategoryLabel(payload.getPullRequest().getBase().getRepo(),
              payload.getNumber(), category);
    } else {
      LOG.fine("Ignoring event, not a merge action -> " +
              payload.getPullRequest().getUrl());
    }
  }

  private void updateModel(String text, String category) {
    try {
      schema.put(text, category);
    } catch (IOException e) {
      LOG.severe("Error adding entry to the classifier " + e.getMessage());
    }
  }

  private void addCategoryLabel(Repository repo, int prNumber, String category) throws Exception {
    IssueService srv = new IssueService(client);
    Issue issue = srv.getIssue(repo, prNumber);
    issue.getLabels().add(new Label().setName(LABEL_PREFIX + LABEL_SEPARATOR + category));
    Issue edited = srv.editIssue(repo, issue);
    LOG.fine("Label " + category + " added to the issue. " + edited);
  }

}