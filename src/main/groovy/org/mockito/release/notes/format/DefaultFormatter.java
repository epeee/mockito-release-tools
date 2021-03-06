package org.mockito.release.notes.format;

import org.mockito.release.internal.util.MultiMap;
import org.mockito.release.notes.contributors.ContributorsSet;
import org.mockito.release.notes.internal.DateFormat;
import org.mockito.release.notes.model.*;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Original formatter
 */
class DefaultFormatter implements SingleReleaseNotesFormatter {

    private final Map<String, String> labelMapping;
    private final String publicationRepository;

    DefaultFormatter(Map<String, String> labelMapping, String publicationRepository) {
        this.labelMapping = labelMapping;
        this.publicationRepository = publicationRepository;
    }

    String format(Map<String, String> labels, Collection<Improvement> improvements) {
        if (improvements.isEmpty()) {
            return "* No notable improvements. See the commits for detailed changes.";
        }
        StringBuilder sb = new StringBuilder("* Improvements: ").append(improvements.size());
        MultiMap<String, Improvement> byLabel = new MultiMap<String, Improvement>();
        Set<Improvement> remainingImprovements = new LinkedHashSet<Improvement>(improvements);

        //Step 1, find improvements that match input labels
        //Iterate label first because the input labels determine the order
        for (String label : labels.keySet()) {
            for (Improvement i : improvements) {
                if (i.getLabels().contains(label) && remainingImprovements.contains(i)) {
                    remainingImprovements.remove(i);
                    byLabel.put(label, i);
                }
            }
        }

        //Step 2, print out the improvements that match input labels
        for (String label : byLabel.keySet()) {
            String labelCaption = labels.get(label);
            Collection<Improvement> labelImprovements = byLabel.get(label);
            sb.append("\n  * ").append(labelCaption).append(": ").append(labelImprovements.size());
            for (Improvement i : labelImprovements) {
                sb.append("\n    * ").append(CommonFormatting.format(i));
            }
        }

        //Step 3, print out remaining changes
        if (!remainingImprovements.isEmpty()) {
            String indent;
            //We want clean view depending if there are labelled improvements or not
            if (byLabel.size() > 0) {
                indent = "  ";
                sb.append("\n  * Remaining changes: ").append(remainingImprovements.size());
            } else {
                indent = "";
            }

            for (Improvement i : remainingImprovements) {
                sb.append("\n").append(indent).append("  * ").append(CommonFormatting.format(i));
            }
        }
        return sb.toString();
    }

    private String format(Contribution contribution, Contributor contributor) {
        if(contributor != null) {
            return String.format("%d: [%s](%s)",
                    contribution.getCommits().size(),
                    contribution.getAuthorName(),
                    contributor.getProfileUrl());
        }
        return contribution.getCommits().size() + ": " + contribution.getAuthorName();
    }

    private String format(ContributionSet contributions, ContributorsSet contributorsSet) {
        StringBuilder sb = new StringBuilder("* Authors: ")
                .append(contributions.getContributions().size())
                .append(", commits: ")
                .append(contributions.getAllCommits().size())
                .append(", published to: ")
                .append(publicationRepository);

        for (Contribution c : contributions.getContributions()) {
            Contributor contributor = contributorsSet.findByAuthorName(c.getAuthorName());
            sb.append("\n  * ").append(format(c, contributor));
        }

        return sb.toString();
    }

    public String formatVersion(ReleaseNotesData data) {
        String now = DateFormat.formatDate(data.getDate());

        return "### " + data.getVersion() + " (" + now + ")" + "\n\n"
                + format(data.getContributions(), data.getContributors()) + "\n"
                + format(labelMapping, data.getImprovements()) + "\n\n";
    }
}
