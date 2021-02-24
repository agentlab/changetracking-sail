package ru.agentlab.changetracking.sail;

import org.eclipse.rdf4j.model.Statement;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ChangeTrackingCallbacks {

    private final Set<ChangeTrackingCallback> callbacks = new HashSet<>();

    public void notifyStatementsRemoved(Collection<Statement> statements) {
        for (Statement statement : statements) {
            notifyStatementRemoved(statement);
        }
    }

    public void notifyStatementsAdded(Collection<Statement> statements) {
        for (Statement statement : statements) {
            notifyStatementAdded(statement);
        }
    }

    public void notifyStatementRemoved(Statement st) {
        callbacks.forEach(cb -> cb.onStatementRemoved(st));
    }

    public void notifyStatementAdded(Statement st) {
        callbacks.forEach(cb -> cb.onStatementAdded(st));

    }

    public void subscribe(ChangeTrackingCallback callback) {
        callbacks.add(callback);
    }

    public void unsubscribe(ChangeTrackingCallback callback) {
        callbacks.remove(callback);
    }
}
