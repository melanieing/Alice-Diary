package com.alice.project.event;

import org.springframework.context.ApplicationEvent;

import com.alice.project.domain.Post;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class PostCreatedEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;
	private Post post;

	public PostCreatedEvent(Post p) {
		super(p);
		this.post = p;
		log.info("AliceCreatedEvent 생성자");
		log.info("c.getMember()" + p.getMember().getName());
	}

}

