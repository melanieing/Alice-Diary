package com.alice.project.web;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class MsgFileDto {

	private String theOtherId;
	private String theOtherName;
	private String originName;
	private String saveName;
	private Long fileNum;
	private LocalDateTime sendDate;

	public MsgFileDto(String theOtherId, String originName, String saveName, Long fileNum, LocalDateTime sendDate,
			String theOtherName) {
		this.theOtherId = theOtherId;
		this.originName = originName;
		this.saveName = saveName;
		this.fileNum = fileNum;
		this.sendDate = sendDate;
		this.theOtherName = theOtherName;
	}

}
