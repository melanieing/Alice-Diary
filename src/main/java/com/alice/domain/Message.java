package com.alice.project.domain;

import java.time.LocalDateTime;
import java.util.Comparator;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "message")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
@EqualsAndHashCode(of = "num")
@DynamicInsert
public class Message implements Comparator<Message>, Comparable<Message> {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MESSAGE_SEQ_GENERATOR")
	@SequenceGenerator(name = "MESSAGE_SEQ_GENERATOR", sequenceName = "SEQ_MESSAGE_NUM", initialValue = 1, allocationSize = 1)
	@Column(name = "message_num")
	private Long num; // 쪽지 번호
	@Column(nullable = false)
	private Long user1Num; // 두 사람 중 작은 num 가진 사람 번호
	@Column(nullable = false)
	private Long user2Num; // 두 사람 중 큰 num 가진 사람 번호
	@Column(nullable = false)
	private Long msgStatus; // default 3, user1만 지우면 2, user2만 지우면 1, 둘 다 지우면 0
	@Column(nullable = false)
	private Long direction; // user1이 user2에게 보내면 : 0, 반대면 1

	@Column(nullable = false)
	private LocalDateTime sendDate; // 쪽지 발송일자
	@Column(nullable = false, length = 4000)
	private String content; // 쪽지내용

	@OneToOne(mappedBy = "message", cascade = CascadeType.ALL)
	@JoinColumn(name = "file_num")
	@JsonManagedReference
	private AttachedFile file = new AttachedFile();

	public Message(Long num, Long user1Num, Long user2Num, LocalDateTime sendDate, String content, Long msgStatus,
			Long direction) {
		this.num = num;
		this.user1Num = user1Num;
		this.user2Num = user2Num;
		this.sendDate = sendDate;
		this.content = content;
		this.msgStatus = msgStatus;
		this.direction = direction;
	}

	public Message(Long user1Num, Long user2Num, Long msgStatus, Long direction, LocalDateTime sendDate, String content,
			AttachedFile file) {
		this.user1Num = user1Num;
		this.user2Num = user2Num;
		this.msgStatus = msgStatus;
		this.direction = direction;
		this.sendDate = sendDate;
		this.content = content;
		this.file = file;
	}

	@Builder
	public Message(Long user1Num, Long user2Num, LocalDateTime sendDate, String content, Long msgStatus,
			Long direction) {
		this.user1Num = user1Num;
		this.user2Num = user2Num;
		this.sendDate = sendDate;
		this.content = content;
		this.msgStatus = msgStatus;
		this.direction = direction;
	}

	@Override
	public int compare(Message o1, Message o2) {
		return o2.getSendDate().compareTo(o1.getSendDate());
	}

	@Override
	public int compareTo(Message m) {
		if (this.sendDate.isBefore(m.sendDate)) {
			return -1;
		} else if (this.sendDate.isAfter(m.sendDate)) {
			return 1;
		} else {
			return 0;
		}
	}

	// 댓글 쪽지 보내기 객체 생성 메서드
	public static Message createMessage(Long user1Num, Long user2Num, String content, Long direction) {
		Message message = new Message(user1Num, user2Num, LocalDateTime.now(), content, 3L, direction);
		return message;
	}

	// 초대장 보내기 객체 생성 메서드
	public static Message createInviteMsg(Long user1Num, Long user2Num, Long direction, String comName) {
		Message message = new Message(user1Num, user2Num, LocalDateTime.now(),
				"새로운 커뮤니티(" + comName + ")에 초대되었습니다! 얼른 방문해보세요 :)", 3L, direction);
		return message;
	}

   @ManyToOne(fetch=FetchType.LAZY, cascade = CascadeType.ALL) // 모든 연관관계는 항상 지연로딩으로 설정(성능상이점)
   @JoinColumn(name="receiver_num")
   private Member member; // 쪽지 받는회원 객체

	// 연관관계 메서드 (양방향관계)
   public void setMember(Member member) {
      this.member = member;
      member.getMessages().add(this);
   }

}