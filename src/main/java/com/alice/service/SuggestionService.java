package com.alice.project.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alice.project.domain.Suggestion;
import com.alice.project.repository.SuggestionRepository;
import com.alice.project.web.SearchDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional(readOnly = true) // 기본적으로 못바꾸게 해놓고
@RequiredArgsConstructor // final 필드 생성자 생성해줌
@Slf4j
public class SuggestionService {
	private final SuggestionRepository suggestionRepository;

	@Transactional
	public Suggestion saveSuggest(Suggestion suggestion) {
		return suggestionRepository.save(suggestion); // insert
	}

	/* 건의사항 전체 조회 */
	public Page<Suggestion> getSuggestionList(Pageable pageable) {
		return suggestionRepository.findAll(pageable);
	}

	/* 회원 검색 기능 */
	public Page<Suggestion> searchSuggestion(SearchDto searchDto, Pageable pageable) {
		String type = searchDto.getType();
		String keyword = searchDto.getKeyword();

		Page<Suggestion> suggestionList = null;

		if (type.equals("id")) {
			suggestionList = suggestionRepository.findAll(pageable);
			List<Suggestion> list = new ArrayList<>();
			for (Suggestion s : suggestionList) {
				if (s.getMember().getId().contains(keyword)) {
					list.add(s);
				}
			}
			final int start = (int) pageable.getOffset();
			final int end = Math.min((start + pageable.getPageSize()), list.size());
			suggestionList = new PageImpl<>(list.subList(start, end), pageable, list.size());
		} else if (type.equals("content")) {
			suggestionList = suggestionRepository.findByContentContaining(keyword, pageable);
		} else if (type.equals("name")) {
			suggestionList = suggestionRepository.findAll(pageable);
			List<Suggestion> list = new ArrayList<>();
			for (Suggestion s : suggestionList) {
				if (s.getMember().getName().contains(keyword)) {
					list.add(s);
				}
			}
			final int start = (int) pageable.getOffset();
			final int end = Math.min((start + pageable.getPageSize()), list.size());
			suggestionList = new PageImpl<>(list.subList(start, end), pageable, list.size());
		}
		return suggestionList;
	}
}