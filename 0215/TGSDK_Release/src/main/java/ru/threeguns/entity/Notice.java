package ru.threeguns.entity;

import org.json.JSONException;
import org.json.JSONObject;

import kh.hyper.core.Module;
import ru.threeguns.engine.controller.TGController;

public class Notice {
	private int id;
	private String content;
	private String url;
	private String timestamp;
	private String sdklang;

	public String getSdklang() {
		return sdklang;
	}

	public Notice() {
	}

	public Notice(int id, String content, String url, String timestamp, String sdklang) {
		super();
		this.id = id;
		this.content = content;
		this.url = url;
		this.timestamp = timestamp;
		this.sdklang = sdklang;
	}

	public String getUrl() {
		return url;
	}

	public int getId() {
		return id;
	}

	public String getContent() {
		return content;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public static Notice fromJSON(JSONObject json) throws JSONException {
		Notice n = new Notice();
		n.id = json.getInt("id");
		n.content = json.getString("notice");
		n.url = json.getString("url");
		n.timestamp = json.getString("timestamp");
		n.sdklang = Module.of(TGController.class).appLanguage;
		return n;
	}

}
