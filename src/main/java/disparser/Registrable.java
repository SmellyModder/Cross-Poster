package disparser;

public interface Registrable<T> {
	public String getName();
	
	public T get();
}