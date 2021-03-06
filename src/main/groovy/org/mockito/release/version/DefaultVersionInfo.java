package org.mockito.release.version;

import org.mockito.release.internal.gradle.util.StringUtil;
import org.mockito.release.notes.util.IOUtil;

import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

class DefaultVersionInfo implements VersionInfo {

    private final File versionFile;
    private final LinkedList<String> notableVersions;
    private final String version;

    DefaultVersionInfo(File versionFile, String version, LinkedList<String> notableVersions) {
        this.versionFile = versionFile;
        this.version = version;
        this.notableVersions = notableVersions;
    }

    static DefaultVersionInfo fromFile(File versionFile) {
        Properties properties = readProperties(versionFile);
        String version = properties.getProperty("version");
        if (version == null) {
            throw new IllegalArgumentException("Missing 'version=' properties in file: " + versionFile);
        }
        LinkedList<String> notableVersions = parseNotableVersions(properties);
        return new DefaultVersionInfo(versionFile, version, notableVersions);
    }

    private static LinkedList<String> parseNotableVersions(Properties properties) {
        LinkedList<String> result = new LinkedList<String>();
        String value = properties.getProperty("notableVersions");
        if (value != null) {
            String[] versions = value.split(",");
            for (String v : versions) {
                result.add(v.trim());
            }
        }
        return result;
    }

    private static Properties readProperties(File versionFile) {
        Properties p = new Properties();
        FileReader reader = null;
        try {
            reader = new FileReader(versionFile);
            p.load(reader);
        } catch (Exception e) {
            throw new RuntimeException("Problems reading version file: " + versionFile);
        } finally {
            IOUtil.close(reader);
        }
        return p;
    }

    public String getVersion() {
        return version;
    }

    public DefaultVersionInfo bumpVersion(boolean updateNotable) {
        String content = IOUtil.readFully(versionFile);
        if (updateNotable) {
            notableVersions.addFirst(version);
            String asString = "notableVersions=" + StringUtil.join(notableVersions, ", ") + "\n";
            if (notableVersions.size() == 1) {
                //when no prior notable versions, we just add new entry
                content += "\n" + asString;
            } else {
                //update existing entry
                content = content.replaceAll("(?m)^notableVersions=(.*?)\n", asString);
            }
        }

        String newVersion = new VersionBumper().incrementVersion(this.version);
        if (!content.endsWith("\n")) {
            //This makes the regex simpler. Add arbitrary end of line at the end of file should not bother anyone.
            //See also unit tests for this class
            content += "\n";
        }
        String updated = content.replaceAll("(?m)^version=(.*?)\n", "version=" + newVersion + "\n");

        IOUtil.writeFile(versionFile, updated);
        return new DefaultVersionInfo(versionFile, newVersion, notableVersions);
    }

    public Collection<String> getNotableVersions() {
        return notableVersions;
    }

    public boolean isNotableRelease() {
        //TODO also check for env variable here / commit message, we already check for 'TRAVIS_COMMIT_MESSAGE' elsewhere
        return version.endsWith(".0") || version.endsWith(".0.0");
    }
}
