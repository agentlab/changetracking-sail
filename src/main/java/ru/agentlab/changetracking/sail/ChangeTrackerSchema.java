package ru.agentlab.changetracking.sail;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;

public abstract class ChangeTrackerSchema {
    public static final String NAMESPACE = "http://agentlab.ru/config/sail/changetracker#";

    public static final IRI INCLUDE_GRAPH;
    public static final IRI EXCLUDE_GRAPH;

    public static final IRI EVENTS_BUFFER_SIZE;

    public static final IRI INTERACTIVE_NOTIFICATIONS;

    static {
        INCLUDE_GRAPH = Values.iri(NAMESPACE, "includeGraph");
        EXCLUDE_GRAPH = Values.iri(NAMESPACE, "excludeGraph");
        INTERACTIVE_NOTIFICATIONS = Values.iri(NAMESPACE, "interactiveNotifications");
        EVENTS_BUFFER_SIZE = Values.iri(NAMESPACE, "eventsBufferSize");
    }
}
