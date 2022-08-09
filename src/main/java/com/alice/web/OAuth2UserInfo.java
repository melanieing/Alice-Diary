package com.alice.project.web;

import java.util.Map;

public interface OAuth2UserInfo {
	Map<String, Object> getAttributes();

	String getProviderId();

	String getProvider();

	String getEmail();

	String getName();

}