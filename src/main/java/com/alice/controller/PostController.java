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
import com.alice.project.service.ReportService;
import com.alice.project.web.PostSearchDto;
import com.alice.project.web.ReplyDto;
import com.alice.project.web.WriteFormDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;
	private final AttachedFileService attachedFileService;
	private final MemberService memberService;
	private final ReplyService replyService;
	private final ReportService reportService;
	private final NotificationRepository notificationRepository;

	// 글쓰기
	@GetMapping("/open/post")
	public String writeform(Model model, @AuthenticationPrincipal UserDetails user) {

		model.addAttribute("writeFormDto", new WriteFormDto());
		model.addAttribute("member", memberService.findById(user.getUsername()));
		return "community/writeForm";
	}

	// 글쓰기
	@PostMapping("/open/post")
	public String writeSubmit(WriteFormDto writeFormDto, HttpSession session,
			@AuthenticationPrincipal UserDetails user) {

		Member member = memberService.findById(user.getUsername());

		Post post = Post.createPost(writeFormDto, member);
		Post writedPost = postService.write(post);

		attachedFileService.postFileUpload(writeFormDto.getOriginName(), writedPost, session, user.getUsername());

		return "redirect:./list";
	}

	// 게시글 리스트 가져오기
	@GetMapping("/open/list")
	public String list(Model model, @ModelAttribute("postSearchDto") PostSearchDto postSearchDto,
			@AuthenticationPrincipal UserDetails user,
			@PageableDefault(page = 0, size = 5, direction = Sort.Direction.DESC) Pageable pageable) {

		String keyword = postSearchDto.getKeyword();
		Long size = 0L;
		Page<Post> list = null;
		List<Long> countReply = new ArrayList<Long>();

		if (keyword == null) {
			list = postService.list(pageable);
			size = list.getTotalElements();
			for (Post p : list) {
				Long cnttmp = 0L;
				cnttmp = replyService.getCountReply(p.getNum());
				countReply.add(cnttmp);
			}

		} else {
			list = postService.searchList(postSearchDto, pageable);
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

		model.addAttribute("countReply", countReply);
		model.addAttribute("list", list);
		model.addAttribute("size", size);
		model.addAttribute("nowPage", nowPage);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("keyword", keyword);
		model.addAttribute("type", postSearchDto.getType());
		Member mb = memberService.findById(user.getUsername());
		model.addAttribute("member", mb);
      		 long count = notificationRepository.countByMemberAndChecked(mb, false);
        		model.addAttribute("hasNotification", count > 0);

		return "community/list";
	}

	// 게시글 상세보기
	@GetMapping("/open/get")
	public String postView(Model model, Long num, Pageable pageable, HttpSession session,
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

		return "community/postView";
	}

	// get 게시글 수정하기 첨부파일도 수정
	@GetMapping("/open/put")
	public String getUpdate(Long num, Model model, Pageable pageable, @AuthenticationPrincipal UserDetails user) {
		log.info("수정컨트롤러 get");

		Post getUpdate = postService.postView(num);

		WriteFormDto updateDto = new WriteFormDto(num, getUpdate.getTitle(), getUpdate.getContent());
		List<AttachedFile> files = attachedFileService.fileView(getUpdate, pageable);

		model.addAttribute("files", files);
		model.addAttribute("updateDto", updateDto);
		model.addAttribute("member", memberService.findById(user.getUsername()));
		return "community/updateForm";
	}

	// post 게시글 수정하기 첨부파일도 수정
	@PostMapping("/open/put")
	public String updatePorc(WriteFormDto updateDto, HttpSession session, @AuthenticationPrincipal UserDetails user) {

		String postNum = Long.toString(updateDto.getPostNum());

		postService.updatePost(updateDto.getPostNum(), updateDto);

		Post updatedPost = postService.findOne(updateDto.getPostNum());

		attachedFileService.postFileUpload(updateDto.getOriginName(), updatedPost, session, user.getUsername());

		return "redirect:/open/get?num=" + postNum;
	}

	// 게시글 수정에서 파일하나 삭제하기
	@PostMapping("/open/put/filedelete")
	@ResponseBody
	public JSONObject oneFileDelete(Long num, Long postNum) {

		postService.deleteOneFile(num);

		JSONObject jObj = new JSONObject();

		List<AttachedFile> files = attachedFileService.fileDeleteAfterList(postNum);

		jObj.put("files", files);

		return jObj;
	}

	// 공개게시글 삭제하기
	@RequestMapping("/open/delete")
	public String postDelete(Long num) {
		log.info("컨트롤러 실행 num:" + num);
		List<Reply> replies = replyService.getReplyByPostNum(num);

		for (Reply r : replies) {
			reportService.deleteReportWithReply(r.getNum()); // 게시글의 댓글에 대한 신고 삭제
		}

		reportService.deleteReportWithPost(num); // 게시글에 대한 신고삭제
		postService.deletePostwithReply(num);
		postService.deletePostwithFile(num);
		postService.deletePost(num);

		return "redirect:list";
	}

}