package ru.agentlab.changetracking.sail;

import it.uniroma2.art.semanticturkey.changetracking.sail.config.ChangeTrackerSchema;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.SESAME;
import org.eclipse.rdf4j.sail.config.AbstractDelegatingSailImplConfig;
import org.eclipse.rdf4j.sail.config.SailConfigException;
import org.eclipse.rdf4j.sail.config.SailImplConfig;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ChangeTrackerConfig extends AbstractDelegatingSailImplConfig {
    private Set<IRI> includeGraph;
    private Set<IRI> excludeGraph;
    private Boolean interactiveNotifications;

    public ChangeTrackerConfig() {
        this(null);
    }

    public ChangeTrackerConfig(SailImplConfig delegate) {
        super(ChangeTrackingFactory.SAIL_TYPE, delegate);
        includeGraph = Collections.emptySet();
        excludeGraph = Collections.singleton(SESAME.NIL);
        interactiveNotifications = null;
    }

    public Boolean getInteractiveNotifications() {
        return interactiveNotifications;
    }

    public void setInteractiveNotifications(Boolean interactiveNotifications) {
        this.interactiveNotifications = interactiveNotifications;
    }

    public Optional<Boolean> isInteractiveNotifications() {
        return Optional.ofNullable(interactiveNotifications);
    }

    public Set<IRI> getIncludeGraph() {
        return Collections.unmodifiableSet(includeGraph);
    }

    public void setIncludeGraph(Set<IRI> includeGraph) {
        this.includeGraph = new HashSet<>(includeGraph);
    }

    public Set<IRI> getExcludeGraph() {
        return Collections.unmodifiableSet(excludeGraph);
    }

    public void setExcludeGraph(Set<IRI> excludeGraph) {
        this.excludeGraph = new HashSet<>(excludeGraph);
    }

    @Override
    public Resource export(Model graph) {
        Resource implNode = super.export(graph);
        for (IRI g : includeGraph) {
            graph.add(implNode, ChangeTrackerSchema.INCLUDE_GRAPH, Values.literal(g));
        }
        for (IRI g : excludeGraph) {
            graph.add(implNode, ChangeTrackerSchema.EXCLUDE_GRAPH, Values.literal(g));
        }
        if (interactiveNotifications != null) {
            graph.add(implNode,
                      ChangeTrackerSchema.INTERACTIVE_NOTIFICATIONS,
                      Values.literal(interactiveNotifications)
            );
        }
        return implNode;
    }

    @Override
    public void parse(Model graph, Resource implNode) throws SailConfigException {
        super.parse(graph, implNode);
        includeGraph = graph.filter(implNode, ChangeTrackerSchema.INCLUDE_GRAPH, null)
                .stream()
                .map(Statement::getObject)
                .filter(obj -> obj instanceof IRI)
                .map(obj -> (IRI) obj)
                .collect(Collectors.toSet());

        excludeGraph = graph.filter(implNode, ChangeTrackerSchema.EXCLUDE_GRAPH, null)
                .stream()
                .map(Statement::getObject)
                .filter(obj -> obj instanceof IRI)
                .map(obj -> (IRI) obj)
                .collect(Collectors.toSet());


        Set<Value> interactiveNotificationsGraph = graph
                .filter(implNode, ChangeTrackerSchema.INTERACTIVE_NOTIFICATIONS, null).objects();
        if (interactiveNotificationsGraph.contains(Values.literal(true))) {
            interactiveNotifications = true;
        } else if (interactiveNotificationsGraph.contains(Values.literal(false))) {
            interactiveNotifications = false;
        } else {
            interactiveNotifications = null;
        }
    }
}
