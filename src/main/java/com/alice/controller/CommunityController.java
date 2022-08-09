package com.alice.project.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alice.project.domain.AttachedFile;
import com.alice.project.domain.Community;
import com.alice.project.domain.Friend;
import com.alice.project.domain.Member;
import com.alice.project.domain.Message;
import com.alice.project.domain.Post;
import com.alice.project.domain.Reply;
import com.alice.project.repository.NotificationRepository;
import com.alice.project.service.AttachedFileService;
import com.alice.project.service.CommunityService;
import com.alice.project.service.FriendService;
import com.alice.project.service.FriendsGroupService;
import com.alice.project.service.MemberService;
import com.alice.project.service.MessageService;
import com.alice.project.service.PostService;
import com.alice.project.service.ReplyService;
import com.alice.project.service.ReportService;
import com.alice.project.web.AlarmMemberListDto;
import com.alice.project.web.CommunityCreateDto;
import com.alice.project.web.FriendshipDto;
import com.alice.project.web.PostSearchDto;
import com.alice.project.web.ReplyDto;
import com.alice.project.web.WriteFormDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
public class CommunityController {

	private final MemberService memberService;
	private final FriendService friendService;
	private final FriendsGroupService friendsGroupService;
	private final CommunityService communityService;
	private final PostService postService;
	private final AttachedFileService attachedFileService;
	private final ReplyService replyService;
	private final ReportService reportService;
	private final MessageService messageService;
	private final NotificationRepository notificationRepository;

	@GetMapping("/community/checkExist")
	public String checkExist(@AuthenticationPrincipal UserDetails user) {

		// 내가 방장인 커뮤니티 목록불러오기
		Member hostMem = memberService.findById(user.getUsername());
		List<Community> hostComs = communityService.findByMember(hostMem);

		// 내가 소속회원인 커뮤니티 목록 불러오기
		List<Community> memberComs = communityService.getAll();
		List<Community> resultList = new ArrayList<>();
		if (memberComs.size() != 0) {
			for (Community c : memberComs) {
				String memberList = c.getMemberList();
				if (memberList != null) {
					Boolean result = memberList.contains(user.getUsername());
					if (result) {
						resultList.add(c);
					}
				}
			}
		}

		if (hostComs.size() > 0) {
			return "redirect:./" + hostComs.get(0).getNum() + "/list";
		} else if (resultList.size() > 0) {
			return "redirect:./" + resultList.get(0).getNum() + "/list";
		} else {
			return "redirect:./create";
		}
	}

	@GetMapping("/community/create")
	public String createForm(@AuthenticationPrincipal UserDetails user, Model model) {

		// 내가 방장인 커뮤니티 목록불러오기
		Member hostMem = memberService.findById(user.getUsername());
		List<Community> hostComs = communityService.findByMember(hostMem);

		// 내가 소속회원인 커뮤니티 목록 불러오기
		List<Community> memberComs = communityService.getAll();
		List<Community> resultList = new ArrayList<>();
		if (memberComs.size() != 0) {
			for (Community c : memberComs) {
				String memberList = c.getMemberList();
				if (memberList != null) {
					Boolean result = memberList.contains(user.getUsername());
					if (result) {
						resultList.add(c);
					}
				}
			}
		}

		model.addAttribute("hostComs", hostComs);
		model.addAttribute("myComList", resultList);

		// 내 친구들 목록 불러오기
		List<Friend> fList = friendService.weAreFriend(hostMem.getNum());
		CommunityCreateDto ccdto = new CommunityCreateDto();
		List<AlarmMemberListDto> cTmp = new ArrayList<AlarmMemberListDto>();

		for (Friend f : fList) {
			Member fInfo = memberService.findByNum(f.getMember().getNum());
			AlarmMemberListDto tmp = new AlarmMemberListDto();
			tmp.setId(fInfo.getId());
			tmp.setName(fInfo.getName());
			cTmp.add(tmp);
		}
		ccdto.setFriendsList(cTmp);

		model.addAttribute("ccdto", ccdto);
		model.addAttribute("member", hostMem);
        long count = notificationRepository.countByMemberAndChecked(hostMem, false);
        model.addAttribute("hasNotification", count > 0);

		return "community/createCommunity";
	}

	// 친구 검색
	@PostMapping("/community/searchFriend")
	@ResponseBody
	public List<FriendshipDto> searchFriend(String searchFriend, @AuthenticationPrincipal UserDetails user) {
		Long adderNum = memberService.findById(user.getUsername()).getNum();
		List<Member> myFriends = friendService.searchFriend(searchFriend, adderNum);
		List<FriendshipDto> searchFriendList = new ArrayList<FriendshipDto>();
		for (Member f : myFriends) {
			Member sf = memberService.findByNum(f.getNum());
			Friend fg = friendService.groupNum(adderNum, f.getNum());
			String groupName = friendsGroupService.getGroupName(fg.getGroupNum());
			log.info("그룹이름:" + groupName);
			FriendshipDto dto = new FriendshipDto(sf.getId(), sf.getName(), groupName);
			searchFriendList.add(dto);
		}
		return searchFriendList;
	}

	// 커뮤니티 생성하기
	@PostMapping("/community/create")
	public String communityCreate(CommunityCreateDto dto, @AuthenticationPrincipal UserDetails user) {
		log.info("post 도착");

		Member member = memberService.findById(user.getUsername());

		Community com = Community.createCommunity(dto.getComMembers(), dto.getComName(), dto.getDescription(), member);
		log.info("service create하기 전!");
		communityService.create(com);
		log.info("service create한 후!");

		// 초대장 쪽지발송
		Long messageFromNum = member.getNum();
		log.info("초대장 발송");

		for (String f : com.getMemberList().split(",")) {
			Long messageToNum = memberService.findNumById(f);

			Long user1Num = 0L;
			Long user2Num = 0L;

			if (messageFromNum < messageToNum) {
				user1Num = messageFromNum;
				user2Num = messageToNum;

				messageService.inviteMsg(Message.createInviteMsg(user1Num, user2Num, 0L, com.getName()));

			} else {
				user1Num = messageToNum;
				user2Num = messageFromNum;

				messageService.inviteMsg(Message.createInviteMsg(user1Num, user2Num, 1L, com.getName()));
			}
		}
		String comNum = Long.toString(com.getNum());

		return "redirect:./" + comNum + "/list";
	}

	// 게시글 리스트 가져오기
	@GetMapping("community/{comNum}/list")
	public String list(@PathVariable Long comNum, Model model,
			@ModelAttribute("postSearchDto") PostSearchDto postSearchDto, @AuthenticationPrincipal UserDetails user,
			@PageableDefault(page = 0, size = 5, direction = Sort.Direction.DESC) Pageable pageable) {

		// 내가 방장인 커뮤니티 목록불러오기
		Member hostMem = memberService.findById(user.getUsername());
		List<Community> hostComs = communityService.findByMember(hostMem);
		model.addAttribute("hostComs", hostComs);

		// 내가 소속회원인 커뮤니티 목록 불러오기
		List<Community> memberComs = communityService.getAll();
		List<Community> resultList = new ArrayList<>();
		if (memberComs.size() != 0) {
			for (Community c : memberComs) {
				String memberList = c.getMemberList();
				if (memberList != null) {
					Boolean result = memberList.contains(user.getUsername());
					if (result) {
						resultList.add(c);
					}
				}
			}
		}
		model.addAttribute("myComList", resultList);

		String memberList = communityService.findMemListByNum(comNum);
		String[] memList = null;
		if (memberList != null) {
			memList = memberList.split(",");
		}
		model.addAttribute("memberList", memList);

		String hostMemberId = communityService.findMemberIdByNum(comNum);
		model.addAttribute("hostMemId", hostMemberId);

		String keyword = postSearchDto.getKeyword();
		Long size = 0L;
		Page<Post> list = null;
		List<Long> countReply = new ArrayList<Long>();

		if (keyword == null) {
			list = postService.comList(comNum, pageable);
			size = list.getTotalElements();
			for (Post p : list) {
				Long cnttmp = 0L;
				cnttmp = replyService.getCountReply(p.getNum());
				countReply.add(cnttmp);
			}

		} else {
			list = postService.comSearchList(comNum, postSearchDto, pageable);
			size = list.getTotalElements();
			for (Post p : list) {
				Long cnttmp = 0L;
				cnttmp = replyService.getCountReply(p.getNum());
				countReply.add(cnttmp);
			}

		}

		int nowPage = list.getPageable().getPageNumber() + 1;
		int startPage = Math.max(nowPage - 2, 1);
		int endPage = 0;
		if (startPage == 1) {
			if (list.getTotalPages() < 5) {
				endPage = list.getTotalPages();
			} else {
				endPage = 5;
			}
		} else {
			endPage = Math.min(nowPage + 2, list.getTotalPages());
		}

		if (endPage == list.getTotalPages() && (endPage - startPage) < 5) {
			startPage = (endPage - 4 <= 0) ? 1 : endPage - 4;
		}

		log.info(countReply.toString());
		model.addAttribute("countReply", countReply);
		model.addAttribute("comName", communityService.findNameByNum(comNum));
		model.addAttribute("comDescription", communityService.findDescriptionByNum(comNum));
		model.addAttribute("list", list);
		model.addAttribute("comNum", comNum);
		model.addAttribute("size", size);
		log.info("size : " + size);
		model.addAttribute("nowPage", nowPage);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("keyword", keyword);

		model.addAttribute("type", postSearchDto.getType());
		model.addAttribute("member", memberService.findById(user.getUsername()));

		log.info("nowPage:" + nowPage);
		log.info("startPage:" + startPage);
		log.info("endPage:" + endPage);

		return "community/communityList";
	}

	// 글쓰기
	@GetMapping("community/{comNum}/post")
	public String writeform(@PathVariable Long comNum, Model model, @AuthenticationPrincipal UserDetails user) {

		// 내가 방장인 커뮤니티 목록불러오기
		Member hostMem = memberService.findById(user.getUsername());
		List<Community> hostComs = communityService.findByMember(hostMem);
		model.addAttribute("hostComs", hostComs);

		// 내가 소속회원인 커뮤니티 목록 불러오기
		List<Community> memberComs = communityService.getAll();
		List<Community> resultList = new ArrayList<>();
		if (memberComs.size() != 0) {
			for (Community c : memberComs) {
				String memberList = c.getMemberList();
				if (memberList != null) {
					Boolean result = memberList.contains(user.getUsername());
					if (result) {
						resultList.add(c);
					}
				}
			}
		}
		model.addAttribute("myComList", resultList);

		model.addAttribute("comName", communityService.findNameByNum(comNum));
		model.addAttribute("writeFormDto", new WriteFormDto());
		model.addAttribute("comNum", comNum);
		model.addAttribute("member", memberService.findById(user.getUsername()));
		return "community/comWriteForm";
	}

	// 글쓰기
	@PostMapping("community/{comNum}/post")
	public String writeSubmit(@PathVariable Long comNum, WriteFormDto writeFormDto, HttpSession session,
			@AuthenticationPrincipal UserDetails user) {

		Member member = memberService.findById(user.getUsername());
		Community community = communityService.findByNum(comNum);

		Post post = Post.creatCommunity(writeFormDto, member, community);
		Post writedPost = postService.write(post);

		attachedFileService.postFileUpload(writeFormDto.getOriginName(), writedPost, session, user.getUsername());

		return "redirect:./list";
	}

	// 커뮤니티게시글 삭제하기
	@RequestMapping("/community/delete")
	public String comPostDelete(Long comNum, Long num) {
		log.info("컨트롤러 실행 num:" + num);
		List<Reply> replies = replyService.getReplyByPostNum(num);

		for (Reply r : replies) {
			reportService.deleteReportWithReply(r.getNum()); // 게시글의 댓글에 대한 신고 삭제
		}

		reportService.deleteReportWithPost(num); // 게시글에 대한 신고삭제
		postService.deletePostwithReply(num);
		postService.deletePostwithFile(num);
		postService.deletePost(num);

		return "redirect:./" + comNum + "/list";
	}

	// 게시글 상세보기
	@GetMapping("community/{comNum}/get/{num}")
	public String postView(@PathVariable Long comNum, Model model, @PathVariable Long num, Pageable pageable,
			HttpSession session, @AuthenticationPrincipal UserDetails user) {

		// 내가 방장인 커뮤니티 목록불러오기
		Member hostMem = memberService.findById(user.getUsername());
		List<Community> hostComs = communityService.findByMember(hostMem);
		model.addAttribute("hostComs", hostComs);

		// 내가 소속회원인 커뮤니티 목록 불러오기
		List<Community> memberComs = communityService.getAll();
		List<Community> resultList = new ArrayList<>();
		if (memberComs.size() != 0) {
			for (Community c : memberComs) {
				String memberList = c.getMemberList();
				if (memberList != null) {
					Boolean result = memberList.contains(user.getUsername());
					if (result) {
						resultList.add(c);
					}
				}
			}
		}
		model.addAttribute("myComList", resultList);

		log.info("num :" + num);
		log.info("user.getUsername() :" + user.getUsername());
		Post viewPost = postService.postView(num);

		postService.viewCntUp(num);

		model.addAttribute("postView", viewPost);

		List<AttachedFile> files = attachedFileService.fileView(viewPost, pageable);
		model.addAttribute("files", files);

		List<ReplyDto> replyList = replyService.replyList(num);

		model.addAttribute("replyList", replyList);
		model.addAttribute("member", memberService.findById(user.getUsername()));

		String comName = communityService.findNameByNum(comNum);
		model.addAttribute("comName", comName);

		return "community/comPostView";
	}

	// get 게시글 수정하기 첨부파일도 수정
	@GetMapping("/community/{comNum}/put/{num}")
	public String getUpdate(@PathVariable Long comNum, @PathVariable Long num, Model model, Pageable pageable,
			@AuthenticationPrincipal UserDetails user) {
		log.info("수정컨트롤러 get");

		// 내가 방장인 커뮤니티 목록불러오기
		Member hostMem = memberService.findById(user.getUsername());
		List<Community> hostComs = communityService.findByMember(hostMem);
		model.addAttribute("hostComs", hostComs);

		// 내가 소속회원인 커뮤니티 목록 불러오기
		List<Community> memberComs = communityService.getAll();
		List<Community> resultList = new ArrayList<>();
		if (memberComs.size() != 0) {
			for (Community c : memberComs) {
				String memberList = c.getMemberList();
				if (memberList != null) {
					Boolean result = memberList.contains(user.getUsername());
					if (result) {
						resultList.add(c);
					}
				}
			}
		}
		model.addAttribute("myComList", resultList);

		Post getUpdate = postService.postView(num);

		WriteFormDto updateDto = new WriteFormDto(num, getUpdate.getTitle(), getUpdate.getContent());
		List<AttachedFile> files = attachedFileService.fileView(getUpdate, pageable);

		String comName = communityService.findNameByNum(comNum);
		model.addAttribute("comName", comName);

		model.addAttribute("files", files);
		model.addAttribute("updateDto", updateDto);
		model.addAttribute("member", memberService.findById(user.getUsername()));
		model.addAttribute("comNum", comNum);
		model.addAttribute("num", num); // 해당 게시글 번호
		return "community/comUpdateForm";
	}

	// post 게시글 수정하기 첨부파일도 수정
	@PostMapping("/community/{comNum}/put/{num}")
	public String updatePorc(@PathVariable Long comNum, @PathVariable Long num, WriteFormDto updateDto,
			HttpSession session, @AuthenticationPrincipal UserDetails user) {

		String postNum = Long.toString(updateDto.getPostNum());

		postService.updatePost(updateDto.getPostNum(), updateDto);

		Post updatedPost = postService.findOne(updateDto.getPostNum());

		attachedFileService.postFileUpload(updateDto.getOriginName(), updatedPost, session, user.getUsername());

		return "redirect:/community/" + comNum + "/get/" + num;
	}

	// 게시글 수정에서 파일하나 삭제하기
	@PostMapping("/community/put/filedelete")
	@ResponseBody
	public JSONObject oneFileDelete(Long num, Long postNum) {

		postService.deleteOneFile(num);

		JSONObject jObj = new JSONObject();

		List<AttachedFile> files = attachedFileService.fileDeleteAfterList(postNum);

		jObj.put("files", files);

		return jObj;
	}

	// 커뮤니티 탈퇴하기
	@RequestMapping("/community/resign")
	@ResponseBody
	public boolean resign(Long comNum, String userId) {
		communityService.resign(comNum, userId);

		return true;
	}

	// 커뮤니티 관리페이지 - 커뮤니티 정보 가져오기
	@GetMapping("community/{comNum}/manage")
	public String getComManage(@PathVariable Long comNum, Model model, @AuthenticationPrincipal UserDetails user) {

		// 내가 방장인 커뮤니티 목록불러오기
		Member hostMem = memberService.findById(user.getUsername());
		List<Community> hostComs = communityService.findByMember(hostMem);
		model.addAttribute("hostComs", hostComs);

		// 내가 소속회원인 커뮤니티 목록 불러오기
		List<Community> memberComs = communityService.getAll();
		List<Community> resultList = new ArrayList<>();
		if (memberComs.size() != 0) {
			for (Community c : memberComs) {
				String memberList = c.getMemberList();
				if (memberList != null) {
					Boolean result = memberList.contains(user.getUsername());
					if (result) {
						resultList.add(c);
					}
				}
			}
		}
		model.addAttribute("myComList", resultList);

		Community community = communityService.findByNum(comNum);
		String memberList = community.getMemberList();
		List<String> members = new ArrayList<String>();
		if (memberList != null) {
			String[] mList = memberList.split(",");
			for (String m : mList) {
				members.add(memberService.findById(m).getName());
			}
		}
		model.addAttribute("comMembers", members);

		CommunityCreateDto manageCom = new CommunityCreateDto(memberList, community.getName(),
				community.getDescription(), comNum);

		model.addAttribute("manageCom", manageCom);
		model.addAttribute("comNum", comNum);
		model.addAttribute("member", memberService.findById(user.getUsername()));

		return "community/manageCommunity";
	}

	// 커뮤니티 관리페이지 - 수정하기
	@PostMapping("community/{comNum}/manage")
	public String postComManage(@PathVariable Long comNum, CommunityCreateDto manageCom,
			@AuthenticationPrincipal UserDetails user) {

		communityService.edit(comNum, manageCom);

		return "redirect:./list";
	}

	// 커뮤니티 삭제하기
	@RequestMapping("/community/communitydelete")
	@ResponseBody
	public String comDelete(Long comNum) {
		log.info("삭제컨트롤러도착" + comNum);
		List<Post> posts = postService.getPostBycomNum(comNum);

		for (Post p : posts) {
			List<Reply> replies = replyService.getReplyByPostNum(p.getNum());
			for (Reply r : replies) {
				reportService.deleteReportWithReply(r.getNum());
			}
			reportService.deleteReportWithPost(p.getNum());
			postService.deletePostwithFile(p.getNum());
			postService.deletePostwithReply(p.getNum());
			postService.deletePost(p.getNum());
		}

		communityService.deleteCom(comNum);

		return "redirect:/AliceDiary/community/create";

	}

}