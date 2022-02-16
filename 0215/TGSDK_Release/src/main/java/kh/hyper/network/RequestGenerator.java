package kh.hyper.network;

import java.lang.reflect.Method;

public interface RequestGenerator<T> {
	T generateRequest(Class<?> serverClass, Method method, Object[] args);
}
