package com.alice.project.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alice.project.domain.FriendsGroup;
import com.alice.project.domain.Member;
import com.alice.project.service.FriendService;
import com.alice.project.service.FriendsGroupService;
import com.alice.project.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class FriendsGroupController {

	private final FriendService friendService;
	private final MemberService memberService;
	private final FriendsGroupService friendsGroupService;

	// 그룹명 등록
	@PostMapping("/friends/addGroup")
	@ResponseBody
	public Integer addGroup(String groupName, @AuthenticationPrincipal UserDetails user, Model model) {
		if (groupName == null || groupName.isEmpty() || groupName.equals("")) {
			return 0; // 실패하면 0
		}
		Long creatorNum = memberService.findById(user.getUsername()).getNum();
		log.info("그룹생성한 회원 번호!!" + creatorNum);
		log.info("그룹 이름 !!!" + groupName);
		FriendsGroup saveGroup = null;
		saveGroup = friendsGroupService.addGroup(creatorNum, groupName);

		model.addAttribute("grouplist", saveGroup);
		return 1; // 성공하면 1
	}

	// 그룹명 목록확인
	@GetMapping("/friends/groupList")
	public String friendsGrouplist(Model model, @AuthenticationPrincipal UserDetails user) {
		Member member = memberService.findById(user.getUsername());

		List<FriendsGroup> friendGroups = friendsGroupService.findAllByAdder(member.getNum());
		model.addAttribute("member", member);
		model.addAttribute("grouplist", friendGroups);
		return "friends/friendsGrouplist";
	}

}