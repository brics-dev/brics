package gov.nih.tbi.api.query.utils;

public interface ObjectAdapter<T> {
	/**
	 * Converts the defined domain object into an API object.
	 * @return <T>
	 */
	public T adapt();
}
