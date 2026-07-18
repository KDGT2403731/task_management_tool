package com.example.taskmanagementtool.entity;

import java.io.Serializable;

import lombok.Data;

@Data
public class TeamMemberId implements Serializable {
	private Long team;
	private Long user;
}
