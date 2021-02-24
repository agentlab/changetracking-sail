package ru.agentlab.changetracking.sail;

import org.eclipse.rdf4j.model.Statement;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChangeTrackingCallbacks {

    private final Set<ChangeTrackingCallback> callbacks = new HashSet<>();

    public void onCommit(Set<Statement> addedStatements, Set<Statement> removedStatements) {
        for (var cb : callbacks) {
            cb.onCommit(addedStatements, removedStatements);
        }
    }

    public void close() {
        callbacks.clear();
    }

    public void subscribe(ChangeTrackingCallback callback) {
        callbacks.add(callback);
    }

    public void unsubscribe(ChangeTrackingCallback callback) {
        callbacks.remove(callback);
    }
}
