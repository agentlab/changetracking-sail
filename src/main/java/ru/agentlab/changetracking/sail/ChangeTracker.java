package ru.agentlab.changetracking.sail;

import com.google.common.base.MoreObjects;
import it.uniroma2.art.semanticturkey.changetracking.vocabulary.CHANGETRACKER;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ChangeTracker extends NotifyingSailWrapper {
    private final Logger logger = LoggerFactory.getLogger(ChangeTracker.class);
    private final LinkedHashModel graphManagement;
    private final Boolean interactiveNotifications;

    public ChangeTracker(Set<IRI> includeGraph, Set<IRI> excludeGraph, Optional<Boolean> interactiveNotifications) {
        this.graphManagement = new LinkedHashModel();
        for (IRI g : includeGraph) {
            graphManagement.add(CHANGETRACKER.GRAPH_MANAGEMENT, CHANGETRACKER.INCLUDE_GRAPH, g);
        }
        for (IRI g : excludeGraph) {
            graphManagement.add(CHANGETRACKER.GRAPH_MANAGEMENT, CHANGETRACKER.EXCLUDE_GRAPH, g);
        }
        this.interactiveNotifications = interactiveNotifications.orElse(true);
    }

    @Override
    public ChangeTrackerConnection getConnection() throws SailException {
        logger.debug("Obtaining new connection");
        NotifyingSailConnection delegate = super.getConnection();
        return new ChangeTrackerConnection(delegate, this);
    }

    @Override
    public IsolationLevel getDefaultIsolationLevel() {
        IsolationLevel isolationLevel = super.getDefaultIsolationLevel();

        if (!interactiveNotifications) {
            return isolationLevel;
        } else {
            if (isolationLevel.isCompatibleWith(IsolationLevels.SERIALIZABLE)) {
                return isolationLevel;
            } else {
                return MoreObjects.firstNonNull(
                        IsolationLevels.getCompatibleIsolationLevel(IsolationLevels.SERIALIZABLE,
                                                                    super.getSupportedIsolationLevels()
                        ),
                        IsolationLevels.SERIALIZABLE
                );
            }
        }
    }

    @Override
    public List<IsolationLevel> getSupportedIsolationLevels() {
        List<IsolationLevel> supportedByDelegate = super.getSupportedIsolationLevels();

        if (!interactiveNotifications) {
            return supportedByDelegate;
        } else {
            return supportedByDelegate.stream()
                    .filter(level -> level.isCompatibleWith(IsolationLevels.SERIALIZABLE))
                    .collect(Collectors.toList());
        }
    }

    public Model getGraphManagement() {
        return graphManagement;
    }
}
