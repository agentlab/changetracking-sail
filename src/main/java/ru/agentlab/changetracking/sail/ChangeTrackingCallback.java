package ru.agentlab.changetracking.sail;

import org.eclipse.rdf4j.model.Statement;

public interface ChangeTrackingCallback {
    void onStatementAdded(Statement st);

    void onStatementRemoved(Statement st);
}
