package com.alice.project.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alice.project.domain.AttachedFile;
import com.alice.project.domain.Message;
import com.alice.project.domain.Post;
import com.alice.project.domain.PostType;
import com.alice.project.repository.AttachedFileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AttachedFileService {

	private final AttachedFileRepository attachedFileRepository;

	public void postFileUpload(List<MultipartFile> files, Post post, HttpSession session, String id) {
		log.info("list size : " + files.size());
		if (files.size() != 0) {
			log.info("service run");
			String savePath = "";
			if (post.getPostType() == PostType.NOTICE) {
				savePath = "C:\\Temp\\upload\\notice\\";
			} else if (post.getPostType() == PostType.OPEN) {
				savePath = "C:\\Temp\\upload\\open\\";
			} else if (post.getPostType() == PostType.CUSTOM) {
				savePath = "C:\\Temp\\upload\\community\\";
			}
			for (MultipartFile multipartFile : files) {
				String ofile = multipartFile.getOriginalFilename();
				String sfile = makeSfile(multipartFile, savePath, session, id);

				log.info("service run222222222");

				AttachedFile file = new AttachedFile(ofile, sfile, savePath, post);

				attachedFileRepository.save(file);
				log.info("service run444444444");

			}

		} else {
			log.info("upload file FAIL!!");
		}
	}

	// 저장파일 이름만들기
	@Transactional
	public String makeSfile(MultipartFile file, String savePath, HttpSession session, String id) {

		String ofile = file.getOriginalFilename();
		String currentTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

		String sfile = id + "_" + currentTime + "_" + ofile;

		log.info("ofile:" + ofile);
		log.info("sfile:" + sfile);
		log.info("savePath:" + savePath);

		try {
			file.transferTo(new File(savePath + sfile));
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		}

		return sfile;
	}

	/* 쪽지 첨부파일 저장 */
	public String saveMsgFile(MultipartFile file, Message msg, HttpSession session, String id) {
		String savePath = "C:\\Temp\\upload\\message\\";
		String ofile = file.getOriginalFilename();
		String currentTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		String sfile = id + "_" + currentTime + "_" + ofile;

		AttachedFile savefile = new AttachedFile(ofile, sfile, savePath, msg);

		attachedFileRepository.save(savefile);
		try {
			file.transferTo(new File(savePath + sfile));
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		}

		return sfile;
	}

	// 파일 다운로드
	public ResponseEntity<UrlResource> postFileDownload(Long num)
			throws MalformedURLException, UnsupportedEncodingException {

		Optional<AttachedFile> findFile = attachedFileRepository.findById(num);
		AttachedFile attachedFile = findFile.orElse(null);
		String savedFilePath = "";
		UrlResource resource = null;

		if (findFile != null) {
			String sFileName = attachedFile.getSaveName();
			String oFileName = attachedFile.getOriginName();

			String encodeoFileName;

			encodeoFileName = URLEncoder.encode(oFileName, "UTF-8").replace("+", "%20");

			savedFilePath = "attachment; filename=\"" + encodeoFileName + "\"";
			resource = new UrlResource("file:" + attachedFile.getFilePath() + sFileName);

		}
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, savedFilePath).body(resource);
	}

	// 게시글 상세보기에서 저장된 파일 보여주기
	public List<AttachedFile> fileView(Post post, Pageable pageable) {
		log.info("service run fileVIEW");
		List<AttachedFile> afs = attachedFileRepository.findAllByPostNum(post.getNum(), pageable);
		log.info("!!!!!!!!!!!!!!!!!!!!!!asf.size:" + afs.size());
		for (AttachedFile af : afs) {
			String oriName = af.getOriginName();
			log.info("AFS의 fileView!!! af : " + oriName);
		}
		return attachedFileRepository.findAllByPostNum(post.getNum(), pageable);
	}

	public AttachedFile getFileByMsgNum(Long msgNum) {
		return attachedFileRepository.findByMessageNum(msgNum);
	}

	// 게시글 수정에서 파일 한개 삭제 후 파일리스트 다시 불러오기
	@Transactional
	public List<AttachedFile> fileDeleteAfterList(Long postNum) {

		List<AttachedFile> afs = attachedFileRepository.findAllByPostNum(postNum);

		return afs;
	}
}
