package ru.agentlab.changetracking.sail;

import org.eclipse.rdf4j.model.Statement;

import java.util.Set;

public class TransactionChanges {
    private final Set<Statement> addedStatements;
    private final Set<Statement> removedStatements;

    public TransactionChanges(Set<Statement> addedStatements, Set<Statement> removedStatements) {
        this.addedStatements = addedStatements;
        this.removedStatements = removedStatements;
    }

    public Set<Statement> getAddedStatements() {
        return addedStatements;
    }

    public Set<Statement> getRemovedStatements() {
        return removedStatements;
    }
}
