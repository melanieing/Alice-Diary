package com.alice.project.event;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alice.project.domain.Member;
import com.alice.project.domain.Notification;
import com.alice.project.domain.NotificationType;
import com.alice.project.domain.Post;
import com.alice.project.repository.MemberRepository;
import com.alice.project.repository.NotificationRepository;
import com.alice.project.repository.PostRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class PostEventListener implements ApplicationListener<PostCreatedEvent> {

	private final PostRepository postRepository;
	private final NotificationRepository notificationRepository;
	private final MemberRepository memberRepository;

	private void createNotification(Post post, Member member, String wording,
			NotificationType notificationType) {
		
		Notification notification = new Notification();
		notification.setTitle("공지사항 탭에서 확인해주세요.");
		notification.setLink("/notice/get?num=" + post.getNum());
		notification.setWording(wording);
		notification.setChecked(false);
		notification.setMember(member);
		notification.setCreatedDateTime(LocalDateTime.now());
		notification.setNotificationType(notificationType);
		notificationRepository.save(notification);
	}

	@Override
	public void onApplicationEvent(PostCreatedEvent postCreatedEvent) {
		log.info("!" + postCreatedEvent.getPost().getContent());
		Post post = postRepository.findByNum(postCreatedEvent.getPost().getNum());
		
		List<Member> members = memberRepository.findAll();
		
		for (Member m : members) {
			createNotification(post, m, "⭐공지사항이 등록되었습니다. 확인해보세요⭐", NotificationType.NOTICE);
			
		}

	}

}
