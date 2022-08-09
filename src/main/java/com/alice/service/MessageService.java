package com.alice.project.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alice.project.domain.Member;
import com.alice.project.domain.Message;
import com.alice.project.event.MessageCreatedEvent;
import com.alice.project.repository.MemberRepository;
import com.alice.project.repository.MessageRepository;
import com.alice.project.web.MessageDto;
import com.alice.project.web.MsgFileDto;
import com.alice.project.web.MsgListDto;
import com.alice.project.web.SearchDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional(readOnly = true) // 기본적으로 못바꾸게 해놓고
@RequiredArgsConstructor // final 붙은 필드만 가진 생성자 만들어줌
@Slf4j
public class MessageService {

	private final MemberService ms;
	private final AttachedFileService afs;
	private final MessageRepository messageRepository;
	private final MemberRepository memberRepository;
	private final HttpSession httpSession;
	private final ApplicationEventPublisher eventPublisher; // for notification

	public List<Message> findUserMsg(Long userNum) {
		HashMap<Long, Message> map = new HashMap<>();
		List<Message> msgList = new ArrayList<>();
		Long key;
		msgList = messageRepository.findByUserNum(userNum);
		if (msgList == null) {
			return null;
		}
		for (Message m : msgList) {
			if (userNum == m.getUser1Num()) {
				key = m.getUser2Num();
			} else {
				key = m.getUser1Num();
			}
			if (map.get(key) == null) {
				map.put(key, m);
			} else {
				if (m.getSendDate().isAfter(map.get(key).getSendDate())) {
					map.put(key, m);
				}
			}
		}
		List<Message> resultList = new ArrayList<>();

		for (Long k : map.keySet()) {
			resultList.add(map.get(k));
		}
		Collections.sort(resultList);
		return resultList;
	}

	public List<Message> findUserConv(Long userNum, Long youNum) {
		List<Message> msgList = new ArrayList<>();
		msgList = messageRepository.findByUserConv(userNum, youNum);

		if (msgList == null) {
			return null;
		} else {
			Collections.sort(msgList);
		}

		return msgList;
	}

	@Transactional
	public Integer changeMsgStatus(String fromId, String toId) {
		Long fromNum = findNumById(fromId);
		Long toNum = findNumById(toId);
		Boolean flag = false;
		if (fromNum < toNum) {
			flag = true;
		}

		messageRepository.changeMsgStatus(fromNum, toNum, flag);
		return 1;
	}

	// 보낸사람이 000인 받은사람 번호 리스트 받아오기
	public List<Message> findMsgsBySenderNum(Long num) {
		return messageRepository.findByMessageFromNum(num);
	}

	public String findIdByNum(Long num) {
		Member member = memberRepository.findByNum(num);
		return member.getId();
	}

	/* 아이디로 회원번호 찾기 */
	public Long findNumById(String id) {
		if (memberRepository.findById(id) == null) {
			return 0L;
		}
		Member member = memberRepository.findById(id);
		return member.getNum();
	}

	public Message findRecentMsgs(Long mfn, Long mtn) {
		if (messageRepository.findRecentMsgByNum(mfn, mtn) == null) {
			return null;
		}
		Message msg = messageRepository.findRecentMsgByNum(mfn, mtn);
		log.info("MS의 message : " + msg.toString());
		return msg;
	}

	public List<Message> findMsgs(Long mfn, Long mtn) {
		List<Message> msgF = messageRepository.findMsgs(mfn, mtn);
		List<Message> msgT = messageRepository.findMsgs(mtn, mfn);
		List<Message> msgs = new ArrayList<>();
		msgs.addAll(msgF);
		msgs.addAll(msgT);
		Collections.reverse(msgs);
		return msgs;
	}

	// 사용자측에서 삭제하지 않은 메시지만 가져오기
	public List<Message> findLiveMsgs(Long mfn, Long mtn) {
		List<Message> msgF = messageRepository.findLiveMsgs(mfn, mtn);
		List<Message> msgs = new ArrayList<>();
		msgs.addAll(msgF);
		Collections.sort(msgs);

		return msgs;
	}

	/* f번이 t번과의 쪽지함 삭제(관계상태0으로 업데이트) */
	@Transactional
	public Integer cutMsgRelations(Long messageFromNum, Long messageToNum) {
		Integer fresult = messageRepository.updateMsgRelationFrom(messageFromNum, messageToNum);
		Integer tresult = messageRepository.updateMsgRelationTo(messageFromNum, messageToNum);

		return fresult + tresult; // 2이면 성공
	}

	/* 쪽지 전송 */
	@Transactional
	public Message sendMsg(MessageDto mdto) {
		Long msgStatus = 3L; // 기본 쪽지 상태 (양쪽 모두 안 지운 상태)

		Message message = new Message(mdto.getUser1Num(), mdto.getUser2Num(), mdto.getSendDate(), mdto.getContent(),
				msgStatus, mdto.getDirection());
		String senderId = "";
		Long receiverNum = 0L;
		if (mdto.getDirection() == 0) {
			senderId = ms.findByNum(mdto.getUser1Num()).getId(); // 보내는 사람 아이디
			receiverNum = mdto.getUser2Num();
		} else if (mdto.getDirection() == 1) {
			senderId = ms.findByNum(mdto.getUser2Num()).getId(); // 보내는 사람 아이디
			receiverNum = mdto.getUser2Num();
		}
		if (mdto.getOriginName() != null) {
			afs.saveMsgFile(mdto.getOriginName(), message, httpSession, senderId);
		}
		log.info("!!!!!!!!!!!!요기!!!!!! : " + message.toString());
		Message result = messageRepository.save(message);
		
		result.setMember(ms.findByNum(receiverNum)); // 쪽지에 '받는' 회원객체 넣어주기
		eventPublisher.publishEvent(new MessageCreatedEvent(result)); // for notification
		return result;
	}

	public List<MsgListDto> searchMsgByContent(String content, Long num) {
		List<Message> msgList = messageRepository.searchByContent(content, num);
		List<MsgListDto> mldtos = new ArrayList<>();

		Long receiverNum = 0L;
		if (msgList == null) {
			return null;
		}
		for (Message m : msgList) {
			if (num == m.getUser1Num()) {
				if (m.getMsgStatus() < 2) {
					continue;
				}
				receiverNum = m.getUser2Num();
			} else {
				if (m.getMsgStatus() % 2 == 0) {
					continue;
				}
				receiverNum = m.getUser1Num();
			}
			MsgListDto mldto = new MsgListDto();
			mldto.setUser1Num(m.getUser1Num());
			mldto.setUser2Num(m.getUser2Num());
			mldto.setSendDate(m.getSendDate());
			mldto.setRecentContent(m.getContent());
			mldto.setMessageToId(ms.findOne(receiverNum).getId());
			mldto.setMessageFromId(ms.findOne(num).getId());
			mldto.setDirection(m.getDirection());
			mldtos.add(mldto);
		}

		return mldtos;
	}

	/* 쪽지함 사진 모아보기 기능 */
	public List<MsgFileDto> findMsgPictures(Long num) {
		List<MsgFileDto> mpdtos = new ArrayList<>();

		List<Message> msglist = messageRepository.findByUserNum(num);
		if (msglist == null) {
			return null;
		}

		for (Iterator<Message> it = msglist.iterator(); it.hasNext();) {
			Message m = it.next();
			if (m.getFile() == null) {
				it.remove();
			}
		}

		for (Message m : msglist) {
			MsgFileDto mpdto = new MsgFileDto();
			String originName = m.getFile().getOriginName();
			String saveNmae = m.getFile().getSaveName();
			if (originName != null) {
				if (originName.endsWith(".jpg") || originName.endsWith(".png") || originName.endsWith(".jpeg") || originName.endsWith(".tif")) {
					mpdto.setOriginName(originName);
					mpdto.setSaveName(saveNmae);
					mpdto.setSendDate(m.getSendDate());
					Long theOtherNum = m.getUser1Num() == num ? m.getUser2Num() : m.getUser1Num();
					mpdto.setTheOtherId(ms.findByNum(theOtherNum).getId());
					mpdto.setTheOtherName(ms.findByNum(theOtherNum).getName());
					mpdto.setFileNum(m.getFile().getNum());
					mpdtos.add(mpdto);
				}
			} else {
				continue;
			}
		}

		return mpdtos;
	}

	/* 쪽지함 사진 모아보기 검색기능 */
	public List<MsgFileDto> searchMsgPicturesByKeyword(Long num, SearchDto searchDto) {
		List<MsgFileDto> mpdtos = new ArrayList<>();
		List<MsgFileDto> resultlist = new ArrayList<>();
		String type = searchDto.getType();
		String keyword = searchDto.getKeyword();

		mpdtos = findMsgPictures(num);
		Iterator<MsgFileDto> iterator = mpdtos.iterator();
		while (iterator.hasNext()) {
			MsgFileDto mpdto = iterator.next();
			if (type.equals("filename")) {
				if (mpdto.getOriginName().contains(keyword)) {
					resultlist.add(mpdto);
				}
			} else if (type.equals("name")) {
				if (mpdto.getTheOtherName().contains(keyword)) {
					resultlist.add(mpdto);
				}
			}
		}
		return resultlist;
	}

	/* 쪽지함 문서 모아보기 기능 */
	public List<MsgFileDto> findMsgDocs(Long num) {
		List<MsgFileDto> mpdtos = new ArrayList<>();
		List<Message> msglist = messageRepository.findByUserNum(num);

		if (msglist == null) {
			return null;
		}

		for (Iterator<Message> it = msglist.iterator(); it.hasNext();) {
			Message m = it.next();
			if (m.getFile() == null) {
				it.remove();
			}
		}
		for (Message m : msglist) {
			MsgFileDto mpdto = new MsgFileDto();
			String originName = m.getFile().getOriginName();
			String saveNmae = m.getFile().getSaveName();
			if (originName != null) {
				if (originName.endsWith(".txt") || originName.endsWith(".pdf") || originName.endsWith(".docx")
						|| originName.endsWith(".hwpx") || originName.endsWith(".xlsx") || originName.endsWith(".xls")
						|| originName.endsWith(".pptx")) {
					mpdto.setOriginName(originName);
					mpdto.setSaveName(saveNmae);
					mpdto.setSendDate(m.getSendDate());
					Long theOtherNum = m.getUser1Num() == num ? m.getUser2Num() : m.getUser1Num();
					mpdto.setTheOtherId(ms.findByNum(theOtherNum).getId());
					mpdto.setFileNum(m.getFile().getNum());
					mpdto.setTheOtherName(ms.findByNum(theOtherNum).getName());
					mpdtos.add(mpdto);
				}
			} else {
				continue;
			}
		}
		return mpdtos;
	}

	/* 쪽지함 문서 모아보기 검색기능 */
	public List<MsgFileDto> searchMsgDocsByKeyword(Long num, SearchDto searchDto) {
		List<MsgFileDto> mfdtos = new ArrayList<>();
		List<MsgFileDto> resultlist = new ArrayList<>();
		String type = searchDto.getType();
		String keyword = searchDto.getKeyword();

		mfdtos = findMsgDocs(num);
		Iterator<MsgFileDto> iterator = mfdtos.iterator();
		while (iterator.hasNext()) {
			MsgFileDto mfdto = iterator.next();
			if (type.equals("filename")) {
				if (mfdto.getOriginName().contains(keyword)) {
					resultlist.add(mfdto);
				}
			} else if (type.equals("name")) {
				if (mfdto.getTheOtherName().contains(keyword)) {
					resultlist.add(mfdto);
				}
			}
		}
		return resultlist;
	}

	/* 댓글 쪽지 전송 */
	@Transactional
	public Message replyMsg(Message message) {
		Long dir = message.getDirection();
		Long receiverNum = 0L;
		if (dir == 0) {
			receiverNum = message.getUser2Num();
		} else if (dir == 1) {
			receiverNum = message.getUser1Num();
		}
		Message result = messageRepository.save(message);
		result.setMember(ms.findByNum(receiverNum)); // 쪽지에 '받는' 회원객체 넣어주기
		eventPublisher.publishEvent(new MessageCreatedEvent(result)); // for notification
		return messageRepository.save(message);
	}

	/* 커뮤니티 초대장 발송 */
	@Transactional
	public Message inviteMsg(Message message) {
		return messageRepository.save(message);
	}
}