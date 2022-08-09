package com.alice.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.alice.project.domain.Member;
import com.alice.project.domain.Notification;

@Transactional(readOnly = true)
public interface NotificationRepository extends JpaRepository<Notification, Long> {
	
	long countByMemberAndChecked(Member member, boolean checked);

	@Transactional
	List<Notification> findByMemberAndCheckedOrderByCreatedDateTimeDesc(Member member, boolean checked);

	@Transactional
	void deleteByMemberAndChecked(Member member, boolean checked);
	
}
