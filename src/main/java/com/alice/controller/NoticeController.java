package com.alice.project.controller;

import java.time.format.DateTimeFormatter;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alice.project.domain.AttachedFile;
import com.alice.project.domain.Member;
import com.alice.project.domain.Post;
import com.alice.project.domain.Reply;
import com.alice.project.repository.NotificationRepository;
import com.alice.project.service.AttachedFileService;
import com.alice.project.service.MemberService;
import com.alice.project.service.PostService;
import com.alice.project.service.ReplyService;
import com.alice.project.web.PostSearchDto;
import com.alice.project.web.ReplyDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {

	private final PostService postService;
	private final MemberService memberService;
	private final AttachedFileService attachedFileService;
	private final ReplyService replyService;
	private final NotificationRepository notificationRepository;

	/* 공지사항 목록 */
	@GetMapping("/list")
	public String showNoticeList(
			@PageableDefault(page = 0, size = 5, direction = Sort.Direction.DESC) Pageable pageable,
			@ModelAttribute("postSearchDto") PostSearchDto postSearchDto, 
			@AuthenticationPrincipal UserDetails user,
			Model model, Long num) {
		Page<Post> notices = null;
		String type = postSearchDto.getType();
		String keyword = postSearchDto.getKeyword();
		Member mb = memberService.findById(user.getUsername());
		model.addAttribute("member", mb);
		model.addAttribute("type", type);
		model.addAttribute("keyword", keyword);

		List<Long> countReply = new ArrayList<Long>();
		
		if (keyword==null || type==null || keyword.isEmpty() || type.isEmpty()) {
			notices = postService.notceList(pageable);
			for (Post p : notices) {
				Long cnttmp = 0L;
				cnttmp = replyService.getCountReply(p.getNum());
				countReply.add(cnttmp);
			}
		} else {
			notices = postService.searchNoticeList(postSearchDto, pageable); // 새로운 서비스의 메서드 사용할 예정
			for (Post p : notices) {
				Long cnttmp = 0L;
				cnttmp = replyService.getCountReply(p.getNum());
				countReply.add(cnttmp);
			}
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

		model.addAttribute("countReply", countReply);
		model.addAttribute("notices", notices);
		model.addAttribute("nowPage", nowPage);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("size", size);
        long count = notificationRepository.countByMemberAndChecked(mb, false);
        model.addAttribute("hasNotification", count > 0);

		return "/notice/noticeList";
	}

	/* 공지사항 상세보기 */
	@GetMapping("/get")
	public String postNoticeView(Model model, Long num, Pageable pageable, HttpSession session,
			@AuthenticationPrincipal UserDetails user) {
		model.addAttribute("member", memberService.findById(user.getUsername()));

		log.info("num :" + num);
		Post viewPost = postService.postView(num);

		postService.viewCntUp(num);

		model.addAttribute("postView", viewPost);

		List<AttachedFile> files = attachedFileService.fileView(viewPost, pageable);
		model.addAttribute("files", files);

		List<ReplyDto> replyList = replyService.replyList(num);

		model.addAttribute("replyList", replyList);

		return "/notice/noticeView";
	}

	/* 공지사항 댓글 등록 */
	@PostMapping("/reply")
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
	@PostMapping("/replyreply")
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
	@PostMapping("/deletereply")
	public String replyDelete(Long num) {
		log.info("컨트롤러 실행 ");

		replyService.replyDelete(num);

		return "redirect:/notice/list";
	}

}
