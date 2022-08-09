package com.alice.project.domain;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.alice.project.web.CalendarFormDto;
import com.fasterxml.jackson.annotation.JsonBackReference;

import groovy.transform.builder.Builder;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "calendar")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
@EqualsAndHashCode(of = "num")
public class Calendar {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CAL_SEQ_GENERATOR")
	@SequenceGenerator(name = "CAL_SEQ_GENERATOR", sequenceName = "SEQ_CALENDAR_NUM", initialValue = 1, allocationSize = 1)
	@Column(name = "calendar_num")
	private Long num; // 일정 번호
	private String memberList; // 참여자 리스트
	private LocalDate startDate; // 일정 시작일자
	private LocalDate endDate; // 일정 종료일자
	private String content; // 일정내용
	private String memo; // 일정메모
	private String location; // 일정 장소
	private String color; // 일정 색
	private Boolean publicity; // 일정 공개여부
	private LocalDate alarm; // 일정 알람

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mem_num")
	@JsonBackReference
	private Member member; // 일정 생성 회원 객체

	// 연관관계 메서드 (양방향관계)
	public void setMember(Member member) {
		this.member = member;
		member.getCalendars().add(this);
	}

	// 일정 객체 생성 메서드
	public static Calendar createCalendar(Member member) {
		Calendar calendar = new Calendar();
		calendar.setMember(member);
		return calendar;
	}

	@Builder
	public Calendar(Long num, String memberList, LocalDate startDate, LocalDate endDate, String content, String memo,
			String location, String color, Boolean publicity, LocalDate alarm) {
		super();
		this.num = num;
		this.memberList = memberList;
		this.startDate = startDate;
		this.endDate = endDate;
		this.content = content;
		this.memo = memo;
		this.location = location;
		this.color = color;
		this.publicity = publicity;
		this.alarm = alarm;
	}

	// 일정 객체 생성 메서드
	public static Calendar createCalendar(CalendarFormDto dto, Member member) {
		Calendar calendar = new Calendar(dto.getMemberList(), dto.getStartDate(), dto.getEndDate(), dto.getContent(),
				dto.getMemo(), dto.getLocation(), dto.getColor(), dto.getPublicity(), dto.getAlarmDate(), member);

		return calendar;
	}

	public static Calendar updateBirth(Calendar c, LocalDate newBirth) {
		Calendar calendar = new Calendar("", newBirth, newBirth, c.getContent(), c.getMemo(), c.getLocation(),
				c.getColor(), c.getPublicity(), newBirth, c.getMember());
		return calendar;
	}

	@Builder
	public Calendar(String memberList, LocalDate startDate, LocalDate endDate, String content, String memo,
			String location, String color, Boolean publicity, LocalDate alarm, Member member) {
		super();
		this.memberList = memberList;
		this.startDate = startDate;
		this.endDate = endDate;
		this.content = content;
		this.memo = memo;
		this.location = location;
		this.color = color;
		this.publicity = publicity;
		this.alarm = alarm;
		this.member = member;
	}

	@Builder
	public Calendar(Member m, LocalDate date) {
		super();
		this.content = "🎉생일🎉";
		this.startDate = date;
		this.endDate = date;
		this.alarm = date;
		this.memo = "";
		this.color = "black";
		this.location = "";
		this.memberList = "";
		this.publicity = true;
		this.member = m;
	}
}