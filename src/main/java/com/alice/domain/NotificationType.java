package com.alice.project.domain;

//새로운 쪽지왔을 때 (message)
//앨리스 알림 등록 시 (calendar)
//누군가 나를 친구추가했을 때 (friend)
//커뮤니티 초대됐을 때 (community)
//내 글에 댓글 달렸을 때 (post-reply)
//공지사항이 등록되었을 때 (notice)

public enum NotificationType {
	MESSAGE, ALICE, FRIEND, COMMUNITY, REPLY, NOTICE;
}
