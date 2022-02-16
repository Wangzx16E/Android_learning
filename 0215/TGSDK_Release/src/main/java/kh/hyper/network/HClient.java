package kh.hyper.network;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kh.hyper.network.annotations.Adapter;
import kh.hyper.network.annotations.Generator;
import kh.hyper.network.annotations.Parser;
import kh.hyper.utils.AnnotationUtil;

public final class HClient {
	private static Map<Class<?>, Object> proxyClientMap = new HashMap<Class<?>, Object>();
	private static ExecutorService networkExecutor = Executors.newCachedThreadPool();

	@SuppressWarnings("unchecked")
	public static <C, T, R, F> C of(final Class<C> clazz) {
		C t = (C) proxyClientMap.get(clazz);
		if (t == null) {
			synchronized (clazz) {
				if (t == null) {
					Class<?> adapterClass = OKHttpAdapter.class;
					Class<?> generatorClass = HRequestGenerator.class;
					Class<?> parserClass = HResultParser.class;

					Adapter adapterAnnotation = AnnotationUtil.getAnnotation(clazz, Adapter.class);
					if (adapterAnnotation != null) {
						adapterClass = (Class<NetworkAdapter<?, ?>>) adapterAnnotation.value();
					}
					Generator generatorAnnotation = AnnotationUtil.getAnnotation(clazz, Generator.class);
					if (generatorAnnotation != null) {
						generatorClass = (Class<RequestGenerator<?>>) generatorAnnotation.value();
					}
					Parser parserAnnotation = AnnotationUtil.getAnnotation(clazz, Parser.class);
					if (parserAnnotation != null) {
						parserClass = (Class<ResultParser<?, ?>>) parserAnnotation.value();
					}

					final NetworkAdapter<T, R> adapter;
					final RequestGenerator<T> generator;
					final ResultParser<R, F> parser;
					Object obj;
					try {
						obj = adapterClass.newInstance();
						adapter = (NetworkAdapter<T, R>) obj;

						obj = generatorClass.newInstance();
						generator = (RequestGenerator<T>) obj;

						obj = parserClass.newInstance();
						parser = (ResultParser<R, F>) obj;
					} catch (InstantiationException e) {
						throw new IllegalArgumentException("Create adapter instace failed.", e);
					} catch (IllegalAccessException e) {
						throw new IllegalArgumentException("Create adapter instace failed.", e);
					}
					t = (C) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[] { clazz }, new InvocationHandler() {
						@Override
						public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
							ResultHandler<?> resultHandler = null;
							Class<?>[] paramClasses = method.getParameterTypes();
							for (int i = paramClasses.length - 1; i >= 0; i--) {
								if (ResultHandler.class.isAssignableFrom(paramClasses[i])) {
									resultHandler = (ResultHandler<?>) args[i];
								}
							}
							if (resultHandler != null) {
								if (method.getReturnType() != void.class) {
									throw new IllegalStateException("Method " + method.getName() + " have both 'ResultHandler' param and ReturnType.");
								}
								final ResultHandler<F> handler = ((ResultHandler<F>) resultHandler);
								final T request = generator.generateRequest(clazz, method, args);
								networkExecutor.execute(new Runnable() {
									public void run() {
										R result = adapter.connectInternal(request);
										F fr = parser.parseResult(result);
										handler.onResult(fr);
									}
								});
								return null;
							} else {
								T request = generator.generateRequest(clazz, method, args);
								R result = adapter.connectInternal(request);
								return parser.parseResult(result);
							}
						}
					});
					proxyClientMap.put(clazz, t);
				}
			}
		}
		return t;
	}

}
