package com.alice.project.controller;

import javax.servlet.http.HttpSession;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.alice.project.config.PrincipalDetails;
import com.alice.project.domain.Member;
import com.alice.project.domain.Suggestion;
import com.alice.project.service.MemberService;
import com.alice.project.service.ReportService;
import com.alice.project.service.SuggestionService;
import com.alice.project.web.SuggestionDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LeaveController {

	private final MemberService memberService;
	private final SuggestionService suggestionService;
	private final ReportService reportService;

	@GetMapping(value = "/member/{id}/leave" )
	public String memberLeave(@PathVariable String id, Model model, @AuthenticationPrincipal PrincipalDetails user) {
		log.info("GET 진입");
		Member member = memberService.findById(user.getUsername());
		model.addAttribute("member", member);
		model.addAttribute("suggestionDto", new SuggestionDto());
		return "/leave/memberLeave";
	}

	@PostMapping(value = "/member/{id}/leave")
	public String memberLeave(@PathVariable String id, SuggestionDto suggestionDto, Model model,
			HttpSession session) {
		log.info("LEAVE POST 진입");
		Member member = memberService.findById(id);
			Suggestion suggestion = Suggestion.createSuggestion(suggestionDto, member);
			suggestionService.saveSuggest(suggestion); // 건의 사항 insert
			member = Member.changeMemberOut(member); // sataus USER_OUT으로 바꾸기
			Member.changeMemberOutName(member); // 닉네임 (알수없음)으로 수정
//			reportService.deleteReportWithOutMember(member.getNum()); // 관련된 신고하기 게시물 다 지우기
			memberService.saveMember(member); // update

			log.info("member status == " + member.getStatus());
			log.info("member id == " + member.getId());
		return "redirect:/logout";
	}
}
