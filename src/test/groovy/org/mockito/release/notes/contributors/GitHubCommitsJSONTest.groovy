package org.mockito.release.notes.contributors

import org.json.simple.JsonObject
import spock.lang.Specification

class GitHubCommitsJSONTest extends Specification {

    def "parse commits"() {
        def commit = new JsonObject([commit: [author: [name: "Continuous Delivery Drone"]],
                                      author: [login: "continuous-delivery-drone",
                                               html_url: "https://github.com/continuous-delivery-drone"]])

        when:
        def contributor = GitHubCommitsJSON.toContributor(commit)

        then:
        contributor.name == "Continuous Delivery Drone"
        contributor.login == "continuous-delivery-drone"
        contributor.profileUrl == "https://github.com/continuous-delivery-drone"
    }

    def "return null when author doesn't exit (author deleted GitHub account)"() {
        // example: https://github.com/mockito/mockito/commit/87f3a5ee98fcc7c4b6d6d30aec0a2e64562a36eb account for Ben Yu doesn't exist anymore
        def commit = new JsonObject([commit: [author: [name: "Continuous Delivery Drone"]],
                                      author: null])

        when:
        def contributor = GitHubCommitsJSON.toContributor(commit)

        then:
        contributor == null
    }

    def "return true when commit contains revision"() {
        def commit = new JsonObject([sha: '1234'])

        when:
        def result = GitHubCommitsJSON.containsRevision(commit, "1234")

        then:
        result == true
    }

    def "return false when commit NOT contains revision"() {
        def commit = new JsonObject([sha: '1234'])

        when:
        def result = GitHubCommitsJSON.containsRevision(commit, "abc")

        then:
        result == false
    }
}
