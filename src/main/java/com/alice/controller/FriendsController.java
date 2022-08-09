package com.alice.project.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alice.project.domain.Friend;
import com.alice.project.domain.Member;
import com.alice.project.domain.Status;
import com.alice.project.repository.NotificationRepository;
import com.alice.project.service.FriendService;
import com.alice.project.service.FriendsGroupService;
import com.alice.project.service.MemberService;
import com.alice.project.service.ProfileService;
import com.alice.project.web.FriendsDto;
import com.alice.project.web.FriendshipDto;
import com.alice.project.web.MemberDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class FriendsController {

	private final FriendService friendService;
	private final MemberService memberService;
	private final FriendsGroupService friendsGroupService;
	private final ProfileService profileService;
	private final NotificationRepository notificationRepository;


	// 친구 추가(회원 name으로 id검색)
	@PostMapping("/friends/add")
	public String addFriend(String searchName, @AuthenticationPrincipal UserDetails user) {
		Member m = memberService.findById(user.getUsername());
		log.info("member : " + m.getId());
		log.info("member : " + searchName);

		friendService.addFriendship(m, searchName);

		return "redirect:/friends";
	}

	// 친구 검색해서 추가
	@PostMapping("/friends/searchMember")
	@ResponseBody
	public MemberDto searchMember(String name, @AuthenticationPrincipal UserDetails user) {
		log.info("member name : " + name);
		MemberDto mdto = new MemberDto();
		Member member = memberService.findByName(name);
		
		if (member == null || member.equals("") || member.getStatus().equals(Status.ADMIN)
				|| member.getStatus().equals(Status.USER_OUT))
		{
			log.info("!?????????????????????????????!!!!!!!!!!!!!");
			mdto.setName("noFriend");
			return mdto; // name이 "noFriend"인 사람
		}
		log.info("member.getnum : " + member.getNum());
		mdto.setId(member.getId());
		mdto.setMbti(member.getMbti());
		mdto.setName(member.getName());
		mdto.setProfileImg(member.getProfileImg());
		
		return mdto;
	}

	// 추가된 친구 목록 조회
	@GetMapping("/friends")
	public String friendshiplist(Model model, HttpSession session, @AuthenticationPrincipal UserDetails user) {
		Long adderNum = memberService.findById(user.getUsername()).getNum();

		List<Friend> friendList = friendService.friendship(adderNum);
		List<FriendshipDto> friendship = new ArrayList<>();

		for (Friend f : friendList) {
			Member m = memberService.findByNum(f.getAddeeNum());
			log.info("status : " + m.getStatus());
			if (m.getStatus() == Status.USER_OUT) {
				continue;
			}
			String groupName = friendsGroupService.getGroupName(f.getGroupNum());
			FriendshipDto dto = new FriendshipDto(m.getNum(), m.getId(), m.getName(), m.getMobile(), m.getBirth(),
					m.getGender(), m.getEmail(), groupName);

			friendship.add(dto);
		}
		Member mb = memberService.findById(user.getUsername());
		model.addAttribute("member", mb);
		model.addAttribute("friendList", friendship);
        long count = notificationRepository.countByMemberAndChecked(mb, false);
        model.addAttribute("hasNotification", count > 0);
		return "friends/friendslist";
	}

	// 친구 목록에서 친구 검색
	@PostMapping("/friends/searchFriend")
	@ResponseBody
	public List<FriendshipDto> searchFriend(String friends, @AuthenticationPrincipal UserDetails user) {
		Long adderNum = memberService.findById(user.getUsername()).getNum();
		List<Member> searchFriend = friendService.searchFriend(friends, adderNum);
		List<FriendshipDto> searchFriendList = new ArrayList<FriendshipDto>();
		for (Member f : searchFriend) {
			Member sf = memberService.findByNum(f.getNum());
			Friend fg = friendService.groupNum(adderNum, f.getNum());
			String groupName = friendsGroupService.getGroupName(fg.getGroupNum());
			log.info("그룹이름:" + groupName);
			FriendshipDto dto = new FriendshipDto(sf.getNum(), sf.getId(), sf.getName(), sf.getMobile(), sf.getBirth(),
					sf.getGender(), sf.getEmail(), groupName);
			searchFriendList.add(dto);
		}
		return searchFriendList;
	}

	// 친구 프로필 상세보기
	@GetMapping("/friends/friendInfo/{id}")
	public String friendInfo(Model model, @PathVariable("id") String id, @AuthenticationPrincipal UserDetails user) {
		Member friend = profileService.findById(id);
		Member member = memberService.findById(user.getUsername());
		log.info("member=" + member);

		List<String> wishList = new ArrayList<String>();
		if (friend.getWishlist() != null) {
			String wish = friend.getWishlist().replaceAll(",", " ");
			String[] wishs = wish.split(" ");
			for (String s : wishs) {
				wishList.add(s);
			}
		}

		model.addAttribute("wishList", wishList);
		model.addAttribute("friend", friend);
		model.addAttribute("member", member);
		model.addAttribute("myFriendGroup", friendsGroupService.findAllByAdder(member.getNum()));
		model.addAttribute("friendsDto", new FriendsDto());
		model.addAttribute("member", memberService.findById(user.getUsername()));

		log.info("그룹 목록 확인: " + friendsGroupService.findAllByAdder(member.getNum()));
		return "friends/friendInfo";
	}

	// 친구 삭제하기
	@PostMapping("friends/deleteFriend")
	@ResponseBody
	public void deleteFriend(@AuthenticationPrincipal UserDetails user, String addeeId) {
		log.info("여기오니?");
		Member member = memberService.findById(user.getUsername());
		Long adderNum = member.getNum();

		Long addeeNum = memberService.findNumById(addeeId);
		log.info("addeeNum : " + addeeNum);
		friendService.deleteFriend(adderNum, addeeNum);

	}

	// 친구의 소속 그룹 변경
	@PostMapping("/friends/changeGroup")
	public String changeGroup(@AuthenticationPrincipal UserDetails user,
			@ModelAttribute("friendsDto") FriendsDto friendsDto) {
		log.info("들어왔음??");
		Member member = memberService.findById(user.getUsername());
		Long addeeNum = friendsDto.getAddeeNum();
		Friend friend = friendService.groupNum(member.getNum(), addeeNum);
		Long friendNum = friend.getNum();
		Long groupNum = friendsDto.getGroupNum();
		log.info("addeeNum" + addeeNum);
		log.info("groupNum" + groupNum);
		log.info("friendNum" + friendNum);
		friendService.changeGroup(friendNum, addeeNum, groupNum, member);

		return "redirect:/friends";
	}
}