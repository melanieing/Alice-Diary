package com.alice.project.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.alice.project.domain.Calendar;

@Transactional(readOnly = true)
@Repository
public interface CalendarRepository extends JpaRepository<Calendar, Long> {
	@Query(value = "SELECT * FROM Calendar c INNER JOIN Member m ON m.member_num = :num AND c.mem_num = m.member_num WHERE :today BETWEEN c.alarm AND c.start_date ORDER BY c.start_date", nativeQuery = true)
	List<Calendar> getAlarmEvents(Long num, LocalDate today);

	@Query("SELECT c " + "FROM Calendar AS c WHERE mem_num = :num ORDER BY start_date")
	List<Calendar> findByMemNum(Long num);

	@Query(value = "SELECT * FROM Calendar WHERE mem_num = :num AND content LIKE '%'||:content||'%' ORDER BY start_date", nativeQuery = true)
	List<Calendar> findByContent(Long num, String content);

	@Query(value = "SELECT * FROM Calendar WHERE mem_num = :num AND start_date >= :start ORDER BY start_date", nativeQuery = true)
	List<Calendar> findByStart(Long num, LocalDate start);

	@Query(value = "SELECT * FROM Calendar WHERE mem_num = :num AND end_date <= :end ORDER BY start_date", nativeQuery = true)
	List<Calendar> findByEnd(Long num, LocalDate end);

	@Query(value = "SELECT * FROM Calendar WHERE mem_num = :num AND start_date >= :start AND end_date <= :end ORDER BY start_date", nativeQuery = true)
	List<Calendar> findByStartEnd(Long num, LocalDate start, LocalDate end);

	@Query(value = "SELECT * FROM Calendar WHERE mem_num = :num AND end_date <= :end AND content LIKE '%'||:content||'%' ORDER BY start_date", nativeQuery = true)
	List<Calendar> findByContentEnd(Long num, String content, LocalDate end);

	@Query(value = "SELECT * FROM Calendar WHERE mem_num = :num AND start_date >= :start AND content LIKE '%'||:content||'%' ORDER BY start_date", nativeQuery = true)
	List<Calendar> findByContentStart(Long num, String content, LocalDate start);

	@Query(value = "SELECT * FROM Calendar WHERE mem_num = :num AND start_date >= :start AND end_date <= :end AND content LIKE '%'||:content||'%' ORDER BY start_date", nativeQuery = true)
	List<Calendar> findByAll(Long num, String content, LocalDate start, LocalDate end);

	@Query("SELECT c FROM Calendar AS c WHERE mem_num = :num AND c.publicity = true")
	List<Calendar> findOtherEvents(Long num);

	@Query("SELECT c FROM Calendar AS c WHERE calendar_num = :num")
	Calendar findByNum(Long num);

	@Query("SELECT c FROM Calendar AS c WHERE mem_num = :num AND start_date BETWEEN :today AND :startDate AND publicity = true")
	List<Calendar> findFriendEvents(Long num, LocalDate today, LocalDate startDate);

	@Query("SELECT c.num FROM Calendar AS c WHERE mem_num = :num AND color=:happyBirthDay")
	List<Long> findBirthEvents(Long num, String happyBirthDay);

}
