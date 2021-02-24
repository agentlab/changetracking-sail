package ru.agentlab.changetracking.sail;

import it.uniroma2.art.semanticturkey.changetracking.sail.FlagUpdateHandler;
import it.uniroma2.art.semanticturkey.changetracking.sail.StagingArea;
import it.uniroma2.art.semanticturkey.changetracking.sail.UpdateHandler;
import it.uniroma2.art.semanticturkey.changetracking.vocabulary.CHANGETRACKER;
import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.vocabulary.SESAME;
import org.eclipse.rdf4j.sail.*;
import org.eclipse.rdf4j.sail.helpers.NotifyingSailConnectionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ChangeTrackerConnection extends NotifyingSailConnectionWrapper {
    private static final Logger logger = LoggerFactory.getLogger(ChangeTrackerConnection.class);
    private final ChangeTracker sail;
    private final StagingArea stagingArea;
    private final Model connectionLocalGraphManagement;
    private final UpdateHandler readOnlyHandler;
    private SailConnectionListener connectionListener;
    private final ChangeTrackingCallbacks callbacks = new ChangeTrackingCallbacks();

    public ChangeTrackerConnection(NotifyingSailConnection wrappedCon, ChangeTracker sail) {
        super(wrappedCon);
        this.sail = sail;
        this.stagingArea = new StagingArea();
        this.connectionLocalGraphManagement = null;
        this.readOnlyHandler = new FlagUpdateHandler();
        initializeListener();
    }

    public void subscribe(ChangeTrackingCallback callback) {
        callbacks.subscribe(callback);
    }

    public void unsubscribe(ChangeTrackingCallback callback) {
        callbacks.unsubscribe(callback);
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
        try {
            super.close();
        } finally {
            removeConnectionListener(connectionListener);
        }
    }

    @Override
    public void begin(IsolationLevel level) throws SailException {
        if (level == null) {
            level = sail.getDefaultIsolationLevel();
        }

        List<IsolationLevel> supportedIsolationLevels = sail.getSupportedIsolationLevels();
        IsolationLevel compatibleLevel = IsolationLevels.getCompatibleIsolationLevel(level,
                                                                                     supportedIsolationLevels
        );
        if (compatibleLevel == null) {
            throw new UnknownSailTransactionStateException("Isolation level " + level
                                                                   + " not compatible with this Sail. Supported levels are: " + supportedIsolationLevels);
        }
        super.begin(compatibleLevel);

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
        synchronized (sail) {
            if (readOnlyHandler.isReadOnly()) {
                stagingArea.clear();
                super.commit();
            } else if (stagingArea.isEmpty()) {
                super.commit();
            } else {
                prepare();
                sendNotifications();
                super.commit();
                stagingArea.clear();
            }
            readOnlyHandler.clearHandler();
        }
    }

    private void sendNotifications() {
        callbacks.notifyStatementsRemoved(stagingArea.getRemovedStatements());
        callbacks.notifyStatementsAdded(stagingArea.getAddedStatements());
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

        if (context == null) {
            context = SESAME.NIL;
        }

        // A context is included iff it is pulled in by graph inclusions but thrown out by graph exclusions

        if (model.contains(CHANGETRACKER.GRAPH_MANAGEMENT, CHANGETRACKER.INCLUDE_GRAPH, context)
                || model.contains(CHANGETRACKER.GRAPH_MANAGEMENT, CHANGETRACKER.INCLUDE_GRAPH,
                                  SESAME.WILDCARD
        )
                || model.filter(CHANGETRACKER.GRAPH_MANAGEMENT, CHANGETRACKER.INCLUDE_GRAPH, null)
                .isEmpty()) {

            if (model.contains(CHANGETRACKER.GRAPH_MANAGEMENT, CHANGETRACKER.EXCLUDE_GRAPH, context) || model
                    .contains(CHANGETRACKER.GRAPH_MANAGEMENT, CHANGETRACKER.EXCLUDE_GRAPH, SESAME.WILDCARD)) {
                return false;
            }

            return true;
        }
        return false;
    }

    @Override
    public void addStatement(Resource subj, IRI pred, Value obj, Resource... contexts) throws SailException {
        if (contexts.length == 0) {
            try {
                readOnlyHandler.addStatement(subj, pred, obj, contexts);
                super.addStatement(subj, pred, obj, contexts);
            } catch (Exception e) {
                readOnlyHandler.recordCorruption();
                throw e;
            }
        }
    }

    @Override
    public void addStatement(UpdateContext modify, Resource subj, IRI pred, Value obj, Resource... contexts)
            throws SailException {
        if (contexts.length == 0) {
            try {
                readOnlyHandler.addStatement(modify, subj, pred, obj, contexts);
                super.addStatement(modify, subj, pred, obj, contexts);
            } catch (Exception e) {
                readOnlyHandler.recordCorruption();
                throw e;
            }
        }
    }

    @Override
    public void removeStatements(Resource subj, IRI pred, Value obj, Resource... contexts)
            throws SailException {
        if (contexts.length == 0) {
            try {
                readOnlyHandler.removeStatements(subj, pred, obj, contexts);
                super.removeStatements(subj, pred, obj, contexts);
            } catch (Exception e) {
                readOnlyHandler.recordCorruption();
                throw e;
            }
        }
    }

    @Override
    public void removeStatement(UpdateContext modify, Resource subj, IRI pred, Value obj, Resource... contexts)
            throws SailException {
        if (contexts.length == 0) {
            try {
                readOnlyHandler.removeStatement(modify, subj, pred, obj, contexts);
                super.removeStatement(modify, subj, pred, obj, contexts);
            } catch (Exception e) {
                readOnlyHandler.recordCorruption();
                throw e;
            }
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
