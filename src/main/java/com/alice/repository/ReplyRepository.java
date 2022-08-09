package com.alice.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.alice.project.domain.Reply;

@Repository
@Transactional(readOnly = true)
public interface ReplyRepository extends JpaRepository<Reply, Long> {

	List<Reply> findByPostNum(Long num);

	@Modifying
	@Transactional
	@Query(value = "delete from Reply where post_num = :num", nativeQuery = true)
	Integer deletePostwithReply(Long num);

	@Query(value = "SELECT * FROM Reply WHERE post_num = :num AND parent_rep_num IS NULL ORDER BY rep_date ASC", nativeQuery = true)
	List<Reply> findParentReplysByNum(Long num);

	@Query(value = "SELECT * FROM Reply WHERE parent_rep_num = :num ORDER BY rep_date DESC", nativeQuery = true)
	List<Reply> findChildByParentNum(Long num);

	Reply findByNum(Long num);

	@Modifying
	@Transactional
	@Query(value = "UPDATE REPLY set status = 'DEAD' WHERE reply_num = :num", nativeQuery = true)
	Integer deleteParentHaveChild(Long num);

	@Query(value = "select post_num from Reply where reply_num = :num", nativeQuery = true)
	Integer searchPostNumByReplyNum(Long num);

	@Query(value = "select count(*) from Reply where post_num = :postNum", nativeQuery = true)
	Long getCountReply(Long postNum);
	
	@Query(value = "select * from Reply where member_num = :memNum", nativeQuery = true)
	List<Reply> selectByMemberNum(Long memNum);

}
