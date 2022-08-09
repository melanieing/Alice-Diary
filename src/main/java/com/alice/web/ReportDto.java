package com.alice.project.web;

import java.time.LocalDateTime;

import com.alice.project.domain.Member;
import com.alice.project.domain.ReportReason;
import com.alice.project.domain.ReportType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ReportDto {

	private String content;
	private LocalDateTime reportDate;
	private ReportType reportType;
	private Long targetNum; // 신고대상 게시글 혹은 댓글 번호
	private Member member; // 신고한 회원 - 객체로 넣어주기
	private ReportReason reportReason;

	private Long replyNum;

}