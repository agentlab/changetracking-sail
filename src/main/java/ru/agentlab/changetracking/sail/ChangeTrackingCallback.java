package ru.agentlab.changetracking.sail;

import org.eclipse.rdf4j.model.Statement;

import java.util.Set;

public interface ChangeTrackingCallback {
    void onCommit(Set<Statement> addedStatements, Set<Statement> removedStatements);
}
