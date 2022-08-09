package com.alice.project.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import com.alice.project.config.CurrentMember;
import com.alice.project.domain.Member;
import com.alice.project.domain.Notification;
import com.alice.project.repository.NotificationRepository;
import com.alice.project.service.MemberService;
import com.alice.project.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.var;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
	private final NotificationRepository repository;

	private final NotificationService service;
	private final MemberService memberService;


	@GetMapping("/notifications")
	public String getNotifications(@AuthenticationPrincipal UserDetails user, Model model) {
		log.info("user.getUsername = " + user.getUsername());
		Member member = memberService.findById(user.getUsername());
		log.info("member = " + member);
		List<Notification> notifications = repository.findByMemberAndCheckedOrderByCreatedDateTimeDesc(member, false);
		long numberOfChecked = repository.countByMemberAndChecked(member, true);
		putCategorizedNotifications(model, notifications, numberOfChecked, notifications.size());
		model.addAttribute("isNew", true);
		model.addAttribute("member", member);
		service.markAsRead(notifications);
		return "notification/list";
	}

	@GetMapping("/notifications/old")
	public String getOldNotifications(@CurrentMember Member member, Model model) {
		List<Notification> notifications = repository.findByMemberAndCheckedOrderByCreatedDateTimeDesc(member, true);
		long numberOfNotChecked = repository.countByMemberAndChecked(member, false);
		putCategorizedNotifications(model, notifications, notifications.size(), numberOfNotChecked);
		model.addAttribute("isNew", false);
		model.addAttribute("member", member);
		return "notification/list";
	}

	@DeleteMapping("/notifications")
	public String deleteNotifications(@CurrentMember Member member) {
		repository.deleteByMemberAndChecked(member, true);
		return "redirect:/notifications";
	}

	private void putCategorizedNotifications(Model model, List<Notification> notifications, long numberOfChecked,
			long numberOfNotChecked) {
		List<Notification> newMessageNotifications = new ArrayList<>();
		List<Notification> newCalendarNotifications = new ArrayList<>();
		List<Notification> newFriendNotifications = new ArrayList<>();
		List<Notification> newCommunityNotifications = new ArrayList<>();
		List<Notification> newReplyNotifications = new ArrayList<>();
		List<Notification> newNoticeNotifications = new ArrayList<>();
		
		for (var notification : notifications) {
			switch (notification.getNotificationType()) {
			case MESSAGE:
				newMessageNotifications.add(notification);
				break;
			case ALICE:
				newCalendarNotifications.add(notification);
				break;
			case FRIEND:
				newFriendNotifications.add(notification);
				break;
			case COMMUNITY:
				newCommunityNotifications.add(notification);
				break;
			case REPLY:
				newReplyNotifications.add(notification);
				break;
			case NOTICE:
				newNoticeNotifications.add(notification);
				break;
			}
		}

		model.addAttribute("numberOfNotChecked", numberOfNotChecked);
		model.addAttribute("numberOfChecked", numberOfChecked);
		model.addAttribute("notifications", notifications);
		model.addAttribute("newMessageNotifications", newMessageNotifications);
		model.addAttribute("newCalendarNotifications", newCalendarNotifications);
		model.addAttribute("newFriendNotifications", newFriendNotifications);
		model.addAttribute("newCommunityNotifications", newCommunityNotifications);
		model.addAttribute("newReplyNotifications", newReplyNotifications);
		model.addAttribute("newNoticeNotifications", newNoticeNotifications);
	}
}
