package com.alice.project.event;

import org.springframework.context.ApplicationEvent;

import com.alice.project.domain.Reply;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class ReplyCreatedEvent extends ApplicationEvent {
	private static final long serialVersionUID = 1L;
    private Reply reply;

    public ReplyCreatedEvent(Reply r) {
    	super(r);
		this.reply = r;
		log.info("ReplyCreatedEvent 생성자");
		log.info("댓글 쓴 사람" + r.getMember().getName());
    }
}

