package org.mockito.release.exec;

import java.io.File;

/**
 * Process execution services
 */
public class Exec {

    //TODO move entire "org.mockito.release.exec" -> "org.mockito.release.internal.exec"

    /**
     * Provides process runner for given working dir
     */
    public static ProcessRunner getProcessRunner(File workDir) {
        return new DefaultProcessRunner(workDir);
    }
}
