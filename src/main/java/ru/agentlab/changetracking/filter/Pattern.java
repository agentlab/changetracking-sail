package ru.agentlab.changetracking.filter;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;

import java.util.ArrayList;
import java.util.List;

public record Pattern(Resource subject, IRI predicate, Value object, Filtering filtering,
                      MatchingStrategy subPatternsFiltering,
                      List<SubPattern> subPatterns, IRI... contexts) implements FilteringPattern {

    public Pattern(Resource subject, IRI predicate, Value object, Filtering filtering) {
        this(
                subject,
                predicate,
                object,
                filtering,
                MatchingStrategy.ALL_PATTERNS,
                new ArrayList<>()
        );
    }

    public Pattern(Statement statement, Filtering filtering) {
        this(
                statement.getSubject(),
                statement.getPredicate(),
                statement.getObject(),
                filtering,
                MatchingStrategy.ALL_PATTERNS,
                new ArrayList<>()
        );
    }

    public Pattern(Statement statement, Filtering filtering, List<SubPattern> subPatterns) {
        this(
                statement.getSubject(),
                statement.getPredicate(),
                statement.getObject(),
                filtering,
                MatchingStrategy.ALL_PATTERNS,
                subPatterns
        );
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
        boolean anySubpatternMatched = subPatterns.size() == 0;
        for (Statement matchedWithRoot : fromRoot) {
            for (SubPattern subpattern : subPatterns) {
                var fromSubpattern = statements.filter(
                        matchedWithRoot.getSubject(),
                        subpattern.predicate(),
                        subpattern.object()
                );
                if (fromSubpattern.size() == 0) {
                    if (subPatternsFiltering.equals(MatchingStrategy.ALL_PATTERNS)) {
                        return fromSubpattern;
                    }
                } else {
                    anySubpatternMatched = true;
                }
            }
        }
        if (!anySubpatternMatched) {
            return new LinkedHashModel();
        }
        return fromRoot;
    }

}
