package com.alice.project.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
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

import com.alice.project.web.SuggestionDto;
import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "suggestion")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
@EqualsAndHashCode(of = "num")
@DynamicInsert
public class Suggestion {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SUGGESTION_SEQ_GENERATOR")
	@SequenceGenerator(name = "SUGGESTION_SEQ_GENERATOR", sequenceName = "SEQ_SUGGESTION_NUM", initialValue = 1, allocationSize = 1)
	@Column(name = "suggest_num")
	private Long num; // 건의사항 번호

	@Column(length = 4000)
	private String content; // 건의 내용
	@Column(nullable = false)
	private LocalDateTime suggestDate; // 건의 일자

	@ManyToOne(fetch = FetchType.LAZY) // 모든 연관관계는 항상 지연로딩으로 설정(성능상이점)
	@JoinColumn(name = "member_num")
	@JsonBackReference
	private Member member; // 건의자 객체

	@PrePersist
	public void suggestDate() {
		this.suggestDate = LocalDateTime.now();
	}

	// 연관관계 메서드 (양방향관계)
	public void setMember(Member member) {
		this.member = member;
		member.getSuggestions().add(this);
	}

	// 건의사항 객체 생성 메서드
	public static Suggestion createSuggestion(SuggestionDto suggestionDto, Member member) {
		Suggestion suggestion = new Suggestion(suggestionDto.getSuggestNum(), suggestionDto.getContent(),
				suggestionDto.getSuggestDate(), member);
		return suggestion;
	}

	public Suggestion(Long num, String content, LocalDateTime suggestDate, Member member) {
		this.num = num;
		this.content = content;
		this.suggestDate = suggestDate;
		this.member = member;
	}
}