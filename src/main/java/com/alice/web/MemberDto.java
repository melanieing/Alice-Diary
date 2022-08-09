package com.alice.project.web;

import java.time.LocalDate;

import com.alice.project.domain.Gender;
import com.alice.project.domain.Member;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class MemberDto {
	private String id; // 회원 아이디
	private String password; // 회원 비밀번호
	private String name; // 회원 이름
	private LocalDate birth; // 회원 생일
	private Gender gender; // 회원 성별 [MALE, FEMALE, UNKNOWN]
	private String email; // 회원 이메일
	private String mobile; // 회원 전화번호
	private String mbti; // 회원 MBTI
	private String wishlist; // 회원 위시리스트
	private String profileImg; // 프로필사진 저장된 파일명(ex. 회원아이디.jpeg)

	public Member toEntity() { // 필요한 엔티티는 이런식으로 추가
		Member member = Member.builder().id(id).password(password).name(name).birth(birth).gender(gender).email(email)
				.mobile(mobile).mbti(mbti).wishlist(wishlist).profileImg(profileImg).build();
		return member;
	}

}