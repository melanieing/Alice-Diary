package com.alice.project.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.ArrayUtils;

import com.alice.project.controller.AliceController;
import com.alice.project.domain.Calendar;
import com.alice.project.domain.Friend;
import com.alice.project.domain.Member;
import com.alice.project.event.AliceCreatedEvent;
import com.alice.project.repository.CalendarRepository;
import com.alice.project.repository.FriendRepository;
import com.alice.project.repository.MemberRepository;
import com.alice.project.web.AlarmMemberListDto;
import com.alice.project.web.CalendarFormDto;
import com.alice.project.web.EventAlarmDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CalendarService {

	private final CalendarRepository calendarRepository;
	private final MemberRepository memberRepository;
	private final ApplicationEventPublisher eventPublisher; // for notification
	private final FriendRepository friendRepository;

	@Transactional
	public Calendar addEvent(CalendarFormDto dto, Member m) {
		Calendar cal = Calendar.createCalendar(dto, m);

		Calendar result = calendarRepository.save(cal);
		result.setMember(m); // for notification
		this.eventPublisher.publishEvent(new AliceCreatedEvent(result));
		return cal;
	}

	@Transactional
	public void addBirthEvents(Member member) {
		int year = member.getBirth().getYear();
		LocalDate birth = member.getBirth().plusYears(LocalDate.now().getYear() - year);
		for (int i = 0; i < 5; i++) {
			Calendar birthEvent = new Calendar(member, birth.plusYears(i));
			calendarRepository.save(birthEvent);
		}
	}

	@Transactional
	public void addNewBirthEvents(Member member, LocalDate newBirth) {
		int year = member.getBirth().getYear();
		LocalDate birth = newBirth.plusYears(LocalDate.now().getYear() - year);
		for (int i = 0; i < 5; i++) {
			Calendar birthEvent = new Calendar(member, birth.plusYears(i));
			calendarRepository.save(birthEvent);
		}
	}

	@Transactional
	public void updateBirthEvent(Member m, LocalDate newBirth) {
		int years = LocalDate.now().getYear() - newBirth.getYear();
		List<Long> myDays = calendarRepository.findBirthEvents(m.getNum(), "black");
		for (Long n : myDays) {
			calendarRepository.deleteById(n);
		}
		addNewBirthEvents(m, newBirth);
	}

	public List<Calendar> eventsList(Long num) {
		List<Calendar> events = calendarRepository.findByMemNum(num);
		return events;
	}

	public List<Calendar> fEventsList(Long num) {
		return calendarRepository.findOtherEvents(num);
	}

	public Calendar eventDetail(Long id) {
		Calendar event = calendarRepository.getById(id);
		return event;
	}

	@Transactional
	public void deleteEvent(Long id) {
		calendarRepository.deleteById(id);
	}

	public List<EventAlarmDto> myAlarm(Long num, LocalDate today) {
		List<Calendar> calList = calendarRepository.getAlarmEvents(num, today);
		List<EventAlarmDto> result = new ArrayList<EventAlarmDto>();
		for (Calendar c : calList) {
			EventAlarmDto tmp = new EventAlarmDto();
			tmp.setContent(c.getContent());
			tmp.setStartDate(c.getStartDate());

			if (c.getMemberList() != null) {
				List<AlarmMemberListDto> aTmp = new ArrayList<AlarmMemberListDto>();

				for (String id : c.getMemberList().split(",")) {
					AlarmMemberListDto mTmp = new AlarmMemberListDto();
					Member friend = memberRepository.findByNum(Long.parseLong(id));
					mTmp.setName(friend.getName());
					mTmp.setId(friend.getId());
					aTmp.add(mTmp);
				}
				tmp.setMemberList(aTmp);
			} else {
				tmp.setMemberList(null);
			}
			result.add(tmp);
		}
		return result;
	}

	public List<EventAlarmDto> friendAlarm(Long num, LocalDate today) {
		// 친구 목록 가져옴
		Member me = memberRepository.findByNum(num);
		List<Friend> fList = friendRepository.weAreFriend(me.getNum());
		List<EventAlarmDto> result = new ArrayList<EventAlarmDto>();

		// 친구들의 일정 중 내가 포함된 일정 찾기
		for (Friend f : fList) {
			Member friend = memberRepository.findByNum(f.getMember().getNum());
			List<Calendar> calList = calendarRepository.findFriendEvents(friend.getNum(), today, today.plusDays(7));
			if (calList != null) {
				for (Calendar c : calList) {
					if (c.getMemberList() != null && c.getMemberList().length() != 0
							&& ArrayUtils.contains(c.getMemberList().split(","), "" + me.getNum())) {
						EventAlarmDto tmp = new EventAlarmDto();
						tmp.setContent(c.getContent());
						tmp.setStartDate(c.getStartDate());
						tmp.setFId(friend.getId());
						tmp.setFName(friend.getName());
						result.add(tmp);
					}
				}
			}
		}
		// 화면에 뿌리기
		return result;
	}

	public List<Calendar> searchByContent(Long num, String content) {
		return calendarRepository.findByContent(num, content);
	}

	public List<Calendar> searchByStart(Long num, LocalDate start) {
		return calendarRepository.findByStart(num, start);
	}

	public List<Calendar> searchByEnd(Long num, LocalDate end) {
		return calendarRepository.findByEnd(num, end);
	}

	public List<Calendar> searchByStartEnd(Long num, LocalDate start, LocalDate end) {
		return calendarRepository.findByStartEnd(num, start, end);
	}

	public List<Calendar> searchByContentEnd(Long num, String content, LocalDate end) {
		return calendarRepository.findByContentEnd(num, content, end);
	}

	public List<Calendar> searchByContentStart(Long num, String content, LocalDate start) {
		return calendarRepository.findByContentStart(num, content, start);
	}

	public List<Calendar> searchByAll(Long num, String content, LocalDate start, LocalDate end) {
		return calendarRepository.findByAll(num, content, start, end);
	}
}