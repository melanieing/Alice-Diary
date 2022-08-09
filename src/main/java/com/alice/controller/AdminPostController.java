package com.alice.project.controller;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.alice.project.domain.Member;
import com.alice.project.domain.Post;
import com.alice.project.domain.Reply;
import com.alice.project.repository.NotificationRepository;
import com.alice.project.service.AttachedFileService;
import com.alice.project.service.CommunityService;
import com.alice.project.service.MemberService;
import com.alice.project.service.PostService;
import com.alice.project.service.ReplyService;
import com.alice.project.service.ReportService;
import com.alice.project.web.PostSearchDto;
import com.alice.project.web.ReplyDto;
import com.alice.project.web.WriteFormDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminPostController {

	private final ReplyService replyService;
	private final PostService postService;
	private final AttachedFileService attachedFileService;
	private final MemberService memberService;
	private final ReportService reportService;
	private final CommunityService communityService;
	private final NotificationRepository notificationRepository;

	// 공지사항 관리
	/* 공지사항 목록 */
	@GetMapping("/notice/list")
	public String showNoticeList(
			@PageableDefault(page = 0, size = 5, direction = Sort.Direction.DESC) Pageable pageable,
			@ModelAttribute("postSearchDto") PostSearchDto postSearchDto, @AuthenticationPrincipal UserDetails user,
			Model model, Long num) {
		Page<Post> notices = null;
		String type = postSearchDto.getType();
		String keyword = postSearchDto.getKeyword();
		Member mb = memberService.findById(user.getUsername());
		model.addAttribute("member", mb);
		if (keyword == null || type == null || keyword.isEmpty() || type.isEmpty()) {
			notices = postService.notceList(pageable);
		} else {
			notices = postService.searchNoticeList(postSearchDto, pageable); // 새로운 서비스의 메서드 사용할 예정
		}

		Long size = notices.getTotalElements();
		int nowPage = notices.getPageable().getPageNumber() + 1;
		int startPage = Math.max(nowPage - 2, 1);
		int endPage = 0;
		if (startPage == 1) {
			if (notices.getTotalPages() < 5) {
				endPage = notices.getTotalPages();
			} else {
				endPage = 5;
			}
		} else {
			endPage = Math.min(nowPage + 2, notices.getTotalPages());
		}

		if (endPage == notices.getTotalPages() && (endPage - startPage) < 5) {
			startPage = (endPage - 4 <= 0) ? 1 : endPage - 4;
		}

		model.addAttribute("notices", notices);
		model.addAttribute("nowPage", nowPage);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("size", size);
		long count = notificationRepository.countByMemberAndChecked(mb, false);
		model.addAttribute("hasNotification", count > 0);

		return "/admin/noticeList";
	}

	/* 공지사항 상세보기 */
	@GetMapping("/notice/get")
	public String postNoticeView(Model model, Long num, Pageable pageable, HttpSession session,
			@AuthenticationPrincipal UserDetails user) {
		Member mb = memberService.findById(user.getUsername());
		model.addAttribute("member", mb);
		log.info("num :" + num);
		Post viewPost = postService.postView(num);

		postService.viewCntUp(num);

		model.addAttribute("postView", viewPost);

		List<AttachedFile> files = attachedFileService.fileView(viewPost, pageable);
		model.addAttribute("files", files);

		List<ReplyDto> replyList = replyService.replyList(num);

		model.addAttribute("replyList", replyList);
		long count = notificationRepository.countByMemberAndChecked(mb, false);
		model.addAttribute("hasNotification", count > 0);
		return "/admin/noticeView";
	}

	/* 공지사항 쓰기 폼 반환 */
	@GetMapping("/notice/post")
	public String writeform(Model model, @AuthenticationPrincipal UserDetails user) {
		log.info("get");
		model.addAttribute("writeFormDto", new WriteFormDto());
		model.addAttribute("member", memberService.findById(user.getUsername()));

		return "/admin/writeForm";
	}

	/* 공지사항 쓰기 */
	@PostMapping("/notice/post")
	public String writeSubmit(WriteFormDto writeFormDto, HttpSession session,
			@AuthenticationPrincipal UserDetails user) {
		log.info("controller 실행");

		Member member = memberService.findById(user.getUsername());

		Post post = Post.createNotice(writeFormDto, member);
		Post result = postService.write(post);

		if (!writeFormDto.getOriginName().isEmpty()) {
			attachedFileService.postFileUpload(writeFormDto.getOriginName(), result, session, user.getUsername());
		}

		return "redirect:/admin/notice/list";
	}

	/* 공지사항 수정 폼 받기 */
	@GetMapping("/notice/put")
	public String getUpdate(Long num, Model model, Pageable pageable, @AuthenticationPrincipal UserDetails user) {
		model.addAttribute("member", memberService.findById(user.getUsername()));

		Post getUpdate = postService.postView(num);

		WriteFormDto updateDto = new WriteFormDto(num, getUpdate.getTitle(), getUpdate.getContent());
		List<AttachedFile> files = attachedFileService.fileView(getUpdate, pageable);

		model.addAttribute("files", files);
		model.addAttribute("updateDto", updateDto);

		return "/admin/updateForm";
	}

	/* 공지사항 수정 */
	@PostMapping("/notice/put")
	public String updatePorc(WriteFormDto updateDto, HttpSession session, @AuthenticationPrincipal UserDetails user) {

		postService.updatePost(updateDto.getPostNum(), updateDto);

		Post updatedPost = postService.findOne(updateDto.getPostNum());
		log.info("updateDto.getOriginName() size!!!!!!!!!!!!!!!!!!!!!!!!!: " + updateDto.getOriginName().size());
		log.info("updateDto.getOriginName()!!!!!!!!!!!!!!!!!!!!!!!!!: " + updateDto.getOriginName());
		if (!updateDto.getOriginName().isEmpty()) {
			attachedFileService.postFileUpload(updateDto.getOriginName(), updatedPost, session, user.getUsername());
		}

		return "redirect:/admin/notice/list";
	}

	/* 공지사항 수정하면서 파일 하나 삭제 */
	@PostMapping("/notice/put/filedelete")
	@ResponseBody
	public JSONObject oneFileDelete(Long num, Long postNum) {
		log.info("!!!!!!! file num : " + num);

		postService.deleteOneFile(num);

		JSONObject jObj = new JSONObject();

		List<AttachedFile> files = attachedFileService.fileDeleteAfterList(postNum);

		jObj.put("files", files);

		return jObj;
	}

	/* 공지사항 삭제 */
	@RequestMapping("/notice/delete")
	public String postDelete(Long num) {
		postService.deletePostwithReply(num);
		postService.deletePostwithFile(num);
		postService.deletePost(num);

		return "redirect:/admin/notice/list";
	}

	/* 공지사항 댓글 등록 */
	@PostMapping("/notice/reply")
	@ResponseBody
	public JSONObject replyWrite(String memberId, Long postNum, String content) {
		log.info(memberId, postNum, content);
		Reply newReply = replyService.replyWrite(memberId, postNum, content);

		JSONObject jObj = new JSONObject();

		jObj.put("replyNum", newReply.getNum());
		jObj.put("id", newReply.getMember().getId());
		jObj.put("name", newReply.getMember().getName());
		jObj.put("repDate", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(newReply.getRepDate()));
		jObj.put("repContent", newReply.getContent());
		jObj.put("postNum", newReply.getPost().getNum());
		jObj.put("profileImg", newReply.getMember().getProfileImg());

		return jObj;
	}

	/* 공지사항 대댓글 등록 */
	@PostMapping("/notice/replyreply")
	@ResponseBody
	public JSONObject replyReplyWrite(String memberId, Long postNum, Long parentRepNum, String content) {
		log.info(memberId, postNum, parentRepNum, content);
		Reply newReplyReply = replyService.replyReplyWrite(memberId, postNum, parentRepNum, content);

		JSONObject jObj = new JSONObject();

		jObj.put("replyNum", newReplyReply.getNum());
		jObj.put("parentRepNum", newReplyReply.getParentRepNum());
		jObj.put("id", newReplyReply.getMember().getId());
		jObj.put("name", newReplyReply.getMember().getName());
		jObj.put("repDate", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(newReplyReply.getRepDate()));
		jObj.put("repContent", newReplyReply.getContent());
		jObj.put("profileImg", newReplyReply.getMember().getProfileImg());

		return jObj;

	}

	/* 공지사항 댓글 삭제 */
	@PostMapping("/notice/deletereply")
	public String replyDelete(Long num) {
		log.info("컨트롤러 실행 ");

		replyService.replyDelete(num);

		return "redirect:/admin/notice/list";
	}

	// 공개 게시판 관리
	/* 공개 게시판 목록 */
	@GetMapping("/open/list")
	public String showOpenCommunityList(
			@PageableDefault(page = 0, size = 5, direction = Sort.Direction.DESC) Pageable pageable,
			@ModelAttribute("postSearchDto") PostSearchDto postSearchDto, @AuthenticationPrincipal UserDetails user,
			Model model, Long num) {
		Page<Post> opens = null;
		String type = postSearchDto.getType();
		String keyword = postSearchDto.getKeyword();
		Member mb = memberService.findById(user.getUsername());
		model.addAttribute("member", mb);
		model.addAttribute("keyword", keyword);
		model.addAttribute("type", type);

		if (keyword == null || type == null || keyword.isEmpty() || type.isEmpty()) {
			opens = postService.list(pageable);
		} else {
			opens = postService.searchList(postSearchDto, pageable); // 새로운 서비스의 메서드 사용할 예정
		}

		Long size = opens.getTotalElements();
		int nowPage = opens.getPageable().getPageNumber() + 1;
		int startPage = Math.max(nowPage - 2, 1);
		int endPage = 0;
		if (startPage == 1) {
			if (opens.getTotalPages() < 5) {
				endPage = opens.getTotalPages();
			} else {
				endPage = 5;
			}
		} else {
			endPage = Math.min(nowPage + 2, opens.getTotalPages());
		}

		if (endPage == opens.getTotalPages() && (endPage - startPage) < 5) {
			startPage = (endPage - 4 <= 0) ? 1 : endPage - 4;
		}

		model.addAttribute("list", opens);
		model.addAttribute("nowPage", nowPage);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("size", size);
		long count = notificationRepository.countByMemberAndChecked(mb, false);
		model.addAttribute("hasNotification", count > 0);

		return "/admin/openList";
	}

	/* 공개 게시판 상세보기 */
	@GetMapping("/open/get")
	public String postOpenView(Model model, Long num, Pageable pageable, HttpSession session,
			@AuthenticationPrincipal UserDetails user) {

		log.info("num :" + num);
		Post viewPost = postService.postView(num);

		postService.viewCntUp(num);

		model.addAttribute("postView", viewPost);

		List<AttachedFile> files = attachedFileService.fileView(viewPost, pageable);
		model.addAttribute("files", files);

		List<ReplyDto> replyList = replyService.replyList(num);

		model.addAttribute("replyList", replyList);
		model.addAttribute("member", memberService.findById(user.getUsername()));

		return "/admin/openView";
	}

	/* 공개게시판 글 내리기 */
	@RequestMapping("/open/delete")
	public String deleteOpenPost(Long num) {
		List<Reply> replies = replyService.getReplyByPostNum(num);
		for (Reply r : replies) {
			reportService.deleteReportWithReply(r.getNum()); // 게시글의 댓글에 대한 신고 삭제
		}

		reportService.deleteReportWithPost(num); // 게시글에 대한 신고삭제
		postService.deletePostwithReply(num); // 게시글의 댓글 삭제
		postService.deletePostwithFile(num); // 게시글의 첨부파일 삭제
		postService.deletePost(num); // 게시글 삭제

		return "redirect:list";
	}

	/* 공개게시판 댓글쓰기 */
	@PostMapping("/open/reply")
	@ResponseBody
	public JSONObject writeOpenReply(String memberId, Long postNum, String content) {
		log.info(memberId, postNum, content);
		Reply newReply = replyService.replyWrite(memberId, postNum, content);

		JSONObject jObj = new JSONObject();

		jObj.put("replyNum", newReply.getNum());
		jObj.put("id", newReply.getMember().getId());
		jObj.put("name", newReply.getMember().getName());
		jObj.put("repDate", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(newReply.getRepDate()));
		jObj.put("repContent", newReply.getContent());
		jObj.put("postNum", newReply.getPost().getNum());
		jObj.put("profileImg", newReply.getMember().getProfileImg());
		return jObj;

	}

	/* 공개게시판 답글쓰기 */
	@PostMapping("/open/replyreply")
	@ResponseBody
	public JSONObject writeOpenChildReply(String memberId, Long postNum, Long parentRepNum, String content) {
		log.info(memberId, postNum, parentRepNum, content);
		Reply newReplyReply = replyService.replyReplyWrite(memberId, postNum, parentRepNum, content);

		JSONObject jObj = new JSONObject();

		jObj.put("replyNum", newReplyReply.getNum());
		jObj.put("parentRepNu", newReplyReply.getParentRepNum());
		jObj.put("id", newReplyReply.getMember().getId());
		jObj.put("name", newReplyReply.getMember().getName());
		jObj.put("repDate", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(newReplyReply.getRepDate()));
		jObj.put("repContent", newReplyReply.getContent());
		jObj.put("profileImg", newReplyReply.getMember().getProfileImg());

		return jObj;

	}

	/* 공개게시판 댓글삭제하기 */
	@PostMapping("/open/deletereply")
	public String deleteReply(Long num) {
		log.info("컨트롤러 실행 ");

		replyService.replyDelete(num);

		return "redirect:list";
	}

	/* 커뮤니티 목록 보기 */
	@GetMapping("/community/list")
	public String showCommunityList(
			@PageableDefault(page = 0, size = 5, direction = Sort.Direction.DESC) Pageable pageable,
			@ModelAttribute("postSearchDto") PostSearchDto postSearchDto, @AuthenticationPrincipal UserDetails user,
			Model model, Long num) {
		Page<Community> communities = null;
		String type = postSearchDto.getType();
		String keyword = postSearchDto.getKeyword();
		Member mb = memberService.findById(user.getUsername());
		model.addAttribute("member", mb);
		model.addAttribute("keyword", keyword);
		model.addAttribute("type", type);

		if (keyword == null || type == null || keyword.isEmpty() || type.isEmpty()) {
			communities = communityService.showCommunityList(pageable);
		} else {
			communities = communityService.searchCommunityList(postSearchDto, pageable); // 새로운 서비스의 메서드 사용할 예정
		}

		Long size = communities.getTotalElements();
		int nowPage = communities.getPageable().getPageNumber() + 1;
		int startPage = Math.max(nowPage - 2, 1);
		int endPage = 0;
		if (startPage == 1) {
			if (communities.getTotalPages() < 5) {
				endPage = communities.getTotalPages();
			} else {
				endPage = 5;
			}
		} else {
			endPage = Math.min(nowPage + 2, communities.getTotalPages());
		}

		if (endPage == communities.getTotalPages() && (endPage - startPage) < 5) {
			startPage = (endPage - 4 <= 0) ? 1 : endPage - 4;
		}

		model.addAttribute("list", communities);
		model.addAttribute("nowPage", nowPage);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("size", size);
		long count = notificationRepository.countByMemberAndChecked(mb, false);
		model.addAttribute("hasNotification", count > 0);

		return "/admin/communityList";
	}

	/* 커뮤니티 상세정보 보기 */
	// 게시글 리스트 가져오기
	@GetMapping("community/{comNum}/list")
	public String list(@PathVariable Long comNum, Model model,
			@ModelAttribute("postSearchDto") PostSearchDto postSearchDto, @AuthenticationPrincipal UserDetails user,
			@PageableDefault(page = 0, size = 5, direction = Sort.Direction.DESC) Pageable pageable) {

		Community community = communityService.findByNum(comNum);
		String comName = community.getName();
		String comDescription = community.getDescription();
		String creatorName = community.getMember().getName();
		String creatorId = community.getMember().getId();
		String memberListToStr = community.getMemberList();

		model.addAttribute("community", community);

		String[] memIdList = null;
		if (memberListToStr != null && !memberListToStr.isEmpty()) {
			memIdList = memberListToStr.split(",");
		}

		Map<String, String> memberMap = new HashMap<String, String>();
		for (String id : memIdList) {
			memberMap.put(id, memberService.findById(id).getName());
		}
		model.addAttribute("memberMap", memberMap);

		String keyword = postSearchDto.getKeyword();
		Long size = 0L;
		Page<Post> list = null;

		if (keyword == null) {
			list = postService.comList(comNum, pageable);
			size = list.getTotalElements();
		} else {
			list = postService.comSearchList(comNum, postSearchDto, pageable);
			size = list.getTotalElements();
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

		model.addAttribute("list", list);
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

		return "admin/communityView";
	}

	// 커뮤니티 삭제하기
	@PostMapping("/community/delete")
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

		return "redirect:/AliceDiary/admin/community/list";

	}
}
