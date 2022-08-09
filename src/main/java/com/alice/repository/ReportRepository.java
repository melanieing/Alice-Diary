package com.alice.project.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.alice.project.domain.Member;
import com.alice.project.domain.Post;
import com.alice.project.domain.Reply;
import com.alice.project.domain.Report;

@Repository
@Transactional(readOnly = true)
public interface ReportRepository extends JpaRepository<Report, Long>, QuerydslPredicateExecutor<Report> {

	// 게시글 신고 유무 판단
	@Query(value = "SELECT r FROM Report r WHERE post_num = :post AND mem_num = :member AND report_type = 'POST'")
	List<Report> findPostReportExist(Post post, Member member);

	// 댓글 신고 유무 판단
	@Query(value = "SELECT r FROM Report r WHERE reply_num = :reply AND mem_num = :member AND report_type = 'REPLY'")
	List<Report> findReplyReportExist(Reply reply, Member member);

	/* 모든 신고목록 반환(페이징 처리) */
	Page<Report> findAll(Pageable pageable);

	/* 모든 신고목록 반환 */
	@Query(value = "select * from Report order by report_num desc", nativeQuery = true)
	List<Report> searchAll();

	/* 신고 객체 하나 반환 */
	@Query(value = "select * from Report where report_num = :reportNum", nativeQuery = true)
	Report getReport(Long reportNum);

	@Query(value = "SELECT * FROM Report WHERE mem_num = :num", nativeQuery = true)
	List<Report> searchByReporterId(Long num);

	Page<Report> findByContentContaining(String keyword, Pageable pageable);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM Report WHERE reply_num = :replyNum", nativeQuery = true)
	void deleteByReplyNum(Long replyNum);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM Report WHERE post_num = :postNum", nativeQuery = true)
	void deleteByPostNum(Long postNum);
	
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM Report WHERE mem_num = :memNum", nativeQuery = true)
	void deleteByMemNum(Long memNum);

}