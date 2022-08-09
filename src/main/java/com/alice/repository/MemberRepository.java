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
import com.alice.project.domain.Status;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, QuerydslPredicateExecutor<Member> {

	// 소셜 로그인으로 반환되는 값 중에서 email을 통해 이미 생성된 사용자인지 처음 가입한 사용자인지 판단
//	Optional<Member> findByEmail(String email);
	Member findByEmail(String email);

	@Query(value = "select member_num from Member where id = :id", nativeQuery = true)
	Long findMemberNumById(String id);

	// 회원 아이디로 찾기
	Member findById(String id);

	// id중복 체크를 위한 메서드
	boolean existsById(String id);

	// nickname 중복 체크를 위한 메서드
	boolean existsByName(String name);

	// id찾기
	Member findByNameAndMobileAndEmail(String name, String mobile, String email);

	// 비밀번호 찾기
	Member findByIdAndNameAndMobile(String id, String name, String mobile);

	// 회원번호로 찾기
	Member findByNum(Long num);

	boolean existsByEmail(String email);

	@Query(value = "select * FROM Member where email = :email", nativeQuery = true)
	Member searchByEmailForToken(String email);

	@Query("SELECT m FROM Member AS m WHERE num IN (SELECT addeeNum FROM Friend WHERE adder_num = :adderNum) AND (name LIKE '%'||:friends||'%' OR id LIKE '%'||:friends||'%')")
	List<Member> findByIdOrName(Long adderNum, String friends);

	Member findByName(String name);

	@Query(value = "select * from Member where ID like '%'||:id||'%'", nativeQuery = true)
	Member findMemberById(String id);
	
	@Query(value = "select * from Member where NAME like '%'||:name||'%'", nativeQuery = true)
	Page<Member> findMemberByName(String name,Pageable pageable);

	@Query("SELECT m FROM Member AS m WHERE id LIKE '%'||:keyword||'%'")
	Page<Member> searchById(String keyword, Pageable pageable);

	@Query("SELECT m FROM Member AS m WHERE name LIKE '%'||:keyword||'%'")
	List<Member> searchByName(String keyword);
	
	@Query("SELECT m FROM Member AS m WHERE num LIKE '%'||:keyword||'%'")
	Page<Member> searchByNum(Long keyword, Pageable pageable);

	Page<Member> findByNameContaining(String keyword, Pageable pageable);

	Page<Member> findByMobileContaining(String keyword, Pageable pageable);

	Page<Member> findByEmailContaining(String keyword, Pageable pageable);

	Page<Member> findByReportCnt(Long keyword, Pageable pageable);

	Page<Member> findByStatus(Status status, Pageable pageable);

	List<Member> findByIdContaining(String keyword);

	// 멤버 신고수 올리기
	@Modifying
	@Transactional
	@Query(value = "update Member set report_cnt = report_cnt + 1 where member_num = :num", nativeQuery = true)
	Integer reportCntUp(Long num);

	// 회원번호로 아이디 찾기
	@Query(value = "select id from Member where member_num = :num", nativeQuery = true)
	String findIdByNum(Long num);

	Member findAllById(String[] members);
	
	List<Member> findByNameContaining(String keyword);
	
}