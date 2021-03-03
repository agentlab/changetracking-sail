package ru.agentlab.changetracking.sail;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A data structure keeping track of quadruples effectively added or removed, as well as of the commit
 * metadata.
 *
 * @author <a href="fiorelli@info.uniroma2.it">Manuel Fiorelli</a>
 */
public class StagingArea {
    private final Set<Statement> addedStatements;
    private final Set<Statement> removedStatements;
    private final Model commitMetadata;

    public StagingArea() {
        addedStatements = new HashSet<>();
        removedStatements = new HashSet<>();
        commitMetadata = new LinkedHashModel();
    }

    public void clear() {
        addedStatements.clear();
        removedStatements.clear();
        commitMetadata.clear();
    }

    public void stageAddition(Statement st) {
        if (removedStatements.contains(st)) {
            removedStatements.remove(st);
            return;
        }
        addedStatements.add(st);
    }

    public void stageRemoval(Statement st) {
        if (addedStatements.contains(st)) {
            addedStatements.remove(st);
            return;
        }
        removedStatements.add(st);
    }

    public Set<Statement> getAddedStatements() {
        return Set.copyOf(addedStatements);
    }

    public Set<Statement> getRemovedStatements() {
        return Set.copyOf(removedStatements);
    }

    public boolean isEmpty() {
        return addedStatements.isEmpty() && removedStatements.isEmpty();
    }

    public Model getCommitMetadataModel() {
        return commitMetadata;
    }
}
