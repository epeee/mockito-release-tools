package org.mockito.release.internal.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.mockito.release.gradle.ReleaseConfiguration;
import org.mockito.release.version.VersionInfo;

/**
 * Adds extension for configuring the release to the root project.
 * Important: it will add to the root project because this is where the configuration belong to!
 * Adds following behavior:
 * <ul>
 *     <li>Adds and preconfigures 'releasing' extension of type {@link ReleaseConfiguration}</li>
 *     <li>Configures 'releasing.dryRun' setting based on 'releasing.dryRun' Gradle project property</li>
 *     <li>Configures 'releasing.notableRelease' setting based on the version we are currently building</li>
 * </ul>
 */
public class ReleaseConfigurationPlugin implements Plugin<Project> {

    private ReleaseConfiguration configuration;

    public void apply(Project project) {
        if (project.getParent() == null) {
            //root project, add the extension
            project.getPlugins().apply(VersioningPlugin.class);
            VersionInfo info = project.getExtensions().getByType(VersionInfo.class);

            configuration = project.getRootProject().getExtensions()
                    .create("releasing", ReleaseConfiguration.class);

            if (project.hasProperty("releasing.dryRun")) {
                Object value = project.getProperties().get("releasing.dryRun");
                configuration.setDryRun(!"false".equals(value));
                //TODO we can actually implement it so that we automatically preconfigure everything by command line parameters
                //e.g. releasing.gitHub.repository is also a property
            }

            configuration.setNotableRelease(info.isNotableRelease());
        } else {
            //not root project, get extension from root project
            configuration = project.getRootProject().getPlugins().apply(ReleaseConfigurationPlugin.class).getConfiguration();
        }
    }

    /**
     * Returns the release configuration instance that is hooked up to the root project
     */
    public ReleaseConfiguration getConfiguration() {
        return configuration;
    }
}
