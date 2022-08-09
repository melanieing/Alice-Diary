package com.alice.project.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.alice.project.domain.FriendsGroup;
import com.alice.project.domain.Member;
import com.alice.project.repository.FriendRepository;
import com.alice.project.repository.FriendsGroupRepository;
import com.alice.project.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendsGroupService {

	private final FriendsGroupRepository friendsGroupRepository;
	private final FriendRepository friendRepository;
	private final MemberRepository memberRepository;

	// 그룹 이름
	public String getGroupName(Long groupNum) {
		FriendsGroup group = friendsGroupRepository.getById(groupNum);
		return group.getGroupName();
	}

	// 그룹 번호
	public FriendsGroup getGroupByNum(Long groupNum) {
		FriendsGroup group = friendsGroupRepository.findByNum(groupNum);
		return group;
	}

	// 그룹명 등록(그룹 생성한 회원번호, 그룹이름)
	public FriendsGroup addGroup(Long groupCreatorNum, String groupName) {
		Member m = memberRepository.findByNum(groupCreatorNum);
		FriendsGroup saveGroup = new FriendsGroup(groupName, m.getNum());
		log.info("그룹생성한 회원 번호!!!!!!" + groupCreatorNum);
		log.info("그룹 이름 !!!!!!!!!" + groupName);
		return friendsGroupRepository.save(saveGroup);
	}

	// 그룹명 전체 조회
	public List<FriendsGroup> friendsGrouplist() {
		return friendsGroupRepository.findAll();
	}

	// 그룹만든사람 객체로 내가만든그룹 전체 불러오기
	public List<FriendsGroup> findAllByAdder(Long memNum) {
		return friendsGroupRepository.findAllByAdder(memNum);
	}
}