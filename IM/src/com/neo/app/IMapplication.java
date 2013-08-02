package com.neo.app;

import android.app.Application;

public class IMapplication extends Application {
	private String userId = "";

	public String getUserId() {
		return userId;
	}

	public void setUserId(String uid) {
		this.userId = uid;
	}

	public String pwd = "";

	public String getPwd() {
		return this.pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	private boolean chatstate=false;

	public void setchatstate(boolean state) {
		this.chatstate = state;
	}

	public boolean getchatstate() {
		return this.chatstate;
	}
}
