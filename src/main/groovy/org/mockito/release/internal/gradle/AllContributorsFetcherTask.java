package org.mockito.release.internal.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.mockito.release.notes.contributors.*;

import java.io.File;

/**
 * Fetch data about all project contributors and store it in file. It is used later in generation pom.xml.
 */
public class AllContributorsFetcherTask extends DefaultTask {

    private static final Logger LOG = Logging.getLogger(AllContributorsFetcherTask.class);

    @Input private String repository;
    @Input private String readOnlyAuthToken;
    @Input private boolean skipTaskExecution;

    @OutputFile private File contributorsFile;

    @TaskAction
    public void fetchAllProjectContributorsFromGitHub() {
        if(skipTaskExecution) {
            LOG.lifecycle("  Fetching all contributors for project SKIPPED (disabled in configuration)");
            return;
        }

        // Fetching info about all contributors is expensive.
        // Task execution is skipped when output file exist for speed up local builds.
        if(contributorsFile.exists()) {
            LOG.lifecycle("  Fetching all contributors for project SKIPPED (output file exist)");
            return;
        }

        LOG.lifecycle("  Fetching all contributors for project");

        GitHubContributorsProvider contributorsProvider = Contributors.getGitHubContributorsProvider(repository, readOnlyAuthToken);
        ProjectContributorsSet allContributorsForProject = contributorsProvider.getAllContributorsForProject();

        AllContributorsSerializer serializer = Contributors.getAllContributorsSerializer(contributorsFile);
        serializer.serialize(allContributorsForProject);
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public void setReadOnlyAuthToken(String readOnlyAuthToken) {
        this.readOnlyAuthToken = readOnlyAuthToken;
    }

    public void setSkipTaskExecution(boolean skipTaskExecution) {
        this.skipTaskExecution = skipTaskExecution;
    }

    public void setContributorsFile(File contributorsFile) {
        this.contributorsFile = contributorsFile;
    }
}
