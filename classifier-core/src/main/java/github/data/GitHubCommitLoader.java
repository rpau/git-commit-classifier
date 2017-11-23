package github.data;


import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.io.IOUtils;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.IssueEvent;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_COMMITS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;

public class GitHubCommitLoader {


    public void getDataTraining(String owner, String repo) throws Exception {
        GitHubClient client = new GitHubClient();
        client.setOAuth2Token("cb6588ee966e21d166f5e0a87b0380c6b179a941");
        GitHubClient diffClient = new GitHubClient(){
            @Override
            protected HttpURLConnection configureRequest(final HttpURLConnection request){
                HttpURLConnection result = super.configureRequest(request);
                result.setRequestProperty(HEADER_ACCEPT,
                        "application/vnd.github.VERSION.diff");
                return result;
            }

            protected Object getBody(GitHubRequest request, InputStream stream)
                    throws IOException {
                return IOUtils.toString(stream);
            }
        };
        diffClient.setOAuth2Token("8e0d5f998c9f2f690d9e34fbbe91b39039bf3965");
        IssueService srv = new IssueService(client);
        CommitService commitsSrv = new CommitService(client);

        Repository repository = new RepositoryService(client).getRepository(owner, repo);


        //getDataTraining(srv, diffClient, commitsSrv, repository, "Bug");
        //getDataTraining(srv, diffClient,  commitsSrv,repository, "Enhancement");
        getDataTraining(srv, diffClient,  commitsSrv,repository, "Feature-Request");
        //getDataTraining(srv, diffClient,  commitsSrv, repository, "CleanUp");
    }


    private void getDataTraining(IssueService srv,
                                               GitHubClient diffClient, CommitService commitsSrv,
                                               Repository repository, String label) throws Exception {
        Map<String, String> filters = new HashMap<String, String>();
        filters.put("state", "closed");
        filters.put("labels", label); //,kind/feature,kind/enhancement
        List<LabeledPoint> dataTraining = new LinkedList<LabeledPoint>();
        List<Issue> issues = srv.getIssues(repository, filters);

        try(CSVWriter writer = new CSVWriter(new FileWriter(new File("commits-"+label+".csv")))) {

            System.out.println("data training: " + label + ". Total: " + issues.size());

            int index = 0;
            for (Issue issue : issues) {

                PageIterator<IssueEvent> events = srv.pageIssueEvents(repository.getOwner().getLogin(), repository.getName(),
                        issue.getNumber());

                while (events.hasNext()) {
                    Collection<IssueEvent> issueEvents = events.next();
                    for (IssueEvent ie : issueEvents) {
                        String eventType = ie.getEvent().toLowerCase();

                        if ("merged".equals(eventType) || "closed".equals(eventType)) {
                            String commit = ie.getCommitId();
                            if (commit != null) {
                                try {

                                    StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
                                    uri.append('/').append(repository.getOwner().getLogin());
                                    uri.append('/').append(repository.getName());
                                    uri.append(SEGMENT_COMMITS);
                                    uri.append('/').append(commit);
                                    GitHubRequest request = new GitHubRequest();
                                    request.setUri(uri);
                                    request.setType(RepositoryCommit.class);

                                    GitHubResponse response = diffClient.get(request);
                                    String patch = response.getBody().toString();

                                } catch (RequestException e) {
                                    System.out.println("Error on commit " + commit + " at issue " + issue.getNumber());
                                }
                            }
                        }
                    }

                }

                index++;
                if (index % 10 == 0) {
                    System.out.println("data training: " + label + ". Status :[ " + index + " / " + issues.size() + " ]");
                }
            }
        }
    }


    public static void main(String[] args) throws Exception {
        GitHubCommitLoader loader = new GitHubCommitLoader();
        loader.getDataTraining("ReactiveX", "RxJava");


    }
}
