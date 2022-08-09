package com.alice.project.domain;

import java.time.LocalDateTime;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseTimeEntity {
	@CreatedDate
	private LocalDateTime createdDate;

	@LastModifiedDate
	private LocalDateTime modifiedDate;

	// 해당 엔티티를 저장하기 이전에 실행
	@PrePersist
	public void onPrePersist() {
		this.createdDate = LocalDateTime.now();
		this.modifiedDate = this.createdDate;
	}

	// 해당 엔티티를 업데이트 하기 이전에 실행
	@PreUpdate
	public void onPreUpdate() {
		this.modifiedDate = LocalDateTime.now();
	}
}