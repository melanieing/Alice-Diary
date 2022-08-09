package com.alice.project.event;

import org.springframework.context.ApplicationEvent;

import com.alice.project.domain.Calendar;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class AliceCreatedEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;
	private Calendar calendar;

	public AliceCreatedEvent(Calendar c) {
		super(c);
		this.calendar = c;
		log.info("AliceCreatedEvent 생성자");
		log.info("c.getMember()" + c.getMember().getName());
	}

}

