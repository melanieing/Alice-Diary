package com.alice.project.web;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CommunityCreateDto {

   public CommunityCreateDto() {
      // TODO Auto-generated constructor stub
   }

   private String comMembers; // 커뮤니티 소속회원

   private String comName; // 커뮤니티 이름

   private String description; // 커뮤니티 설명

   private Long num;

   private List<AlarmMemberListDto> friendsList;

   public CommunityCreateDto(String comMembers, String comName, String description, Long num) {
      super();
      this.comMembers = comMembers;
      this.comName = comName;
      this.description = description;
      this.num = num;
   }
}