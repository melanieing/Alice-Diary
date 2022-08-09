package com.alice.project.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "reply")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
@EqualsAndHashCode(of = "num")
@AllArgsConstructor
@DynamicInsert
public class Reply {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REPLY_SEQ_GENERATOR")
	@SequenceGenerator(name = "REPLY_SEQ_GENERATOR", sequenceName = "SEQ_REPLY_NUM", initialValue = 1, allocationSize = 1)
	@Column(name = "reply_num")
	private Long num; // 댓글 번호

	private Long parentRepNum; // 부모댓글 번호

	@Column(nullable = false)
	private String content; // 댓글 내용

	@Column(nullable = false)
	private LocalDateTime repDate; // 댓글작성일자

	@Column(nullable = false)
	private Long heart = 0L; // 공감 수 (default=0)

	@Column(nullable = false)
	private Boolean edit; // 수정여부 (False, True) SQL문 : CHAR(1) Check(edit IN('0', '1')

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ReplyStatus status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_num")
	@JsonBackReference
	private Post post; // 댓글 소속 게시물 객체

	@ManyToOne(fetch = FetchType.LAZY) // 모든 연관관계는 항상 지연로딩으로 설정(성능상이점)
	@JoinColumn(name = "member_num")
	@JsonBackReference
	private Member member; // 댓글 작성 회원 객체

	/* reports가 null일 수 있음 */
	@OneToMany(mappedBy = "reply", cascade = CascadeType.ALL)
	@JsonManagedReference
	private List<Report> reports = new ArrayList<>(); // 댓글 소속 신고 리스트

	@PrePersist
	public void rep_Date() {
		this.repDate = LocalDateTime.now();
	}

	// 부모댓글쓰기
	@Builder
	public Reply(String content, LocalDateTime repDate, Boolean edit, Post post, Member member, ReplyStatus status) {
		super();

		this.content = content;
		this.repDate = repDate;
		this.edit = edit;
		this.post = post;
		this.member = member;
		this.status = status;
	}

	// 대댓글쓰기
	@Builder
	public Reply(String content, LocalDateTime repDate, Boolean edit, Long parentRepNum, Post post, Member member,
			ReplyStatus status) {
		super();

		this.content = content;
		this.repDate = repDate;
		this.edit = edit;
		this.parentRepNum = parentRepNum;
		this.post = post;
		this.member = member;
		this.status = status;
	}

	// 연관관계 메서드 (양방향관계)
	public void setPost(Post post) {
		this.post = post;
		post.getReplies().add(this);
	}

	public void setMember(Member member) {
		this.member = member;
		member.getReplies().add(this);
	}

}