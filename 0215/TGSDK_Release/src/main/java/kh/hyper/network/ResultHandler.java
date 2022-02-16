package kh.hyper.network;

import kh.hyper.utils.NotProguard;

public interface ResultHandler<T> extends NotProguard {
	void onResult(T t);
}