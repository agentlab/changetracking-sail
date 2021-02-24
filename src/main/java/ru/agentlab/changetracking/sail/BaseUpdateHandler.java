package ru.agentlab.changetracking.sail;

/**
 * Abstract base class of {@link UpdateHandler}s.
 * 
 * @author <a href="fiorelli@info.uniroma2.it">Manuel Fiorelli</a>
 */
public abstract class BaseUpdateHandler implements UpdateHandler {
	private boolean corrupted = false;

	@Override
	public void recordCorruption() {
		this.corrupted = true;
	}

	@Override
	public boolean isCorrupted() {
		return corrupted;
	}
}
