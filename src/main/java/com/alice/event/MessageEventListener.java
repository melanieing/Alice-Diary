package com.alice.project.event;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alice.project.domain.Member;
import com.alice.project.domain.Message;
import com.alice.project.domain.Notification;
import com.alice.project.domain.NotificationType;
import com.alice.project.repository.MemberRepository;
import com.alice.project.repository.MessageRepository;
import com.alice.project.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class MessageEventListener implements ApplicationListener<MessageCreatedEvent> {

	private final MessageRepository messageRepository;
	private final MemberRepository memberRepository;
	private final NotificationRepository notificationRepository;

	private void createNotification(Message message, Member member, String comment, NotificationType notificationType) {
		Notification notification = new Notification();
		Long dir = message.getDirection();
		String senderName = "";
		String senderId = "";
//		String receiverId = "";
		if (dir == 0) {
			Member m = memberRepository.findByNum(message.getUser1Num()); // 보내는 사람
			senderName = m.getName();
			senderId = m.getId();
//			receiverId = memberRepository.findByNum(message.getUser2Num()).getId();
		} else if (dir == 1) {
			Member m = memberRepository.findByNum(message.getUser2Num()); // 보내는 사람
			senderName = m.getName();
			senderId = m.getId();
//			receiverId = memberRepository.findByNum(message.getUser1Num()).getId();
		}
		notification.setTitle("쪽지함에서 확인해주세요.");
		notification.setLink("/messagebox/" + member.getId() + "/" + senderId);
		notification.setChecked(false);
		notification.setCreatedDateTime(LocalDateTime.now());
		notification.setWording(comment);
		notification.setMember(member);
		notification.setNotificationType(notificationType);
		notificationRepository.save(notification);
	}

	@Override
	public void onApplicationEvent(MessageCreatedEvent event) {
		Message message = messageRepository.findByNum(event.getMessage().getNum());
		Long dir = message.getDirection();
		String senderName = "";
		Long receiverNum = 0L;
		if (dir == 0) {
			Long senderNum = message.getUser1Num();
			senderName = memberRepository.findByNum(senderNum).getName();
			receiverNum = message.getUser2Num();
		} else if (dir == 1) {
			Long senderNum = message.getUser2Num();
			senderName = memberRepository.findByNum(senderNum).getName();
			receiverNum = message.getUser1Num();
		}
		
		Member member = memberRepository.findByNum(receiverNum); // 받는 멤버객체
		log.info("받는사람 : " + member.getName());
		if (member.isMessageCreated()) {
			createNotification(message, member, senderName + "으로부터 쪽지가 도착했습니다!", NotificationType.MESSAGE);
		}	
	}
	
}
