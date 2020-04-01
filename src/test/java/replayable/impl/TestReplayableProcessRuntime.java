package replayable.impl;

import org.apache.commons.lang3.tuple.Pair;
import replayable.api.ReplayableProcessRuntime;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class TestReplayableProcessRuntime implements ReplayableProcessRuntime {
    final Map<Pair<Class<?>, String>, Serializable> results;

    public TestReplayableProcessRuntime(Map<Pair<Class<?>, String>, Serializable> results) {
        this.results = results;
    }

    @Override
    public <T> T shadow(Class<T> clazz, T object) {
        class Handler implements InvocationHandler {
            final Map<Pair<Class<?>, String>, Serializable> results;
            final Class<T> clazz;
            final T object;

            public Handler(Class<T> clazz,
                           T object,
                           Map<Pair<Class<?>, String>, Serializable> results) {
                this.clazz = clazz;
                this.object = object;
                this.results = results;
            }

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                try {
                    Pair<Class<?>, String> key = Pair.of(clazz, method.getName());
                    if (!results.containsKey(key)) {
                        method.setAccessible(true);
                        final Object o = method.invoke(object, args);
                        results.put(key, (Serializable)o);
                        return o;
                    }
                    return results.get(key);
                } catch (IllegalAccessException e) {
                    // fatal
                    throw e;
                } catch (InvocationTargetException e) {
                    throw e.getTargetException();
                }
            }
        }

        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),
                new Class[] { clazz },
                new Handler(clazz, object, results));
    }
}
