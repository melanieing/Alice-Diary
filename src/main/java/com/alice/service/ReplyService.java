package com.alice.project.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alice.project.domain.Post;
import com.alice.project.domain.Reply;
import com.alice.project.domain.ReplyStatus;
import com.alice.project.event.ReplyCreatedEvent;
import com.alice.project.repository.MemberRepository;
import com.alice.project.repository.PostRepository;
import com.alice.project.repository.ReplyRepository;
import com.alice.project.web.ReplyDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReplyService {

	private final ReplyRepository replyRepository;

	private final MemberRepository memberRepository;

	private final PostRepository postRepository;
	
	private final ApplicationEventPublisher eventPublisher;

	// 아이디로 회원번호 찾기
	public Long getMemNumById(String memberId) {
		log.info("memberId : " + memberId);
		Long member = memberRepository.findMemberNumById(memberId);
		return member;
	}

	// 댓글쓰기
	@Transactional
	public Reply replyWrite(String memberId, Long postNum, String content) {
		log.info("memberId : " + memberId);

		Reply writedReply = new Reply(content, LocalDateTime.now(), Boolean.FALSE, postRepository.findByNum(postNum),
				memberRepository.findById(memberId), ReplyStatus.ALIVE);
		
		Reply reply = replyRepository.save(writedReply);
		this.eventPublisher.publishEvent(new ReplyCreatedEvent(reply)); // 알림 보내기
		
		return reply;
	}

	// 댓글 불러오기
	public List<ReplyDto> replyList(Long num) {
		List<ReplyDto> result = new ArrayList<>();

		List<Reply> pList = replyRepository.findParentReplysByNum(num);
		for (Reply p : pList) {
			List<Reply> tmp = new ArrayList<Reply>();
			tmp.add(p);
			List<Reply> childL = replyRepository.findChildByParentNum(p.getNum());
			tmp.addAll(childL);
			for (Reply r : tmp) {
				ReplyDto rdto = new ReplyDto();
				rdto.setNum(r.getNum());
				rdto.setParentRepNum(r.getParentRepNum());
				rdto.setContent(r.getContent());
				rdto.setRepDate(r.getRepDate());
				rdto.setEdit(r.getEdit());
				rdto.setMemberId(r.getMember().getId());
				rdto.setPostNum(r.getPost().getNum());
				rdto.setMemberName(r.getMember().getName());
				rdto.setStatus(r.getStatus());
				rdto.setProfileImg(r.getMember().getProfileImg());
				rdto.setMemberName(r.getMember().getName());
				result.add(rdto);
			}
		}
		return result;
	}

	// 대댓쓰기
	@Transactional
	public Reply replyReplyWrite(String memberId, Long postNum, Long parentRepNum, String content) {
		log.info("parentRepNum : " + parentRepNum);

		Reply writedReplyReply = new Reply(content, LocalDateTime.now(), Boolean.FALSE, parentRepNum,
				postRepository.findByNum(postNum), memberRepository.findById(memberId), ReplyStatus.ALIVE);
		
		Reply rereply = replyRepository.save(writedReplyReply);
		this.eventPublisher.publishEvent(new ReplyCreatedEvent(rereply)); // 알림 보내기
		return rereply;
	}

	// 댓글 삭제하기
	@Transactional
	public void replyDelete(Long num) {
		log.info("댓글 지우러 서비스옴!!!!!!!!!!!");

		// 자식있는 부모댓글
		List<Reply> result = replyRepository.findChildByParentNum(num);

		if (result.size() > 0) {
			replyRepository.deleteParentHaveChild(num);
		} else {
			// 자식없는 부모댓글 + 자식댓글
			Reply replyDelete = replyRepository.findByNum(num);
			replyRepository.delete(replyDelete);
		}
	}

	// 댓글번호로 객체 찾기
	public Reply findByNum(Long num) {
		return replyRepository.findByNum(num);
	}

	public Post getPostByReplyNum(Long replyNum) {
		Long postNum = replyRepository.searchPostNumByReplyNum(replyNum).longValue();
		Post post = postRepository.findByNum(postNum);
		return post;
	}

	public List<Reply> getReplyByPostNum(Long postNum) {
		List<Reply> replies = replyRepository.findByPostNum(postNum);
//		List<Long> replyNums = new ArrayList<>();
//		for (Reply r : replies) {
//			replyNums.add(r.getNum());
//		}

		return replies;
	}

	// 댓글 갯수 가져오기
	public Long getCountReply(Long postNum) {
		return replyRepository.getCountReply(postNum);
	}
}