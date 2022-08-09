package com.alice.project.event;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alice.project.domain.Member;
import com.alice.project.domain.Notification;
import com.alice.project.domain.NotificationType;
import com.alice.project.domain.PostType;
import com.alice.project.domain.Reply;
import com.alice.project.repository.NotificationRepository;
import com.alice.project.repository.ReplyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class ReplyEventListener implements ApplicationListener<ReplyCreatedEvent> {

	private final ReplyRepository replyRepository;
	private final NotificationRepository notificationRepository;

	private void createNotification(Reply reply, Member member, String comment, NotificationType notificationType) {
		Notification notification = new Notification();
		notification.setTitle("커뮤니티 탭에서 확인하세요.");
		PostType pt = reply.getPost().getPostType();
		if (pt.equals(PostType.OPEN)) {
			notification.setLink("/open/get?num=" + reply.getPost().getNum()); // 공개게시판 댓글일 경우
		} else if (pt.equals(PostType.CUSTOM)) {
			notification.setLink("/community/" + reply.getPost().getCommunity().getNum() + "/list"); // 개별 커뮤니티 댓글일 경우			
		}
		notification.setChecked(false);
		notification.setCreatedDateTime(LocalDateTime.now());
		notification.setWording(comment);
		notification.setMember(member);
		notification.setNotificationType(notificationType);
		notificationRepository.save(notification);
	}

	@Override
	public void onApplicationEvent(ReplyCreatedEvent event) {
		Reply reply = replyRepository.findByNum(event.getReply().getNum());
		Member replier = reply.getMember();
		Member member = reply.getPost().getMember();
		if (member.isReplyCreated()) {
			if (replier.getNum() != member.getNum()) {
				createNotification(reply, member, replier.getName() + "님이 내 글에 댓글을 달았습니다.", NotificationType.REPLY);				
			}
		}
	}

}
