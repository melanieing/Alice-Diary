package com.alice.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import com.alice.project.domain.Message;

@Repository
public interface MessageRepository
		extends JpaRepository<Message, Long>, MessageRepositoryCustom, QuerydslPredicateExecutor<Message> {

	/* 모든 메시지 반환 */
	List<Message> findAll();
	
	Message findByNum(Long num);
	
	String findByUser1Num(Long user1Num);

}