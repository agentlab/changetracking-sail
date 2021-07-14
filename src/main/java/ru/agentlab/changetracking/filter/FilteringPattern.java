package ru.agentlab.changetracking.filter;

import org.eclipse.rdf4j.model.Model;

public interface FilteringPattern {

    Model filter(Model statements);

    Filtering filtering();
}
