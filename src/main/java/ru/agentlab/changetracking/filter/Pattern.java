package ru.agentlab.changetracking.filter;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import ru.agentlab.changetracking.filter.ChangetrackingFilter;

public class Pattern {
    private final Value object;
    private final Resource subject;
    private final IRI predicate;
    private final ChangetrackingFilter.Filtering filtering;

    public Pattern(Statement statement, ChangetrackingFilter.Filtering filtering) {
        this(statement.getSubject(), statement.getPredicate(), statement.getObject(), filtering);
    }

    public Pattern(Resource subject, IRI predicate, Value object, ChangetrackingFilter.Filtering filtering) {
        this.object = object;
        this.subject = subject;
        this.predicate = predicate;
        this.filtering = filtering;
    }

    public boolean match(Statement statement) {
        boolean objectMatches = nullOrEquals(object, statement.getObject());
        boolean subjectMatches = nullOrEquals(subject, statement.getSubject());
        boolean predicateMatches = nullOrEquals(predicate, statement.getPredicate());
        return objectMatches && subjectMatches && predicateMatches;
    }

    private static boolean nullOrEquals(Value left, Value right) {
        return left == null || left.equals(right);
    }

    public ChangetrackingFilter.Filtering getFiltering() {
        return filtering;
    }

    public Value getObject() {
        return object;
    }

    public Resource getSubject() {
        return subject;
    }

    public IRI getPredicate() {
        return predicate;
    }
}
