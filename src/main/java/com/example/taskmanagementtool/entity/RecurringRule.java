package com.example.taskmanagementtool.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "recurring_rules")
@Data
public class RecurringRule {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 紐づくプロジェクト (FK: project_id)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	// 自動生成時の既定の担当者 (FK: assignee_id)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assignee_id")
	private User assignee;

	@Column(name = "template_title", nullable = false, length = 255)
	private String templateTitle;

	@Column(name = "template_description", columnDefinition = "TEXT")
	private String templateDescription;

	@Column(nullable = false, length = 50)
	private String frequency; // 例: "DAILY", "WEEKLY", "MONTHLY"

	@Column(name = "cron_expression", length = 100)
	private String cronExpression; // クーロン式（例: "0 0 9 1 * ?"）

	@Column(name = "end_date")
	private LocalDate endDate;

	@Column(name = "next_execution_date")
	private LocalDateTime nextExecutionDate;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive = true;
}
