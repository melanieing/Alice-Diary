package com.alice.project.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.alice.project.config.AppProperties;
import com.alice.project.config.PrincipalDetails;
import com.alice.project.domain.Member;
import com.alice.project.domain.Status;
import com.alice.project.repository.MemberRepository;
import com.alice.project.web.MemberAccount;
import com.alice.project.web.UserDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional(readOnly = true) // 기본적으로 못바꾸게 해놓고
@RequiredArgsConstructor // final 필드 생성자 생성해줌
@Slf4j
public class MemberService implements UserDetailsService { // MemberService가 UserDetailService를 구현

	private final MemberRepository memberRepository;
	private final CalendarService calendarService;
	private final PasswordEncoder passwordEncoder;
	private final EntityManager em;
	private final AppProperties appProperties;
	private final TemplateEngine templateEngine;
	private final EmailService emailService;
	private final ModelMapper modelMapper;

	// register member
	@Transactional
	public Member processNewMember(UserDto userDto) {
		log.info("processNewMember 진입");
		Member newMember = saveNewMember(userDto);
		Member.setProfileImg(newMember);
		sendSignUpConfirmEmail(newMember);
		return newMember;
	}

	@Transactional
	private Member saveNewMember(@Valid UserDto userDto) {
		log.info("saveNewMember 진입");
		userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
		Member member = modelMapper.map(userDto, Member.class);
		member.generateEmailCheckToken();
		return memberRepository.save(member);
	}

	// update member profile
	@Transactional
	public Member processUpdateMember(Long num, UserDto userDto, boolean changeIMg) {
		log.info("processUpdateMember 진입");
		Member updateMember = saveUpdateMember(num, userDto, changeIMg);
		Member.updateProfileImg(updateMember, userDto, memberRepository);
		return updateMember;
	}

	@Transactional
	private Member saveUpdateMember(Long num, @Valid UserDto userDto, boolean changeIMg) {
		Member m = memberRepository.findByNum(num);
		log.info("saveUpdateMember 진입");
		if (userDto.getBirth() == null) {
			userDto.setBirth(m.getBirth());
		}
		userDto.setPassword(m.getPassword());
		userDto.setGender(m.getGender());
		userDto.setRegDate(m.getRegDate());
		userDto.setEmail(m.getEmail());
		userDto.setEmailCheckToken(m.getEmailCheckToken());
		userDto.setEmailCheckTokenGeneratedAt(m.getEmailCheckTokenGeneratedAt());
		userDto.setEmailVerified(m.isEmailVerified());
		userDto.setStatus(Status.USER_IN);
		userDto.setNum(num);
		if (!changeIMg) {
			userDto.setSaveName(m.getProfileImg());
		}
		Member member = modelMapper.map(userDto, Member.class);
		return memberRepository.save(member);
	}

	@Transactional
	public void completeSignUp(Member member) {
		member.completeRegister();
		login(member);
		Member.changeMemberIn(member);

		// 유저 생일 5개 추가
		calendarService.addBirthEvents(member);
	}

	@Transactional
	public void login(Member member) {
		List<SimpleGrantedAuthority> authorities = MemberAccount.createAuthor();
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
				new MemberAccount(member, authorities), member.getPassword());
		log.info("token getName: " + token.getName());
		log.info("token getAuthorities: " + token.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(token);
	}

	@Transactional
	public void sendSignUpConfirmEmail(Member newMember) {
		log.info("sendSignUpConfirmEmail 진입");
		Context context = new Context();
		context.setVariable("link",
				"/check-email-token?token=" + newMember.getEmailCheckToken() + "&email=" + newMember.getEmail());
		context.setVariable("id", newMember.getId());
		log.info("newMember.getId() = " + newMember.getId());
		context.setVariable("linkName", "이메일 인증하기");
		context.setVariable("message", "앨리스 다이어리 서비스를 사용하려면 링크를 클릭하세요.");
		context.setVariable("host", appProperties.getHost() + "/AliceDiary");
		String message = templateEngine.process("login/simple-link", context);

		EmailMessage emailMessage = EmailMessage.builder().to(newMember.getEmail()).subject("앨리스 다이어리, 회원 가입 인증")
				.message(message).build();

		log.info("to????????? = " + newMember.getEmail());
		emailService.sendEmail(emailMessage);
	}

	@Transactional
	public Member saveMember(Member member) {
		return memberRepository.save(member); // insert
	}

	public Member findById(String id) {
		return memberRepository.findById(id);
	}

	public Member findByEmail(String email) {
		return memberRepository.findByEmail(email);
	}

//	public Member editPwd(String id, String password) {
//		return memberRepository.findById(id);
//	}

	// id 중복테스트
	public int checkIdDuplicate(String id) {
		boolean check = memberRepository.existsById(id);
		if (check) {
			return 1; // 아이디 중복이면 1
		} else if (id.equals("default")) {
			return 1;
		} else {
			return 0; // 사용 가능 아이디면 0
		}
	}

	// nickname 중복테스트
	public int checkNicknameDuplication(String name) {
		boolean check = memberRepository.existsByName(name);
		if (check) {
			return 1; // 닉네임 중복이면 1
		} else {
			return 0; // 사용 가능 닉네임이면 0
		}
	}

	// id 찾기
	public Member findId(String name, String mobile, String email) {
		Member member = memberRepository.findByNameAndMobileAndEmail(name, mobile, email);
		return member;
	}

	// 비밀번호 찾기
	public Member findPwd(String id, String name, String mobile) {
		Member member = memberRepository.findByIdAndNameAndMobile(id, name, mobile);

		return member;
	}

	public Member findByNum(Long num) {
		return memberRepository.findByNum(num);
	}

	// 비밀번호 재설정
	@Transactional
	public Member updateMember(Member member) {
		return memberRepository.save(member);
	}

	@Transactional
	@Override
	public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException { // 로그인 할 유저의 id를 파라미터로 전달받음
		Member member = memberRepository.findById(id);

		if (member != null) {
			return new PrincipalDetails(member); //// authentication 객체 안에 PrincipalDetails 이 들어간 것
		} else {
			throw new UsernameNotFoundException(id);
		}
	}

	/* 회원 전체 조회 */
	// 값을 가져오는 메서드에서는 기본 읽기전용옵션 적용됨
	public List<Member> findMembers() {
		return memberRepository.findAll();
	}

	/* 회원 전체 조회 */
	public Page<Member> getMemberList(Pageable pageable) {
		return memberRepository.findAll(pageable);
	}

	/* 개별 회원 조회 */
	// 값을 가져오는 메서드에서는 기본 읽기전용옵션 적용됨
	public Member findOne(Long memberNum) {
		Member member = memberRepository.findByNum(memberNum);

		return member == null ? null : member;
	}

	/* 개별 회원 삭제 */
	public int deleteOne(Long memberNum) {
		Member member = memberRepository.findByNum(memberNum);
		Member resultMember = memberRepository.save(Member.changeMemberOut(member));
		em.flush();
		if (!resultMember.getStatus().equals(Status.USER_OUT)) {
			return 0; // 탈퇴회원 처리가 안 됐으면 0 반환
		}
		return 1; // 탈퇴회원 처리가 됐으면 1 반환
	}

	/* 개별 회원 복구 */
	public int returnOne(Long memberNum) {
		Member member = memberRepository.findByNum(memberNum);
		Member resultMember = memberRepository.save(Member.changeMemberIn(member));
		em.flush();
		if (!resultMember.getStatus().equals(Status.USER_IN)) {
			return 0; // 탈퇴회원 처리가 안 됐으면 0 반환
		}
		return 1; // 탈퇴회원 처리가 됐으면 1 반환
	}

	public Member findByName(String name) {
		return memberRepository.findByName(name);
	}

	/* 회원 검색 기능 */
	public Page<Member> searchMember(String type, String keyword, Pageable pageable) {
		Page<Member> memberList = null;
		if (type.equals("num")) {
			memberList = memberRepository.searchByNum(Long.parseLong(keyword), pageable);
		} else if (type.equals("id")) {
			memberList = memberRepository.searchById(keyword, pageable);
		} else if (type.equals("name")) {
			memberList = memberRepository.findByNameContaining(keyword, pageable);
		} else if (type.equals("mobile")) {
			memberList = memberRepository.findByMobileContaining(keyword, pageable);
		} else if (type.equals("email")) {
			memberList = memberRepository.findByEmailContaining(keyword, pageable);
		} else if (type.equals("reportCnt")) {
			memberList = memberRepository.findByReportCnt(Long.parseLong(keyword), pageable);
		}
		return memberList;
	}

	public Page<Member> searchMemberByStatus(String keyword, Pageable pageable) {
		Page<Member> memberList = memberRepository.findAll(pageable);
		List<Member> list = new ArrayList<>();
		for (Member m : memberList) {
			if (m.getStatus().toString().contains(keyword)) {
				list.add(m);
			}
		}
		final int start = (int) pageable.getOffset();
		final int end = Math.min((start + pageable.getPageSize()), list.size());
		memberList = new PageImpl<>(list.subList(start, end), pageable, list.size());
		return memberList;
	}

	// 멤버아이디로 멤버번호 찾기
	public Long findNumById(String id) {
		return memberRepository.findMemberNumById(id);
	}

	// 멤버 신고수 올리기
	public int reportCntUp(Long num) {
		return memberRepository.reportCntUp(num);
	}

}