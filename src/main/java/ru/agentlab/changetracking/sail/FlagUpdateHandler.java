package ru.agentlab.changetracking.sail;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.sail.UpdateContext;

/**
 * An {@link UpdateHandler} managing a flag, telling whether a connection is read-only or not.
 * 
 * @author <a href="fiorelli@info.uniroma2.it">Manuel Fiorelli</a>
 */
public class FlagUpdateHandler extends BaseUpdateHandler {
	private boolean readonly = true;
	
	@Override
	public void addStatement(Resource subj, IRI pred, Value obj, Resource[] newContexts) {
		readonly = false;
	}

	@Override
	public void addStatement(UpdateContext modify, Resource subj, IRI pred, Value obj,
			Resource[] newContexts) {
		readonly = false;
	}

	@Override
	public void removeStatements(Resource subj, IRI pred, Value obj, Resource[] newContexts) {
		readonly = false;
	}

	@Override
	public void removeStatement(UpdateContext modify, Resource subj, IRI pred, Value obj,
			Resource[] newContexts) {
		readonly = false;
	}

	@Override
	public void clear(Resource[] contexts) {
		readonly = false;
	}

	@Override
	public void clearNamespaces() {
		readonly = false;
	}

	@Override
	public void removeNamespace(String prefix) {
		readonly = false;
	}
	
	@Override
	public boolean isReadOnly() {
		return readonly;
	}
	
	@Override
	public void clearHandler(IRI...contexts) {
		readonly = true;
	}

}
