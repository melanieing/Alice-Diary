package com.alice.project.web;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SuggestionDto {

	private Long suggestNum;
	private String content;
	private LocalDateTime suggestDate;
	private String password;
	private String suggesterId;

	@Builder
	public SuggestionDto(Long suggestNum, String content, LocalDateTime suggestDate) {
		super();
		this.suggestNum = suggestNum;
		this.content = content;
		this.suggestDate = suggestDate;
	}

	@Builder
	public SuggestionDto(Long suggestNum, String content, LocalDateTime suggestDate, String suggesterId) {
		this.suggestNum = suggestNum;
		this.content = content;
		this.suggestDate = suggestDate;
		this.suggesterId = suggesterId;
	}
}