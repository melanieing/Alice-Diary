package com.alice.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.alice.project.domain.FriendsGroup;

@Repository
@Transactional(readOnly = true)
public interface FriendsGroupRepository extends JpaRepository<FriendsGroup, Long> {

	// 기본그룹 있는지 확인
	@Query("SELECT m FROM FriendsGroup AS m WHERE group_creator_num = :creatorNum AND group_name = :groupName")
	FriendsGroup searchByCreatorAndGroupName(Long creatorNum, String groupName);

	// 그룹 넘버
	@Query("SELECT m FROM FriendsGroup AS m WHERE group_num = :num")
	FriendsGroup findByNum(Long num);

	// 그룹 이름
	@Query("SELECT m FROM FriendsGroup AS m WHERE group_name = :groupName")
	List<FriendsGroup> findByName(String groupName);

	// 그룹생성 회원 번호
	@Query("SELECT m FROM FriendsGroup AS m WHERE group_creator_num = :groupCreatorNum")
	Long findByGroupCreatorNum(Long groupCreatorNum);

	// adderNum이 등록한 그룹 목록
	@Query(value = "select * from Friends_group where group_creator_num = :memNum order by group_num", nativeQuery = true)
	List<FriendsGroup> findAllByAdder(Long memNum);

}