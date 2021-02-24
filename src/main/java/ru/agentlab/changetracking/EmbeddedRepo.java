package ru.agentlab.changetracking;

import it.uniroma2.art.semanticturkey.changetracking.sail.config.ChangeTrackerConfig;
import it.uniroma2.art.semanticturkey.changetracking.vocabulary.CHANGELOG;
import it.uniroma2.art.semanticturkey.changetracking.vocabulary.PROV;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.UnknownTransactionStateException;
import org.eclipse.rdf4j.repository.base.RepositoryConnectionWrapper;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.sail.nativerdf.config.NativeStoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class EmbeddedRepo implements Closeable {
    private final static Logger logger = LoggerFactory.getLogger(EmbeddedRepo.class);
    private final LocalRepositoryManager repositoryManager;
    private final static String HISTORY_REPO_ID = "history-repo";
    protected static final String HISTORY_NS = "http://example.org/history#";
    public static final IRI HISTORY_GRAPH = SimpleValueFactory.getInstance().createIRI("http://example.org/history");
    private Repository supportRepo;
    private Repository coreRepo;

    public EmbeddedRepo(File baseDir) throws RepositoryException {
        repositoryManager = new LocalRepositoryManager(baseDir);
        repositoryManager.init();

        repositoryManager.addRepositoryConfig(
                new RepositoryConfig(HISTORY_REPO_ID, new SailRepositoryConfig(new NativeStoreConfig())));

        supportRepo = repositoryManager.getRepository(HISTORY_REPO_ID);
        Repositories.consume(supportRepo, conn -> {
            conn.setNamespace(CHANGELOG.PREFIX, CHANGELOG.NAMESPACE);
            conn.setNamespace(PROV.PREFIX, PROV.NAMESPACE);
        });
        ChangeTrackerConfig trackerConfig = new ChangeTrackerConfig(new NativeStoreConfig());
        trackerConfig.setSupportRepositoryID(HISTORY_REPO_ID);
        trackerConfig.setMetadataNS(HISTORY_NS);
        trackerConfig.setValidationEnabled(false);
        trackerConfig.setHistoryEnabled(true);
        trackerConfig.setHistoryGraph(HISTORY_GRAPH);
        repositoryManager.addRepositoryConfig(new RepositoryConfig("test-data",
                                                                   new SailRepositoryConfig(trackerConfig)
        ));
        coreRepo = repositoryManager.getRepository("test-data");
    }

    @Override
    public void close() throws IOException {
        repositoryManager.shutDown();
    }

    public static EmbeddedRepo makeTempRepository()
            throws IOException, RepositoryException,
            RDFParseException, RepositoryConfigException, RDFHandlerException {

        File baseDir = Files.createTempDirectory("changetracking-examples").toFile();

        return new EmbeddedRepo(baseDir);
    }

    public Repository getCoreRepo() {
        return coreRepo;
    }

    public Repository getSupportRepo() {
        return supportRepo;
    }

    protected void printRepositories() throws RepositoryException, UnknownTransactionStateException {
        System.out.println();
        System.out.println("--- Data repo ---");
        System.out.println();

        Repositories.consume(coreRepo, conn -> {
            conn.export(Rio.createWriter(RDFFormat.NQUADS, System.out));
        });

        System.out.println();
        System.out.println("--- Support repo ---");
        System.out.println();

        Repositories.consume(supportRepo, conn -> {
            RDFWriter rdfWriter = Rio.createWriter(RDFFormat.TRIG, System.out);
            rdfWriter.set(BasicWriterSettings.PRETTY_PRINT, true);
            conn.export(rdfWriter);
        });

        System.out.println();
    }
}
