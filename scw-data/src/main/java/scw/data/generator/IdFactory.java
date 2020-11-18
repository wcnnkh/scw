package scw.data.generator;

/**
 * ID工厂
 * @author shuchaowen
 *
 * @param <T>
 */
public interface IdFactory<T> {
	T generator(String name);
}
