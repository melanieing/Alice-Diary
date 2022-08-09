package com.alice.project.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.alice.project.domain.Suggestion;

@Repository
@Transactional(readOnly = true)
public interface SuggestionRepository extends JpaRepository<Suggestion, Long>, QuerydslPredicateExecutor<Suggestion> {

	Page<Suggestion> findAll(Pageable pageable); // 전체 조회 및 페이징처리

	Page<Suggestion> findByContentContaining(String keyword, Pageable pageable);

}