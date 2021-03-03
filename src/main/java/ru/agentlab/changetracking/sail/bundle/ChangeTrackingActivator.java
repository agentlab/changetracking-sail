package ru.agentlab.changetracking.sail.bundle;

import org.eclipse.rdf4j.sail.config.SailRegistry;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.agentlab.changetracking.sail.ChangeTrackingFactory;

@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class ChangeTrackingActivator implements BundleActivator {
    private static final Logger logger = LoggerFactory.getLogger(ChangeTrackingFactory.class);

    private ChangeTrackingFactory changeTrackingFactory;

    @Override
    public void start(BundleContext bundleContext) {
        logger.info("Registering ChangeTracking sail factory...");
        changeTrackingFactory = new ChangeTrackingFactory();
        SailRegistry.getInstance().add(changeTrackingFactory);
        logger.info("Registering ChangeTracking sail factory... Done");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        logger.info("Unregistering ChangeTracking sail factory...");
        SailRegistry.getInstance().remove(changeTrackingFactory);
        logger.info("Unregistering ChangeTracking sail factory... Done");
    }
}