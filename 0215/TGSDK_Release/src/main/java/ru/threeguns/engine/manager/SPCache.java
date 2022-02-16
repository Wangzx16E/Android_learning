package ru.threeguns.engine.manager;

import android.content.Context;
import android.content.SharedPreferences;
import kh.hyper.core.Module;
import kh.hyper.core.Parameter;

public class SPCache extends Module {
	private SharedPreferences sp;

	@Override
	protected void onLoad(Parameter parameter) {
		sp = getContext().getSharedPreferences("TG_SP_CACHE", Context.MODE_PRIVATE);
	}

	@Override
	protected void onRelease() {
		sp = null;
	}

	public void save(String k, String v) {
		sp.edit().putString(k, v).commit();
	}

	public void save(String k, int v) {
		sp.edit().putInt(k, v).commit();
	}

	public String load(String k) {
		return sp.getString(k, null);
	}

	public int loadInt(String k, int defaultValue) {
		return sp.getInt(k, defaultValue);
	}

	public int loadInt(String k) {
		return loadInt(k, 0);
	}

}
