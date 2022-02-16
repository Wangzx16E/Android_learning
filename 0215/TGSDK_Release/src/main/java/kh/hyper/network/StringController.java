package kh.hyper.network;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kh.hyper.core.Module;
import kh.hyper.core.Parameter;

public final class StringController extends Module {
	private Map<String, String> globalStringMap;

	public void save(String k, String v) {
		globalStringMap.put(k, v);
	}

	public String load(String k) {
		return globalStringMap.get(k);
	}

	public String parseString(String s) {
		String ret = new String(s);
		Matcher matcher = Pattern.compile("(\\{)([a-zA-Z0-9\\-\\_]+)(\\})").matcher(ret);
		while (matcher.find()) {
			String k = matcher.group(2);
			if (globalStringMap.containsKey(k)) {
				ret = ret.replace("{" + k + "}", globalStringMap.get(k));
			}
		}
		return ret;
	}

	@Override
	protected void onLoad(Parameter parameter) {
		globalStringMap = new HashMap<String, String>();
	}

	@Override
	protected void onRelease() {
		globalStringMap.clear();
	}

}
