package com.alice.project.domain;

import lombok.Getter;

@Getter
public enum ReportReason {
	BAD, LEAK, SPAM, ETC;
	// 비속어/욕설/불쾌한 표현. 개인정보유출, 스팸홍보/도배글, 기타

}