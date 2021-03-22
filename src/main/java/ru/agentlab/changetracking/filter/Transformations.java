package ru.agentlab.changetracking.filter;


import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;

import java.util.HashMap;
import java.util.Map;

public class Transformations {

    public static Map<IRI, Model> groupBySubject(Model statements) {
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