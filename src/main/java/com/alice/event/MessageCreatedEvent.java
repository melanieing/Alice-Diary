package com.alice.project.event;

import org.springframework.context.ApplicationEvent;

import com.alice.project.domain.Message;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class MessageCreatedEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 1L;
    private Message message;

    public MessageCreatedEvent(Message m) {
		super(m);
		this.message = m;
		log.info("MessageCreatedEvent 생성자");
		log.info("쪽지 받는사람 : " + m.getMember().getName());
    }
}

