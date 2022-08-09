package com.alice.project.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.alice.project.domain.Message;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class MessageRepositoryImpl implements MessageRepositoryCustom {

	@Autowired
	EntityManager entityManager;

	// 사용자 번호로 해당되는 모든 메시지를 가져오기
	@Override
	public List<Message> findByUserNum(Long userNum) {
		List<Message> resultList = entityManager
				.createQuery("SELECT m FROM Message as m WHERE user1Num=?1 or user2Num=?2", Message.class)
				.setParameter(1, userNum).setParameter(2, userNum).getResultList();
		return resultList;
	}

	// 사용자 번호로 해당되는 모든 메시지를 가져오기
	@Override
	public List<Message> findByUserConv(Long userNum, Long youNum) {
		List<Message> resultList = new ArrayList<>();
		if (userNum > youNum) {
			resultList = entityManager
					.createQuery("SELECT m FROM Message as m WHERE (user1Num=?1 and user2Num=?2) "
							+ "and (msgStatus=?3 or msgStatus=?4)", Message.class)
					.setParameter(1, youNum).setParameter(2, userNum).setParameter(3, 3L).setParameter(4, 1L)
					.getResultList();
		} else {
			resultList = entityManager
					.createQuery("SELECT m FROM Message as m WHERE (user1Num=?1 and user2Num=?2) "
							+ "and (msgStatus=?3 or msgStatus=?4)", Message.class)
					.setParameter(1, userNum).setParameter(2, youNum).setParameter(3, 3L).setParameter(4, 2L)
					.getResultList();
		}
		return resultList;
	}

	// 메시지번호로 해당 메시지의 상태 바꾸기 (msgStatus : 0, 1, 2)
	@Override
	public Integer changeMsgStatus(Long fromNum, Long toNum, Boolean flag) {
		String sql = "UPDATE Message m " + "SET msgStatus = msgStatus - :constant "
				+ "WHERE user1Num = :user1Num and user2num= :user2Num " + "and (msgStatus = 3 or msgStatus = :status)";
		int result = 0;
		if (flag) {
			result = entityManager.createQuery(sql).setParameter("constant", 2L).setParameter("user1Num", fromNum)
					.setParameter("user2Num", toNum).setParameter("status", 2L).executeUpdate();
		} else {
			result = entityManager.createQuery(sql).setParameter("constant", 1L).setParameter("user1Num", toNum)
					.setParameter("user2Num", fromNum).setParameter("status", 1L).executeUpdate();
		}
		return result;
	}

	// 메시지 검색
	@Override
	public List<Message> searchByUserNum(Long userNum) {
		List<Message> resultList = entityManager
				.createQuery("SELECT m FROM Message as m WHERE user1Num = :userNum", Message.class)
				.setParameter(1, userNum).getResultList();
		return resultList;
	}

	@Override
	public List<Message> searchByContent(String content, Long userNum) {
		log.info("userNum : " + userNum);
		log.info("!!!!!!!!content ; " + content);
		List<Message> resultList = entityManager
				.createQuery("SELECT m FROM Message as m WHERE m.content like '%||:content||%' "
						+ "and (m.user1Num = :user1Num OR m.user2Num = :user2Num)", Message.class)
				.setParameter(1, content).setParameter(2, userNum).setParameter(3, userNum).getResultList();
//      if (resultList == null) { return null;}
		return resultList;
	}

	@Override
	public Message findRecentMsgByNum(Long messageFromNum, Long messageToNum) {
		List<Message> resultList = entityManager
				.createQuery("SELECT m FROM Message as m WHERE 1=1 AND sendDate = (SELECT MAX(sendDate) FROM Message "
						+ "WHERE messageFromNum=?1 and messageToNum=?2)", Message.class)
				.setParameter(1, messageFromNum).setParameter(2, messageToNum).getResultList();
		if (resultList.isEmpty()) {
			return null;
		}
		return resultList.get(0);
	}

	@Override
	public Message deleteMsgByNum(Long messageFromNum, Long messageToNum) {
		List<Message> resultList = entityManager
				.createQuery(
						"DELETE FROM Message AS m"
								+ "WHERE m.messageFromNum = :messageFromNum and m.messageToNum = :messageToNum)",
						Message.class)
				.setParameter(1, messageFromNum).setParameter(2, messageToNum).getResultList();
		return resultList.get(0);
	}

	@Override
	public List<Message> findByMessageFromNum(Long messageFromNum) {
		List<Message> resultList = entityManager
				.createQuery("SELECT m FROM Message as m WHERE messageFromNum=?1", Message.class)
				.setParameter(1, messageFromNum).getResultList();
		return resultList;
	}

	// 정상적인 메시지목록 반환
	public List<Message> findByLiveMessageFromNum(Long messageFromNum) {
		List<Message> resultList = entityManager
				.createQuery("SELECT m FROM Message as m WHERE messageFromNum=?1 and senderStatus=1", Message.class)
				.setParameter(1, messageFromNum).getResultList();
		return resultList;
	}

	public List<Message> findByLiveMessageToNum(Long messageToNum) {
		List<Message> resultList = entityManager
				.createQuery("SELECT m FROM Message as m WHERE messageToNum=?1 and receiverStatus=1", Message.class)
				.setParameter(1, messageToNum).getResultList();
		return resultList;
	}

	// 쪽지함 하나 보여주기
	public List<Message> findMsgs(Long mfn, Long mtn) {
		List<Message> resultList = entityManager
				.createQuery("SELECT m FROM Message as m "
						+ "WHERE messageFromNum=?1 and messageToNum=?2 ORDER BY sendDate DESC", Message.class)
				.setParameter(1, mfn).setParameter(2, mtn).getResultList();
		return resultList;
	}

	public List<Message> findLiveMsgs(Long mfn, Long mtn) {
		List<Message> resultList = entityManager.createQuery("SELECT m FROM Message as m "
				+ "WHERE messageFromNum=?1 and messageToNum=?2 and senderStatus=1 or receiverStatus=1 ORDER BY sendDate DESC",
				Message.class).setParameter(1, mfn).setParameter(2, mtn).getResultList();
		return resultList;
	}

	@Override
	public List<Message> findByMessageFromNumAndMessageToNum(Long messageFromNum, Long messageToNum) {
		List<Message> resultList = entityManager
				.createQuery("SELECT m FROM Message as m WHERE messageFromNum=?1 and messageToNum=?2", Message.class)
				.setParameter(1, messageFromNum).setParameter(2, messageToNum).getResultList();
		return resultList;
	}

	@Override
	public Integer updateMsgRelationFrom(Long messageFromNum, Long messageToNum) {
		String sql = "UPDATE Message m " + "SET senderStatus = 0 "
				+ "WHERE m.messageFromNum= :messageFromNum and messageToNum= :messageToNum";

		int fromResult = entityManager.createQuery(sql).setParameter("messageFromNum", messageFromNum)
				.setParameter("messageToNum", messageToNum).executeUpdate();

		return fromResult;

	}

	@Override
	public Integer updateMsgRelationTo(Long messageFromNum, Long messageToNum) {
		String sql = "UPDATE Message m " + "SET m.receiverStatus = 0 "
				+ "WHERE m.messageFromNum= :messageFromNum and messageToNum= :messageToNum";

		int toResult = entityManager.createQuery(sql).setParameter("messageFromNum", messageFromNum)
				.setParameter("messageToNum", messageToNum).executeUpdate();
		return toResult;
	}

}