package com.alice.project.event;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alice.project.domain.Friend;
import com.alice.project.domain.Member;
import com.alice.project.domain.Notification;
import com.alice.project.domain.NotificationType;
import com.alice.project.repository.FriendRepository;
import com.alice.project.repository.MemberRepository;
import com.alice.project.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class FriendEventListener implements ApplicationListener<FriendAddEvent> {

	private final FriendRepository friendRepository;
	private final MemberRepository memberRepository;
	private final NotificationRepository notificationRepository;

	private void createNotification(Friend friend, Member member, String wording, NotificationType notificationType) {
		
		Notification notification = new Notification();
		notification.setTitle("친구 목록 탭에서 확인해주세요.");
		notification.setLink("/member/" + friend.getMember().getId());
		notification.setChecked(false);
		notification.setCreatedDateTime(LocalDateTime.now());
		notification.setWording(wording);
		notification.setMember(member);
		notification.setNotificationType(notificationType);
		notificationRepository.save(notification);
		log.info("noti save~");
	}

	@Override
	public void onApplicationEvent(FriendAddEvent event) {
		Friend friend = friendRepository.searchByFriendNum(event.getFriend().getNum());
		Member addee = memberRepository.findByNum(friend.getAddeeNum());
		Member adder = memberRepository.findByNum(friend.getMember().getNum());
		if (addee.isFriendAdded()) {
			createNotification(friend, addee, adder.getName() + "님이 나를 친구로 추가했습니다!", NotificationType.FRIEND);
		}		
	}

}
