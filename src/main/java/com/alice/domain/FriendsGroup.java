package com.alice.project.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "friendsGroup")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
@EqualsAndHashCode(of = "num")
@DynamicInsert
public class FriendsGroup {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GROUP_SEQ_GENERATOR")
	@SequenceGenerator(name = "GROUP_SEQ_GENERATOR", sequenceName = "SEQ_GROUP_NUM", initialValue = 1, allocationSize = 1)
	@Column(name = "group_num")
	private Long num; // 그룹 번호
	@Column(nullable = false)
	private String groupName; // 그룹이름 (default="기본그룹")
	@Column(nullable = false)
	private Long groupCreatorNum; // 그룹생성 회원번호

	// 기본 그룹 생성자
	@Builder
	public FriendsGroup(String groupName) {
		this.groupName = groupName;
	}

	@Builder
	public FriendsGroup(String groupName, Long groupCreatorNum) {
		this.groupName = groupName;
		this.groupCreatorNum = groupCreatorNum;
	}

	// 그룹 객체 생성 메서드(멤버)
//   public static FriendsGroup creatFriendsGroup(Member member) {
//      FriendsGroup friendsGroup = new FriendsGroup();
//      friendsGroup.setMember(member);
//      return friendsGroup;
//   }
//   // 연관관계 메서드 (양방향관계)
//   public void setMember(Member member) {
//      this.member = member;
//      member.getGroups().add(this);
//   }
//
//   @ManyToOne(fetch=FetchType.LAZY) // 모든 연관관계는 항상 지연로딩으로 설정(성능상이점)
//   @JoinColumn(name="member_num")
//   @JsonBackReference
//   private Member member; // 그룹 생성 회원 객체

//   @OneToMany(mappedBy="group")
//   private List<Friend> friends = new ArrayList<>(); // 해당 그룹에 속한 친구객체 리스트

//   
//   @Builder
//   public FriendsGroup(Member member, String groupName) {
//      this.groupName = groupName;
//      setMember(member);
//   }
//   
//   // 그룹 객체 생성 메서드
//   public static FriendsGroup createFriendsGroup(Friend friend) {
//      FriendsGroup friendsGroup = new FriendsGroup();
//      friendsGroup.setMember(member);
//      return friendsGroup;
//   }
}