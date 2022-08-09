package com.alice.project.web;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.alice.project.domain.PostType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WriteFormDto {
	
	private Long postNum; //postNum
	
	private String title;

	private String content;

	private PostType postType;

	private LocalDateTime updateDate;

	private List<MultipartFile> originName;
	
	@Builder
	public WriteFormDto(Long postNum, String title, String content) {
		super();
		this.postNum = postNum;
		this.title = title;
		this.content = content;
	
	}

	@Builder
	public WriteFormDto() {
		super();
	}

}