package com.alice.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alice.project.domain.Member;
import com.alice.project.domain.Post;
import com.alice.project.domain.Reply;
import com.alice.project.domain.Report;
import com.alice.project.repository.ReplyRepository;
import com.alice.project.service.MemberService;
import com.alice.project.service.PostService;
import com.alice.project.service.ReportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ReportController {

	private final MemberService memberService;

	private final ReportService reportService;

	private final PostService postService;

	private final ReplyRepository replyRepository;

	// 게시글신고하기
	@PostMapping("open/reportpost")
	@ResponseBody
	public boolean reportPost(String userId, Long postNum, String reportReason, String content) {

		Member member2 = memberService.findById(userId);
		Post post = postService.findByNum(postNum);

		reportService.postReport(Report.createPostReport(post, reportReason, content, member2));
		memberService.reportCntUp(post.getMember().getNum());

		return true;
	}

	// 게시글 신고유무 판단
	@PostMapping("open/postreportcheck")
	@ResponseBody
	public int postReportcheck(Long postNum, String userId) {

		return reportService.postReportcheck(postNum, userId).size();
	}

	// 댓글신고하기
	@PostMapping("open/reportreply")
	@ResponseBody
	public boolean reportReply(Long replyNum, String userId, String reportReason, String content) {
		log.info("replyNum: " + replyNum);
		Member member2 = memberService.findById(userId);

		Reply reply = replyRepository.findByNum(replyNum);

		reportService.replyReport(Report.createReplyReport(reply, reportReason, content, member2));
		memberService.reportCntUp(reply.getMember().getNum());

		return true;
	}

	// 댓글 신고유무 판단
	@PostMapping("open/replyreportcheck")
	@ResponseBody
	public int replyReportcheck(Long replyNum, String userId) {

		return reportService.replyReportcheck(replyNum, userId).size();
	}
}