package com.alice.project.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.alice.project.domain.AttachedFile;

@Repository
public interface AttachedFileRepository extends JpaRepository<AttachedFile, Long> {

	List<AttachedFile> findAllByPostNum(Long post, Pageable pageable);

	AttachedFile findByNum(Long num);

	@Modifying
	@Transactional
	@Query(value = "delete from Attached_File where post_num = :num", nativeQuery = true)
	Integer deletePostwithFile(Long num);

	@Modifying
	@Query(value = "delete from Attached_File where file_num = :num", nativeQuery = true)
	Integer deleteOneFile(Long num);

	List<AttachedFile> findAllByPostNum(Long postNum);

	AttachedFile findByMessageNum(Long msgNum);
}
