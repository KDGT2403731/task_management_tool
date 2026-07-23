package com.example.taskmanagementtool.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@ToString(exclude = { "project", "parentTask", "subTasks", "milestone", "assignee", "createdBy" })
public class Task {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 所属するプロジェクト (FK: project_id)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	// 親タスク：自己参照リレーション (FK: parent_task_id)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_task_id")
	private Task parentTask;

	// 子タスク（サブタスク）の一覧
	@OneToMany(mappedBy = "parentTask", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Task> subTasks;

	// 紐づくマイルストーン (FK: milestone_id) 
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "milestone_id")
	private Milestone milestone;

	// タスクの担当者 (FK: assignee_id) 
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assignee_id")
	private User assignee;

	// タスクの作成者 (FK: created_by)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by", nullable = false)
	private User createdBy;

	@Column(nullable = false, length = 255)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false, length = 50)
	private String status; // 例: "TODO", "IN_PROGRESS", "DONE" など

	@Column(nullable = false, length = 50)
	private String priority; // 例: "HIGH", "MEDIUM", "LOW"

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "start_date")
	private LocalDate startDate;

	@Column(name = "due_date")
	private LocalDate dueDate;

	@Column(name = "completed_at")
	private LocalDateTime completedAt;

	// 工数見積もり (Plan) 
	@Column(name = "plan_hours")
	private Integer planHours;

	// 実績工数 (Actual) 
	@Column(name = "actual_hours")
	private Integer actualHours;

	// タイムスタンプの自動設定
	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Task other)) {
			return false;
		}
		return id != null && id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}