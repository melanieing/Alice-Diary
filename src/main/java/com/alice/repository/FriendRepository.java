package com.alice.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.alice.project.domain.Friend;

@Repository
@Transactional(readOnly = true)
public interface FriendRepository extends JpaRepository<Friend, Long>, QuerydslPredicateExecutor<Friend> {
	
	@Query("SELECT m FROM Friend AS m WHERE adder_num = :adderNum")
	List<Friend> findByAdderNum(Long adderNum);

	@Transactional
	@Query(value = "SELECT * FROM Friend WHERE adder_num = :adderNum AND addee_num = :addeeNum", nativeQuery = true)
	Friend findGroupByAddeeAdderNum(Long adderNum, Long addeeNum);

	@Transactional
	@Query(value = "SELECT * FROM Friend WHERE adder_num = :adderNum AND addee_num = :addeeNum", nativeQuery = true)
	List<Friend> checkAlreadyFriend(Long adderNum, Long addeeNum);

	@Query(value = "SELECT * FROM Friend WHERE addee_num = :num AND adder_num IN (SELECT addee_num FROM Friend WHERE adder_num = :num)", nativeQuery = true)
	List<Friend> weAreFriend(Long num);

	@Transactional
	@Query(value = "SELECT addee_num From Friend Where friend_num = :friendNum", nativeQuery = true)
	Long searchAddeeNumByFriendNum(Long friendNum);

	Long findFriendByAddeeNum(Long addeeNum);

	@Query("SELECT m FROM Friend AS m WHERE addee_num = :addeeNum")
	List<Friend> findByAddeeNum(Long addeeNum);

	@Query("SELECT m FROM Friend AS m WHERE friend_num = :num")
	Long findByFriendNum(Long num);
	
	// 알림 때문에 추가한 쿼리
	@Query("SELECT m FROM Friend AS m WHERE friend_num = :num")
	Friend searchByFriendNum(Long num);

}