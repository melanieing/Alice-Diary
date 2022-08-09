package com.alice.project.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alice.project.domain.Community;
import com.alice.project.domain.Member;
import com.alice.project.event.CommunityInvitedEvent;
import com.alice.project.repository.CommunityRepository;
import com.alice.project.repository.MemberRepository;
import com.alice.project.web.CommunityCreateDto;
import com.alice.project.web.PostSearchDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CommunityService {

	private final CommunityRepository comRepository;
	private final MemberRepository memberRepository;
	private final ApplicationEventPublisher eventPublisher; // for notification

	// 커뮤니티 생성하기
	@Transactional
	public Community create(Community com) {
		log.info("여기까지는 옴 : 커뮤 서비스");
		Community community = comRepository.save(com);
		// for notification
		this.eventPublisher.publishEvent(new CommunityInvitedEvent(community));
		log.info("이벤트 퍼블리시 날려~");

		return community;
	}

	// 번호로 객체찾기
	public Community findByNum(Long num) {
		return comRepository.findByNum(num);
	}

	// 번호로 커뮤니티이름찾기
	public String findNameByNum(Long num) {
		return comRepository.findNameByNum(num);
	}

	// 번호로 소속회원 찾기
	public String findMemListByNum(Long num) {
		return comRepository.findMemListByNum(num);
	}

	// 커뮤니티 탈퇴하기
	@Transactional
	public void resign(Long comNum, String memId) {
		String memberList = comRepository.findMemListByNum(comNum);
		StringBuffer memList = new StringBuffer();
		List<String> ls = new ArrayList<>(Arrays.asList(memberList.split(",")));
		ls.remove(memId);
		
		String newMemList = "";
		if (ls.size() != 0) { // 커뮤니티에 남아있는 사람이 있을 때!
			for (String i : ls) {
				memList.append(i).append(",");
			}
			memList.deleteCharAt(memList.lastIndexOf(","));
			newMemList = memList.toString();
		}

		comRepository.memberListUpdate(comNum, newMemList);
	}

	// 커뮤니티번호로 방장아이디 찾기
	public String findMemberIdByNum(Long num) {
		Long hostMemNum = comRepository.findMemberNumByNum(num);
		String hostMemberId = memberRepository.findIdByNum(hostMemNum);
		return hostMemberId;
	}

	// 커뮤니티 수정하기
	@Transactional
	public void edit(Long comNum, CommunityCreateDto manageCom) {
		comRepository.nameEdit(comNum, manageCom.getComName());
		comRepository.descriptionEdit(comNum, manageCom.getDescription());

	}
	
	@Transactional
	public void deleteCom(Long comNum) {
		comRepository.deleteCom(comNum);
	}

	// 멤버객체로 커뮤니티 객체 찾기
	public List<Community> findByMember(Member member) {
		return comRepository.findByMember(member);
	}

	// 전체 커뮤니티 중 번호랑 멤버리스트(string) 칼럼 2개 가져오기
	public List<Community> getAll() {
		return comRepository.getAll();
	}
	

	//커뮤니티 번호로 설명 가져오기
	public String findDescriptionByNum(Long comNum) {
		return comRepository.findDescriptionByNum(comNum);
	}

	/* 관리자 모드 : 커뮤니티 관리 */
	// 모든 커뮤니티 가져오기
	public Page<Community> showCommunityList(Pageable pageable) {
		return comRepository.searchAllCommunities(pageable);
	}
	// 커뮤니티 검색
	public Page<Community> searchCommunityList(PostSearchDto postSearchDto, Pageable pageable) {
		String type = postSearchDto.getType();
		String keyword = postSearchDto.getKeyword();
		Page<Community> communities = null;
		List<Community> comsForCreatorSearching = new ArrayList<>();
		if (type.equals("comNum")) {
			communities = comRepository.searchByComNum(Long.parseLong(keyword), pageable);
		} else if (type.equals("name")) {
			communities = comRepository.searchByName(keyword, pageable);
		} else if (type.equals("creator")) {
			List<Member> members = memberRepository.findByNameContaining(keyword);
			for (Member m : members) {
				comsForCreatorSearching.addAll(comRepository.findByMember(m));
			}
			final int start = (int) pageable.getOffset();
			final int end = Math.min((start + pageable.getPageSize()), comsForCreatorSearching.size());
			communities = new PageImpl<>(comsForCreatorSearching.subList(start, end), pageable, comsForCreatorSearching.size());
		}
		return communities;
	}
	

}