package replayable.api;

public interface ReplayableProcessRuntime {
    <T> T shadow(Class<T> clazz, T object);
}
