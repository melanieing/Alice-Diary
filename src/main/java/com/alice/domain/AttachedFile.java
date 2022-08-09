package com.alice.project.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

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

@Entity
@Table(name = "attachedFile")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString(exclude = "post")
@EqualsAndHashCode(of = "num")
@DynamicInsert
public class AttachedFile {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FILE_SEQ_GENERATOR")
	@SequenceGenerator(name = "FILE_SEQ_GENERATOR", sequenceName = "SEQ_FILE_NUM", initialValue = 1, allocationSize = 1)
	@Column(name = "file_num")
	private Long num; // 파일 번호

	@Column
	private String originName; // 원본파일명

	@Column
	private String saveName; // 저장파일명
	@Column
	private String filePath; // 파일경로

	@ManyToOne(fetch = FetchType.LAZY) // 모든 연관관계는 항상 지연로딩으로 설정(성능상이점)
	@JoinColumn(name = "post_num")
	@JsonBackReference
	private Post post; // 소속 게시물 객체

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL) // 모든 연관관계는 항상 지연로딩으로 설정(성능상이점)
	@JoinColumn(name = "message_num")
	@JsonBackReference
	private Message message; // 소속 메시지 객체

	// 연관관계 메서드 (양방향관계)
	public void setPost(Post post) {
		this.post = post;
		post.getFiles().add(this);
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	@Builder
	public AttachedFile(String originName, String saveName, String filePath) {
		this.originName = originName;
		this.saveName = saveName;
		this.filePath = filePath;
	}

	@Builder
	public AttachedFile(String originName, String saveName, String filePath, Post post) {
		this.originName = originName;
		this.saveName = saveName;
		this.filePath = filePath;
		this.post = post;
	}

	@Builder
	public AttachedFile(String originName, String saveName, String filePath, Message message) {
		this.originName = originName;
		this.saveName = saveName;
		this.filePath = filePath;
		this.message = message;
	}

}
