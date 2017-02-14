package com.poncholay.EpictureSdk.imgur.model;

public class ImgurUser {
	private long id;
	private String url;
	private String bio;
	private String proExpiration;
	private double reputation;
	private long created;

	public long getId() {
		return id;
	}

	public String getUrl() {
		return url;
	}

	public String getBio() {
		return bio;
	}

	public String getProExpiration() {
		return proExpiration;
	}

	public double getReputation() {
		return reputation;
	}

	public long getCreated() {
		return created;
	}

	public class ImgurUserWrapper extends ImgurResponseWrapper<ImgurUser> {}
}