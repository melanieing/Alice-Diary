package com.alice.project.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.alice.project.domain.Post;

@Repository
@Transactional(readOnly = true)
public interface PostRepository extends JpaRepository<Post, Long> {

	/* 게시판 공통 쿼리 */
	@Query(value = "select * from Post order by post_num desc", nativeQuery = true)
	Page<Post> findAll(Pageable pageable);

	@Modifying
	@Transactional
	@Query("update Post p set p.viewCnt = p.viewCnt + 1 where p.num = :num")
	Integer viewCntUp(Long num);

	Post findByNum(Long num);

	@Modifying
	@Transactional
	@Query("update Post p set p.title = :title where p.num = :num")
	Integer editTitle(Long num, String title);

	@Modifying
	@Transactional
	@Query("update Post p set p.content = :content where p.num = :num")
	Integer editContent(Long num, String content);

	@Modifying
	@Transactional
	@Query("update Post p set p.updateDate = :updateDate where p.num = :num")
	Integer editDate(Long num, LocalDateTime updateDate);

	/* 공개게시판 쿼리 */
	// 공개게시판 전체글 조회하기
	@Query(value = "select * from Post where post_type = 'OPEN' order by post_num desc", nativeQuery = true)
	Page<Post> findAllOpenPost(Pageable pageable);

	// 공개게시판 목록 조회
	@Query(value = "select post_num, community_num, member_num, view_cnt, content, post_date, post_type, title, update_date from Post where post_type = 'OPEN' order by post_num desc", nativeQuery = true)
	Page<Post> findAllOpens(Pageable pageable);

	// 공개게시판 검색 3개
	@Query(value = "select * from Post where title like '%'||:title||'%' and post_type = 'OPEN' order by post_num desc", nativeQuery = true)
	Page<Post> searchTitle(String title, Pageable pageable);

	@Query(value = "select * from Post where content like '%'||:content||'%' and post_type = 'OPEN' order by post_num desc", nativeQuery = true)
	Page<Post> searchContent(String content, Pageable pageable);

	@Query(value = "select * from Post where member_num = :memberNum and post_type = 'OPEN' order by post_num desc", nativeQuery = true)
	Page<Post> searchWriter(Long memberNum, Pageable pageable);

	/* 커뮤니티게시판 쿼리 */
	// 커뮤니티게시판 전체글 조회하기
	@Query(value = "select * from Post where post_type = 'CUSTOM' and community_num = :comNum order by post_num desc", nativeQuery = true)
	Page<Post> findAllCustomPost(Long comNum, Pageable pageable);

	// 커뮤니티게시판 검색 3개
	@Query(value = "select * from Post where title like '%'||:title||'%' and post_type = 'CUSTOM' and community_num = :comNum order by post_num desc", nativeQuery = true)
	Page<Post> comSearchTitle(Long comNum, String title, Pageable pageable);

	@Query(value = "select * from Post where content like '%'||:content||'%' and post_type = 'CUSTOM' and community_num = :comNum order by post_num desc", nativeQuery = true)
	Page<Post> comSearchContent(Long comNum, String content, Pageable pageable);

	@Query(value = "select * from Post where member_num = :memberNum and post_type = 'CUSTOM' and community_num = :comNum order by post_num desc", nativeQuery = true)
	Page<Post> comSearchWriter(Long comNum, Long memberNum, Pageable pageable);

	@Query(value = "select * from Post where community_num = :comNum", nativeQuery = true)
	List<Post> findBycomNum(Long comNum);

	/* 공지사항 게시판 쿼리 */
	// 공지사항 목록 조회
	@Query(value = "select * from Post where post_type = 'NOTICE' order by post_num desc", nativeQuery = true)
	Page<Post> findAllNotices(Pageable pageable);
	//@Query(value = "select post_num, community_num, member_num, view_cnt, content, post_date, post_type, title, update_date from Post where post_type = 'NOTICE' order by post_num desc", nativeQuery = true)

	// 공지사항 검색
	@Query(value = "select * from Post where title like '%'||:title||'%' and post_type = 'NOTICE' order by post_num desc", nativeQuery = true)
	Page<Post> searchNoticeTitle(String title, Pageable pageable);

	@Query(value = "select * from Post where content like '%'||:content||'%' and post_type = 'NOTICE' order by post_num desc", nativeQuery = true)
	Page<Post> searchNoticeContent(String content, Pageable pageable);
	
	@Query(value = "select * from Post where member_num = :memNum", nativeQuery = true)
	List<Post> selectByMemberNum(Long memNum);
}