package kh.hyper.core;

public class ParameterException extends Exception {
	private static final long serialVersionUID = 1L;

	public ParameterException() {
		super();
	}

	public ParameterException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public ParameterException(String detailMessage) {
		super(detailMessage);
	}

	public ParameterException(Throwable throwable) {
		super(throwable);
	}

}
