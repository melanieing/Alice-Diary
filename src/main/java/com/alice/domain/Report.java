package com.alice.project.domain;

import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Entity
@Table(name = "report")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
@Slf4j
@EqualsAndHashCode(of = "num")
@DynamicInsert
public class Report {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REPORT_SEQ_GENERATOR")
	@SequenceGenerator(name = "REPORT_SEQ_GENERATOR", sequenceName = "SEQ_REPORT_NUM", initialValue = 1, allocationSize = 1)
	@Column(name = "report_num")
	private Long num; // 신고 번호

	@Enumerated(EnumType.STRING)
	private ReportReason reportReason; // 신고사유 [BAD, LEAK, SPAM, ETC]
	@Column(length = 4000)
	private String content; // 신고내용
	@Column(nullable = false)
	private LocalDateTime reportDate; // 신고일자

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ReportType reportType; // 신고종류 [POST, REPLY]

	@ManyToOne(fetch = FetchType.LAZY) // 모든 연관관계는 항상 지연로딩으로 설정(성능상이점)
	@JoinColumn(name = "mem_num")
	@JsonBackReference
	private Member member; // 신고회원 객체

	@ManyToOne(fetch = FetchType.LAZY) // 모든 연관관계는 항상 지연로딩으로 설정(성능상이점)
	@JoinColumn(name = "post_num")
	@JsonBackReference
	private Post post; // 게시물 객체

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL) // 모든 연관관계는 항상 지연로딩으로 설정(성능상이점)
	@JoinColumn(name = "reply_num")
	@JsonBackReference
	private Reply reply; // 댓글 객체

	// 연관관계 메서드 (양방향관계)
	public void setMember(Member member) {
		this.member = member;
		member.getReports().add(this);
	}

	public void setPost(Post post) {
		this.post = post;
		post.getReports().add(this);
	}

	public void setReply(Reply reply) {
		this.reply = reply;
		reply.getReports().add(this);
	}

	@PrePersist
	public void reportDate() {
		this.reportDate = LocalDateTime.now();
	}

	@Builder
	public Report(ReportReason reportReason, String content, LocalDateTime reportDate, ReportType reportType,
			Member member) {
		this.reportReason = reportReason;
		this.content = content;
		this.reportDate = reportDate;
		this.reportType = reportType;
		this.member = member;
	}
  
	// 게시글 신고 객체 생성 메서드
	public static Report createPostReport(Post post, String reportReason, String content, Member member) {
		Report report = new Report(ReportReason.valueOf(reportReason), content, LocalDateTime.now(),
				ReportType.POST, member, post);
		
		return report;
	}
  
	// 댓글 신고 객체 생성 메서드
	public static Report createReplyReport(Reply reply, String reportReason, String content, Member member) {
		Report report = new Report(ReportReason.valueOf(reportReason), content, LocalDateTime.now(),
				ReportType.REPLY, member, reply);

		return report;
	}
	
	// 게시물 신고 
	@Builder
	public Report(ReportReason reportReason, String content, LocalDateTime reportDate, ReportType reportType,
			Member member, Post post) {
		this.reportReason = reportReason;
		this.content = content;
		this.reportDate = reportDate;
		this.reportType = reportType;
		this.member = member;
		this.post = post;
	}
	// 댓글 신고
	@Builder
	public Report(ReportReason reportReason, String content, LocalDateTime reportDate, ReportType reportType,
			Member member, Reply reply) {
		this.reportReason = reportReason;
		this.content = content;
		this.reportDate = reportDate;
		this.reportType = reportType;
		this.member = member;
		this.reply = reply;
	}

}