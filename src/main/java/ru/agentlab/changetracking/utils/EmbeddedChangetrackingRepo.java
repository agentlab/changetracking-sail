package ru.agentlab.changetracking.utils;

import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.nativerdf.config.NativeStoreConfig;
import ru.agentlab.changetracking.sail.ChangeTrackerConfig;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class EmbeddedChangetrackingRepo implements Closeable {
    private final LocalRepositoryManager repositoryManager;
    private final SailRepository repo;

    public EmbeddedChangetrackingRepo(File baseDir, String repoID) throws RepositoryException {
        repositoryManager = new LocalRepositoryManager(baseDir);
        repositoryManager.init();

        ChangeTrackerConfig trackerConfig = new ChangeTrackerConfig(new NativeStoreConfig());
        repositoryManager.addRepositoryConfig(new RepositoryConfig(
                repoID,
                new SailRepositoryConfig(trackerConfig)
        ));
        repo = (SailRepository) repositoryManager.getRepository(repoID);
    }

    @Override
    public void close() {
        repositoryManager.shutDown();
    }

    public static EmbeddedChangetrackingRepo makeTempRepository(String repoID)
            throws IOException, RepositoryException,
            RDFParseException, RepositoryConfigException, RDFHandlerException {

        File baseDir = Files.createTempDirectory(EmbeddedChangetrackingRepo.class.getName()).toFile();

        return new EmbeddedChangetrackingRepo(baseDir, repoID);
    }

    public SailRepositoryConnection getConnection() {
        return repo.getConnection();
    }
}
