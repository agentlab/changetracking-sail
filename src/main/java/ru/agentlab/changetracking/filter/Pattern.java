package ru.agentlab.changetracking.filter;

import org.eclipse.rdf4j.model.*;

import java.util.ArrayList;
import java.util.List;

public record Pattern(Resource subject, IRI predicate, Value object, Filtering filtering,
                      List<SubPattern> subPatterns, IRI... contexts) {

    public Pattern(Statement statement, Filtering filtering) {
        this(statement.getSubject(), statement.getPredicate(), statement.getObject(), filtering, new ArrayList<>());
    }

    public Model filter(Model statements) {
        Model fromRoot;
        if (contexts != null && contexts.length == 0) {
            fromRoot = statements.filter(subject, predicate, object);
        } else {
            fromRoot = statements.filter(subject, predicate, object, contexts);
        }
        if (fromRoot.size() == 0) {
            return fromRoot;
        }
        for (Statement matchedWithRoot : fromRoot) {
            for (SubPattern subpattern : subPatterns) {
                var fromSubpattern = statements.filter(
                        matchedWithRoot.getSubject(),
                        subpattern.predicate(),
                        subpattern.object()
                );
                if (fromSubpattern.size() == 0) {
                    return fromSubpattern;
                }
                if (subpattern.isCollector()) {
                    fromRoot.addAll(fromSubpattern);
                }
            }
        }
        return fromRoot;
    }

}
