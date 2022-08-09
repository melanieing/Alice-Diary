package com.alice.project.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alice.project.domain.Member;
import com.alice.project.domain.Post;
import com.alice.project.domain.PostType;
import com.alice.project.event.PostCreatedEvent;
import com.alice.project.repository.AttachedFileRepository;
import com.alice.project.repository.MemberRepository;
import com.alice.project.repository.PostRepository;
import com.alice.project.repository.ReplyRepository;
import com.alice.project.web.PostSearchDto;
import com.alice.project.web.WriteFormDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

	private final PostRepository postRepository;
	private final MemberRepository memberRepository;
	private final AttachedFileRepository attachedFileRepository;
	private final ReplyRepository replyRepository;
	private final ApplicationEventPublisher eventPublisher; // for notification

	// 글쓰기
	@Transactional
	public Post write(Post post) {
		Post result = postRepository.save(post);
		if (post.getPostType().equals(PostType.NOTICE)) { // 공지사항 등록 시 알림 설정
			this.eventPublisher.publishEvent(new PostCreatedEvent(result));
		}
		return result;
	}

	// 글번호로 post객체 하나 찾기
	public Post findOne(Long num) {
		return postRepository.findByNum(num);
	}

	// 공개게시글 전체 불러오기
	public Page<Post> list(Pageable pageable) {
		return postRepository.findAllOpenPost(pageable);
	}

	// 커뮤니티게시글 전체 불러오기
	public Page<Post> comList(Long comNum, Pageable pageable) {
		return postRepository.findAllCustomPost(comNum, pageable);
	}

	// 공개게시판 검색해서 리스트 불러오기 (닉네임으로 검색 수정했어요!)
	public Page<Post> searchList(PostSearchDto postSearchDto, Pageable pageable) {
		Page<Post> searchList = null;

		if (postSearchDto.getType().equals("title")) { // 게시물 제목으로 검색
			searchList = postRepository.searchTitle(postSearchDto.getKeyword(), pageable);
		} else if (postSearchDto.getType().equals("content")) { // 게시물 내용으로 검색
			searchList = postRepository.searchContent(postSearchDto.getKeyword(), pageable);

		} else if (postSearchDto.getType().equals("writer")) {
			log.info("postSearchDto.getKeyword() :" + postSearchDto.getKeyword());
			Page<Member> members = memberRepository.findMemberByName(postSearchDto.getKeyword(), pageable);
			if (members != null) {
				List<Post> tmpList = new ArrayList<>();
				for (Member m : members) {
					Page<Post> tmp = null;
					tmp = postRepository.searchWriter(m.getNum(), pageable);
					tmpList.addAll(tmp.getContent());
				}
				searchList = new PageImpl<Post>(tmpList, pageable, tmpList.size());
			} else {
				searchList = postRepository.searchWriter(0L, pageable);
			}
		}
		return searchList;
	}

	// 커뮤니티게시판 검색해서 리스트 불러오기
	public Page<Post> comSearchList(Long comNum, PostSearchDto postSearchDto, Pageable pageable) {

		log.info("서비스 로그 postSearchDto :" + postSearchDto.toString());
		Page<Post> searchList = null;
		if (postSearchDto.getType().equals("title")) {
			searchList = postRepository.comSearchTitle(comNum, postSearchDto.getKeyword(), pageable);
		} else if (postSearchDto.getType().equals("content")) {
			searchList = postRepository.comSearchContent(comNum, postSearchDto.getKeyword(), pageable);
		} else if (postSearchDto.getType().equals("writer")) {
			log.info("postSearchDto.getKeyword() :" + postSearchDto.getKeyword());
			Page<Member> members = memberRepository.findMemberByName(postSearchDto.getKeyword(), pageable);
			if (members != null) {
				List<Post> tmpList = new ArrayList<>();
				for (Member m : members) {
					Page<Post> tmp = null;
					tmp = postRepository.searchWriter(m.getNum(), pageable);
					tmpList.addAll(tmp.getContent());
				}
				searchList = new PageImpl<Post>(tmpList, pageable, tmpList.size());
			} else {
				searchList = postRepository.searchWriter(0L, pageable);
			}
		}
		return searchList;
	}

	// 게시글 번호로 상세보기
	public Post postView(Long num) {
		return postRepository.findById(num).get();
	}

	// 조회수 올리기
	@Transactional
	public int viewCntUp(Long num) {
		return postRepository.viewCntUp(num);
	}

	// 글 수정하기
	@Transactional
	public void updatePost(Long num, WriteFormDto updateDto) {

		postRepository.editContent(num, updateDto.getContent());
		postRepository.editTitle(num, updateDto.getTitle());
		postRepository.editDate(num, LocalDateTime.now());
	}

	// 게시글 삭제하기
	@Transactional
	public void deletePost(Long num) {
		log.info("포스트 지우러 옴!!!!!!!!!!!");
		Post deletePost = postRepository.findByNum(num);
		postRepository.delete(deletePost);
	}

	// 게시글 지울때 첨부파일(전체)도 같이 지우기
	@Transactional
	public void deletePostwithFile(Long num) {
		log.info("파일 삭제 num:" + num);
		attachedFileRepository.deletePostwithFile(num);
	}

	// 게시글 지울때 댓글도 같이 지우기
	@Transactional
	public void deletePostwithReply(Long num) {
		log.info("댓글 삭제");
		replyRepository.deletePostwithReply(num);
	}

	// 게시글 수정에서 파일 하나 삭제하기
	@Transactional
	public Integer deleteOneFile(Long num) {
		Integer result = attachedFileRepository.deleteOneFile(num);
		return result;
	}

	// 게시글 번호로 객체 찾기
	public Post findByNum(Long num) {
		return postRepository.findByNum(num);
	}

	// 커뮤니티번호로 게시글 모두 가져오기
	public List<Post> getPostBycomNum(Long comNum) {
		return postRepository.findBycomNum(comNum);
	}

	/* 공지사항 관련 서비스 */
	/* 공지사항 전체 불러오기 */
	public Page<Post> notceList(Pageable pageable) {
		return postRepository.findAllNotices(pageable);
	}

	/* 공개게시판 게시물 전체 불러오기 */
	public Page<Post> openList(Pageable pageable) {
		return postRepository.findAllOpens(pageable);
	}

	/* 공지사항 검색 */
	public Page<Post> searchNoticeList(PostSearchDto postSearchDto, Pageable pageable) {
		Page<Post> searchList = null;

		if (postSearchDto.getType().equals("title")) {
			searchList = postRepository.searchNoticeTitle(postSearchDto.getKeyword(), pageable);
		} else if (postSearchDto.getType().equals("content")) {
			searchList = postRepository.searchNoticeContent(postSearchDto.getKeyword(), pageable);
		}

		return searchList;
	}

}