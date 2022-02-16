package ru.threeguns.network;

import java.net.URLEncoder;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.json.JSONObject;

import com.my.EncryptUtil;

import kh.hyper.core.Module;
import ru.threeguns.engine.controller.Constants;
import ru.threeguns.engine.controller.TGController;

public class NetworkUtil {

	public static String getHostAddress(String behaviourName) {
		if (Constants.DEBUG) {
			return Constants.DEBUG_HOST_ADDRESS;
		}
		if (Module.of(TGController.class).gameDebug) {
			return Constants.GAMEDEBUG_HOST_ADDRESS;
		} else {
			return Constants.HOST_ADDRESS;
		}
	}

	@SuppressWarnings("deprecation")
	public static String makeDataString(Map<String, String> paramMap) {
		TreeSet<String> treeSet = new TreeSet<String>(new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				return lhs.compareTo(rhs);
			}
		});
		treeSet.addAll(paramMap.keySet());

		StringBuilder paramString = new StringBuilder();

		for (String k : treeSet) {
			String v = paramMap.get(k);
			if (v == null) {
				paramString.append(k + "=&");
			} else {
				paramString.append(k + "=" + URLEncoder.encode(v) + "&");
			}
		}
		if (paramString.length() > 0) {
			paramString.deleteCharAt(paramString.length() - 1);
		}

		String sign = EncryptUtil.nativeSign(paramString.toString(), Module.of(TGController.class).appKey);

		Map<String, String> tempMap = new HashMap<String, String>(paramMap);
		tempMap.put("sign", sign);

		JSONObject json = new JSONObject(tempMap);
		return json.toString();
	}
}
