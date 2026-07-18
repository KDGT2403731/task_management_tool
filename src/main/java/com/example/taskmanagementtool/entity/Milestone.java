package com.example.taskmanagementtool.entity;

import java.time.LocalDate;
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
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "milestones")
@Data
public class Milestone {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@Column(nullable = false, length = 255)
	private String title;

	@Column(name = "target_date")
	private LocalDate targetDate;

	@Column(nullable = false, length = 50)
	private String status; // 例: "OPEN", "ACHIEVED", "MISSED" など

	@Column(columnDefinition = "TEXT")
	private String description;

	// このマイルストーンに紐づくタスク一覧
	@OneToMany(mappedBy = "milestone", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Task> tasks;
}
