package com.alice.project.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.expression.Calendars;

import com.alice.project.domain.Member;
import com.alice.project.repository.NotificationRepository;
import com.alice.project.service.CalendarService;
import com.alice.project.service.FriendService;
import com.alice.project.service.MemberService;
import com.alice.project.service.ProfileService;
import com.alice.project.web.UserDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

	private final ProfileService profileService;
	private final PasswordEncoder passwordEncoder;
	private final MemberService memberService;
	private final FriendService friendService;
	private final NotificationRepository notificationRepository;
	private final CalendarService calendarService;

	// 내 프로필 보기 GET
	@GetMapping(value = "/member/{id}")
	public String myProfile(@PathVariable String id, Model model, @AuthenticationPrincipal UserDetails user) {
		Member member = profileService.findById(user.getUsername());
		// 내 프로필 보기
		if (member.getId().equals(id)) {
			// wish list 존재
			List<String> wishList = new ArrayList<String>();
			if (member.getWishlist() != null) {
				String wish = member.getWishlist().replaceAll(",", " ");
				String[] wishs = wish.split(" ");
				for (String s : wishs) {
					wishList.add(s);
				}
			}

			model.addAttribute("person", null);
			model.addAttribute("member", member);
			model.addAttribute("wishList", wishList);
			long count = notificationRepository.countByMemberAndChecked(member, false);
			model.addAttribute("hasNotification", count > 0);
			return "profile/myProfile";
		} else {
			// 친구인지 확인
			Boolean alreadyFriend = friendService.iAmFriend(member.getNum(), id);
			// 서로 친구
			if (alreadyFriend) {
				// 친구 프로필로 이동
				return "redirect:/friends/friendInfo/" + id;
			} else {
				// 외부 프로필로 이동
				Member person = memberService.findById(id);
				// wish list 존재
				List<String> wishList = new ArrayList<String>();
				if (person.getWishlist() != null) {
					String wish = person.getWishlist().replaceAll(",", " ");
					String[] wishs = wish.split(" ");
					for (String s : wishs) {
						wishList.add(s);
					}
				}
				model.addAttribute("member", member);
				model.addAttribute("person", person);
				model.addAttribute("wishList", wishList);
				return "profile/myProfile";
			}

		}
	}

	// 내 프로필 수정 화면 GET
	@GetMapping(value = "/member/update/{id}")
	public String updateProfile(@PathVariable String id, Model model, @AuthenticationPrincipal UserDetails user) {
		log.info("내 프로필 수정 GET 진입!!");
		Member member = profileService.findById(user.getUsername());
		UserDto dto = new UserDto();
		dto.setBirthStr(member.getBirth().format(DateTimeFormatter.ofPattern("yyy-MM-dd")));

		model.addAttribute("member", member);
		model.addAttribute("userDto", dto);
//		model.addAttribute("birthStr", member.getBirth().format(DateTimeFormatter.ofPattern("yyy-MM-dd")));
		return "profile/updateProfile";
	}

	// 내 프로필 수정하기 POST
	@PostMapping(value = "/member/update/{id}")
	public String updateProfile(@PathVariable String id, @ModelAttribute("member") @Valid UserDto userDto,
			BindingResult bindingResult, Long num) {
		log.info("프로필 수정 페이지 진입");
		log.info("member.num == " + num);

		LocalDate newBirth = LocalDate.parse(userDto.getBirthStr(), DateTimeFormatter.ISO_DATE);
		userDto.setBirth(newBirth);
		Member updateInfo = memberService.findById(id);
		if (updateInfo.getBirth() != newBirth) {
			calendarService.updateBirthEvent(updateInfo, newBirth);
		}
		
		if (!userDto.getProfileImg().getOriginalFilename().equals("")) {
			String originName = userDto.getProfileImg().getOriginalFilename();
			String saveName = id + "." + originName.split("\\.")[1];
			log.info("saveName == " + saveName);
			String savePath = "C:\\Temp\\upload\\profile\\";

			try {

				userDto.getProfileImg().transferTo(new File(savePath + saveName));
				userDto.setSaveName(saveName);
				log.info(("userDto.saveName = " + userDto.getSaveName()));
				memberService.processUpdateMember(num, userDto, true);

			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			memberService.processUpdateMember(num, userDto, false);
		}
		return "redirect:/member/{id}";
	}

	// 비밀번호 재설정 Get
	@GetMapping(value = "/member/{id}/editPwd")
	public String updatePwd(@PathVariable String id, Model model, String msg,
			@AuthenticationPrincipal UserDetails user) {
		log.info("프로필 비밀번호 재설정 GET 진입");
		Member member = profileService.findById(user.getUsername());
		model.addAttribute("member", member);
		model.addAttribute("msg", msg);
		return "profile/editPwd";
	}

	// 비밀번호 재설정 Post
	@PostMapping(value = "/member/editPwd")
	public String updatePwd(UserDto userDto, String id, RedirectAttributes re, Model model) {
		log.info("비밀번호 재설정 POST 진입");
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		Member member = memberService.findById(id);
		log.info("비밀번호 재설정 전 Member Password : " + member.getPassword());
//      re.addFlashAttribute("member", member);
		if (!encoder.matches(userDto.getPassword(), member.getPassword())) {
			log.info("현재 비밀번호 에러");
			model.addAttribute("msg", "현재 비밀번호가 일치하지 않습니다. 비밀번호를 다시 한번 확인해주세요.");
			re.addAttribute("msg", "현재 비밀번호가 일치하지 않습니다. 비밀번호를 다시 한번 확인해주세요.");

			return "redirect:/member/" + id + "/editPwd";
		} else {
			UserDto uDto = new UserDto(member, userDto.getNewPwd());
			member = Member.createMember(member.getNum(), uDto, passwordEncoder);
			member = memberService.updateMember(member);
			log.info("비밀번호 재설정 후 Member Password : " + member.getPassword());
		}
		model.addAttribute("member", member);
		return "redirect:/member/" + id;
	}

}