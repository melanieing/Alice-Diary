package com.alice.project.web;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchEventsResultDto {
	private LocalDate startDate;
	private LocalDate endDate;
	private String content;
	private String memberList;
	private String location;
	private String memo;
	private boolean publicity;
}