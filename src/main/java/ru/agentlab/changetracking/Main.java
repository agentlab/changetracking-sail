package ru.agentlab.changetracking;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
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
import ru.agentlab.changetracking.sail.ChangeTrackingCallback;
import ru.agentlab.changetracking.sail.ChangeTrackingFactory;

import java.io.IOException;

public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        SailRegistry.getInstance().add(new ChangeTrackingFactory());
        try (var repo = EmbeddedChangetrackingRepo.makeTempRepository("test-chtr")) {
            SailRepositoryConnection coreConn = repo.getConnection();
            ChangeTrackerConnection sailConnection = (ChangeTrackerConnection) coreConn.getSailConnection();
            var cb = new ChangeTrackingCallback() {

                @Override
                public void onStatementAdded(Statement st) {
                    logger.debug("Statement added: {}", st);
                }

                @Override
                public void onStatementRemoved(Statement st) {
                    logger.debug("Statement removed: {}", st);
                }
            };
            sailConnection.subscribe(cb);

            String prop1 = "uri:urn:property_1";
            String prop2 = "uri:urn:property_2";
            IRI obj = Values.iri("http://example.com/#Pep");
            IRI pred = Values.iri("http://example.com/#Kek");
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
            coreConn.begin();
            Variable objvar = SparqlBuilder.var("objvar");
            var where = GraphPatterns.tp(objvar, pred, Values.iri(prop1));
            var q = Queries.MODIFY()
                    .where(where)
                    .delete(GraphPatterns.tp(objvar, pred, Values.iri(prop2)));
            coreConn.prepareUpdate(q.getQueryString()).execute();
            coreConn.commit();
            dumpRepoContent(coreConn);
            sailConnection.unsubscribe(cb);
            coreConn.close();
        }
    }

    public static void dumpRepoContent(RepositoryConnection conn) {
        for (var statement : conn.getStatements(null, null, null)) {
            var sub = statement.getSubject().stringValue();
            var obj = statement.getObject().stringValue();
            var pred = statement.getPredicate().stringValue();
            logger.info("{}", sub + " " + pred + " " + obj);
        }
    }
}
