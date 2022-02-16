package ru.threeguns.engine.tp;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import kh.hyper.core.Module;
import kh.hyper.core.Parameter;
import kh.hyper.utils.HL;
import ru.threeguns.engine.controller.TGController;

public class ThirdPlatformManager extends Module {
	private List<ThirdPlatform> thirdplatformList;

	@Override
	protected void onLoad(Parameter parameter) {
		thirdplatformList = new ArrayList<ThirdPlatform>();

		try {
			JSONObject configJson = Module.of(TGController.class).configJson;
			if (configJson != null) {
				JSONArray config = configJson.getJSONArray("third_platforms");
				for (int i = 0; i < config.length(); i++) {
					JSONObject tpConfig = config.getJSONObject(i);
					Parameter p = Parameter.parseFromJSON(tpConfig);
					
					if ("true".equals(p.optString("enabled"))) {
						try {
							
							Class<?> clazz = Class.forName("ru.threeguns.engine.tp." + p.optString("name"));
							ThirdPlatform tp = (ThirdPlatform) clazz.getDeclaredConstructor().newInstance();
							load(tp, p);
							thirdplatformList.add(tp);
						} catch (Exception e) {
							HL.w(e);
							throw new RuntimeException("Cannot init this Platform : " + p.optString("name"), e);
						}
					}
				}
			} else {
				HL.w("Cannot read configJson , ThirdPlatform init failed.");
			}
		} catch (JSONException e) {
			HL.w(e);
		} catch (NullPointerException e1) {
			HL.w(e1);
		}

	}

	@Override
	protected void onRelease() {

	}

	public List<ThirdPlatform> getThirdPlatforms() {
		return thirdplatformList;
	}

	public ThirdPlatform getThirdPlatformByName(String name) {
		for (ThirdPlatform platform : thirdplatformList) {
			if (platform.getPlatformName().equals(name)) {
				return platform;
			}
		}
		return null;
	}

}
