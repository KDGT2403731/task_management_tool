package com.example.taskmanagementtool.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "team_members")
@IdClass(TeamMemberId.class)
@Data
public class TeamMember {
	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id", nullable = false)
	private Team team;

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, length = 50)
	private String role; // ER図にあるrole

	@Column(name = "joined_at", nullable = false)
	private LocalDateTime joinedAt; // ER図にあるjoined_at

	@PrePersist
	protected void onCreate() {
		this.joinedAt = LocalDateTime.now();
	}
}