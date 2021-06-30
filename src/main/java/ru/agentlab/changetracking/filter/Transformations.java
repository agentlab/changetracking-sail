package ru.agentlab.changetracking.filter;


import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Transformations {

    public static Map<IRI, Model> groupBySubject(Stream<Statement> statements) {
        return statements.filter(statement -> statement.getSubject().isIRI())
                         .collect(Collectors.groupingBy(
                                 st -> (IRI) st.getSubject(),
                                 HashMap::new,
                                 Collectors.toCollection(LinkedHashModel::new)
                         ));
    }

    public static <T> Stream<T> groupBySubjectFromOrdered(Iterable<Statement> statements,
                                                          Supplier<T> containerSupplier,
                                                          BiConsumer<T, Statement> containerAdd) {
        var iter = statements.iterator();
        if (!iter.hasNext()) {
            return Stream.empty();
        }
        var builder = Stream.<T>builder();
        var st = iter.next();
        var currentSubj = st.getSubject();
        T container = containerSupplier.get();
        containerAdd.accept(container, st);
        while (iter.hasNext()) {
            st = iter.next();
            var subj = st.getSubject();
            if (!subj.equals(currentSubj)) {
                builder.accept(container);
                container = containerSupplier.get();
                currentSubj = subj;
            }
            containerAdd.accept(container, st);
        }
        builder.accept(container);
        return builder.build();
    }

    public static Stream<Model> groupBySubjectFromOrdered(Iterable<Statement> statements) {
        return groupBySubjectFromOrdered(statements, LinkedHashModel::new, Set::add);
    }


    public static Iterable<Model> groupedBySubject(Stream<Statement> statements) {
        return groupBySubject(statements).values();
    }

    public static Iterable<Model> groupedBySubject(Iterable<Statement> statements) {
        return groupBySubject(statements).values();
    }

    public static Map<IRI, Model> groupBySubject(Iterable<Statement> statements) {
        Map<IRI, Model> groupedBySubject = new HashMap<>();
        for (Statement triple : statements) {
            Resource subject = triple.getSubject();
            if (!subject.isIRI()) {
                continue;
            }
            groupedBySubject.compute((IRI) subject, (key, model) -> {
                if (model == null) {
                    model = new LinkedHashModel();
                }
                model.add(triple);
                return model;
            });
        }
        return groupedBySubject;
    }
}