package ru.agentlab.changetracking.filter;

import org.eclipse.rdf4j.model.*;

import java.util.ArrayList;
import java.util.List;

public record Pattern(Resource subject, IRI predicate, Value object, Filtering filtering,
                      List<SubPattern> subPatterns) {

    public Pattern(Statement statement, Filtering filtering) {
        this(statement.getSubject(), statement.getPredicate(), statement.getObject(), filtering, new ArrayList<>());
    }

    private static boolean nullOrEquals(Value left, Value right) {
        return left == null || left.equals(right);
    }

    public Model filter(Model statements) {
        Model fromRoot = statements.filter(subject(), predicate(), object());
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
