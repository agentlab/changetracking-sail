package ru.agentlab.changetracking.filter;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import ru.agentlab.changetracking.sail.TransactionChanges;

import java.util.*;

public class ChangetrackingFilter {
    private final MatchingStrategy strategy;

    public enum MatchingStrategy {
        ALL_PATTERNS,
        ANY_PATTERN
    }

    public static class Builder {
        private List<Pattern> patterns = new ArrayList<>();
        private MatchingStrategy strategy = MatchingStrategy.ALL_PATTERNS;

        public Builder addPattern(Resource subject,
                                  IRI predicate,
                                  Value object,
                                  Filtering filtering) {
            return addPattern(new Pattern(subject, predicate, object, filtering, new ArrayList<>()));
        }

        public Builder addPattern(Resource subject,
                                  IRI predicate,
                                  Value object,
                                  Filtering filtering,
                                  SubPattern... subpatterns) {
            return addPattern(new Pattern(subject, predicate, object, filtering, Arrays.asList(subpatterns)));
        }

        public Builder addPattern(Resource subject,
                                  IRI predicate,
                                  Value object,
                                  Filtering filtering,
                                  List<SubPattern> subpatterns) {
            return addPattern(new Pattern(subject, predicate, object, filtering, subpatterns));
        }

        public Builder addPattern(Pattern pattern) {
            patterns.add(pattern);
            return this;
        }

        public Builder setPatterns(List<Pattern> patterns) {
            this.patterns = patterns;
            return this;
        }

        public Builder setMatchingStrategy(MatchingStrategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public ChangetrackingFilter build() {
            return new ChangetrackingFilter(patterns, strategy);
        }
    }

    private final List<Pattern> patterns;

    private ChangetrackingFilter(List<Pattern> patterns, MatchingStrategy strategy) {
        this.patterns = patterns;
        this.strategy = strategy;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<TransactionChanges> mapIfAnyMatches(TransactionChanges changes) {
        Model added = patterns.stream()
                              .filter(this::filtersAddedStatements)
                              .map(p -> p.filter(changes.getAddedStatements()))
                              .reduce(new LinkedHashModel(), (merged, model) -> {
                                  merged.addAll(model);
                                  return merged;
                              });

        Model removed = patterns.stream()
                                .filter(this::filtersRemovedStatements)
                                .map(p -> p.filter(changes.getRemovedStatements()))
                                .reduce(new LinkedHashModel(), (merged, model) -> {
                                    merged.addAll(model);
                                    return merged;
                                });

        if (added.isEmpty() && removed.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new TransactionChanges(added, removed));
    }

    private boolean filtersAddedStatements(Pattern pattern) {
        return pattern.filtering().equals(Filtering.ADDED) || pattern.filtering().equals(Filtering.ALL);
    }

    private boolean filtersRemovedStatements(Pattern pattern) {
        return pattern.filtering().equals(Filtering.REMOVED) || pattern.filtering().equals(Filtering.ALL);
    }

    public Optional<TransactionChanges> mapIfAllMatches(TransactionChanges changes) {
        Model added = new LinkedHashModel();
        Model removed = new LinkedHashModel();
        for (Pattern pattern : patterns) {
            boolean matched = false;
            if (filtersAddedStatements(pattern)) {
                var matches = pattern.filter(changes.getAddedStatements());
                matched = !matches.isEmpty();
                added.addAll(matches);
            }
            if (filtersRemovedStatements(pattern)) {
                var matches = pattern.filter(changes.getRemovedStatements());
                matched = matched || !matches.isEmpty();
                removed.addAll(matches);
            }
            if (!matched) {
                return Optional.empty();
            }
        }
        return Optional.of(new TransactionChanges(added, removed));
    }

    public Optional<Model> matchModel(Model model) {
        for (Pattern pattern : patterns) {
            if (model.filter(pattern.subject(), pattern.predicate(), pattern.object()).size() == 0) {
                return Optional.empty();
            }
        }
        return Optional.of(model);
    }

    public Optional<TransactionChanges> mapMatched(TransactionChanges changes) {
        return switch (this.strategy) {
            case ALL_PATTERNS -> mapIfAllMatches(changes);
            case ANY_PATTERN -> mapIfAnyMatches(changes);
        };
    }
}
