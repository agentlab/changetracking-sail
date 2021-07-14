package ru.agentlab.changetracking.filter;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;

import java.util.Arrays;
import java.util.Collection;

public record PatternGroup(MatchingStrategy matchingStrategy,
                           Filtering filtering,
                           Collection<FilteringPattern> patterns) implements FilteringPattern {

    public PatternGroup(MatchingStrategy strategy, Filtering filtering, FilteringPattern... patterns) {
        this(strategy, filtering, Arrays.asList(patterns));
    }

    @Override
    public Model filter(Model statements) {
        if (patterns.size() == 1) {
            for (var pattern : patterns) {
                return pattern.filter(statements);
            }
        }
        return switch (matchingStrategy) {
            case ALL_PATTERNS -> collectFromAllPatterns(statements);
            case ANY_PATTERN -> collectFromAnyPattern(statements);
        };
    }

    private Model collectFromAnyPattern(Model statements) {
        for (var pattern : patterns) {
            var fromPattern = pattern.filter(statements);
            if (fromPattern.size() != 0) {
                return fromPattern;
            }
        }
        return new LinkedHashModel();
    }

    private Model collectFromAllPatterns(Model statements) {
        var filtered = new LinkedHashModel();
        for (var pattern : patterns) {
            var fromPattern = pattern.filter(statements);
            if (fromPattern.size() == 0) {
                return fromPattern;
            }
            filtered.addAll(fromPattern);
        }
        return filtered;
    }
}
