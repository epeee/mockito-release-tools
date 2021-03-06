package org.mockito.release.notes.contributors;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.json.simple.DeserializationException;
import org.json.simple.JsonObject;
import org.mockito.release.notes.model.ProjectContributor;
import org.mockito.release.notes.util.GitHubListFetcher;
import org.mockito.release.notes.util.GitHubObjectFetcher;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GitHubAllContributorsFetcher {

    private static final Logger LOG = Logging.getLogger(GitHubAllContributorsFetcher.class);

    ProjectContributorsSet fetchAllContributorsForProject(String repository, String readOnlyAuthToken) {
        LOG.lifecycle("Querying GitHub API for all contributors for project");
        ProjectContributorsSet result = new DefaultProjectContributorsSet();

        try {
            GitHubProjectContributors contributors =
                    GitHubProjectContributors.authenticatingWith(repository, readOnlyAuthToken).build();

            while(contributors.hasNextPage()) {
                List<JsonObject> page = contributors.nextPage();
                result.addAllContributors(extractContributors(page, readOnlyAuthToken));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private Set<ProjectContributor> extractContributors(List<JsonObject> page, String readOnlyAuthToken) throws IOException, DeserializationException {
        Set<ProjectContributor> result = new HashSet<ProjectContributor>();
        for (JsonObject contributor : page) {
            String url = (String) contributor.get("url");
            GitHubObjectFetcher userFetcher = new GitHubObjectFetcher(url, readOnlyAuthToken);
            JsonObject user = userFetcher.getPage();
            result.add(GitHubAllContributorsJson.toContributor(contributor, user));
        }
        return result;
    }

    private static class GitHubProjectContributors {
        private final GitHubListFetcher fetcher;
        private List<JsonObject> lastFetchedPage;

        static GitHubProjectContributorsBuilder authenticatingWith(String repository, String readOnlyAuthToken) {
            return new GitHubProjectContributorsBuilder(repository, readOnlyAuthToken);
        }

        private GitHubProjectContributors(String nextPageUrl) {
            fetcher = new GitHubListFetcher(nextPageUrl);
        }

        public boolean hasNextPage() {
            return fetcher.hasNextPage();
        }

        public List<JsonObject> nextPage() throws IOException, DeserializationException {
            lastFetchedPage = fetcher.nextPage();
            return lastFetchedPage;
        }
    }

    private static class GitHubProjectContributorsBuilder {

        private final String repository;
        private final String readOnlyAuthToken;

        public GitHubProjectContributorsBuilder(String repository, String readOnlyAuthToken) {
            this.repository = repository;
            this.readOnlyAuthToken = readOnlyAuthToken;
        }

        GitHubProjectContributors build() {
            // see API doc: https://developer.github.com/v3/repos/#list-contributors
            String nextPageUrl = String.format("%s%s",
                    "https://api.github.com/repos/" + repository + "/contributors",
                    "?access_token=" + readOnlyAuthToken);
            return new GitHubProjectContributors(nextPageUrl);
        }
    }
}
