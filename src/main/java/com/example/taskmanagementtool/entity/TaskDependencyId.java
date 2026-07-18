package com.example.taskmanagementtool.entity;

import java.io.Serializable;

import lombok.Data;

@Data
public class TaskDependencyId implements Serializable {
	private Long precedingTask;
	private Long succeedingTask;
}