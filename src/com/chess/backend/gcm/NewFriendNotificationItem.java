package com.chess.backend.gcm;

import com.chess.backend.entity.api.BaseResponseItem;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.08.13
 * Time: 5:53
 */
public class NewFriendNotificationItem {
/*
        'sender' => $params['sender'],
        'message' => $params['message'],
        'created_at' => $params['createdAt'],
*/
	private String username;
	private String message;
	private long createdAt;
	private String avatar_url;
	private long request_id;
	/* Local additions */
	private boolean seen;

	public String getUsername() {
		return BaseResponseItem.getSafeValue(username);
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getMessage() {
		return BaseResponseItem.getSafeValue(message);
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
	}

	public String getAvatar() {
		return BaseResponseItem.getSafeValue(avatar_url);
	}

	public void setAvatar(String avatar_url) {
		this.avatar_url = avatar_url;
	}

	public void setUserSawIt(boolean seen) {
		this.seen = seen;
	}

	public boolean userSawIt() {
		return seen;
	}

	public long getRequestId() {
		return request_id;
	}

	public void setRequestId(long request_id) {
		this.request_id = request_id;
	}
}