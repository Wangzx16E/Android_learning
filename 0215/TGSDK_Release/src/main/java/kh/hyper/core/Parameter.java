package kh.hyper.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

public final class Parameter {
	private Map<String, Object> internalMap;

	public Parameter() {
		internalMap = new HashMap<String, Object>();
	}

	public Parameter put(String key, Object value) {
		internalMap.put(key, value);
		return this;
	}

	public Parameter remove(String key) {
		internalMap.remove(key);
		return this;
	}

	public Parameter clear() {
		internalMap.clear();
		return this;
	}

	public Set<String> keySet() {
		return internalMap.keySet();
	}

	public Object get(String key) throws ParameterException {
		return get(key, Object.class);
	}

	public Object opt(String key) {
		return opt(key, Object.class);
	}

	public <X> X get(String key, Class<X> clazz) throws ParameterException {
		if (key == null) {
			throw new ParameterException("Key is null");
		}
		Object ret = internalMap.get(key);
		if (ret == null) {
			throw new ParameterException("Value is null with key : " + key);
		}
		if (!clazz.isInstance(ret)) {
			throw new ParameterException("Read [" + ret.getClass().getName() + " : " + ret + "] with key : " + key + " , expected class type : "
					+ clazz.getName());
		}
		return clazz.cast(ret);
	}

	public <X> X opt(String key, Class<X> clazz) {
		return opt(key, clazz, null);
	}

	public <X> X opt(String key, Class<X> clazz, X defaultValue) {
		if (key == null) {
			return defaultValue;
		}
		Object ret = internalMap.get(key);
		return ret == null || !clazz.isInstance(ret) ? defaultValue : clazz.cast(ret);
	}

	public String optString(String key) {
		if (key == null) {
			return "";
		}
		Object ret = internalMap.get(key);
		return ret == null ? "" : ret.toString();
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		boolean first = true;
		s.append("[");
		for (String key : internalMap.keySet()) {
			if (first) {
				first = false;
			} else {
				s.append(" , ");
			}
			s.append(key);
			s.append(" = ");
			s.append(internalMap.get(key));
		}
		s.append("]");
		return s.toString();
	}

	public static Parameter parseFromJSON(JSONObject json) {
		Parameter parameter = new Parameter();
		Iterator<?> iter = json.keys();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			parameter.put(key, json.opt(key));
		}
		return parameter;
	}
}
