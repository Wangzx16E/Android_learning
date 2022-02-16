package ru.threeguns.manager;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import kh.hyper.core.IM;
import kh.hyper.core.Parameter;
import kh.hyper.utils.NotProguard;
import ru.threeguns.engine.manager.impl.ShareManagerImpl;
import ru.threeguns.event.handler.FollowHandler;
import ru.threeguns.event.handler.InviteFriendHandler;
import ru.threeguns.event.handler.ShareHandler;

@IM(ShareManagerImpl.class)
public interface ShareManager extends NotProguard {
	String VK = "VK";
	String FACEBOOK = "Facebook";

	void requestShare(String platformName, Parameter parameter, ShareHandler handler);
	
	void requestPhtotoShare(String platformName, Parameter parameter, ShareHandler handler);

	void requestInviteFriend(String platformName, Parameter parameter, InviteFriendHandler handler);

	void requestFollow(String platformName, Parameter parameter, FollowHandler handler);

	void requestExtra(String platformName, Parameter parameter, ShareManager.ResultHandler handler);

	public static interface ResultHandler {
		void onSuccess(JSONObject json);

		void onFailed(String message);
	}

	public static class ShareRequest implements NotProguard {
		private String appName;
		private String caption;
		private String description;
		private String message;
		private String targetLink;
		private String pictureLink;
		private Map<String, String> extraParams;

		public ShareRequest() {
			extraParams = new HashMap<String, String>();
		}

		public String getAppName() {
			return appName;
		}

		public ShareRequest setAppName(String appName) {
			this.appName = appName;
			return this;
		}

		public String getCaption() {
			return caption;
		}

		public ShareRequest setCaption(String caption) {
			this.caption = caption;
			return this;
		}

		public String getDescription() {
			return description;
		}

		public ShareRequest setDescription(String description) {
			this.description = description;
			return this;
		}

		public String getTargetLink() {
			return targetLink;
		}

		public ShareRequest setTargetLink(String targetLink) {
			this.targetLink = targetLink;
			return this;
		}

		public String getPictureLink() {
			return pictureLink;
		}

		public ShareRequest setPictureLink(String pictureLink) {
			this.pictureLink = pictureLink;
			return this;
		}

		public String getExtraParam(String key) {
			return extraParams.get(key);
		}

		public ShareRequest setExtraParams(String key, String value) {
			extraParams.put(key, value);
			return this;
		}

		public String getMessage() {
			return message;
		}

		public ShareRequest setMessage(String message) {
			this.message = message;
			return this;
		}

	}

}
