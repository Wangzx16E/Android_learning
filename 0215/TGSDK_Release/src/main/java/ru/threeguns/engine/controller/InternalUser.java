package ru.threeguns.engine.controller;

import org.json.JSONException;
import org.json.JSONObject;

import ru.threeguns.entity.User;

public final class InternalUser implements User {
	private String userId;
	private String username;
	private String password;
	private String token;
	private String userType;
	private String nickname;

	public InternalUser setPassword(String password) {
		this.password = password;
		return this;
	}

	public InternalUser setUserId(String userId) {
		this.userId = userId;
		return this;
	}

	public InternalUser setUsername(String username) {
		this.username = username;
		return this;
	}

	public InternalUser setToken(String token) {
		this.token = token;
		return this;
	}

	public InternalUser setUserType(String userType) {
		this.userType = userType;
		return this;
	}

	public InternalUser setNickname(String nickname) {
		this.nickname = nickname;
		return this;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public String getUserId() {
		return userId;
	}

	// @Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getToken() {
		return token;
	}

	@Override
	public String getUserType() {
		return userType;
	}

	@Override
	public String getNickname() {
		return nickname;
	}

	public static InternalUser parseFromJSON(JSONObject json) throws JSONException {
		String userId = json.getString("user_id");
		String token = json.getString("token");
		String userType = json.getString("type");
		String username = json.getString("username");
		String nickname = json.optString("nickname");

		InternalUser user = new InternalUser();
		user.setUserId(userId)//
				.setNickname(nickname)//
				.setUsername(username)//
				.setUserType(userType)//
				.setToken(token);
		return user;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InternalUser other = (InternalUser) obj;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}

}
