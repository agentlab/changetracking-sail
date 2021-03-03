package ru.agentlab.changetracking;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.sail.config.SailRegistry;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.agentlab.changetracking.sail.ChangeTrackerConnection;
import ru.agentlab.changetracking.sail.ChangeTrackingFactory;

import java.io.IOException;

public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        chtr();
    }

    public static void chtr() throws IOException {
        SailRegistry.getInstance().add(new ChangeTrackingFactory());
        EmbeddedChangetrackingRepo.makeTempRepository("test-chtr");
        var repo = EmbeddedChangetrackingRepo.makeTempRepository("test-repo");
        SailRepositoryConnection coreConn = repo.getConnection();
        ChangeTrackerConnection sailConnection = (ChangeTrackerConnection) coreConn.getSailConnection();
        sailConnection.events()
                .take(2)
                .doFinally(signal -> {
                    logger.info("Received {}, shutting down the subscriber...", signal);
                    coreConn.close();
                    repo.close();
                    logger.info("Shutting down the subscriber... Done");
                })
                .doOnComplete(() -> dumpRepoContent(coreConn))
                .subscribe(changes -> {
                    changes.getAddedStatements().forEach(st -> logger.info("added: {}", st));
                    changes.getRemovedStatements().forEach(st -> logger.info("removed: {}", st));
                });

        sailConnection.events()
                .doFinally(signal -> logger.info("im complete"))
                .subscribe(changes -> logger.info("im seeing changes"));

        sailConnection.events()
                .doFinally(signal -> logger.info("im also complete"))
                .subscribe(changes -> logger.info("im also seeing changes"));

        String prop1 = "urn:agentlab:property_1";
        String prop2 = "urn:agentlab:property_2";
        IRI obj = Values.iri("urn:agentlab:object");
        IRI pred = Values.iri("urn:agentlab:predicate");

        coreConn.begin();
        coreConn.add(obj,
                     pred,
                     Values.iri(prop1)
        );
        coreConn.add(obj,
                     pred,
                     Values.iri(prop2)
        );
        coreConn.commit();

        Variable objvar = SparqlBuilder.var("objvar");
        var q = Queries.MODIFY()
                .where(GraphPatterns.tp(objvar, pred, Values.iri(prop1)))
                .delete(GraphPatterns.tp(objvar, pred, Values.iri(prop2)));
        logger.info(q.getQueryString());
        coreConn.prepareUpdate(q.getQueryString()).execute();
    }

    public static void dumpRepoContent(RepositoryConnection conn) {
        logger.info("repository content");
        logger.info("---------------");
        for (var statement : conn.getStatements(null, null, null)) {
            var sub = statement.getSubject().stringValue();
            var obj = statement.getObject().stringValue();
            var pred = statement.getPredicate().stringValue();
            logger.info("{} {} {}", sub, pred, obj);
        }
        logger.info("---------------");
    }
}
