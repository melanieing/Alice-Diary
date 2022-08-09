package com.alice.project.controller;

import org.modelmapper.internal.util.Members;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alice.project.domain.Member;
import com.alice.project.domain.Report;
import com.alice.project.domain.ReportType;
import com.alice.project.domain.Suggestion;
import com.alice.project.repository.NotificationRepository;
import com.alice.project.service.AttachedFileService;
import com.alice.project.service.MemberService;
import com.alice.project.service.PostService;
import com.alice.project.service.ReplyService;
import com.alice.project.service.ReportService;
import com.alice.project.service.SuggestionService;
import com.alice.project.web.SearchDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

	private final MemberService memberService;
	private final ReportService reportService;
	private final ReplyService replyService;
	private final SuggestionService suggestionService;
	private final PostService postService;
	private final AttachedFileService attachedFileService;
	private final NotificationRepository notificationRepository;

	// 회원관리
	/* 회원 목록 */
	@GetMapping(value = "/member")
	public String showMemberList(
			@PageableDefault(page = 0, size = 5, sort = "num", direction = Sort.Direction.DESC) Pageable pageable,
			@ModelAttribute("searchDto") SearchDto searchDto, @AuthenticationPrincipal UserDetails user, Model model,
			Long num) {
		String type = searchDto.getType();
		String keyword = searchDto.getKeyword();
		Member mb = memberService.findById(user.getUsername());
		model.addAttribute("member", mb);

		Page<Member> members = null;

		if (keyword == null || type == null || keyword.isEmpty() || type.isEmpty()) {
			members = memberService.getMemberList(pageable);
		} else {
			if (type.equals("status")) {
				members = memberService.searchMemberByStatus(keyword, pageable);
			} else {
				members = memberService.searchMember(type, keyword, pageable);
			}
			model.addAttribute("keyword", keyword);
			model.addAttribute("type", type);
		}

		Long size = members.getTotalElements();
		int nowPage = members.getPageable().getPageNumber() + 1;
		int startPage = Math.max(nowPage - 2, 1);
		int endPage = 0;
		if (startPage == 1) {
			if (members.getTotalPages() < 5) {
				endPage = members.getTotalPages();
			} else {
				endPage = 5;
			}
		} else {
			endPage = Math.min(nowPage + 2, members.getTotalPages());
		}

		if (endPage == members.getTotalPages() && (endPage - startPage) < 5) {
			startPage = (endPage - 4 <= 0) ? 1 : endPage - 4;
		}

		model.addAttribute("members", members);
		model.addAttribute("nowPage", nowPage);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("size", size);
		long count = notificationRepository.countByMemberAndChecked(mb, false);
		model.addAttribute("hasNotification", count > 0);
		return "/admin/memberList";
	}

	/* 회원 정보 상세보기 */
	@GetMapping(value = "/member/{id}")
	public String showMemberOne(@PathVariable("id") String id, Model model, @AuthenticationPrincipal UserDetails user) {
		Member userInfo = memberService.findById(id);
		model.addAttribute("member", memberService.findById(user.getUsername()));
		model.addAttribute("person", userInfo);
		return "/admin/memberDetail";
	}

	/* 회원 내보내기 */
	@DeleteMapping(value = "/member/{num}")
	@ResponseBody
	public int changeMemberOut(@PathVariable("num") Long num, Model model) {
		int result = memberService.deleteOne(num);
		log.info("**********result : " + result);
		return result;
	}

	/* 회원 복구하기 */
	@PatchMapping(value = "/member/{num}")
	@ResponseBody
	public int changeMemberIn(@PathVariable("num") Long num, Model model) {
		int result = memberService.returnOne(num);
		log.info("**********result : " + result);
		return result;
	}

	// 신고관리
	/* 신고 목록 */
	@GetMapping(value = "/reports")
	public String showReportList(
			@PageableDefault(page = 0, size = 5, sort = "num", direction = Sort.Direction.DESC) Pageable pageable,
			@ModelAttribute("searchDto") SearchDto searchDto, Model model, Long num,
			@AuthenticationPrincipal UserDetails user) {
		Page<Report> reports = reportService.findReports(pageable);
		Member mb = memberService.findById(user.getUsername());
		model.addAttribute("member", mb);

		String type = searchDto.getType();
		String keyword = searchDto.getKeyword();

		if (keyword == null || type == null || keyword.isEmpty() || type.isEmpty()) {
			reports = reportService.findReports(pageable);
		} else {
			model.addAttribute("keyword", searchDto.getKeyword());
			model.addAttribute("type", searchDto.getType());
			if (type.equals("reporterName") || type.equals("content")) {
				reports = reportService.searchReport(searchDto, pageable);
			} else if (type.equals("targetName")) { // 신고 대상 닉네임
				reports = reportService.searchReportByTargetName(searchDto, pageable);
			} else if (type.equals("reportReason")) {
				reports = reportService.searchReportByReportReason(searchDto, pageable);
			} else if (type.equals("reportType")) {
				reports = reportService.searchReportByReportType(searchDto, pageable);
			}
		}

		Long size = reports.getTotalElements();
		int nowPage = reports.getPageable().getPageNumber() + 1;
		int startPage = Math.max(nowPage - 2, 1);
		int endPage = 0;
		if (startPage == 1) {
			if (reports.getTotalPages() < 5) {
				endPage = reports.getTotalPages();
			} else {
				endPage = 5;
			}
		} else {
			endPage = Math.min(nowPage + 2, reports.getTotalPages());
		}

		if (endPage == reports.getTotalPages() && (endPage - startPage) < 5) {
			startPage = (endPage - 4 <= 0) ? 1 : endPage - 4;
		}

		model.addAttribute("reports", reports);
		model.addAttribute("nowPage", nowPage);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("size", size);

		long count = notificationRepository.countByMemberAndChecked(mb, false);
		model.addAttribute("hasNotification", count > 0);

		return "/admin/reportList";
	}

	/* 신고 상세보기 */
	@GetMapping(value = "/reports/{reportNum}")
	public String viewReport(@PathVariable Long reportNum, Model model, Long num,
			@AuthenticationPrincipal UserDetails user) {
		model.addAttribute("member", memberService.findById(user.getUsername()));
		Report report = reportService.findReport(reportNum);
		model.addAttribute("reporter", report.getMember());
		model.addAttribute("target", (report.getReportType() == ReportType.POST) ? report.getPost().getMember()
				: report.getReply().getMember());
		model.addAttribute("report", report);
		log.info("신고자 닉네임 : " + report.getMember().getName());

		return "/admin/reportView";
	}

	// 건의관리
	/* 건의 목록 */
	@GetMapping(value = "/suggestion")
	public String showSuggestionList(
			@PageableDefault(page = 0, size = 10, sort = "num", direction = Sort.Direction.DESC) Pageable pageable,
			@ModelAttribute("searchDto") SearchDto searchDto, Model model, Long num,
			@AuthenticationPrincipal UserDetails user) {
		Page<Suggestion> suggestions = null;
		Member mb = memberService.findById(user.getUsername());
		model.addAttribute("member", mb);
		String type = searchDto.getType();
		String keyword = searchDto.getKeyword();

		if (keyword == null || type == null || keyword.isEmpty() || type.isEmpty()) {
			suggestions = suggestionService.getSuggestionList(pageable);
		} else {
			model.addAttribute("keyword", keyword);
			model.addAttribute("type", type);
			suggestions = suggestionService.searchSuggestion(searchDto, pageable);
		}

		Long size = suggestions.getTotalElements();
		int nowPage = suggestions.getPageable().getPageNumber() + 1;
		int startPage = Math.max(nowPage - 2, 1);
		int endPage = 0;
		if (startPage == 1) {
			if (suggestions.getTotalPages() < 5) {
				endPage = suggestions.getTotalPages();
			} else {
				endPage = 5;
			}
		} else {
			endPage = Math.min(nowPage + 2, suggestions.getTotalPages());
		}

		if (endPage == suggestions.getTotalPages() && (endPage - startPage) < 5) {
			startPage = (endPage - 4 <= 0) ? 1 : endPage - 4;
		}

		model.addAttribute("suggestions", suggestions);
		model.addAttribute("nowPage", nowPage);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("size", size);
		long count = notificationRepository.countByMemberAndChecked(mb, false);
		model.addAttribute("hasNotification", count > 0);

		return "/admin/suggestionList";
	}

}
