package com.alice.project.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alice.project.domain.Member;
import com.alice.project.repository.MemberRepository;
import com.alice.project.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true) // 기본적으로 못바꾸게 해놓고
@RequiredArgsConstructor
public class ProfileService {
	private final MemberRepository memberRepository;
	private final ProfileRepository profileRepository;

	public Member findByEmail(String email) {
		Member member = memberRepository.findByEmail(email);
		return member;
	}

	public Member findById(String id) {
		Member member = memberRepository.findById(id);
		return member;
	}

	public Member findMemById(String id) {
		Member updateMember = profileRepository.findById(id);
		return updateMember;
	}
}