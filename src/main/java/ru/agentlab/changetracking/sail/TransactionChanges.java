package ru.agentlab.changetracking.sail;

import org.eclipse.rdf4j.model.Model;

import java.util.Objects;

public class TransactionChanges {
    private final Model addedStatements;
    private final Model removedStatements;

    public TransactionChanges(Model addedStatements, Model removedStatements) {
        this.addedStatements = addedStatements;
        this.removedStatements = removedStatements;
    }

    public Model getAddedStatements() {
        return addedStatements;
    }

    public Model getRemovedStatements() {
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
