package com.example.taskmanagementtool.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "task_dependencies")
@IdClass(TaskDependencyId.class)
@Data
public class TaskDependency {
	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "preceding_task_id", nullable = false)
	private Task precedingTask;

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "succeeding_task_id", nullable = false)
	private Task succeedingTask;

	@Column(name = "dependency_type", length = 50)
	private String dependencyType; // 例: "FS" (Finish-to-Start) など

	@Column(name = "lag_days")
	private Integer lagDays;
}
