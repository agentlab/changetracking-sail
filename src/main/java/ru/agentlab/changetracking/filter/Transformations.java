package ru.agentlab.changetracking.filter;


import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;

import java.util.HashMap;
import java.util.Map;
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