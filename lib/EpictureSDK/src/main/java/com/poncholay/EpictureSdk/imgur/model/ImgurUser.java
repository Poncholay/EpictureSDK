package com.poncholay.EpictureSdk.imgur.model;

import com.poncholay.EpictureSdk.model.EpictureUser;
import com.poncholay.EpictureSdk.model.response.EpictureResponseWrapper;

public class ImgurUser implements EpictureUser {
	private long id;
	private String url;
	private String bio;
	private String proExpiration;
	private double reputation;
	private long created;

	@Override
	public long getId() {
		return id;
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public String getProExpiration() {
		return proExpiration;
	}

	public void setProExpiration(String proExpiration) {
		this.proExpiration = proExpiration;
	}

	public double getReputation() {
		return reputation;
	}

	public void setReputation(double reputation) {
		this.reputation = reputation;
	}

	public long getCreated() {
		return created;
	}

	public void setCreated(long created) {
		this.created = created;
	}

	public class ImgurUserWrapperEpicture extends EpictureResponseWrapper<ImgurUser> {}
}