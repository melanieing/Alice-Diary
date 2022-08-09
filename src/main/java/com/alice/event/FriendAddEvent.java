package com.alice.project.event;

import org.springframework.context.ApplicationEvent;

import com.alice.project.domain.Friend;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class FriendAddEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;
	private Friend friend;

	public FriendAddEvent(Friend f) {
		super(f);
		this.friend = f;
		log.info("FriendAddEvent 생성자");
		log.info("친구를 추가한 주체" + f.getMember().getName());
	}
    
}

