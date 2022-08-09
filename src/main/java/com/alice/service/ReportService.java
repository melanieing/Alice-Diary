package com.alice.project.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alice.project.domain.Member;
import com.alice.project.domain.Post;
import com.alice.project.domain.Reply;
import com.alice.project.domain.Report;
import com.alice.project.repository.MemberRepository;
import com.alice.project.repository.PostRepository;
import com.alice.project.repository.ReplyRepository;
import com.alice.project.repository.ReportRepository;
import com.alice.project.web.SearchDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional(readOnly = true) // 기본적으로 못바꾸게 해놓고
@RequiredArgsConstructor // final 붙은 필드만 가진 생성자 만들어줌
@Slf4j
public class ReportService {

	private final MemberService ms;
	private final ReportRepository reportRepository;
	private final MemberRepository memberRepository;
	private final ReplyRepository replyRepository;
	private final PostRepository postRepository;

	// 게시글 신고하기
	@Transactional
	public Report postReport(Report report) {
		return reportRepository.save(report);
	}

	// 댓글 신고하기
	@Transactional
	public Report replyReport(Report report) {
		return reportRepository.save(report);
	}

	// 게시글 신고유무판단
	public List<Report> postReportcheck(Long postNum, String userId) {
		Member member = memberRepository.findById(userId);
		Post post = postRepository.findByNum(postNum);
		return reportRepository.findPostReportExist(post, member);
	}

	// 댓글신고유무판단
	public List<Report> replyReportcheck(Long replyNum, String userId) {
		Member member = memberRepository.findById(userId);
		Reply reply = replyRepository.findByNum(replyNum);

		return reportRepository.findReplyReportExist(reply, member);
	}

	/* 신고 목록 반환 */
	public Page<Report> findReports(Pageable pageable) {
		return reportRepository.findAll(pageable);
	}

	/* 신고 객체 하나 반환 */
	public Report findReport(Long reportNum) {
		return reportRepository.getReport(reportNum);
	}

	/* 신고 검색 기능 */
	public Page<Report> searchReport(SearchDto sdto, Pageable pageable) {
		Page<Report> reportList = null;

		String type = sdto.getType();
		String keyword = sdto.getKeyword();
		List<Report> reports = new ArrayList<>();
		if (type.equals("reporterName")) { // 신고자 검색 (신고자 아이디)
			List<Member> memberlist = memberRepository.findByNameContaining(keyword);
			if (memberlist != null) {
				for (Member m : memberlist) {
					List<Report> results = reportRepository.searchByReporterId(m.getNum());
					reports.addAll(results);
				}
			}
			final int start = (int) pageable.getOffset();
			final int end = Math.min((start + pageable.getPageSize()), reports.size());
			reportList = new PageImpl<>(reports.subList(start, end), pageable, reports.size());
		} else if (type.equals("content")) { // 신고내용 검색
			reportList = reportRepository.findByContentContaining(keyword, pageable);
		}
		return reportList;
	}

	public Page<Report> searchReportByTargetName(SearchDto sdto, Pageable pageable) {
		String type = sdto.getType(); // targetName (신고대상자 닉네임)
		String keyword = sdto.getKeyword();

		Page<Report> reportList = reportRepository.findAll(pageable);
		List<Report> list = new ArrayList<>();
		log.info("reportList.getSize()  : " + reportList.getSize());
		if (reportList != null) {
			for (Report report : reportList) {
				log.info("!!!!!!!!!!!!report.getReply().getId() : " + report.getReply().getMember().getId());
				if (report.getReportType().toString().equals("POST")) {
					log.info("!!!!!!!!!!!!!!!!!!postid : " + report.getPost().getMember().getId());
					if (report.getPost().getMember().getName().contains(keyword)) {
						list.add(report);
					}
				} else if (report.getReportType().toString().equals("REPLY")) {
					log.info("!!!!!!!!!!!!!!!!!!replyid : " + report.getReply().getMember().getId());
					if (report.getReply().getMember().getName().contains(keyword)) {
						list.add(report);
					}
				}
			}
		}
		final int start = (int) pageable.getOffset();
		final int end = Math.min((start + pageable.getPageSize()), list.size());
		reportList = new PageImpl<>(list.subList(start, end), pageable, list.size());

		log.info("!!!!!!!!!!!!!reportList.getTotalElements : " + reportList.getTotalElements());
		return reportList;
	}

	public Page<Report> searchReportByReportReason(SearchDto sdto, Pageable pageable) {
		String keyword = sdto.getKeyword();

		Page<Report> reportList = reportRepository.findAll(pageable);
		List<Report> list = new ArrayList<>();
		for (Report report : reportList) {
			if (report.getReportReason().toString().contains(keyword)) {
				list.add(report);
			}
		}
		final int start = (int) pageable.getOffset();
		final int end = Math.min((start + pageable.getPageSize()), list.size());
		reportList = new PageImpl<>(list.subList(start, end), pageable, list.size());

		return reportList;
	}

	public Page<Report> searchReportByReportType(SearchDto sdto, Pageable pageable) {
		String keyword = sdto.getKeyword();

		Page<Report> reportList = reportRepository.findAll(pageable);
		List<Report> list = new ArrayList<>();
		for (Report report : reportList) {
			if (report.getReportType().toString().contains(keyword)) {
				list.add(report);
			}
		}
		final int start = (int) pageable.getOffset();
		final int end = Math.min((start + pageable.getPageSize()), list.size());
		reportList = new PageImpl<>(list.subList(start, end), pageable, list.size());

		return reportList;
	}

	@Transactional
	public void deleteReportWithReply(Long replyNum) {
		reportRepository.deleteByReplyNum(replyNum);
	}

	@Transactional
	public void deleteReportWithPost(Long postNum) {
		reportRepository.deleteByPostNum(postNum);
	}
//	
//	@Transactional
//	public void deleteReportWithOutMember(Long memNum) {
//		// 탈퇴회원이 쓴 댓글에 대한 신고 지우기
//		List<Reply> replies = replyRepository.selectByMemberNum(memNum);
//		for (Reply r : replies) {
//			reportRepository.deleteByPostNum(r.getNum());
//		}	
//		
//		// 탈퇴회원이 쓴 게시물에 대한 신고 지우기 
//		List<Post> posts = postRepository.selectByMemberNum(memNum);
//		for (Post p : posts) { 
//			reportRepository.deleteByPostNum(p.getNum());
//		}
//	
//		// 탈퇴회원이 한 신고 다 지우기
//		reportRepository.deleteByMemNum(memNum);
//		
//	}

}