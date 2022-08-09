package com.alice.project.web;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EventAlarmDto {
	private String content;
	private List<AlarmMemberListDto> memberList;
	private LocalDate startDate;
	private String fName;
	private String fId;
}