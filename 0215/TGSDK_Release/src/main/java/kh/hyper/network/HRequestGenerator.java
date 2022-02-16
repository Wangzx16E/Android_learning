package kh.hyper.network;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Pair;
import kh.hyper.core.Module;
import kh.hyper.network.annotations.Address;
import kh.hyper.network.annotations.Body;
import kh.hyper.network.annotations.Header;
import kh.hyper.network.annotations.Host;
import kh.hyper.network.annotations.HttpMethod;
import kh.hyper.network.annotations.P;
import kh.hyper.network.annotations.TimeOut;
import kh.hyper.utils.AnnotationUtil;

public class HRequestGenerator implements RequestGenerator<RequestEntity> {

	private static final int DEFAULT_TIMEOUT = 15;

	@SuppressWarnings("unchecked")
	@Override
	public RequestEntity generateRequest(Class<?> serverClass, Method method, Object[] args) {
		Host hostAnnotation = AnnotationUtil.getAnnotation(serverClass, Host.class);
		if (hostAnnotation == null) {
			throw new NullPointerException("Have no 'Host' annotation");
		}
		String hostAddress = parseString(hostAnnotation.value());

		List<Pair<String, String>> headerList = new ArrayList<Pair<String, String>>();
		Header headerAnnotation = AnnotationUtil.getAnnotation(serverClass, Header.class);
		if (headerAnnotation != null) {
			String[] hs = headerAnnotation.value();
			int len = hs.length;
			if (len % 2 == 1) {
				throw new IllegalArgumentException("Invalid pair count of header");
			}
			for (int i = 0; i < len; i += 2) {
				headerList.add(new Pair<String, String>(//
						hs[i], //
						parseString(hs[i + 1])//
				));
			}

		}

		int timeout = DEFAULT_TIMEOUT;
		TimeOut t = AnnotationUtil.getAnnotation(serverClass, TimeOut.class);
		if (t != null) {
			timeout = t.value();
		}

		String httpMethod = null;
		HttpMethod m = AnnotationUtil.getAnnotation(serverClass, HttpMethod.class);
		if (m != null) {
			httpMethod = parseString(m.value());
		}

		if (httpMethod == null) {
			throw new NullPointerException("Have no 'HttpMethod' annotation");
		}

		HttpMethod httpMethodAnnotation = method.getAnnotation(HttpMethod.class);
		if (httpMethodAnnotation != null) {
			httpMethod = parseString(httpMethodAnnotation.value());
		}

		Map<String, String> params = new HashMap<String, String>();
		List<Pair<String, String>> headers = new ArrayList<Pair<String, String>>();

		Header methodHeaderAnnotation = method.getAnnotation(Header.class);
		if (methodHeaderAnnotation != null) {
			String[] hs = methodHeaderAnnotation.value();
			int len = hs.length;
			if (len % 2 == 1) {
				throw new IllegalArgumentException("Invalid pair count of header");
			}
			for (int i = 0; i < len; i += 2) {
				headers.add(new Pair<String, String>(//
						hs[i], //
						parseString(hs[i + 1])//
				));
			}
		}

		Map<String, String> addressParameterMap = new HashMap<String, String>();
		byte[] body = null;

		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		for (int i = 0, length = args.length; i < length; i++) {
			Annotation[] annotations = parameterAnnotations[i];
			if (annotations.length > 0) {
				if (annotations[0] instanceof P) {
					String k = ((P) annotations[0]).value()[0];
					if ("".equals(k)) {
						Object v = args[i];
						if (v instanceof Map) {
							params.putAll((Map<? extends String, ? extends String>) v);
						} else {
							throw new IllegalStateException("Param[" + i + "] have not key.");
						}
					} else {
						if (args[i] != null) {
							String v = args[i].toString();
							params.put(k, v);
						}
					}
				} else if (annotations[0] instanceof Header) {
					if (args[i] != null) {
						String k = ((Header) annotations[0]).value()[0];
						String v = args[i].toString();
						headers.add(new Pair<String, String>(k, v));
					}
				} else if (annotations[0] instanceof Address) {
					if (args[i] == null) {
						throw new NullPointerException("args[" + i + "]" + " is null");
					}
					String k = ((Address) annotations[0]).value();
					String v = args[i].toString();
					addressParameterMap.put(k, v);
				} else if (annotations[0] instanceof Body) {
					Object v = args[i];
					if (v instanceof byte[]) {
						body = (byte[]) v;
					} else if (v instanceof File) {
						File file = (File) v;
						try {
							FileInputStream fis = new FileInputStream(file);
							ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
							byte[] b = new byte[1024 * 16];
							int n;
							while ((n = fis.read(b)) != -1) {
								bos.write(b, 0, n);
							}
							fis.close();
							bos.close();
							body = bos.toByteArray();
						} catch (FileNotFoundException e) {
							throw new IllegalArgumentException("File not exist.", e);
						} catch (IOException e) {
							throw new IllegalArgumentException("Read file throw IOException.", e);
						}
					} else if (v instanceof String) {
						body = ((String) v).getBytes();
					} else {
						throw new IllegalArgumentException("Cannot convert type '" + v.getClass().getName() + "' to RequestBody.");
					}
					if (!"PUT".equals(httpMethod) && !"POST".equals(httpMethod)) {
						throw new IllegalArgumentException("Only PUT and POST method have RquestBody.");
					}
				}
			}
		}

		P methodParamAnnotation = method.getAnnotation(P.class);
		if (methodParamAnnotation != null) {
			String[] ps = methodParamAnnotation.value();
			int len = ps.length;
			if (len % 2 == 1) {
				throw new IllegalArgumentException("Invalid pair count of P");
			}
			for (int i = 0; i < len; i += 2) {
				params.put(ps[i], ps[i + 1]);
			}
		}

		Address addressAnnotation = method.getAnnotation(Address.class);
		if (addressAnnotation == null) {
			throw new NullPointerException("Must have a 'Address' annotation");
		}
		String address = addressAnnotation.value();

		String finalAddress = new String(address);
		Matcher matcher = Pattern.compile("(\\{)([a-zA-Z0-9\\-\\_]+)(\\})").matcher(address);
		while (matcher.find()) {
			String k = matcher.group(2);
			if (!addressParameterMap.containsKey(k)) {
				throw new IllegalStateException("url do not contains '{" + k + "}'");
			} else {
				finalAddress = finalAddress.replace("{" + k + "}", addressParameterMap.get(k));
			}
		}

		String url = hostAddress + finalAddress;

		if ("GET".equals(httpMethod)) {
			url = url + "?" + buildParamString(params);
			params.clear();
		}

		if ("DELETE".equals(httpMethod) && !params.isEmpty()) {
			throw new IllegalArgumentException("DELETE method cannot have params");
		}

		RequestEntity.Builder builder = new RequestEntity.Builder();
		builder.url(url)//
				.method(httpMethod)//
				.timeout(timeout)//
				.params(params)//
				.headers(headerList)//
				.headers(headers)//
				.body(body);

		return builder.build();
	}

	protected String parseString(String s) {
		return Module.of(StringController.class).parseString(s);
	}

	protected String buildParamString(Map<String, String> params) {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			try {
				builder.append(entry.getKey() + "=" + (entry.getValue() == null ? "" : URLEncoder.encode(entry.getValue(), "UTF-8")) + "&");
			} catch (UnsupportedEncodingException ignored) {
			}
		}
		if (builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		return builder.toString();
	}

}
