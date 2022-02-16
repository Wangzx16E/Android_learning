package ru.threeguns.engine.manager.impl;

import kh.hyper.core.Module;
import kh.hyper.core.Parameter;
import kh.hyper.event.EventManager;
import ru.threeguns.engine.tp.ThirdPlatform;
import ru.threeguns.engine.tp.ThirdPlatformManager;
import ru.threeguns.event.FollowEvent;
import ru.threeguns.event.InviteFriendEvent;
import ru.threeguns.event.ShareEvent;
import ru.threeguns.event.handler.FollowHandler;
import ru.threeguns.event.handler.InviteFriendHandler;
import ru.threeguns.event.handler.ShareHandler;
import ru.threeguns.manager.ShareManager;

public class ShareManagerImpl extends Module implements ShareManager {

	@Override
	public void requestShare(String platformName, Parameter parameter, ShareHandler handler) {
		if (parameter == null) {
			throw new NullPointerException("Parameter cannot be null");
		}
		if (handler != null) {
			handler.register();
		} else {
			throw new NullPointerException("Handler cannot be null");
		}
		ThirdPlatform tp = Module.of(ThirdPlatformManager.class).getThirdPlatformByName(platformName);
		if (tp != null) {
			tp.requestShare(parameter);
		} else {
			EventManager.instance.dispatch(new ShareEvent(ShareEvent.FAILED, "Cannot find platfrom : " + platformName));
		}
	}
	
	@Override
	public void requestPhtotoShare(String platformName, Parameter parameter, ShareHandler handler) {

		if (parameter == null) {
			throw new NullPointerException("Parameter cannot be null");
		}
		if (handler != null) {
			handler.register();
		} else {
			throw new NullPointerException("Handler cannot be null");
		}
		ThirdPlatform tp = Module.of(ThirdPlatformManager.class).getThirdPlatformByName(platformName);
		if (tp != null) {
			tp.requestPhotoShare(parameter);
		} else {
			EventManager.instance.dispatch(new ShareEvent(ShareEvent.FAILED, "Cannot find platfrom : " + platformName));
		}
	}

	@Override
	public void requestInviteFriend(String platformName, Parameter parameter, InviteFriendHandler handler) {
		if (parameter == null) {
			throw new NullPointerException("Parameter cannot be null");
		}
		if (handler != null) {
			handler.register();
		} else {
			throw new NullPointerException("Handler cannot be null");
		}
		ThirdPlatform tp = Module.of(ThirdPlatformManager.class).getThirdPlatformByName(platformName);
		if (tp != null) {
			tp.requestInviteFriend(parameter);
		} else {
			EventManager.instance.dispatch(new InviteFriendEvent(InviteFriendEvent.FAILED, "Cannot find platfrom : " + platformName));
		}
	}

	@Override
	public void requestFollow(String platformName, Parameter parameter, FollowHandler handler) {
		if (parameter == null) {
			throw new NullPointerException("Parameter cannot be null");
		}
		if (handler != null) {
			handler.register();
		} else {
			throw new NullPointerException("Handler cannot be null");
		}
		ThirdPlatform tp = Module.of(ThirdPlatformManager.class).getThirdPlatformByName(platformName);
		if (tp != null) {
			tp.requestFollow(parameter);
		} else {
			EventManager.instance.dispatch(new InviteFriendEvent(FollowEvent.FAILED, "Cannot find platfrom : " + platformName));
		}
	}

	@Override
	public void requestExtra(String platformName, Parameter parameter, ResultHandler handler) {
		if (parameter == null) {
			throw new NullPointerException("Parameter cannot be null");
		}
		if (handler == null) {
			throw new NullPointerException("ResultHandler cannot be null");
		}
		ThirdPlatform tp = Module.of(ThirdPlatformManager.class).getThirdPlatformByName(platformName);
		if (tp != null) {
			tp.requestExtra(parameter, handler);
		} else {
			handler.onFailed("Cannot find platfrom : " + platformName);
		}
	}

	@Override
	protected void onLoad(Parameter parameter) {

	}

	@Override
	protected void onRelease() {

	}

}
