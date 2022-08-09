package com.alice.project.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alice.project.domain.Friend;
import com.alice.project.domain.Member;
import com.alice.project.domain.Message;
import com.alice.project.handler.Alert;
import com.alice.project.repository.NotificationRepository;
import com.alice.project.service.FriendService;
import com.alice.project.service.MemberService;
import com.alice.project.service.MessageService;
import com.alice.project.web.FriendshipDto;
import com.alice.project.web.MessageDto;
import com.alice.project.web.MsgFileDto;
import com.alice.project.web.MsgListDto;
import com.alice.project.web.MsgSearchDto;
import com.alice.project.web.SearchDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MessageController {

	private final MessageService messageService;
	private final MemberService memberService;
	private final FriendService friendService;
	private final NotificationRepository notificationRepository;


	// 쪽지 목록 보기
	@GetMapping(value = "/messagebox/{id}")
	public String messageList(@PathVariable("id") String id, Model model, @ModelAttribute MessageDto mdto,
			@ModelAttribute SearchDto sdto, MsgSearchDto msdto, @AuthenticationPrincipal UserDetails user) {
		Long num = messageService.findNumById(id); // tester의 userNum = 1
		String type = sdto.getType();
		String keyword = sdto.getKeyword();
		model.addAttribute("type", type);
		model.addAttribute("keyword", keyword);

		model.addAttribute("mdto", mdto);
		model.addAttribute("fromId", id);
		model.addAttribute("msdto", msdto);
		Member host = memberService.findById(user.getUsername());
		model.addAttribute("member", host);

		// 친구목록 가져오기
		List<Friend> friendsList = friendService.friendship(num);
		List<FriendshipDto> friendsDtoList = new ArrayList<>();
		for (Friend f : friendsList) {
			FriendshipDto tmp = new FriendshipDto();
			Member m = memberService.findByNum(f.getAddeeNum());
			tmp.setNum(m.getNum());
			tmp.setName(m.getName());
			tmp.setId(m.getId());
			friendsDtoList.add(tmp);
		}
		model.addAttribute("friendsList", friendsDtoList);

		List<Message> msgList = new ArrayList<>();
		msgList = messageService.findUserMsg(num);
		if (msgList.size() == 0) {
			model.addAttribute("mldtos", msgList);
			model.addAttribute("fromNum", num);
			model.addAttribute("mdto", mdto);
			return "message/msgList";
		}

		List<MsgListDto> mldtos = new ArrayList<>();
		Long receiverNum = 0L;
		for (Message m : msgList) {
			if (num == m.getUser1Num()) { // user1Num이 사용자라면 (2, 3만 보여야 함)
				if (m.getMsgStatus() < 2) { // 0, 1
					continue;
				}
				receiverNum = m.getUser2Num();
			} else { // user2Num이 사용자라면 (1, 3만 보여야 함)
				if (m.getMsgStatus() % 2 == 0) { // 0, 2
					continue;
				}
				receiverNum = m.getUser1Num();
			}
			MsgListDto mldto = new MsgListDto();
			Member receiver = memberService.findOne(receiverNum);
			Member sender = memberService.findOne(num);
			mldto.setUser1Num(m.getUser1Num());
			mldto.setUser2Num(m.getUser2Num());
			mldto.setSendDate(m.getSendDate());
			mldto.setRecentContent(m.getContent());
			mldto.setMessageToId(receiver.getId());
			mldto.setMessageFromId(sender.getId());
			mldto.setSenderProfileImg(receiver.getProfileImg());
			mldto.setMessageFromName(sender.getName());
			mldto.setMessageToName(receiver.getName());
			log.info("프로필이미지" + mldto.getSenderProfileImg().toString());
			mldto.setDirection(m.getDirection());
			mldtos.add(mldto);
		}

		model.addAttribute("receiverNum", receiverNum);
		model.addAttribute("fromNum", num);
		model.addAttribute("mdto", mdto);

		if (type != null && keyword != null) {
			for (Iterator<MsgListDto> it = mldtos.iterator(); it.hasNext();) {
				MsgListDto mldto = it.next();
				if (type.equals("id")) {
					if (!mldto.getMessageToId().contains(keyword)) {
						it.remove();
					}
				} else if (type.equals("content")) {
					if (!mldto.getRecentContent().contains(keyword)) {
						it.remove();
					}
				}
			}
		} else if (type == null || keyword == null) {
			model.addAttribute("mldtos", mldtos);
	        long count = notificationRepository.countByMemberAndChecked(host, false);
	        model.addAttribute("hasNotification", count > 0);
			return "message/msgList";
		}

		model.addAttribute("mldtos", mldtos);
        long count = notificationRepository.countByMemberAndChecked(host, false);
        model.addAttribute("hasNotification", count > 0);
		return "message/msgList";
	}

	// 개별 쪽지함 상세보기
	@GetMapping("/messagebox/{fromId}/{toId}")
	public String showMessages(@PathVariable("fromId") String fromId, @PathVariable("toId") String toId, Model model,
			@AuthenticationPrincipal UserDetails user, HttpSession session, MessageDto mdto) {
		Long fromNum = messageService.findNumById(fromId);
		Long toNum = messageService.findNumById(toId);
		Member theother = memberService.findById(toId);
		model.addAttribute("friendProfile", theother.getProfileImg());
		model.addAttribute("friendName", theother.getName());

		List<Message> msgList = messageService.findUserConv(fromNum, toNum);

		model.addAttribute("fromId", fromId);
		model.addAttribute("mdtos", msgList);

		model.addAttribute("userNum", fromNum);
		model.addAttribute("toNum", toNum);
		model.addAttribute("toId", toId);
		model.addAttribute("mdto", mdto);
		Member host = memberService.findById(user.getUsername());
		model.addAttribute("member", host);
		
		// 쪽지목록도 가져오기!!
		// 친구목록 가져오기
		List<Friend> friendsList = friendService.friendship(fromNum);
		List<FriendshipDto> friendsDtoList = new ArrayList<>();
		for (Friend f : friendsList) {
			FriendshipDto tmp = new FriendshipDto();
			Member m = memberService.findByNum(f.getAddeeNum());
			tmp.setNum(m.getNum());
			tmp.setName(m.getName());
			tmp.setId(m.getId());
			friendsDtoList.add(tmp);
		}
		model.addAttribute("friendsList", friendsDtoList);

		List<Message> msgListAll = new ArrayList<>();
		msgListAll = messageService.findUserMsg(fromNum);
		if (msgListAll.size() == 0) {
			return "message/msgList";
		}

		List<MsgListDto> mldtos = new ArrayList<>();
		Long receiverNum = 0L;
		for (Message m : msgListAll) {
			if (fromNum == m.getUser1Num()) { // user1Num이 사용자라면 (2, 3만 보여야 함)
				if (m.getMsgStatus() < 2) { // 0, 1
					continue;
				}
				receiverNum = m.getUser2Num();
			} else { // user2Num이 사용자라면 (1, 3만 보여야 함)
				if (m.getMsgStatus() % 2 == 0) { // 0, 2
					continue;
				}
				receiverNum = m.getUser1Num();
			}
			MsgListDto mldto = new MsgListDto();
			Member receiver = memberService.findOne(receiverNum);
			Member sender = memberService.findOne(fromNum);
			mldto.setUser1Num(m.getUser1Num());
			mldto.setUser2Num(m.getUser2Num());
			mldto.setSendDate(m.getSendDate());
			mldto.setRecentContent(m.getContent());
			mldto.setMessageToId(receiver.getId());
			mldto.setMessageFromId(sender.getId());
			mldto.setSenderProfileImg(receiver.getProfileImg());
			mldto.setMessageFromName(sender.getName());
			mldto.setMessageToName(receiver.getName());
			mldto.setDirection(m.getDirection());
			mldtos.add(mldto);
		}

		model.addAttribute("receiverNum", receiverNum);
		model.addAttribute("mldtos", mldtos);
		model.addAttribute("fromNum", fromNum);
        long count = notificationRepository.countByMemberAndChecked(host, false);
        model.addAttribute("hasNotification", count > 0);
		return "message/msgList";
	}

	// 쪽지 목록에서 쪽지 보내기
	@PostMapping("/messagebox/{id}")
	public String sendMessage(@PathVariable("id") String id, Model model, HttpSession session,
			@ModelAttribute MessageDto mdto) {
		Long senderNum = messageService.findNumById(id);
		Long receiverNum = messageService.findNumById(mdto.getMessageToId());
		MultipartFile originName = mdto.getOriginName();
		MessageDto fileAlarm = new MessageDto();

		if (senderNum > receiverNum) {
			mdto.setUser1Num(receiverNum);
			mdto.setUser2Num(senderNum);
			mdto.setDirection(1L);
		} else {
			mdto.setUser1Num(senderNum);
			mdto.setUser2Num(receiverNum);
			mdto.setDirection(0L);
		}

		mdto.setMessageFromId(id);
		mdto.setSendDate(LocalDateTime.now());

		Message resultMsg = messageService.sendMsg(mdto); // 쪽지+첨부파일 저장
		log.info("!!!!!!!!!!!setContent : " + mdto.getOriginName());

		if (!mdto.getOriginName().isEmpty()) { // 첨부파일이 있다면
			fileAlarm.setContent("( ﾉ ﾟｰﾟ)ﾉ" + originName.getOriginalFilename() + "를 보냅니다!");
			fileAlarm.setDirection(mdto.getDirection());
			fileAlarm.setUser1Num(mdto.getUser1Num());
			fileAlarm.setUser2Num(mdto.getUser2Num());
			fileAlarm.setMessageFromId(mdto.getMessageFromId());
			fileAlarm.setMessageToId(mdto.getMessageToId());
			fileAlarm.setSendDate(mdto.getSendDate());
			log.info("!!!!!!!!!!!setContent : " + fileAlarm.getContent());
			messageService.sendMsg(fileAlarm); // 첨부파일 알림 쪽지보내기
		}
		log.info("mdto" + mdto.toString());

		log.info("result.toString() : " + resultMsg.toString());
		return "redirect:./" + id;
	}

	@PostMapping("/messagebox/delete")
	@ResponseBody
	public String deleteMessageFromList(String fromId, String toId, Model model) {
		messageService.changeMsgStatus(fromId, toId);
		model.addAttribute("data", new Alert("쪽지가 성공적으로 삭제되었습니다!", "/AliceDiary/messagebox/" + fromId));
		return "1";
	}

	// 쪽지함 내에서 쪽지보내기
	@PostMapping("/messagebox/{fromId}/{toId}")
	public String sendMessages(@PathVariable("fromId") String fromId, @PathVariable("toId") String toId, Model model,
			@ModelAttribute MessageDto mdto, @AuthenticationPrincipal UserDetails user, HttpSession session) {
		Long senderNum = messageService.findNumById(fromId);
		Long receiverNum = messageService.findNumById(toId);
		MessageDto fileAlarm = new MessageDto();
		MultipartFile originName = mdto.getOriginName();
		if (senderNum > receiverNum) {
			mdto.setUser1Num(receiverNum);
			mdto.setUser2Num(senderNum);
			mdto.setDirection(1L);
		} else {
			mdto.setUser1Num(senderNum);
			mdto.setUser2Num(receiverNum);
			mdto.setDirection(0L);

		}

		mdto.setMessageFromId(fromId);
		mdto.setSendDate(LocalDateTime.now());
		log.info("mdto" + mdto.toString());

		Message result = messageService.sendMsg(mdto);
		log.info("!!!!!!!!!!!setContent : " + mdto.getOriginName());

		if (!mdto.getOriginName().isEmpty()) { // 첨부파일이 있다면
			fileAlarm.setContent(originName.getOriginalFilename() + "를 보냅니다!");
			fileAlarm.setDirection(mdto.getDirection());
			fileAlarm.setUser1Num(mdto.getUser1Num());
			fileAlarm.setUser2Num(mdto.getUser2Num());
			fileAlarm.setMessageFromId(mdto.getMessageFromId());
			fileAlarm.setMessageToId(mdto.getMessageToId());
			fileAlarm.setSendDate(mdto.getSendDate());
			log.info("!!!!!!!!!!!setContent : " + fileAlarm.getContent());
			messageService.sendMsg(fileAlarm); // 첨부파일 알림 쪽지보내기
		}
		log.info("mdto" + mdto.toString());
		log.info("result.toString() : " + result.toString());
		return "redirect:./" + toId;
	}

	// 쪽지 검색하기
	@GetMapping("/messagebox/{id}/search")
	public String searchByContent(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "type", required = true) String type, @PathVariable String id, String content,
			Model model, @ModelAttribute MessageDto mdto, MsgSearchDto msdto,
			@AuthenticationPrincipal UserDetails user) {
		Long num = messageService.findNumById(id); // tester의 userNum = 1
		model.addAttribute("member", memberService.findById(user.getUsername()));

		model.addAttribute("mdto", mdto);
		model.addAttribute("fromId", id);
		model.addAttribute("msdto", msdto);

		List<Message> msgList = new ArrayList<>();
		List<MsgListDto> mldtos = new ArrayList<>();
		Long receiverNum = 0L;
		msgList = messageService.findUserMsg(num);
		if (msdto.getType().equals("name")) { // id로 검색할 경우
			if (msgList == null) { return "message/msgList"; }

			for (Message m : msgList) {
				if (num == m.getUser1Num()) {
					if (m.getMsgStatus() < 2) { continue; }
					receiverNum = m.getUser2Num();
				} else { 
					if (m.getMsgStatus() % 2 == 0) { continue; }
					receiverNum = m.getUser1Num();
				}
				MsgListDto mldto = new MsgListDto();
				Member receiver = memberService.findOne(receiverNum);
				Member sender = memberService.findOne(num);
				mldto.setUser1Num(m.getUser1Num());
				mldto.setUser2Num(m.getUser2Num());
				mldto.setSendDate(m.getSendDate());
				mldto.setRecentContent(m.getContent());
				mldto.setMessageToId(receiver.getId());
				mldto.setMessageFromId(sender.getId());
				mldto.setSenderProfileImg(receiver.getProfileImg());
				mldto.setMessageFromName(sender.getName());
				mldto.setMessageToName(receiver.getName());
				mldto.setDirection(m.getDirection());
				if (mldto.getMessageFromName().contains(msdto.getKeyword())
						|| mldto.getMessageToName().contains(msdto.getKeyword())) {
					mldtos.add(mldto);
				} else { continue; }
			}
		} else if (msdto.getType().equals("content")) { // 내용으로 검색
			for (Message m : msgList) {
				if (!m.getContent().contains(msdto.getKeyword())) {
					continue;
				} else {
					if (num == m.getUser1Num()) {
						if (m.getMsgStatus() < 2) { continue; }
						receiverNum = m.getUser2Num();
					} else {
						if (m.getMsgStatus() % 2 == 0) { continue; }
						receiverNum = m.getUser1Num();
					}
					MsgListDto mldto = new MsgListDto();
					Member receiver = memberService.findOne(receiverNum);
					Member sender = memberService.findOne(num);
					mldto.setUser1Num(m.getUser1Num());
					mldto.setUser2Num(m.getUser2Num());
					mldto.setSendDate(m.getSendDate());
					mldto.setRecentContent(m.getContent());
					mldto.setMessageToId(receiver.getId());
					mldto.setMessageFromId(sender.getId());
					mldto.setSenderProfileImg(receiver.getProfileImg());
					mldto.setMessageFromName(sender.getName());
					mldto.setMessageToName(receiver.getName());
					mldto.setDirection(m.getDirection());
					mldtos.add(mldto);
				}
			}
		} else { // 타입 선택 안할 시 전체 목록 반환
			return "redirect:/messagebox/" + id;
		}

		model.addAttribute("receiverNum", receiverNum);
		model.addAttribute("mldtos", mldtos);
		model.addAttribute("mdto", mdto);
		model.addAttribute("fromNum", num);

		return "message/msgList";
	}

	// 사진 파일 모아보기
	@GetMapping(value = "/messagebox/pictures/{id}")
	public String showPictureList(@PathVariable("id") String id, @ModelAttribute("searchDto") SearchDto searchDto,
			@AuthenticationPrincipal UserDetails user, Model model, @ModelAttribute MsgFileDto mpdto, Long num) {
		String type = searchDto.getType();
		String keyword = searchDto.getKeyword();
		model.addAttribute("keyword", keyword);
		model.addAttribute("type", type);

		Member member = memberService.findById(user.getUsername());
		Long memNum = member.getNum();
		model.addAttribute("member", member);

		List<MsgFileDto> mpdtos = new ArrayList<>();
		Integer size = 0;

		if (keyword == null || type == null || keyword.isEmpty() || type.isEmpty()) {
			mpdtos = messageService.findMsgPictures(memNum);
			if (mpdtos == null) {
				return "/message/pictureList";
			}
			size = mpdtos.size();
		} else {
			mpdtos = messageService.searchMsgPicturesByKeyword(memNum, searchDto);
			if (mpdtos == null) {
				return "/message/pictureList";
			}
			size = mpdtos.size();
		}

		model.addAttribute("mpdtos", mpdtos);
		model.addAttribute("size", size);
        long count = notificationRepository.countByMemberAndChecked(member, false);
        model.addAttribute("hasNotification", count > 0);
		return "/message/pictureList";
	}

	// 문서 파일 모아보기
	@GetMapping(value = "/messagebox/docs/{id}")
	public String showDocList(@PathVariable("id") String id, @ModelAttribute("searchDto") SearchDto searchDto,
			@AuthenticationPrincipal UserDetails user, Model model, @ModelAttribute MsgFileDto mpdto, Long num) {
		String type = searchDto.getType();
		String keyword = searchDto.getKeyword();
		model.addAttribute("keyword", keyword);
		model.addAttribute("type", type);

		Member member = memberService.findById(user.getUsername());
		Long memNum = member.getNum();
		model.addAttribute("member", member);

		List<MsgFileDto> mpdtos = new ArrayList<>();
		Integer size = 0;

		if (keyword == null || type == null || keyword.isEmpty() || type.isEmpty()) {
			mpdtos = messageService.findMsgDocs(memNum);
			if (mpdtos == null) {
				return "/message/docList";
			}
			size = mpdtos.size();
		} else {
			mpdtos = messageService.searchMsgDocsByKeyword(memNum, searchDto);
			if (mpdtos == null) {
				return "/message/docList";
			}
			size = mpdtos.size();
		}

		model.addAttribute("mpdtos", mpdtos);
		model.addAttribute("size", size);
        long count = notificationRepository.countByMemberAndChecked(member, false);
        model.addAttribute("hasNotification", count > 0);

		return "/message/docList";
	}

	// 댓글에서 쪽지보내기
	@PostMapping("/message/replymsg")
	@ResponseBody
	public boolean replyMsg(String messageFromId, String messageToId, String content) {

		Long messageFromNum = memberService.findNumById(messageFromId);
		Long messageToNum = memberService.findNumById(messageToId);

		Long user1Num = 0L;
		Long user2Num = 0L;

		if (messageFromNum < messageToNum) {
			user1Num = messageFromNum;
			user2Num = messageToNum;

			messageService.replyMsg(Message.createMessage(user1Num, user2Num, content, 0L));

		} else {
			user1Num = messageToNum;
			user2Num = messageFromNum;

			messageService.replyMsg(Message.createMessage(user1Num, user2Num, content, 1L));
		}

		return true;
	}

}
