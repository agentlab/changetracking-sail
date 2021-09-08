package ru.agentlab.changetracking.sail;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF4J;
import org.eclipse.rdf4j.sail.config.AbstractDelegatingSailImplConfig;
import org.eclipse.rdf4j.sail.config.SailConfigException;
import org.eclipse.rdf4j.sail.config.SailImplConfig;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ChangeTrackerConfig extends AbstractDelegatingSailImplConfig {
    public final static int DEFAULT_EVENTS_QUEUE_BUFFER_SIZE = 64;
    private Set<IRI> includeGraph;
    private Set<IRI> excludeGraph;
    private int eventsQueueBufferSize = DEFAULT_EVENTS_QUEUE_BUFFER_SIZE;

    public ChangeTrackerConfig() {
        this(null);
    }

    public ChangeTrackerConfig(SailImplConfig delegate) {
        super(ChangeTrackingFactory.SAIL_TYPE, delegate);
        includeGraph = Collections.emptySet();
        excludeGraph = Collections.singleton(RDF4J.NIL);
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

    public int getEventsQueueBufferSize() {
        return eventsQueueBufferSize;
    }

    public void setEventsQueueBufferSize(int eventsQueueBufferSize) {
        this.eventsQueueBufferSize = eventsQueueBufferSize;
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
    }
}
