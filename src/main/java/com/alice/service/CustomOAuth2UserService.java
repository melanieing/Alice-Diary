package com.alice.project.service;

import java.time.LocalDate;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.alice.project.config.PrincipalDetails;
import com.alice.project.domain.Gender;
import com.alice.project.domain.Member;
import com.alice.project.domain.Status;
import com.alice.project.repository.CalendarRepository;
import com.alice.project.repository.MemberRepository;
import com.alice.project.web.GoogleUserInfo;
import com.alice.project.web.NaverUserInfo;
import com.alice.project.web.OAuth2UserInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final CalendarService calendarService;

	// 구글로부터 받은 userRequest 데이터에 대한 후처리가 되는 함수
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2UserService delegate = new DefaultOAuth2UserService();
		OAuth2User oAuth2User = delegate.loadUser(userRequest);

		OAuth2UserInfo oAuth2UserInfo = null;
		// OAuth2 서비스 id 구분코드
		String provider = userRequest.getClientRegistration().getRegistrationId();

		if (provider.equals("google")) {
			oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
		} else if (provider.equals("naver")) {
			oAuth2UserInfo = new NaverUserInfo(oAuth2User.getAttributes());
		}

		// OAuth2 로그인 진행 시 키가 되는 필드 값 (PK)
		String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
				.getUserNameAttributeName();
		// String providerId = oAuth2UserInfo.getProviderId();
		String id = "";
		// 아이디 난수로 중복없게 만들기
		boolean check = true;
		while (check) {
			Integer i = (int) (Math.random() * 100000000) + 1; // 1~100000000까지의 난수 생성
			id = provider + "_" + i; // google_83912342
			if (memberRepository.existsById(id)) { // 아이디가 있으면 안 돼! 다시 돌려
				continue;
			} else { // 아이디가 없어서 쓸 수 있어
				check = false;
			}
		}

		String password = passwordEncoder.encode("password");
		String email = oAuth2UserInfo.getEmail();
		String status = "ROLE_USER_IN";
		String name = oAuth2UserInfo.getName();
		String profileImg = "default.png";

		Member member = memberRepository.findByEmail(email);
		if (member == null) {
			System.out.println("처음 가입한 계정입니다.");
			member = Member.builder().id(id).password(password).birth(LocalDate.of(1900, 01, 01)).email(email)
					.gender(Gender.UNKNOWN).mobile("01000000000").name(name).regDate(LocalDate.now())
					.status(Status.USER_IN).build();
			Member.setProfileImg(member);
			memberRepository.save(member);

			// 소셜 유저 생일 저장
			calendarService.addBirthEvents(member);
		} else {
			System.out.println("이미 가입한 적이 있습니다.");
		}
		return new PrincipalDetails(member, oAuth2User.getAttributes());
	}
}