package com.alice.project.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alice.project.domain.Notification;
import com.alice.project.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
	private final NotificationRepository notificationRepository;

	public void markAsRead(List<Notification> notifications) {
		notifications.forEach(n -> n.setChecked(true));
		notificationRepository.saveAll(notifications);
	}
}
