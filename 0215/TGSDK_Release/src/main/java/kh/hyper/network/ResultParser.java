package kh.hyper.network;

public interface ResultParser<T, R> {
	R parseResult(T t);
}
