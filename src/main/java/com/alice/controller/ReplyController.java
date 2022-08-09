package com.alice.project.controller;

import java.time.format.DateTimeFormatter;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alice.project.domain.Reply;
import com.alice.project.service.ReplyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ReplyController {

	private final ReplyService replyService;

	// 댓글쓰기
	@PostMapping("/open/reply")
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

	// 대댓쓰기
	@PostMapping("/open/replyreply")
	@ResponseBody
	public JSONObject replyReplyWrite(String memberId, Long postNum, Long parentRepNum, String content) {
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

	// 댓글 삭제하기
	@PostMapping("/open/deletereply")
	public String replyDelete(Long num) {
		log.info("컨트롤러 실행 ");

		replyService.replyDelete(num);

		return "redirect:list";
	}

}