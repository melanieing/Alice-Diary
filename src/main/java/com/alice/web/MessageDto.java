package com.alice.project.web;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.alice.project.domain.Message;
import com.alice.project.service.MessageService;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class MessageDto implements Comparable<MessageDto> {

	@Autowired
	private MessageService ms;

	private Long user1Num; // 보내는 사람 번호
	private Long user2Num; // 받는사람 번호
	private LocalDateTime sendDate; // 발송일자
	private String content; // 메시지
	private String messageFromId; // 보내는 사람 아이디
	private String messageToId; // 받는 사람 아이디
	private Long direction; // user1이 user2에게 보내면 0, user2가 user1에게 보내면 1
	private MultipartFile originName; // 파일 이름

	@Builder
	public MessageDto(Long user1Num, Long user2Num, LocalDateTime sendDate, String content, String messageFromId,
			String messageToId, Long direction) {
		this.user1Num = user1Num;
		this.user2Num = user2Num;
		this.sendDate = sendDate;
		this.content = content;
		this.messageFromId = messageFromId;
		this.messageToId = messageToId;
		this.direction = direction;
	}

	// 첨부파일 있을 때 사용
	public MessageDto(MessageService ms, Long user1Num, Long user2Num, LocalDateTime sendDate, String content,
			String messageFromId, String messageToId, Long direction, MultipartFile originName) {
		this.ms = ms;
		this.user1Num = user1Num;
		this.user2Num = user2Num;
		this.sendDate = sendDate;
		this.content = content;
		this.messageFromId = messageFromId;
		this.messageToId = messageToId;
		this.direction = direction;
		this.originName = originName;
	}

	public MessageDto(Long user1Num, Long user2Num, LocalDateTime sendDate, String content, Long direction) {
		this.user1Num = user1Num;
		this.user2Num = user2Num;
		this.sendDate = sendDate;
		this.content = content;
		this.messageFromId = ms.findIdByNum(user1Num);
		this.messageToId = ms.findIdByNum(user2Num);
		this.direction = direction;

	}

	public MessageDto(Message message, MessageService ms) {
		this.user1Num = message.getUser1Num();
		this.user2Num = message.getUser2Num();
		this.sendDate = message.getSendDate();
		this.content = message.getContent();
	}

	@Override
	public int compareTo(MessageDto o) {
		return this.getSendDate().compareTo(o.getSendDate());
	}

}