package org.mockito.release.notes.vcs

import org.mockito.release.exec.ProcessRunner
import spock.lang.Specification
import spock.lang.Subject

class GitRevisionProviderTest extends Specification {

    def runner = Mock(ProcessRunner)
    @Subject provider = new GitRevisionProvider(runner)

    def "should trim new lines from git command output"() {
        runner.run("git", "rev-list", "-n", "1", "v1.1") >> "1234\n"

        when:
        def result = provider.getRevisionForTagOrRevision("v1.1")

        then:
        result == "1234"
    }
}
