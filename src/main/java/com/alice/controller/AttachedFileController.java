package com.alice.project.controller;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.alice.project.service.AttachedFileService;

@Controller
public class AttachedFileController {

	@Autowired
	private AttachedFileService attachedFileService;

	// 파일 다운로드하기
	@GetMapping("/community/download/{num}")
	public ResponseEntity<UrlResource> fileDownload(@PathVariable("num") Long num)
			throws MalformedURLException, UnsupportedEncodingException {

		return attachedFileService.postFileDownload(num);
	}

	/* 공지사항 첨부파일 다운로드하기 */
	@GetMapping("/admin/notice/download/{num}")
	public ResponseEntity<UrlResource> adminNoticefileDownload(@PathVariable("num") Long num)
			throws MalformedURLException, UnsupportedEncodingException {

		return attachedFileService.postFileDownload(num);
	}

	@GetMapping("/notice/download/{num}")
	public ResponseEntity<UrlResource> noticefileDownload(@PathVariable("num") Long num)
			throws MalformedURLException, UnsupportedEncodingException {

		return attachedFileService.postFileDownload(num);
	}

	/* 쪽지함 첨부파일 다운로드하기 */
	@GetMapping("/messagebox/pictures/download/{num}")
	public ResponseEntity<UrlResource> msgPicturefileDownload(@PathVariable("num") Long num)
			throws MalformedURLException, UnsupportedEncodingException {

		return attachedFileService.postFileDownload(num);
	}

	@GetMapping("/messagebox/docs/download/{num}")
	public ResponseEntity<UrlResource> msgDocfileDownload(@PathVariable("num") Long num)
			throws MalformedURLException, UnsupportedEncodingException {

		return attachedFileService.postFileDownload(num);
	}

}