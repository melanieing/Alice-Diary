package com.alice.project.handler;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class Alert {
	String word = "";
	String href = "";
	
	public Alert(String word, String href) {
		super();
		this.word = word;
		this.href = href;
	}
	
	
}
