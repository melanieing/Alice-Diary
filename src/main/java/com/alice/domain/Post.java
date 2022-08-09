package com.alice.project.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;

import com.alice.project.web.WriteFormDto;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
@EqualsAndHashCode(of = "num")
@DynamicInsert
public class Post {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "POST_SEQ_GENERATOR")
	@SequenceGenerator(name = "POST_SEQ_GENERATOR", sequenceName = "SEQ_POST_NUM", initialValue = 1, allocationSize = 1)
	@Column(name = "post_num")
	private Long num; // 게시물번호
	@Column(nullable = false)
	private String title; // 게시물 제목
	@Column(nullable = false)
	private LocalDateTime postDate; // 게시물 작성일자
	private LocalDateTime updateDate; // 게시물 수정일자

	@Column(length = 4000, nullable = false)
	private String content; // 게시물 내용
	private Long viewCnt = 0L; // 게시물 조회수 (default=0)

	@Enumerated(EnumType.STRING)
	private PostType postType; // 게시물 종류 [NOTICE, OPEN, CUSTOM]

	@ManyToOne(fetch = FetchType.LAZY) // 모든 연관관계는 항상 지연로딩으로 설정(성능상이점)
	@JoinColumn(name = "member_num")
	@JsonBackReference
	private Member member; // 작성회원 객체

	/* community가 null일 수 있음 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "community_num")
	@JsonBackReference
	private Community community; // 소속 커뮤니티 객체

	/* replies가 null일 수 있음 */
	@OneToMany(mappedBy = "post")
	@JsonManagedReference
	private List<Reply> replies = new ArrayList<>(); // 게시물 소속 댓글 리스트

	/* files가 null일 수 있음 */
	@OneToMany(mappedBy = "post")
	@JsonManagedReference
	private List<AttachedFile> files = new ArrayList<>(); // 게시물 소속 첨부파일 리스트

	/* reports가 null일 수 있음 */
	@OneToMany(mappedBy = "post")
	@JsonManagedReference
	private List<Report> reports = new ArrayList<>(); // 게시물 소속 신고 리스트

	// 연관관계 메서드 (양방향관계)

	public void setMember(Member member) {
		this.member = member;
		member.getPosts().add(this);
	}

	public void setCommunity(Community community) {
		this.community = community;
		community.getPosts().add(this);
	}

	@PrePersist
	public void post_Date() {
		this.postDate = LocalDateTime.now();
	}

	@PreUpdate
	public void update_Date() {
		this.updateDate = LocalDateTime.now();
	}

	// 게시물 객체 생성 메서드
	public static Post createPost(WriteFormDto wirteFormDto, Member member) {
		Post post = new Post(wirteFormDto.getTitle(), LocalDateTime.now(), wirteFormDto.getContent(), PostType.OPEN,
				member);

		return post;
	}

	/* 공지사항 객체 생성 메서드 */
	public static Post createNotice(WriteFormDto wirteFormDto, Member member) {
		Post post = new Post(wirteFormDto.getTitle(), LocalDateTime.now(), wirteFormDto.getContent(), PostType.NOTICE,
				member);

		return post;
	}

	// 조회수 증가 메소드
	public void viewCntUp() {
		this.viewCnt++;
	}

	@Builder
	public Post(String title, LocalDateTime postDate, String content, PostType postType, Member member) {
		super();
		this.title = title;
		this.postDate = postDate;
		this.content = content;
		this.postType = postType;
		this.member = member;
	}

	// 커뮤니티 객체 생성 메서드
	public static Post creatCommunity(WriteFormDto wirteFormDto, Member member, Community community) {
		Post post = new Post(wirteFormDto.getTitle(), LocalDateTime.now(), wirteFormDto.getContent(), PostType.CUSTOM,
				member, community);

		return post;
	}

	public Post(String title, LocalDateTime postDate, String content, PostType postType, Member member,
			Community community) {
		super();
		this.title = title;
		this.postDate = postDate;
		this.content = content;
		this.postType = postType;
		this.member = member;
		this.community = community;
	}

}