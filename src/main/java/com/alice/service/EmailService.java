package com.alice.project.service;

import org.springframework.stereotype.Service;

@Service
public interface EmailService {

	void sendEmail(EmailMessage emailMessage);
	
}