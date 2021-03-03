package ru.agentlab.changetracking.sail;

import org.eclipse.rdf4j.model.Statement;

import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionChanges that = (TransactionChanges) o;
        return Objects.equals(addedStatements,
                              that.addedStatements
        ) && Objects.equals(removedStatements, that.removedStatements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addedStatements, removedStatements);
    }
}
