package ru.agentlab.changetracking.sail;

import com.google.common.base.MoreObjects;
import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.sail.NotifyingSailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.helpers.NotifyingSailWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ChangeTracker extends NotifyingSailWrapper {
    private final Logger logger = LoggerFactory.getLogger(ChangeTracker.class);
    private final LinkedHashModel graphManagement;
    private final ChangeTrackingEvents callbacks;

    public ChangeTracker(Set<IRI> includeGraph, Set<IRI> excludeGraph, int eventsBufferSize) {
        this.graphManagement = new LinkedHashModel();
        for (IRI g : includeGraph) {
            graphManagement.add(CHANGETRACKER.GRAPH_MANAGEMENT, CHANGETRACKER.INCLUDE_GRAPH, g);
        }
        for (IRI g : excludeGraph) {
            graphManagement.add(CHANGETRACKER.GRAPH_MANAGEMENT, CHANGETRACKER.EXCLUDE_GRAPH, g);
        }

        callbacks = new ChangeTrackingEvents(eventsBufferSize);
    }

    @Override
    public void shutDown() throws SailException {
        var res = callbacks.close();
        if (res.isFailure()) {
            logger.warn("failed to complete change obsevers");
        } else {
            logger.info("Successfully completed observers");
        }
        super.shutDown();
    }

    @Override
    public ChangeTrackerConnection getConnection() throws SailException {
        logger.debug("Obtaining new connection");
        NotifyingSailConnection delegate = super.getConnection();
        return new ChangeTrackerConnection(delegate, callbacks, this);
    }

    public Model getGraphManagement() {
        return graphManagement;
    }
}
