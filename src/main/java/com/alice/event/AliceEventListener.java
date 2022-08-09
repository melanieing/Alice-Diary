package com.alice.project.event;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alice.project.domain.Calendar;
import com.alice.project.domain.Member;
import com.alice.project.domain.Notification;
import com.alice.project.domain.NotificationType;
import com.alice.project.repository.CalendarRepository;
import com.alice.project.repository.MemberRepository;
import com.alice.project.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class AliceEventListener implements ApplicationListener<AliceCreatedEvent> {

	private final CalendarRepository calendarRepository;
	private final NotificationRepository notificationRepository;
	private final MemberRepository memberRepository;

	private void createNotification(Calendar calendar, Member member, String wording,
			NotificationType notificationType) {
		if (calendar.getPublicity().equals(true)) { // 공개일정만 알림 보내기
			Notification notification = new Notification();
			notification.setTitle("앨리스 탭에서 확인해주세요.");
			notification.setLink("/alice/");
			notification.setWording(wording);
			notification.setChecked(false);
			notification.setMember(member);
			notification.setCreatedDateTime(LocalDateTime.now());
			notification.setNotificationType(notificationType);
			notificationRepository.save(notification);		
		}
	}

	@Override
	public void onApplicationEvent(AliceCreatedEvent aliceCreatedEvent) {
		log.info("!" + aliceCreatedEvent.getCalendar().getContent());
		Calendar calendar = calendarRepository.findByNum(aliceCreatedEvent.getCalendar().getNum());
		log.info("aliceCreatedEvent.getCalendar() : " + aliceCreatedEvent.getCalendar().toString());
		String memberList = calendar.getMemberList();
		String[] memList = null;
		if (memberList != null) {
			memList = memberList.split(",");
		}

		Member member = calendar.getMember();
		log.info("member" + member.getName());
		if (member.isAliceCreated()) {
			if (memList != null && memList.length != 0) {
				for (String memNum : memList) {
					log.info("memNum : " + memNum);
					Member party = memberRepository.findByNum(Long.parseLong(memNum));
					createNotification(calendar, party, party.getName() + "님이 참여하게 된 일정이 생겼습니다!", NotificationType.ALICE);
				}
			}
			createNotification(calendar, member, "내 캘린더에 새로운 일정이 추가됐습니다!", NotificationType.ALICE);
		}
	}

}
