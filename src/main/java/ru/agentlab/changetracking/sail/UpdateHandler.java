package ru.agentlab.changetracking.sail;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.sail.UpdateContext;

/**
 * Handles update requests coming from a connection.
 * 
 * @author <a href="fiorelli@info.uniroma2.it">Manuel Fiorelli</a>
 */
public interface UpdateHandler {
	/**
	 * Marks this handler as corrupted. That should happen, when it hasn't be possible to record an update
	 */
	void recordCorruption();

	/**
	 * Checks whether this handler is corrupted. This check should be performed, before the updates are
	 * committed to the triple store.
	 * 
	 * @return
	 */
	boolean isCorrupted();

	/**
	 * Checks whether no update has been recorded.
	 * 
	 * @return
	 */
	boolean isReadOnly();

	void addStatement(Resource subj, IRI pred, Value obj, Resource[] newContexts);

	void addStatement(UpdateContext modify, Resource subj, IRI pred, Value obj, Resource[] newContexts);

	void removeStatements(Resource subj, IRI pred, Value obj, Resource[] newContexts);

	void removeStatement(UpdateContext modify, Resource subj, IRI pred, Value obj, Resource[] newContexts);

	void clear(Resource[] contexts);

	void clearNamespaces();

	void removeNamespace(String prefix);

	void clearHandler(IRI...contexts);

}
