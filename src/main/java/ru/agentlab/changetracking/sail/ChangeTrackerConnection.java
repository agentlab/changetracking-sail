package ru.agentlab.changetracking.sail;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.vocabulary.SESAME;
import org.eclipse.rdf4j.sail.*;
import org.eclipse.rdf4j.sail.helpers.NotifyingSailConnectionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

import java.util.List;

public class ChangeTrackerConnection extends NotifyingSailConnectionWrapper {
    private static final Logger logger = LoggerFactory.getLogger(ChangeTrackerConnection.class);
    private final ChangeTracker sail;
    private final StagingArea stagingArea;
    private final Model connectionLocalGraphManagement;
    private final UpdateHandler readOnlyHandler;
    private SailConnectionListener connectionListener;
    private final ChangeTrackingEvents eventSource;

    public ChangeTrackerConnection(NotifyingSailConnection wrappedCon,
                                   ChangeTrackingEvents eventSource,
                                   ChangeTracker sail) {
        super(wrappedCon);
        this.eventSource = eventSource;
        this.sail = sail;
        this.stagingArea = new StagingArea();
        this.connectionLocalGraphManagement = null;
        this.readOnlyHandler = new FlagUpdateHandler();
        initializeListener();
    }

    private void initializeListener() {
        this.connectionListener = new SailConnectionListener() {
            @Override
            public void statementAdded(Statement st) {
                if (shouldTrackStatement(st)) {
                    stagingArea.stageAddition(st);
                }
            }

            @Override
            public void statementRemoved(Statement st) {
                if (shouldTrackStatement(st)) {
                    stagingArea.stageRemoval(st);
                }
            }
        };
        addConnectionListener(connectionListener);
    }

    @Override
    public void close() throws SailException {
        removeConnectionListener(connectionListener);
        super.close();
    }

    public Flux<TransactionChanges> events() {
        return eventSource.events();
    }

    public Flux<TransactionChanges> events(Scheduler scheduler) {
        return eventSource.events(scheduler);
    }

    @Override
    public void begin(IsolationLevel level) throws SailException {
        if (level == null) {
            level = sail.getDefaultIsolationLevel();
        }

        super.begin(level);
        stagingArea.clear();
        readOnlyHandler.clearHandler();
        logger.debug("Transaction Begin / Isolation Level = {}", level);
    }

    @Override
    public void rollback() throws SailException {
        try {
            readOnlyHandler.clearHandler();
            stagingArea.clear();
        } finally {
            super.rollback();
        }
        logger.debug("Transaction Rollback");
    }

    @Override
    public void commit() throws SailException {
        if (readOnlyHandler.isReadOnly()) {
            stagingArea.clear();
            super.commit();
            return;
        } else if (stagingArea.isEmpty()) {
            super.commit();
            return;
        }
        prepare();
        var changes = new TransactionChanges(
                stagingArea.getAddedStatements(),
                stagingArea.getRemovedStatements()
        );
        super.commit();
        stagingArea.clear();
        readOnlyHandler.clearHandler();
        var result = eventSource.nextEvent(changes);
        if (result.isFailure()) {
            logger.debug("fail to emit next changes {}", result);
        }
    }

    private Model getGraphManagementModel() {
        if (connectionLocalGraphManagement != null) {
            return connectionLocalGraphManagement;
        } else {
            return sail.getGraphManagement();
        }
    }

    protected boolean shouldTrackStatement(Statement st) {
        Model model = getGraphManagementModel();

        Resource context = st.getContext();

        if (model.filter(CHANGETRACKER.GRAPH_MANAGEMENT, CHANGETRACKER.INCLUDE_GRAPH, null)
                 .isEmpty()) {

            return !model.contains(CHANGETRACKER.GRAPH_MANAGEMENT, CHANGETRACKER.EXCLUDE_GRAPH, context) && !model
                    .contains(CHANGETRACKER.GRAPH_MANAGEMENT, CHANGETRACKER.EXCLUDE_GRAPH, SESAME.WILDCARD);
        }
        return false;
    }

    @Override
    public void addStatement(Resource subj, IRI pred, Value obj, Resource... contexts) throws SailException {
        try {
            readOnlyHandler.addStatement(subj, pred, obj, contexts);
            super.addStatement(subj, pred, obj, contexts);
        } catch (Exception e) {
            readOnlyHandler.recordCorruption();
            throw e;
        }
    }

    @Override
    public void addStatement(UpdateContext modify, Resource subj, IRI pred, Value obj, Resource... contexts)
            throws SailException {
        try {
            readOnlyHandler.addStatement(modify, subj, pred, obj, contexts);
            super.addStatement(modify, subj, pred, obj, contexts);
        } catch (Exception e) {
            readOnlyHandler.recordCorruption();
            throw e;
        }
    }

    @Override
    public void removeStatements(Resource subj, IRI pred, Value obj, Resource... contexts)
            throws SailException {
        try {
            readOnlyHandler.removeStatements(subj, pred, obj, contexts);
            super.removeStatements(subj, pred, obj, contexts);
        } catch (Exception e) {
            readOnlyHandler.recordCorruption();
            throw e;
        }
    }

    @Override
    public void removeStatement(UpdateContext modify, Resource subj, IRI pred, Value obj, Resource... contexts)
            throws SailException {
        try {
            readOnlyHandler.removeStatement(modify, subj, pred, obj, contexts);
            super.removeStatement(modify, subj, pred, obj, contexts);
        } catch (Exception e) {
            readOnlyHandler.recordCorruption();
            throw e;
        }
    }

    @Override
    public void clear(Resource... contexts) throws SailException {
        readOnlyHandler.clear(contexts);
        try {
            super.clear(contexts);
        } catch (Exception e) {
            readOnlyHandler.recordCorruption();
            throw e;
        }
    }

    @Override
    public void clearNamespaces() throws SailException {
        readOnlyHandler.clearNamespaces();
        try {
            super.clearNamespaces();
        } catch (Exception e) {
            readOnlyHandler.recordCorruption();
            throw e;
        }
    }

    @Override
    public void removeNamespace(String prefix) throws SailException {
        readOnlyHandler.removeNamespace(prefix);
        try {
            super.removeNamespace(prefix);
        } catch (Exception e) {
            readOnlyHandler.recordCorruption();
            throw e;
        }
    }
}
