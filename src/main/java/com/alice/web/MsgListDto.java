package com.alice.project.web;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class MsgListDto {
	private Long user1Num; // 보내는 사람 번호
	private Long user2Num; // 받는사람 번호
	private LocalDateTime sendDate; // 발송일자
	private String recentContent; // 가장 최근 메시지
	private String messageFromId; // 보내는 사람 아이디
	private String messageToId; // 받는 사람 아이디
	private String messageFromName;
	private String messageToName;
	private Long direction; // user1Num->user2Num : 0, 반대면 1
	private String senderProfileImg; // 보내는 사람 프로필 사진

	@Builder
	public MsgListDto(Long user1Num, Long user2Num, LocalDateTime sendDate, String recentContent, String messageFromId,
			String messageToId, Long direction, String senderProfileImg, String messageFromName, String messageToName) {
		this.user1Num = user1Num;
		this.user2Num = user2Num;
		this.sendDate = sendDate;
		this.recentContent = recentContent;
		this.messageFromId = messageFromId;
		this.messageToId = messageToId;
		this.messageFromName = messageFromName;
		this.messageToName = messageToName;
		this.direction = direction;
		this.senderProfileImg = senderProfileImg;
	}

//   public MsgListDto(Long messageFromNum, Long messageToNum, 
//         String recentContent, String messageFromId, String messageToId) {
//      this.messageFromNum = messageFromNum;
//      this.messageToNum = messageToNum;
//      this.sendDate = LocalDateTime.now();
//      this.recentContent = recentContent;
//      this.messageFromId = messageFromId;
//      this.messageToId = messageToId;
//   }

}