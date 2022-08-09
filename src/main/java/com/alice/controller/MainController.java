package com.alice.project.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alice.project.domain.Member;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MainController {

	private final HttpSession httpSession;

	// main 페이지로
	@GetMapping(value = "/")
	public String main(@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "exception", required = false) String exception, Model model) {
		Member member = (Member) httpSession.getAttribute("member");
		model.addAttribute("error", error);
		model.addAttribute("exception", exception);

		if (member != null) {
			model.addAttribute("userName", member.getName());
		}

		return "index";
	}
}