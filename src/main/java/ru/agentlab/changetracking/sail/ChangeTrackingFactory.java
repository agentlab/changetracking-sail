package ru.agentlab.changetracking.sail;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.config.SailConfigException;
import org.eclipse.rdf4j.sail.config.SailFactory;
import org.eclipse.rdf4j.sail.config.SailImplConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ChangeTrackingFactory implements SailFactory {
    private static final Logger logger = LoggerFactory.getLogger(ChangeTrackingFactory.class);

    public static final String SAIL_TYPE = "http://agentlab.ru/sail/changetracker";

    @Override
    public String getSailType() {
        return SAIL_TYPE;
    }

    @Override
    public SailImplConfig getConfig() {
        return new ChangeTrackerConfig();
    }

    @Override
    public Sail getSail(SailImplConfig parentConfig) throws SailConfigException {
        if (!SAIL_TYPE.equals(parentConfig.getType())) {
            throw new SailConfigException("Invalid Sail type: " + parentConfig.getType());
        }
        ChangeTrackerConfig config = (ChangeTrackerConfig) parentConfig;
        Set<IRI> includedGraph = config.getIncludeGraph();
        Set<IRI> excludedGraph = config.getExcludeGraph();
        return new ChangeTracker(includedGraph, excludedGraph, config.getEventsQueueBufferSize());
    }

}
