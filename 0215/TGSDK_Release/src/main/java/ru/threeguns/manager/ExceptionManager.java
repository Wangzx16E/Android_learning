package ru.threeguns.manager;

import kh.hyper.core.IM;
import kh.hyper.utils.NotProguard;
import ru.threeguns.engine.manager.impl.ExceptionManagerImpl;

/**
 * 如需统计Exception,使用这个Manager
 */
@IM(ExceptionManagerImpl.class)
public interface ExceptionManager extends NotProguard {

	/**
	 * 开启自动捕获异常,开启后将自动notify所有未捕获的异常
	 */
	void enableAutoCatchException();

	/**
	 * 调用这个方法来统计Exception
	 * 
	 * @param exception 抛出的异常
	 */
	void notifyException(Throwable exception);

	/**
	 * 调用这个方法来统计Exception
	 * @param thread 抛出异常的线程
	 * @param exception 抛出的异常
	 */
	void notifyException(Thread thread, Throwable exception);
}
