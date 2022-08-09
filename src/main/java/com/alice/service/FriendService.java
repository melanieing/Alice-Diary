package com.alice.project.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alice.project.domain.Friend;
import com.alice.project.domain.FriendsGroup;
import com.alice.project.domain.Member;
import com.alice.project.event.FriendAddEvent;
import com.alice.project.repository.FriendRepository;
import com.alice.project.repository.FriendsGroupRepository;
import com.alice.project.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendService {

	private final FriendRepository friendRepository;
	private final MemberRepository memberRepository;
	private final FriendsGroupRepository fgRepository;
	private final MemberService ms;
	private final ApplicationEventPublisher eventPublisher; // for notification

	// 새로운 친구추가 서비스(추가하는 멤버회원 번호, 추가되는 멤버회원의 아이디)
	@Transactional
	public boolean addFriendship(Member member, String addeeName) {
		Member addee = memberRepository.findByName(addeeName);
		Long adderNum = member.getNum();
		String groupName = "기본그룹";

//		if (adderNum == addee.getNum()) { // 자기 자신이 자신을 추가하면 안 됨
//			return false;
//		}
		FriendsGroup defaultGroup = fgRepository.searchByCreatorAndGroupName(adderNum, groupName);
		if (defaultGroup == null) { // 기본 그룹이 없으면 만들기
			fgRepository.save(new FriendsGroup(groupName, adderNum));
		}

		List<Friend> check = friendRepository.checkAlreadyFriend(adderNum, addee.getNum());
		Long groupNum = 1L; // 기본그룹에 추가
		if (check.size() <= 0) {
			Friend friend = new Friend(member, addee.getNum(), groupNum);
			
			// for notification
			Friend result = friendRepository.save(friend);
			friend.setMember(member);
			this.eventPublisher.publishEvent(new FriendAddEvent(result));
			log.info("friendService 퍼블리쉬 때려~");
			
			return true; // 추기되면 true
		} else {
			return false;
		}
	}

	// friendService에서 멤버에 있는 num으로 프렌드 객체를 만들기
	@Transactional
	public Friend groupNum(Long adderNum, Long addeeNum) {
		return friendRepository.findGroupByAddeeAdderNum(adderNum, addeeNum);
	}

	// adderNum이 추가한 친구목록
	public List<Friend> friendship(Long adderNum) {
		return friendRepository.findByAdderNum(adderNum);
	}

	// 모든 친구관계 조회하는 서비스
	public List<Friend> findAll() {
		return friendRepository.findAll();
	}

	// 회원으로 가입한 친구 검색
	public Member searchMember(String id) {
		return memberRepository.findById(id);
	}

	// 추가된 친구 목록에서 이름 & 아이디로 검색
	public List<Member> searchFriend(String friends, Long adderNum) {
		return memberRepository.findByIdOrName(adderNum, friends);
	}

	// 친구 상세보기(등록된 친구 번호 조회)
	public List<Friend> friendInfo(Long addeeNum) {
		return friendRepository.findByAddeeNum(addeeNum);
	}

	public boolean searchExist(Long adderNum, Long addeeNum) {
		// 존재하지 않음
		if (friendRepository.findGroupByAddeeAdderNum(adderNum, addeeNum) == null) {
			return false;
		}
		// 존재함
		return true;
	}

	// 친구 삭제하기
	@Transactional
	public void deleteFriend(Long adderNum, Long addeeNum) {
		Member adder = ms.findByNum(adderNum);
		Friend friend = friendRepository.findGroupByAddeeAdderNum(adderNum, addeeNum);
		friendRepository.delete(friend);
		// friendRepository.deleteFriend(adderNum, addeeNum);
	}

	// 친구 소속 그룹 변경
	@Transactional
	public Friend changeGroup(Long friendNum, Long addeeNum, Long groupNum, Member member) {

		Friend friend = new Friend(friendNum, member, addeeNum, groupNum);
		return friendRepository.save(friend);
	}

	public List<Friend> weAreFriend(Long adderNum) {
		return friendRepository.weAreFriend(adderNum);
	}

	public Boolean iAmFriend(Long adderNum, String fId) {
		List<Friend> fList = weAreFriend(adderNum);
		Member member = ms.findById(fId);
		for (Friend f : fList) {
			if (member.getNum() == f.getMember().getNum()) {
				return true;
			}
		}
		return false;
	}

}