package ru.agentlab.changetracking.filter;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;

import java.util.ArrayList;
import java.util.List;

public record PropertyPattern(IRI predicate, Value object, Filtering filtering,
                              MatchingStrategy subPatternsFiltering,
                              List<SubPattern> subPatterns) implements FilteringPattern {

    public PropertyPattern(IRI predicate, Value object, Filtering filtering) {
        this(
                predicate,
                object,
                filtering,
                MatchingStrategy.ALL_PATTERNS,
                new ArrayList<>()
        );
    }

    @Override
    public Model filter(Model statements) {
        var fromRoot = statements.filter(null, predicate, object);
        if (fromRoot.size() == 0) {
            return fromRoot;
        }
        boolean anySubpatternMatched = subPatterns.size() == 0;
        for (Statement matchedWithRoot : statements) {
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

    @Override
    public Filtering filtering() {
        return filtering;
    }
}
