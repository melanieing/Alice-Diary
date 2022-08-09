package com.alice.project.web;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class FriendsDto {

	private Long addeeNum;
	private Long groupNum;
	private String groupName;

	public FriendsDto(Long addeeNum, Long groupNum, String groupName) {
		super();
		this.addeeNum = addeeNum;
		this.groupNum = groupNum;
		this.groupName = groupName;
	}

}