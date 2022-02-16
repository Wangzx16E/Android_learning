package kh.hyper.network;

public interface NetworkAdapter<RequestType, ResultType> {
	ResultType connectInternal(RequestType request);
}