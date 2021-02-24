package ru.agentlab.changetracking.sail;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Values;

/**
 * Constant for the Change Tracker vocabulary used to interact with the {@link ChangeTracker}.
 *
 * @author <a href="fiorelli@info.uniroma2.it">Manuel Fiorelli</a>
 */
public abstract class CHANGETRACKER {
    /**
     * http://semanticturkey.uniroma2.it/ns/change-tracker#
     */
    public static final String NAMESPACE = "http://agentlab.ru/ns/change-tracker#";

    /**
     * Recommended prefix for the CHANGETRACKER namespace: "ct"
     */
    public static final String PREFIX = "ct";

    /**
     * An immutable {@link Namespace} constant that represents the CHANGETRACKER namespace.
     */
    public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);


    /**
     * ct:graph-management
     */
    public static final IRI GRAPH_MANAGEMENT;

    /**
     * ct:includeGraph
     */
    public static final IRI INCLUDE_GRAPH;

    /**
     * ct:excludeGraph
     */
    public static final IRI EXCLUDE_GRAPH;

    static {
        GRAPH_MANAGEMENT = Values.iri(NAMESPACE, "graph-management");
        INCLUDE_GRAPH = Values.iri(NAMESPACE, "includeGraph");
        EXCLUDE_GRAPH = Values.iri(NAMESPACE, "excludeGraph");
    }
}
