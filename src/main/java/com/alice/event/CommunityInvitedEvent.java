package com.alice.project.event;

import org.springframework.context.ApplicationEvent;

import com.alice.project.domain.Community;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class CommunityInvitedEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;
	private Community community;

	public CommunityInvitedEvent(Community c) {
    	super(c);
		this.community = c;
		log.info("CommunityInvitedEvent 생성자");
		log.info("c.getMember() 누구야 너 " + c.getMember().getName());
	}

}

