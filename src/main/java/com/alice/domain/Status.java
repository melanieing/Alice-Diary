package com.alice.project.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {
	USER_IN("ROLE_USER_IN", "회원"), USER_OUT("ROLE_USER_OUT", "탈퇴 회원"), ADMIN("ROLE_USER_ADMIN", "관리자");
	// 등록회원, 탈퇴회원, 관리자

	private final String key;
	private final String title;
}