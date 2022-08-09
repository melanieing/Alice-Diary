package com.alice.project.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alice.project.domain.Message;

@Repository
public interface MessageRepositoryCustom {

	Message findRecentMsgByNum(Long messageFromNum, Long messageToNum);

	Message deleteMsgByNum(Long messageFromNum, Long messageToNum);

	/* 보낸 회원 번호로 메시지리스트 반환 */
	List<Message> findByMessageFromNum(Long messageFromNum);

	/* 보낸 회원 번호의 살아있는 메시지리스트 반환 */
	List<Message> findByLiveMessageFromNum(Long messageFromNum);

	/* 받는 회원 번호의 살아있는 메시지리스트 반환 */
	List<Message> findByLiveMessageToNum(Long messageFromNum);

	/* 보낸 회원번호와 받은 회원번호로 메시리리스트 반환 */
	List<Message> findByMessageFromNumAndMessageToNum(Long messageFromNum, Long messageToNum);

	/* 쪽지함 삭제 = 두 사람 관계에서 한 사람만 0으로 바꾸기 */
	Integer updateMsgRelationFrom(Long messageFromNum, Long messageToNum);

	Integer updateMsgRelationTo(Long messageFromNum, Long messageToNum);

	/* 쪽지함 하나 보여주기 */
	List<Message> findMsgs(Long mfn, Long mtn);

	/* 정상적인 쪽지함 하나 보여주기 */
	public List<Message> findLiveMsgs(Long mfn, Long mtn);

	List<Message> findByUserNum(Long userNum);

	List<Message> searchByUserNum(Long userNum);

	List<Message> searchByContent(String content, Long userNum);

	List<Message> findByUserConv(Long userNum, Long youNum);

	Integer changeMsgStatus(Long fromNum, Long toNum, Boolean flag);

}