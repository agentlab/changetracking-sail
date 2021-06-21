package ru.agentlab.changetracking.filter;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

public record SubPattern(IRI predicate, Value object, boolean isCollector) {
}
